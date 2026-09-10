# Domain semantics: natural language → LAND IT data

All land-use filter values are **COS Nomes** (strings). Full catalog: `cos_land_use_catalog.md`. Clarification rules: `clarification_types.md`.

### Land use → `POSP` / `POSA`

**Regra:** Todos os usos do solo (ex: "olival", "eucalipto", "florestas") referem-se à **proposta (`POSP`)**, a menos que o utilizador especifique "atual" ou "uso atual" (`POSA`).

Quando a query contém um uso do solo, o agente DEVE criar uma camada `POSP` separada, mesmo que a query também contenha atributos da Transformação (custo, declive, área). As condições de uso do solo vão na POSP; os atributos de Transformação vão na camada `Unidades de Transformação`.

### Modificadores "novo" e "replantação"

Estes modificadores mudam o **campo** a comparar dentro da mesma camada `POSP` — não implicam conflito de camadas.

- **"novo" / "nova" / "novas"** (ex: "novas florestas de eucalipto", "nova área de olival")
  → o uso proposto existe, mas **não existe atualmente**.
  → Filtrar: `POSP == "<COS Nome>"` **AND** `POSA != "<COS Nome>"`.
  → Exemplo JsonLogic:
  ```json
  {
    "and": [
      { "==": [ { "var": "POSP" }, "Florestas de eucalipto" ] },
      { "!=": [ { "var": "POSA" }, "Florestas de eucalipto" ] }
    ]
  }

- **"replantação" (qualquer tipo de planta ou árvore)**
  → o uso existe simultaneamente na proposta e no atual.
  → Filtrar: POSP == "<COS Nome>" AND POSA == "<COS Nome>".
  → Exemplo JsonLogic:

```json
{
"and": [
{ "==": [ { "var": "POSP" }, "Florestas de eucalipto" ] },
{ "==": [ { "var": "POSA" }, "Florestas de eucalipto" ] }
]
}
```

Regra: Estes modificadores aplicam-se sem clarificação se a espécie/uso for identificável (ex: "novas florestas de eucalipto"). Se o uso em si for ambíguo ("novas florestas"), clarificar apenas o uso (GENERIC_CHOICE), mantendo o modificador na memória do plano.

### Matching rules

1. **Exact name or synonym** → single COS name with `==`.
2. **Multiple types** ("olival ou sobreiro") → `in` with name list.
3. **Parent category** ("pomares", "florestas") → expand group from `cos_land_use_catalog.md` with `in`, or clarify if ambiguous.
4. **Specific subclass** ("pomares de citrinos") → child name (`"Pomares: Citrinos"`).
5. **Change detection** ("eucalipto mantido") → still `"Florestas de eucalipto"` on POSP; transformation layer only for cost/slope explicitly.

### Layer conflict

If query mixes land use (POSP) with Transformação attributes (`slope`, `cost`), clarify with `LAYER_CONFLICT`. See `clarification_types.md`.

### Frequent synonyms

| User says | COS Name | Notes |
|-----------|----------|-------|
| pinheiro / pinheiro bravo / pinhal | `"Florestas de pinheiro bravo"` | |
| pinheiro manso | `"Florestas de pinheiro manso"` | |
| eucalipto / eucaliptal | `"Florestas de eucalipto"` | |
| sobreiro / montado | `"Florestas de sobreiro"` | |
| azinheira / montado de azinho | `"Florestas de azinheira"` | |
| olival / oliveira | `"Olivais"` | |
| culturas temporárias / agrícola | `"Culturas temporárias de sequeiro e regadio"` | |
| matos / incultos | `"Matos"` | |
| pastagem melhorada | `"Pastagens melhoradas"` | |
| pastagem espontânea | `"Pastagens espontâneas"` | |
| floresta / áreas florestais | ambiguous | → clarify or `in` with all Florestas |
| vinha / vinhas | `"Vinhas"` | |
| arroz / arrozais | `"Arrozais"` | |
| castanheiro | `"Florestas de castanheiro"` | |
| curso de água / rio | ambiguous | natural vs artificializado |
| urbano / edificado | ambiguous | clarify urban type |
| energia solar | `"Infraestruturas de produção de energia solar"` | |
| pedreira | `"Pedreiras"` | |
| espaço verde urbano | `"Espaços verdes"` | |

Full mapping table (106 entries) — see `cos_land_use_catalog.md`.

## Slope → `slope`

Categorical binary field on `Unidades de Transformação`. **Only two valid values:**

| User says | Filter value |
|-----------|-------------|
| declive alto / elevado / inclinado | `">= 25%"` |
| declive baixo / suave / plano | `"< 25%"` |
| declive > 25% / >= 25% | `">= 25%"` |
| declive < 25% | `"< 25%"` |

- Explicit qualifier ("declive elevado") → apply directly with warning.
- Bare "declive" without qualifier → clarify with `multi_choice` (`SLOPE`).
- Never use `numeric_threshold` for slope.

## Cost → `cost`

**REGRRA OBRIGATÓRIA:** `cost` é um valor numérico. NUNCA assuma um valor para "custo elevado", "custo baixo", "caro" ou "barato" sem perguntar ao utilizador.

- Se o utilizador fornecer um valor explícito (`"custo > 5000€"`), aplicar diretamente.
- Se o utilizador usar termos vagos (`"custo elevado"`, `"custo baixo"`), **PERGUNTAR** com `numeric_threshold` (`COST`).

A pergunta deve ser:
- "Qual o valor mínimo de custo que considera elevado?" → para `custo elevado`
- "Qual o valor máximo de custo que considera baixo?" → para `custo baixo`

## Area → `area`

**REGRRA OBRIGATÓRIA:** `area` é um valor numérico em hectares. NUNCA assuma um valor para "área grande", "área extensa", "área pequena" sem perguntar ao utilizador.

- Se o utilizador fornecer um valor explícito (`"área > 5 ha"`), aplicar diretamente.
- Se o utilizador usar termos vagos (`"área grande"`, "área pequena"), **PERGUNTAR** com `numeric_threshold` (`AREA`).

A pergunta deve ser:
- "Qual o valor mínimo de área (em hectares) que considera grande?" → para `área grande`
- "Qual o valor máximo de área (em hectares) que considera pequena?" → para `área pequena`
- 
## Proposed vs current comparison

"When current is X and proposed is Y" → single filter on POSP layer with `POSA` and `POSP` conditions combined via `and`.

## Implicit transformation

"Áreas a converter para olival" → POSP with `POSP = "Olivais"`. Use `Unidades de Transformação` only when cost, slope, or transformation unit is explicitly mentioned.

### Quando a query contém `ou` entre categorias COS diferentes

Exemplo: "mostra florestas ou áreas urbanas"

Neste caso, o agente DEVE clarificar **cada categoria separadamente**, a menos que já tenha resolvido uma delas.

Fluxo correto:
1. Se a query contém `ou` entre categorias diferentes, o agente pergunta primeiro:
   "Pretende incluir florestas, áreas urbanas, ou ambas?"
   (Isto é uma `multi_choice` com opções: Florestas, Áreas Urbanas, Ambas)

2. Depois, para cada categoria selecionada:
    - Se o utilizador escolheu florestas (ou ambas) → clarifica `GENERIC_CHOICE` com as espécies florestais.
    - Se o utilizador escolheu áreas urbanas (ou ambas) → clarifica `GENERIC_CHOICE` com os tipos urbanos.

**Regra de ouro:** O agente só pode usar `GENERIC_CHOICE` uma vez por categoria. Se já clarificou florestas, não volta a perguntar.