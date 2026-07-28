package modules;

import DTOs.requests.ScenariosFilterInputDTO;
import base.BackendService;
import base.modules.FunctionModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import usefulObjects.filters.FilterExpression;
import modules.ClarificationOption;
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
    private static final String GEMINI_API_KEY = loadApiKey();
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + GEMINI_API_KEY;
    private static final String SKILLS_CACHE = loadSkills();

    private BackendService backendService;

    @Override
    public void initialize(BackendService backendService) {
        this.backendService = backendService;
    }

    @Override
    public Uni<NLQueryFunctionOutput> execute(NLQueryFunctionInput input) {
        String prompt = buildPrompt(input);
        return Uni.createFrom().item(() -> sendToGemini(prompt))
                .onItem().transform(this::extractJsonFromGeminiResponse)
                .onItem().transformToUni(agentResponse -> handleAgentResponse(agentResponse, input.getScenario(), input.getVersion()));
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
     * Processes the agent's response, extracting the filter from it and persisting it in LAND IT through saveFilter().
     *
     * @param agentResponse The JSON node containing the agent's response.
     * @param scenarioId    The active scenario ID.
     * @param versionId     The active version ID.
     * @return A {@link Uni} with the operation result.
     */
    private Uni<NLQueryFunctionOutput> handleAgentResponse(JsonNode agentResponse, Long scenarioId, Long versionId) {
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

        JsonNode filterNode = extractFilterNode(agentResponse);
        if (filterNode == null) {
            String warning = extractFirstWarning(agentResponse);
            return Uni.createFrom().item(NLQueryFunctionOutput.error("Error: " + warning));
        }

        try {
            String title = filterNode.path("title").asText("NL generated filter.");
            ScenariosFilterInputDTO dto = buildFilterInputDTO(filterNode);
            return backendService.saveFilter(scenarioId, versionId, dto)
                    .map(saved -> NLQueryFunctionOutput.success("Filter created successfully: " + title));
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(NLQueryFunctionOutput.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Parses the clarification options from the agent's response.
     *
     * @param optionsNode The JSON node containing the options array.
     * @return A list of {@link ClarificationOption} objects.
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
     *
     * @param node      The source JSON node.
     * @param fieldName The field to read.
     * @return The trimmed field value, or {@code null} when missing, null or blank.
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
     * Extracts the filter node from the agent's response.
     *
     * @param agentResponse The JSON node containing the agent's response.
     * @return The "filter" node, or {@code null} if it does not exist.
     */
    private JsonNode extractFilterNode(JsonNode agentResponse) {
        JsonNode plan = agentResponse.get("plan");
        if (plan == null || plan.isNull()) {
            return null;
        }
        return plan.get("filter");
    }

    /**
     * Extracts the first warning message from the agent's response.
     *
     * @param agentResponse The JSON node containing the agent's response.
     * @return The text of the first warning, or a generic message if no warnings exist.
     */
    private String extractFirstWarning(JsonNode agentResponse) {
        JsonNode warnings = agentResponse.path("warnings");
        if (warnings.isArray() && !warnings.isEmpty()) {
            return warnings.get(0).asText();
        }
        return "The agent could not generate a filter.";
    }

    /**
     * Builds a {@link ScenariosFilterInputDTO} from the filter node of the agent's response.
     *
     * @param filterNode The JSON node containing the filter definition.
     * @return The DTO ready to be persisted.
     */
    private ScenariosFilterInputDTO buildFilterInputDTO(JsonNode filterNode) {
        String title = filterNode.path("title").asText("Filter generated by NL");
        String description = filterNode.path("description").asText(null);
        boolean activated = filterNode.path("activated").asBoolean(true);
        List<FilterExpression.Layer> layers = parseLayers(filterNode.path("layers"));
        FilterExpression expression = new FilterExpression(title, layers, activated);
        if (description != null) {
            expression.setDescription(description);
        }
        return new ScenariosFilterInputDTO(List.of(expression));
    }

    /**
     * Parses the list of filter layers from the JSON node.
     *
     * @param layersNode The JSON node containing the layers array.
     * @return A list of {@link FilterExpression.Layer} objects.
     * @throws IllegalArgumentException if the node is not a non-empty array.
     */
    private List<FilterExpression.Layer> parseLayers(JsonNode layersNode) {
        List<FilterExpression.Layer> layers = new ArrayList<>();
        if (!layersNode.isArray() || layersNode.isEmpty()) {
            throw new IllegalArgumentException("'layers' must be a non-empty array.");
        }
        for (JsonNode layerNode : layersNode) {
            String layerName = layerNode.path("layerName").asText();
            String ruleJson = layerNode.path("ruleJson").toString();
            layers.add(new FilterExpression.Layer(layerName, ruleJson));
        }
        return layers;
    }

    /**
     * Sends the prompt to the Gemini API and returns the raw response.
     *
     * @param prompt The complete prompt text to send.
     * @return The Gemini API response as a JSON string.
     * @throws RuntimeException if communication fails or the API returns an error.
     */
    private String sendToGemini(String prompt) {
        try {
            Map<String, Object> payload = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonPayload = MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API returned " + response.statusCode() + ": " + response.body());
            }
            return response.body();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error contacting Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts and cleans the JSON from the Gemini response, removing markdown code fences.
     *
     * @param rawResponse The raw response from the Gemini API.
     * @return The cleaned JSON as a {@link JsonNode}.
     * @throws RuntimeException if parsing fails.
     */
    private JsonNode extractJsonFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = MAPPER.readTree(rawResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Remove markdown ```json ... ```
            String clean = text.replaceAll("(?i)```json\\s*|```", "").trim();
            return MAPPER.readTree(clean);

        } catch (Exception e) {
            throw new RuntimeException("Invalid Gemini response: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the complete prompt to be sent to Gemini.
     * The prompt includes the skills (documentation), the current context (scenarioId, versionId),
     * the user's original query and the last clarification exchange.
     *
     * @param input The function input received from the frontend.
     * @return The complete prompt as a string.
     */
    private String buildPrompt(NLQueryFunctionInput input) {
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

        return prompt.toString();
    }

    /**
     * Loads all skill markdown files from the /skills directory, which is in the resources directory.
     * The files are read and concatenated into a single string, which is cached
     * for reuse across all calls.
     *
     * @return The concatenated content of all skills.
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
     *
     * @return the list of skill filenames.
     */
    private static List<String> getDefaultSkillFileList() {
        return Arrays.asList(
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
     * Loads the Gemini API key from the configuration file.
     * The file must be located at {@code /config.properties} in the resources directory,
     * and contain the property {@code gemini.api.key}.
     *
     * @return The API key.
     * @throws IllegalStateException if the key is not found.
     */
    private static String loadApiKey() {
        try (InputStream is = NLQueryFunction.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String propKey = props.getProperty("gemini.api.key");
                if (propKey != null && !propKey.isBlank()) {
                    return propKey.trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config.properties: " + e.getMessage());
        }
        throw new IllegalStateException(
                "GEMINI_API_KEY not defined. Set the GEMINI_API_KEY environment variable or create src/main/resources/config.properties with: gemini.api.key=AIzaSy..."
        );
    }
}