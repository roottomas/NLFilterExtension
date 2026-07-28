# Filter schema and JsonLogic operators

Filters are persisted via `ScenariosFilterInputDTO` → `FilterExpression` → layers with JsonLogic rules.

Output envelope: see `agent_role_and_output.md`.

## Layer names

| Concept | `layerName` |
|---------|-------------|
| Proposta (POSP) | `POSP` |
| Transformações | `Unidades de Transformação` |
| Serviços de ecossistema | `Serviços de Ecossistemas` |

## Field names (`var`)

| Concept | `var` | Notes |
|---------|-------|-------|
| Uso proposto | `POSP` | COS Nome string |
| Uso atual | `POSA` | COS Nome string |
| Área | `area` | m² |
| Custo | `cost` | euros (Transformação) |
| Declive | `slope` | **categorical** — see below |
| REN | `ren` | boolean (Ecossistemas) |
| Nome entidade | `entity_name` | POSP |
| Variante execução | `execution_variant` | POSP |
| Requer transformação | `requires_transformation` | Transformação |
| ID serviço ecossistema | `ecoSerId` | Ecossistemas |

Full attribute list: `layers_and_attributes.md`.

## Operators

| Operator | Use |
|----------|-----|
| `==`, `!=`, `>`, `>=`, `<`, `<=` | Comparisons |
| `and`, `or` | Logic combinators |
| `in` | Value in list: `{ "in": [{ "var": "POSP" }, ["A", "B"]] }` |

Use `or` for alternative conditions (A **or** B). Use `and` for combined conditions (A **and** B).

## Unit conversion

| User says | Write |
|-----------|-------|
| 5 ha | `50000` (area, m²) |
| 1 km² | `1000000` (area, m²) |

## Slope — special case

`slope` is **categorical**, not numeric. Only two valid values: `"< 25%"` and `">= 25%"`.

Always use `==` with the exact string:

```json
{ "or": [{ "==": [{ "var": "slope" }, ">= 25%"] }] }
```

**Never** use numeric operators or values like `25`, `0.25`, `">= 27%"`.

## Common mistakes

| Wrong | Correct |
|-------|---------|
| `"value": "eucalipto"` | `{ "==": [{ "var": "POSP" }, "Florestas de eucalipto"] }` |
| `area_m2`, `uso_solo_proposto`, `declive`, `custo_estimado` | `area`, `POSP`, `slope`, `cost` |
| COS ID `"5.1.1.6"` | COS Name `"Florestas de eucalipto"` |
| `{ "==": [{ "var": "POSP" }, "in [A, B]"] }` | `{ "in": [{ "var": "POSP" }, ["A", "B"]] }` |
| Slope with `>= 25` or `0.25` | Slope with `">= 25%"` string |
