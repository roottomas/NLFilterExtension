# Tipos de clarificação

Três tipos estruturados. Usar **um de cada vez**, por ordem de prioridade abaixo. Estrutura JSON da resposta: `agent_role_and_output.md`.

## Tópicos de clarificação (tabela canónica)

Incluir `clarification_topic` em **todas** as respostas de clarificação (permite ao backend saber que perguntas já foram resolvidas e evitar repetições).

| Topic             | Quando usar                                                                  | Tipo               |
|-------------------|------------------------------------------------------------------------------|--------------------|
| `LAYER_CONFLICT`  | Atributo POSP + atributo de Transformação na mesma query                     | `multi_choice`     |
| `AREA`            | Limiar de área em falta ("área grande")                                      | `numeric_threshold`|
| `COST`            | Limiar de custo em falta ("custo elevado")                                   | `numeric_threshold`|
| `SLOPE`           | Declive sem qualificador                                                     | `multi_choice`     |
| `GENERIC_CHOICE`  | Qualquer escolha múltipla de classes COS (florestas, urbano, pomares, etc.)  | `multi_choice`     |
| `POSA_VS_POSP`    | Falta distinguir uso atual vs proposto                                       | `multi_choice`     |
| `GENERIC_NUMERIC` | Outro limiar numérico (fallback)                                             | `numeric_threshold`|
| `GENERIC_TEXT`    | Ambiguidade não classificável (fallback)                                     | `free_text`        |

Se a query contém múltiplas categorias (ex: florestas OU urbano), clarificar **cada categoria separadamente** com `GENERIC_CHOICE`, uma vez por categoria, até todas estarem resolvidas.

## Prioridade (ordem obrigatória)

Antes de qualquer outra clarificação, resolver por esta ordem:

1. **Limiar numérico em falta** — "área grande", "custo elevado" → `numeric_threshold` (`AREA`, `COST`). **NUNCA** assumir valores de custo/área — ver frases-modelo em `domain_semantics.md`.
2. **Conflito de camada** — uso do solo + atributo de Transformação → `multi_choice` (`LAYER_CONFLICT`).
3. **Declive ambíguo** — "declive elevado" sem valor → `multi_choice` com 2 opções (`SLOPE`).
4. **Classe COS ambígua** — "floresta", "urbano" → `multi_choice` (`GENERIC_CHOICE`).
5. **POSA vs POSP em falta** — "mostrar olival" sem contexto → `multi_choice` (`POSA_VS_POSP`).
6. **Outras ambiguidades** → `free_text` (`GENERIC_TEXT`).

## Quando NÃO clarificar

| Condição | Exemplo |
|----------|---------|
| Mapeamento 1:1 em `domain_semantics.md` | "eucalipto" → Florestas de eucalipto |
| Contexto resolve POSA/POSP | "na proposta" → POSP; "atual" → POSA |
| Subclasse única no COS | "olival" → Olivais |
| Valor numérico explícito | "área > 5 ha" |
| Declive com qualificador claro | "declive elevado" → `">= 25%"` (aplicar diretamente) |

## Histórico de clarificações

Se `CLARIFICATION HISTORY` contém respostas, usá-las diretamente. Não repetir perguntas sobre tópicos já resolvidos. Quando todas as ambiguidades estão resolvidas, gerar o filtro.

Formato das respostas do utilizador:

| Tipo | Formato |
|------|---------|
| `multi_choice` | Valores separados por vírgula, ex: `"Florestas de eucalipto, Florestas de sobreiro"` |
| `numeric_threshold` | JSON string, ex: `{"min":0,"max":50}` |
| `free_text` | Texto livre |

---

## 1. `multi_choice`

Para ambiguidades resolvíveis por lista de opções.

**Regras:**
- Sempre incluir `clarification_options` com `id`, `label`, `value`.
- **Nunca** incluir opção "Outra" — o frontend adiciona-a automaticamente.
- **Classes COS:** incluir opção agregadora `"Todas as <categoria>"` com `value = "__GROUP__:<grupo>"`. Máx. 12–15 opções. Ver grupos em `cos_land_use_catalog.md`. (Expansão de `__GROUP__` no filtro: `filter_schema_and_operators.md`.)

**Conflito de camada** (POSP + Transformação):

```json
{
  "plan": null,
  "clarification_question": "A query mistura atributos de camadas diferentes. Pretende filtrar apenas na POSP, apenas nas Unidades de Transformação, ou criar dois filtros separados?",
  "clarification_type": "multi_choice",
  "clarification_topic": "LAYER_CONFLICT",
  "clarification_options": [
    { "id": "posp_only", "label": "Apenas POSP", "value": "POSP_ONLY" },
    { "id": "transform_only", "label": "Apenas Unidades de Transformação", "value": "TRANSFORM_ONLY" },
    { "id": "both_separate", "label": "Ambos no mesmo filtro (duas camadas)", "value": "BOTH_SEPARATE" }
  ],
  "user_response": null,
  "confidence": 0.60
}
```

**Classes COS ambíguas** (florestas, urbano, etc.):

```json
{
  "plan": null,
  "clarification_question": "Que ocupações pretende incluir?",
  "clarification_type": "multi_choice",
  "clarification_topic": "GENERIC_CHOICE",
  "clarification_options": [
    { "id": "all", "label": "Todas as florestas", "value": "__GROUP__:Florestas" },
    { "id": "eucalipto", "label": "Eucalipto", "value": "Florestas de eucalipto" },
    { "id": "sobreiro", "label": "Sobreiro", "value": "Florestas de sobreiro" }
  ],
  "user_response": null,
  "confidence": 0.55
}
```

**Declive** (sempre `multi_choice`, nunca `numeric_threshold`):

```json
{
  "plan": null,
  "clarification_question": "Que intervalo de declive pretende?",
  "clarification_type": "multi_choice",
  "clarification_topic": "SLOPE",
  "clarification_options": [
    { "id": "low", "label": "Declive < 25% (baixo)", "value": "< 25%" },
    { "id": "high", "label": "Declive >= 25% (elevado)", "value": ">= 25%" }
  ],
  "user_response": null,
  "confidence": 0.60
}
```

---

## 2. `numeric_threshold`

Para valores quantificáveis sem número especificado. **Não usar para `slope`** (ver acima).

Campos obrigatórios: `field`, `unit`.

```json
{
  "plan": null,
  "clarification_question": "Qual o intervalo de área pretendido?",
  "clarification_type": "numeric_threshold",
  "clarification_topic": "AREA",
  "field": "area",
  "unit": "hectares",
  "user_response": null,
  "confidence": 0.60,
  "warnings": []
}
```

| Termo vago                 | Field  | Unit     |
|----------------------------|--------|----------|
| "grande", "pequena" (área) | `area` | hectares |
| "caro", "barato"           | `cost` | euros    |

---

## 3. `free_text`

Fallback para ambiguidades não classificáveis.

```json
{
  "plan": null,
  "clarification_question": "Pode descrever melhor o que entende por '...'?",
  "clarification_type": "free_text",
  "clarification_topic": "GENERIC_TEXT",
  "user_response": null,
  "confidence": 0.40,
  "warnings": []
}
```

## Clarificação durante o update

Quando o utilizador edita um filtro e a query contém termos ambíguos ("florestas", "urbano"), a clarificação tem **prioridade** sobre a atualização: clarificar primeiro o termo ambíguo, e só depois aplicar a atualização com o `filterId`. Ver `agent_role_and_output.md` (§Detetar intenção de atualizar filtro).
