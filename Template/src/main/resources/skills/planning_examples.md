# Natural language planning examples

Each example shows full reasoning and output JSON. Land-use values use **COS Nomes** (strings), not identifiers. Filters use **JsonLogic** in `plan.filter` (see `filter_schema_and_operators.md`). The `steps` array is always empty for now; in a more advanced version we'll use it; the executor directly calls `saveFilter` with the provided filter.

All outputs must include top-level `clarification_question` and `user_response`. Use `null` for both when the request is clear enough to create a filter.

---

## Regras aplicadas nestes exemplos (ver ficheiros dedicados)

- **Formato do `ruleJson`, operadores (`or`/`and`, `in`, `==`), exceção `slope`, conversão ha→m² e `__GROUP__`:** `filter_schema_and_operators.md`.
- **Regras da `description` do filtro (obrigatória quando há filtro):** `agent_role_and_output.md` (§Description rules).
- **Tópicos de clarificação (`clarification_topic`) e prioridade:** `clarification_types.md`.

---

## 1 — Simple categorical filter (proposta) → `or`

**Input:** "mostrar apenas áreas de eucalipto na proposta"

**Reasoning:** proposta → camada `POSP`, atributo `POSP`; eucalipto → `"Florestas de eucalipto"`

{
"plan": {
"steps": [],
"filter": {
"title": "Eucalipto na proposta",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "==": [{ "var": "POSP" }, "Florestas de eucalipto"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.97,
"warnings": []
}

---

## 2 — Simple categorical filter (uso atual) → `or`

**Input:** "mostrar onde o uso atual é olival"

**Reasoning:** uso atual → camada POSP, atributo POSA; olival → "Olivais"

{
"plan": {
"steps": [],
"filter": {
"title": "Olival no uso atual",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso atual é 'Olivais'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "==": [{ "var": "POSA" }, "Olivais"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.97,
"warnings": []
}

---

## 3 — Numeric filter with unit conversion → `or`

**Input:** "polígonos da POSP com área superior a 5 hectares"

**Reasoning:** 5 ha = 50 000 m²; var area; operator >

{
"plan": {
"steps": [],
"filter": {
"title": "Área > 5 ha",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP com área superior a 5 hectares (50 000 m²).",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ ">": [{ "var": "area" }, "50000"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.99,
"warnings": []
}

---

## 4 — Multiple land uses (OU) + area (E) → `and`

**Input:** "olival ou sobreiro na proposta com pelo menos 2 hectares"

**Reasoning:** olival → "Olivais", sobreiro → "Florestas de sobreiro"; 2 ha = 20 000 m²

{
"plan": {
"steps": [],
"filter": {
"title": "Olival ou Sobreiro ≥ 2 ha",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Olivais' ou 'Florestas de sobreiro', E com área superior ou igual a 2 hectares (20 000 m²).",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"and": [
{ "in": [{ "var": "POSP" }, ["Olivais", "Florestas de sobreiro"]] },
{ ">=": [{ "var": "area" }, 20000] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.96,
"warnings": []
}

---

## 5 — Parent category «pomares» (usa in com lista de todos os pomares)

**Input:** "mostrar todos os pomares na proposta"

**Reasoning:** expandir grupo Pomares do catálogo COS; usa in porque são vários valores

{
"plan": {
"steps": [],
"filter": {
"title": "Todos os Pomares",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é qualquer uma das classes de pomares: Pomares, Pomares: Citrinos, Pomares: Marmeleiros, Pomares: Nogueira, Pomares: Amendoal, Pomares: Medronhal ou Pomares: Pinheiro Manso.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "in": [{ "var": "POSP" }, ["Pomares", "Pomares: Citrinos", "Pomares: Marmeleiros", "Pomares: Nogueira", "Pomares: Amendoal", "Pomares: Medronhal", "Pomares: Pinheiro Manso"]] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.93,
"warnings": ["Inclui o tipo genérico Pomares e todas as subclasses."]
}

---

## 6 — Transformation layer (slope + cost) → `and`

**Input:** "unidades de transformação em terreno inclinado com custo acima de 5000€"

**Reasoning:** inclinado → slope = ">= 25%"; custo → cost > 5000. AMBAS as condições devem ser verdadeiras → `and`

{
"plan": {
"steps": [],
"filter": {
"title": "Declive ≥ 25% e Custo > 5000€",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra as Unidades de Transformação com declive igual ou superior a 25% (terreno inclinado) e custo estimado superior a 5 000 €.",
"activated": true,
"layers": [
{
"layerName": "Unidades de Transformação",
"ruleJson": {
"and": [
{ "==": [{ "var": "slope" }, ">= 25%"] },
{ "==": [{ "var": "cost" }, "> 5000"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.92,
"warnings": ["«Inclinado» interpretado como declive >= 25%."]
}

---

## 7 — Ecosystem services (REN) → `or`

**Input:** "serviços de ecossistema em zona REN"

**Reasoning:** ren = true

{
"plan": {
"steps": [],
"filter": {
"title": "Serviços de Ecossistema em REN",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os Serviços de Ecossistemas que estão localizados em REN.",
"activated": true,
"layers": [
{
"layerName": "Serviços de Ecossistemas",
"ruleJson": {
"or": [
{ "==": [{ "var": "ren" }, "true"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.98,
"warnings": []
}

---

## 8 — Respect active user filters → `or`

**Input:** "dentro do que já tenho filtrado, mostrar só pinheiro bravo na proposta"

**Reasoning:** pinheiro bravo → "Florestas de pinheiro bravo"; usa == porque é um único valor

{
"plan": {
"steps": [],
"filter": {
"title": "Pinheiro Bravo sobre filtros ativos",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Restringe a POSP a mostrar apenas os polígonos cujo uso proposto é 'Florestas de pinheiro bravo', aplicado sobre o conjunto já filtrado pelo utilizador.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "==": [{ "var": "POSP" }, "Florestas de pinheiro bravo"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.88,
"warnings": ["Assume que o executor compõe com os filtros ativos do utilizador."]
}

---

## 9 — Simple area filter → `or`

**Input:** "Ver polígonos da POSP com área superior a 100 metros quadrados"

**Reasoning:** area > 100

{
"plan": {
"steps": [],
"filter": {
"title": "Área > 100 m²",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP com área superior a 100 metros quadrados.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ ">": [{ "var": "area" }, "100"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.99,
"warnings": []
}

---

## 10 — Example using current use (POSA) with multiple conditions → `and`

**Input:** "mostrar áreas onde o uso atual é mato e a proposta é pinheiro bravo"

**Reasoning:** uso atual → "Matos"; proposta → "Florestas de pinheiro bravo"; AMBAS as condições devem ser verdadeiras → `and`

{
"plan": {
"steps": [],
"filter": {
"title": "Mato atual → Pinheiro Bravo proposto",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso atual é 'Matos' e cujo uso proposto é 'Florestas de pinheiro bravo'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"and": [
{ "==": [{ "var": "POSA" }, "Matos"] },
{ "==": [{ "var": "POSP" }, "Florestas de pinheiro bravo"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.95,
"warnings": ["A comparação entre uso atual e proposta é feita na mesma camada POSP."]
}

---

## Exemplo 11 — Clarification: ambiguous land-use category (multi_choice)

**Input:** "mostrar floresta"

**Reasoning:** floresta pode significar todas as classes florestais ou uma espécie específica. Usar multi_choice com lista de opções.

{
"plan": null,
"clarification_question": "Que ocupações florestais pretende incluir?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "all", "label": "Todas as florestas", "value": "__GROUP__:Florestas" },
{ "id": "eucalipto", "label": "Eucalipto", "value": "Florestas de eucalipto" },
{ "id": "sobreiro", "label": "Sobreiro", "value": "Florestas de sobreiro" },
{ "id": "azinheira", "label": "Azinheira", "value": "Florestas de azinheira" },
{ "id": "pinheiro_bravo", "label": "Pinheiro bravo", "value": "Florestas de pinheiro bravo" },
{ "id": "pinheiro_manso", "label": "Pinheiro manso", "value": "Florestas de pinheiro manso" }
],
"user_response": null,
"confidence": 0.55,
"warnings": ["A expressão 'floresta' é ambígua no catálogo COS."]
}

---

## Exemplo 12 — Final filter after multi_choice clarification

**Original query:** "mostrar floresta"
**Previous clarification question:** "Que ocupações florestais pretende incluir?"
**User response:** "Florestas de eucalipto, Florestas de sobreiro"

**Reasoning:** a resposta resolve a ambiguidade. O utilizador selecionou duas opções. Proposta por omissão → POSP.

{
"plan": {
"steps": [],
"filter": {
"title": "Eucalipto e Sobreiro na proposta",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto' ou 'Florestas de sobreiro'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de sobreiro"]] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": "Florestas de eucalipto, Florestas de sobreiro",
"confidence": 0.95,
"warnings": []
}

---

## Exemplo 13 — Clarification: POSA vs POSP missing (multi_choice)

**Input:** "mostrar olival"

**Reasoning:** "olival" é claro como classe COS, mas a query não diz se pretende uso atual ou proposta. Usar multi_choice com 2 opções.

{
"plan": null,
"clarification_question": "Pretende filtrar pelo uso atual ou pela proposta?",
"clarification_type": "multi_choice",
"clarification_topic": "POSA_VS_POSP",
"clarification_options": [
{ "id": "posa", "label": "Uso atual", "value": "POSA" },
{ "id": "posp", "label": "Proposta", "value": "POSP" }
],
"user_response": null,
"confidence": 0.58,
"warnings": ["Falta distinguir entre POSA e POSP."]
}

---

## Exemplo 14 — Clarification: numeric threshold missing (numeric_threshold)

**Input:** "áreas com declive elevado"

**Reasoning:** "declive elevado" não tem um valor numérico especificado. Usar numeric_threshold com field e unit.

{
"plan": null,
"clarification_question": "Qual o intervalo de declive pretendido?",
"clarification_type": "numeric_threshold",
"clarification_topic": "SLOPE",
"field": "slope",
"unit": "%",
"user_response": null,
"confidence": 0.65,
"warnings": ["'Declive elevado' precisa de clarificação numérica."]
}

---

## Exemplo 15 — Clarification: free_text fallback

**Input:** "zonas com vista para o mar"

**Reasoning:** "zonas com vista para o mar" não é uma classe COS nem um valor numérico. Usar free_text como fallback.

{
"plan": null,
"clarification_question": "Pode descrever melhor o que entende por 'zonas com vista para o mar'? (ex: distância à costa, altitude, orientação)",
"clarification_type": "free_text",
"clarification_topic": "GENERIC_TEXT",
"user_response": null,
"confidence": 0.40,
"warnings": []
}

---

## Exemplo 16 — No clarification: explicit proposed use → `or`

**Input:** "mostrar olival na proposta"

**Reasoning:** "olival" mapeia para "Olivais" e "na proposta" indica o campo POSP. Não pedir clarificação.

{
"plan": {
"steps": [],
"filter": {
"title": "Olival na proposta",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Olivais'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "==": [{ "var": "POSP" }, "Olivais"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.97,
"warnings": []
}

---

## Exemplo 17 — Filter with slope condition → `or`

**Input:** "unidades de transformação com declive superior a 25%"

**Reasoning:** slope = ">= 25%"

{
"plan": {
"steps": [],
"filter": {
"title": "Declive >= 25%",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra as Unidades de Transformação com declive igual ou superior a 25%.",
"activated": true,
"layers": [
{
"layerName": "Unidades de Transformação",
"ruleJson": {
"or": [
{ "==": [{ "var": "slope" }, ">= 25%"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.95,
"warnings": []
}

---

## Exemplo 18 — Filter with slope low → `or`

**Input:** "unidades de transformação com declive baixo"

**Reasoning:** slope = "< 25%"

{
"plan": {
"steps": [],
"filter": {
"title": "Declive < 25%",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra as Unidades de Transformação com declive inferior a 25%.",
"activated": true,
"layers": [
{
"layerName": "Unidades de Transformação",
"ruleJson": {
"or": [
{ "==": [{ "var": "slope" }, "< 25%"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.95,
"warnings": []
}

---

## Exemplo 19 — Clarification for slope (multi_choice with 2 options)

**Input:** "unidades de transformação com declive"

**Reasoning:** "declive" sem qualificador é ambíguo. Usar multi_choice com 2 opções.

{
"plan": null,
"clarification_question": "Que intervalo de declive pretende?",
"clarification_type": "multi_choice",
"clarification_topic": "SLOPE",
"clarification_options": [
{ "id": "low", "label": "Declive inferior a 25% (baixo)", "value": "< 25%" },
{ "id": "high", "label": "Declive igual ou superior a 25% (elevado)", "value": ">= 25%" }
],
"user_response": null,
"confidence": 0.60,
"warnings": ["O campo declive tem apenas dois valores válidos: inferior a 25% e igual ou superior a 25%."]
}

---

## Exemplo 20 — Multiple conditions com OR (um OU outro) → `or`

**Input:** "mostrar eucalipto ou pinheiro bravo na proposta"

**Reasoning:** eucalipto → "Florestas de eucalipto"; pinheiro bravo → "Florestas de pinheiro bravo". O utilizador quer UM OU OUTRO → `or` com `in`

{
"plan": {
"steps": [],
"filter": {
"title": "Eucalipto ou Pinheiro Bravo",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto' ou 'Florestas de pinheiro bravo'.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de pinheiro bravo"]] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.96,
"warnings": []
}

---

## Exemplo 21 — Multiple conditions com AND (um E outro) → `and`

**Input:** "mostrar eucalipto na proposta com área > 5 ha"

**Reasoning:** eucalipto → "Florestas de eucalipto"; área > 5 ha. O utilizador quer AMBAS as condições → `and`

{
"plan": {
"steps": [],
"filter": {
"title": "Eucalipto com área > 5 ha",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto' E com área superior a 5 hectares (50 000 m²).",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"and": [
{ "==": [{ "var": "POSP" }, "Florestas de eucalipto"] },
{ ">": [{ "var": "area" }, "50000"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.97,
"warnings": []
}

---

## Exemplo 22 — Filtro com duas camadas (POSP + Transformação)

**Input:** "mostrar florestas com declive elevado"

**Reasoning:** A query contém atributos de duas camadas: florestas (POSP) e declive (Transformação). O agente cria um único filtro com duas layers, cada uma com o seu ruleJson.

**Clarificações (simuladas):**
- "Que ocupações florestais?" → "Florestas de pinheiro bravo"
- "Que intervalo de declive?" → ">= 25%"

**Resposta final:**

{
"plan": {
"steps": [],
"filter": {
"title": "Pinheiro bravo com declive elevado",
"description": "Este filtro foi criado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de pinheiro bravo' e as Unidades de Transformação com declive igual ou superior a 25%.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"or": [
{ "==": [{ "var": "POSP" }, "Florestas de pinheiro bravo"] }
]
}
},
{
"layerName": "Unidades de Transformação",
"ruleJson": {
"or": [
{ "==": [{ "var": "slope" }, ">= 25%"] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.95,
"warnings": ["Filtro com duas camadas."]
}

## Exemplo 23 — Detetar intenção de update (need_filters)

**Input:** "muda no filtro dos eucaliptos a área para > 10 ha"

**Reasoning:** O utilizador quer atualizar um filtro existente. O agente deteta as palavras-chave "muda" e "filtro dos eucaliptos".

**Resposta:**

{
"plan": null,
"clarification_question": null,
"need_filters": true,
"user_response": null,
"confidence": 0.75,
"warnings": ["O utilizador pretende editar um filtro existente."]
}

---

## Exemplo 24 — Identificar filtro a partir da lista (com filterId)

**Input (segundo prompt, após backend fornecer lista de filtros):**

**Lista de filtros fornecida pelo backend:**
- ID: 1, Title: "Eucalipto na proposta", Description: "Mostra eucaliptos na POSP"
- ID: 2, Title: "Pinheiro bravo", Description: "Mostra pinheiros bravos"

**Query original:** "muda no filtro dos eucaliptos a área para > 10 ha"

**Clarification History:** (a troca anterior com `need_filters: true`)

**Reasoning:** O agente identifica que "filtro dos eucaliptos" corresponde ao filtro com título "Eucalipto na proposta" (ID 1). Gera um novo filtro com a área atualizada (10 ha = 100000 m²).

**Resposta:**

{
"plan": {
"filterId": 1,
"filter": {
"title": "Eucalipto na proposta (área > 10 ha)",
"description": "Este filtro foi atualizado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto' com área superior a 10 hectares.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"and": [
{ "==": [{ "var": "POSP" }, "Florestas de eucalipto"] },
{ ">=": [{ "var": "area" }, 100000] }
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.92,
"warnings": ["Filtro atualizado com sucesso."]
}

---

## Exemplo 25 — Múltiplos filtros com nomes semelhantes (clarificação)

**Input:** "altera o filtro das florestas para incluir também sobreiro"

**Lista de filtros fornecida:**
- ID: 3, Title: "Florestas de eucalipto"
- ID: 4, Title: "Florestas de pinheiro bravo"

**Reasoning:** Existem vários filtros com "florestas" no título. O agente não consegue identificar um único filtro, pelo que pede clarificação ao utilizador.

**Resposta:**

{
"plan": null,
"clarification_question": "Encontrei vários filtros com 'florestas' no título. Qual pretende alterar?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "3", "label": "Florestas de eucalipto", "value": "3" },
{ "id": "4", "label": "Florestas de pinheiro bravo", "value": "4" }
],
"user_response": null,
"confidence": 0.60,
"warnings": ["Múltiplos filtros correspondem à pesquisa."]
}

## Exemplo 26 — Update com termo ambíguo (clarificação antes de atualizar)

**Input (primeira chamada):** "muda o filtro dos eucaliptos para incluir florestas"

**Lista de filtros existentes (fornecida pelo backend):**
- ID: 1, Title: "Eucaliptos com área entre 5 e 20 ha"

**Reasoning:** O utilizador quer atualizar o filtro com ID 1, mas o termo "florestas" é ambíguo. O agente NÃO deve aplicar diretamente "florestas". Deve clarificar.

**Resposta:**

{
"plan": null,
"clarification_question": "Que ocupações florestais pretende incluir no filtro?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "all", "label": "Todas as florestas", "value": "__GROUP__:Florestas" },
{ "id": "eucalipto", "label": "Eucalipto", "value": "Florestas de eucalipto" },
{ "id": "sobreiro", "label": "Sobreiro", "value": "Florestas de sobreiro" },
{ "id": "azinheira", "label": "Azinheira", "value": "Florestas de azinheira" },
{ "id": "pinheiro_bravo", "label": "Pinheiro bravo", "value": "Florestas de pinheiro bravo" },
{ "id": "pinheiro_manso", "label": "Pinheiro manso", "value": "Florestas de pinheiro manso" }
],
"user_response": null,
"confidence": 0.55,
"warnings": ["O termo 'florestas' é ambíguo. A clarificar antes de atualizar."]
}

---

## Exemplo 27 — Aplicar update após clarificação

**Input (segunda chamada, após resposta do utilizador):**
- Resposta à clarificação: "Florestas de sobreiro, Florestas de azinheira"
- Filtro identificado: ID 1 ("Eucaliptos com área entre 5 e 20 ha")

**Reasoning:** O utilizador especificou as espécies. O agente pode agora gerar o filtro atualizado com o `filterId`.

**Resposta:**

{
"plan": {
"filterId": 1,
"filter": {
"title": "Eucaliptos, sobreiro e azinheira com área entre 5 e 20 ha",
"description": "Este filtro foi atualizado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto', 'Florestas de sobreiro' ou 'Florestas de azinheira' e com área entre 5 e 20 hectares.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": {
"and": [
{ "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de sobreiro", "Florestas de azinheira"]] },
{ "and": [
{ ">=": [{ "var": "area" }, 50000] },
{ "<=": [{ "var": "area" }, 200000] }
]
}
]
}
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.92,
"warnings": ["Filtro atualizado com espécies especificadas."]
}

## Exemplo 28 — Update com referência vaga (sem título)

**Input:** "muda o filtro"

**Lista de filtros existentes (fornecida pelo backend):**
- ID: 1, Title: "Eucaliptos com área entre 5 e 20 ha"
- ID: 2, Title: "Pinheiro manso"
- ID: 3, Title: "Florestas de sobreiro"

**Reasoning:** O utilizador não especificou qual filtro. O agente deve perguntar.

**Resposta:**

{
"plan": null,
"clarification_question": "Qual filtro pretende atualizar?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "1", "label": "Eucaliptos com área entre 5 e 20 ha", "value": "1" },
{ "id": "2", "label": "Pinheiro manso", "value": "2" },
{ "id": "3", "label": "Florestas de sobreiro", "value": "3" }
],
"user_response": null,
"confidence": 0.60,
"warnings": ["O utilizador não especificou qual filtro atualizar."]
}

---

## Exemplo 29 — Update com operador lógico ambíguo

**Input:** "muda o filtro dos eucaliptos para mostrar eucalipto ou pinheiro bravo"

**Lista de filtros existentes (fornecida pelo backend):**
- ID: 1, Title: "Eucaliptos com área entre 5 e 20 ha"

**Reasoning:** O utilizador especificou o filtro ("dos eucaliptos"), mas o operador lógico "ou" pode ser interpretado como `or` ou `and`. O agente clarifica antes de atualizar.

**Resposta:**

{
"plan": null,
"clarification_question": "Pretende incluir eucalipto E pinheiro bravo (ambos), ou eucalipto OU pinheiro bravo (um ou outro)?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "both", "label": "Eucalipto E Pinheiro bravo (ambos)", "value": "AND" },
{ "id": "either", "label": "Eucalipto OU Pinheiro bravo (um ou outro)", "value": "OR" }
],
"user_response": null,
"confidence": 0.55,
"warnings": ["Operador lógico ambíguo. A clarificar."]
}

## Exemplo 30 — Update com preservação de condições existentes

**Filtro original:**
- ID: 1, Title: "Eucaliptos e pinheiros bravos"
- Layers:
    - POSP: { "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de pinheiro bravo"]] }
    - Unidades de Transformação: { "and": [{ ">=": [{ "var": "area" }, 50000] }, { "<=": [{ "var": "area" }, 200000] }] }

**Input:** "no filtro dos eucaliptos, muda a área para 3.5"

**Lista de filtros fornecida pelo backend:**
- ID: 1, Title: "Eucaliptos e pinheiros bravos"
  Layers:
    - LayerName: POSP
      RuleJson: { "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de pinheiro bravo"]] }
    - LayerName: Unidades de Transformação
      RuleJson: { "and": [{ ">=": [{ "var": "area" }, 50000] }, { "<=": [{ "var": "area" }, 200000] }] }

**Reasoning:** O utilizador pede para alterar a área para 3.5 ha. As condições de uso do solo NÃO foram mencionadas, por isso devem ser mantidas. O agente atualiza apenas a área.

**Resposta:**

{
"plan": {
"filterId": 1,
"filter": {
"title": "Eucaliptos e pinheiros bravos (área >= 3.5 ha)",
"description": "Este filtro foi atualizado pela extensão 'NL Filter Extension'. Mostra os polígonos da POSP cujo uso proposto é 'Florestas de eucalipto' ou 'Florestas de pinheiro bravo', com área superior ou igual a 3.5 hectares.",
"activated": true,
"layers": [
{
"layerName": "POSP",
"ruleJson": { "in": [{ "var": "POSP" }, ["Florestas de eucalipto", "Florestas de pinheiro bravo"]] }
},
{
"layerName": "Unidades de Transformação",
"ruleJson": { ">=": [{ "var": "area" }, 35000] }
}
]
}
},
"clarification_question": null,
"user_response": null,
"confidence": 0.95,
"warnings": ["Preservadas condições de uso do solo (eucalipto e pinheiro bravo). Área atualizada para >= 3.5 ha."]
}

## Exemplo 32 — Múltiplos filtros com a mesma referência

**Lista de filtros existentes (fornecida pelo backend):**
- ID: 1, Title: "Eucaliptos com área entre 5 e 20 ha"
- ID: 2, Title: "Eucaliptos e pinheiros bravos"
- ID: 3, Title: "Eucaliptos com custo baixo"

**Input:** "no dos eucaliptos adiciona pinheiros"

**Reasoning:** Existem 3 filtros com "eucaliptos" no título. O agente não pode saber qual deles o utilizador quer modificar.

**Resposta:**

{
"plan": null,
"clarification_question": "Encontrei vários filtros com 'eucaliptos'. Qual pretende atualizar?",
"clarification_type": "multi_choice",
"clarification_topic": "GENERIC_CHOICE",
"clarification_options": [
{ "id": "1", "label": "Eucaliptos com área entre 5 e 20 ha", "value": "1" },
{ "id": "2", "label": "Eucaliptos e pinheiros bravos", "value": "2" },
{ "id": "3", "label": "Eucaliptos com custo baixo", "value": "3" }
],
"user_response": null,
"confidence": 0.50,
"warnings": ["Múltiplos filtros correspondem à referência 'eucaliptos'."]
}
