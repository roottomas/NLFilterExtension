package modules;

import DTOs.requests.ScenariosFilterInputDTO;
import base.BackendService;
import base.modules.FunctionModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import usefulObjects.filters.FilterExpression;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class NLQueryFunction implements FunctionModule<NLQueryFunctionInput, NLQueryFunctionOutput> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";
    private static final String GEMINI_API_KEY = loadApiKey();
    private static final String GEMINI_MODEL = loadModel();
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent";
    private static final String SKILLS_CACHE = loadSkills();

    private BackendService backendService;

    @Override
    public void initialize(BackendService backendService) {
        this.backendService = backendService;
    }

    @Override
    public Uni<NLQueryFunctionOutput> execute(NLQueryFunctionInput input) {
        return askAgent(buildPrompt(input))
                .onItem().transformToUni(agentResponse -> handleAgentResponse(agentResponse, input, true));
    }

    /**
     * Sends a prompt to the model and returns the parsed agent response.
     */
    private Uni<JsonNode> askAgent(String prompt) {
        return Uni.createFrom().item(() -> sendToGemini(prompt))
                .onItem().transform(this::extractJsonFromGeminiResponse);
    }

    private String resolveClarificationTopic(JsonNode agentResponse) {
        String topic = extractTextField(agentResponse, "clarification_topic");
        if (topic != null && !topic.isBlank()) {
            return topic;
        }
        String clarificationType = extractTextField(agentResponse, "clarification_type");
        if ("multi_choice".equals(clarificationType)) {
            return "GENERIC_CHOICE";
        }
        if ("numeric_threshold".equals(clarificationType)) {
            return "GENERIC_NUMERIC";
        }
        return "GENERIC_TEXT";
    }

    /**
     * Processes the agent's response.
     */
    private Uni<NLQueryFunctionOutput> handleAgentResponse(JsonNode agentResponse,
                                                          NLQueryFunctionInput input,
                                                          boolean updateFlowAllowed) {
        try {
            Long scenarioId = input.getScenario();
            Long versionId = input.getVersion();

            // O agente só pode pedir a lista de filtros uma vez por interação: sem esta
            // guarda, um `need_filters` repetido fazia handleUpdateFlow e handleAgentResponse
            // chamarem-se mutuamente sem fim.
            if (agentResponse.path("need_filters").asBoolean(false)) {
                if (!updateFlowAllowed) {
                    return Uni.createFrom().item(NLQueryFunctionOutput.error(
                            "The agent requested the filter list twice for the same query."));
                }
                return handleUpdateFlow(input);
            }

            String clarificationQuestion = extractTextField(agentResponse, "clarification_question");
            if (clarificationQuestion != null) {
                String clarificationType = extractTextField(agentResponse, "clarification_type");
                String topic = resolveClarificationTopic(agentResponse);

                if ("multi_choice".equals(clarificationType)) {
                    List<ClarificationOption> options = parseClarificationOptions(agentResponse.path("clarification_options"));
                    return Uni.createFrom().item(NLQueryFunctionOutput.multiChoice(clarificationQuestion, options, topic));
                }
                if ("numeric_threshold".equals(clarificationType)) {
                    String field = extractTextField(agentResponse, "field");
                    String unit = extractTextField(agentResponse, "unit");
                    return Uni.createFrom().item(NLQueryFunctionOutput.numericThreshold(clarificationQuestion, field, unit, topic));
                }
                return Uni.createFrom().item(NLQueryFunctionOutput.freeText(clarificationQuestion, topic));
            }

            JsonNode plan = agentResponse.get("plan");
            if (plan == null || plan.isNull()) {
                String warning = extractFirstWarning(agentResponse);
                return Uni.createFrom().item(NLQueryFunctionOutput.error("Error: " + warning));
            }

            JsonNode filterNode = plan.get("filter");
            if (filterNode == null || filterNode.isNull()) {
                return Uni.createFrom().item(NLQueryFunctionOutput.error("No filter provided."));
            }

            JsonNode filterIdNode = plan.get("filterId");
            Long filterId = null;
            if (filterIdNode != null && !filterIdNode.isNull()) {
                if (filterIdNode.isNumber()) {
                    filterId = filterIdNode.asLong();
                } else if (filterIdNode.isTextual()) {
                    try {
                        filterId = Long.parseLong(filterIdNode.asText().trim());
                    } catch (NumberFormatException ignored) {}
                }
            }
            boolean isUpdate = filterId != null && filterId > 0;

            String title = filterNode.path("title").asText("NL generated filter.");
            String description = filterNode.path("description").asText(null);
            List<FilterExpression.Layer> layers = parseLayers(filterNode.path("layers"));
            // ATENCAO: o 3.o argumento do construtor e o campo `all`, NAO o `activated`.
            // O `activated` tem de ser posto explicitamente, senao fica false e o filtro
            // nao e aplicado no mapa.
            FilterExpression fe = new FilterExpression(title, layers, false);

            if (description != null && !description.isBlank()) {
                fe.setDescription(description);
            }

            fe.setActivated(true);

            if (isUpdate) {
                fe.setId(filterId);
            }

            ScenariosFilterInputDTO dto = new ScenariosFilterInputDTO(List.of(fe));

            if (isUpdate) {
                return backendService.updateFilter(scenarioId, versionId, filterId, dto)
                        .replaceWith(NLQueryFunctionOutput.success("Filter updated successfully: " + title))
                        .onFailure().recoverWithItem(throwable -> {
                            System.err.println("Update filter failed: " + throwable.getMessage());
                            return NLQueryFunctionOutput.error("Update failed: " + throwable.getMessage());
                        });
            } else {
                return backendService.saveFilter(scenarioId, versionId, dto)
                        .map(saved -> NLQueryFunctionOutput.success("Filter created successfully: " + title))
                        .onFailure().recoverWithItem(throwable -> {
                            System.err.println("Create filter failed: " + throwable.getMessage());
                            return NLQueryFunctionOutput.error("Creation failed: " + throwable.getMessage());
                        });
            }
        } catch (Exception e) {
            return Uni.createFrom().item(NLQueryFunctionOutput.error("Internal error: " + e.getMessage()));
        }
    }

    /**
     * Parses the list of filter layers from the JSON node.
     */
    private List<FilterExpression.Layer> parseLayers(JsonNode layersNode) {
        List<FilterExpression.Layer> layers = new ArrayList<>();
        if (!layersNode.isArray() || layersNode.isEmpty()) {
            throw new IllegalArgumentException("'layers' must be a non-empty array.");
        }
        for (JsonNode layerNode : layersNode) {
            String layerName = layerNode.path("layerName").asText();

            JsonNode ruleJsonNode = layerNode.path("ruleJson");
            String ruleJson;
            if (ruleJsonNode.isTextual()) {
                ruleJson = ruleJsonNode.asText();
            } else {
                ruleJson = ruleJsonNode.toString();
            }

            layers.add(new FilterExpression.Layer(layerName, ruleJson));
        }
        return layers;
    }

    /**
     * Handles the update flow when the agent requests the list of existing filters
     * (via `need_filters: true`).
     * This method fetches all filters for the current scenario/version via
     * `getUserFilters()`, builds a list with each filter's ID, title,
     * and description, injects this list into a new prompt, and re-sends it to the agent.
     *
     * The agent uses this list to identify which filter the user wants to modify,
     * returning the corresponding `filterId`. The response is then forwarded to
     * `handleAgentResponse`, which performs the actual update via `updateFilter`.
     *
     * @param input The function input containing the query and clarification history.
     * @return A {@link Uni} emitting the result of the update operation.
     */
    private Uni<NLQueryFunctionOutput> handleUpdateFlow(NLQueryFunctionInput input) {
        Long scenarioId = input.getScenario();
        Long versionId = input.getVersion();

        return backendService.getUserFilters(scenarioId, versionId)
                .flatMap(filtersDTO -> {

                    List<FilterExpression> expressions = filtersDTO.getExpressions();
                    if (expressions == null || expressions.isEmpty()) {
                        return Uni.createFrom().item(NLQueryFunctionOutput.error("No existing filters found to update."));
                    }

                    StringBuilder filtersList = new StringBuilder();
                    filtersList.append("\n## EXISTING FILTERS\n");
                    filtersList.append("Abaixo estão os filtros existentes no cenário:\n\n");
                    for (FilterExpression expr : expressions) {
                        filtersList.append("ID: ").append(expr.getId())
                                .append(", Title: ").append(expr.getTitle())
                                .append("\n");
                        if (expr.getDescription() != null && !expr.getDescription().isBlank()) {
                            filtersList.append("   Description: ").append(expr.getDescription()).append("\n");
                        }
                        filtersList.append("   Layers:\n");
                        for (FilterExpression.Layer layer : expr.getLayers()) {
                            filtersList.append("     - LayerName: ").append(layer.getLayerName()).append("\n");
                            filtersList.append("       RuleJson: ").append(layer.getRuleJson()).append("\n");
                        }
                    }
                    filtersList.append("\nO utilizador pretende atualizar um filtro existente. ");
                    filtersList.append("Identifique qual filtro o utilizador deseja alterar e devolva `plan.filterId` com o ID correspondente.\n");

                    return askAgent(buildPrompt(input, filtersList.toString()))
                            .flatMap(newAgentResponse -> handleAgentResponse(newAgentResponse, input, false));
                })
                .onFailure().recoverWithItem(err -> {
                    System.err.println("Erro ao buscar ou processar filtros: " + err.getMessage());
                    return NLQueryFunctionOutput.error("Failed to fetch existing filters: " + err.getMessage());
                });
    }

    /**
     * Parses the clarification options from the agent's response.
     */
    private List<ClarificationOption> parseClarificationOptions(JsonNode optionsNode) {
        List<ClarificationOption> options = new ArrayList<>();
        if (optionsNode.isArray()) {
            for (JsonNode optNode : optionsNode) {
                String id = extractTextField(optNode, "id");
                String label = extractTextField(optNode, "label");
                String value = extractTextField(optNode, "value");
                if (id != null && label != null && value != null) {
                    options.add(new ClarificationOption(id, label, value));
                }
            }
        }
        return options;
    }

    /**
     * Extracts a non-blank text field from a JSON response.
     */
    private String extractTextField(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        String value = field.asText(null);
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    /**
     * Extracts the first warning message from the agent's response.
     */
    private String extractFirstWarning(JsonNode agentResponse) {
        JsonNode warnings = agentResponse.path("warnings");
        if (warnings.isArray() && !warnings.isEmpty()) {
            return warnings.get(0).asText();
        }
        return "The agent could not generate a filter.";
    }

    /**
     * Sends the prompt to the Gemini API. The call is deliberately synchronous: the whole
     * Uni chain has to stay on the thread that subscribes to it, because the calls made to
     * BackendService afterwards need the request-scoped identity of that thread.
     * The API key travels in a header, never in the request URL.
     */
    private String sendToGemini(String prompt) {
        String jsonPayload;
        try {
            Map<String, Object> payload = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            jsonPayload = MAPPER.writeValueAsString(payload);
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize the prompt: " + e.getMessage(), e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", GEMINI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("The call to the agent was interrupted.", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not reach the agent: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API returned " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    /**
     * Extracts and cleans the JSON from the Gemini response, removing markdown code fences.
     */
    private JsonNode extractJsonFromGeminiResponse(String rawResponse) {
        JsonNode root;
        try {
            root = MAPPER.readTree(rawResponse);
        } catch (IOException e) {
            throw new RuntimeException("Gemini response is not valid JSON: " + e.getMessage(), e);
        }

        // Uma resposta sem 'candidates' significa normalmente conteúdo bloqueado ou quota
        // esgotada. Sem esta verificação, o get(0) rebentava com uma mensagem opaca.
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            String reason = root.path("promptFeedback").path("blockReason").asText(null);
            if (reason == null) {
                reason = root.path("error").path("message").asText("no candidates returned");
            }
            throw new RuntimeException("Gemini returned no answer: " + reason);
        }

        String text = candidates.get(0)
                .path("content").path("parts").path(0)
                .path("text").asText("");

        String clean = text.replaceAll("(?i)```json\\s*|```", "").trim();
        if (clean.isEmpty()) {
            throw new RuntimeException("Gemini returned an empty answer.");
        }
        try {
            return MAPPER.readTree(clean);
        } catch (IOException e) {
            throw new RuntimeException("The agent did not return valid JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the complete prompt to be sent to Gemini.
     */
    private String buildPrompt(NLQueryFunctionInput input) {
        return buildPrompt(input, null);
    }

    /**
     * Builds the prompt, optionally appending extra context such as the list of
     * existing filters used by the update flow.
     */
    private String buildPrompt(NLQueryFunctionInput input, String extraContext) {
        StringBuilder prompt = new StringBuilder(SKILLS_CACHE);
        prompt.append("\n\n## CURRENT CONTEXT\n")
                .append("scenarioId: ").append(input.getScenario()).append("\n")
                .append("versionId: ").append(input.getVersion()).append("\n\n")
                .append("## ORIGINAL USER QUERY\n")
                .append(input.getQuery()).append("\n");

        if (input.getClarificationHistory() != null && !input.getClarificationHistory().isEmpty()) {
            prompt.append("\n## CLARIFICATION HISTORY\n");
            prompt.append("Perguntas já respondidas — usar estes valores, não repetir:\n\n");
            for (int i = 0; i < input.getClarificationHistory().size(); i++) {
                ClarificationExchange ex = input.getClarificationHistory().get(i);
                prompt.append(i + 1).append(". ");
                if (ex.getTopic() != null && !ex.getTopic().isBlank()) {
                    prompt.append("[").append(ex.getTopic()).append("] ");
                }
                prompt.append("Q: ").append(ex.getQuestion()).append("\n");
                prompt.append("   A: ").append(ex.getAnswer()).append("\n\n");
            }
        }

        if (extraContext != null && !extraContext.isBlank()) {
            prompt.append(extraContext);
        }

        return prompt.toString();
    }

    /**
     * Loads all skill markdown files from the /skills directory.
     */
    private static String loadSkills() {
        StringBuilder sb = new StringBuilder();
        List<String> skillFiles = getDefaultSkillFileList();

        for (String fileName : skillFiles) {
            try (InputStream is = NLQueryFunction.class.getResourceAsStream("/skills/" + fileName)) {
                if (is == null) {
                    System.err.println("Warning: skills/" + fileName + " not found.");
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String content = reader.lines().collect(Collectors.joining("\n"));
                    sb.append("\n---\n\n# ").append(fileName).append("\n\n");
                    sb.append(content);
                    sb.append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error when reading skills/" + fileName + ": " + e.getMessage());
            }
        }
        return sb.toString();
    }

    /**
     * Returns the default list of skill filenames to load.
     */
    private static List<String> getDefaultSkillFileList() {
        return List.of(
                "agent_role_and_output.md",
                "domain_semantics.md",
                "cos_land_use_catalog.md",
                "filter_schema_and_operators.md",
                "layers_and_attributes.md",
                "planning_examples.md",
                "SKILL.md",
                "land_it_context.md",
                "functions_reference.md",
                "clarification_types.md"
        );
    }

    /**
     * Loads the Gemini API key, preferring the GEMINI_API_KEY environment variable
     * so that the key does not have to be packaged inside the extension jar.
     */
    private static String loadApiKey() {
        String env = System.getenv("GEMINI_API_KEY");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String key = readProperty("gemini.api.key");
        if (key != null) {
            return key;
        }
        throw new IllegalStateException(
                "GEMINI_API_KEY undefined: set the environment variable or gemini.api.key in config.properties."
        );
    }

    /**
     * Loads the model name, so that switching model does not require a code change.
     */
    private static String loadModel() {
        String env = System.getenv("GEMINI_MODEL");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String model = readProperty("gemini.model");
        return model != null ? model : DEFAULT_MODEL;
    }

    /**
     * Reads a single non-blank property from config.properties, or null.
     */
    private static String readProperty(String name) {
        try (InputStream is = NLQueryFunction.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String value = props.getProperty(name);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config.properties: " + e.getMessage());
        }
        return null;
    }
}