# LAND IT layers and attributes

Use these exact strings in filter definitions. Field names for JsonLogic: `filter_schema_and_operators.md`.

## Layer identifiers

| Layer (PT) | `layerName` | Scope |
|------------|-------------|-------|
| Planta de Ocupação do Solo Proposta | `POSP` | per scenario version |
| Planta de Ocupação do Solo Atual | `ScenarioPOSA` | per scenario version |
| Unidades de Transformação | `Unidades de Transformação` | per scenario version |
| Camada de edição do utilizador | `UserLayer` | per user per AIGP |

## POSP attributes

| Field | Type | Description |
|-------|------|-------------|
| `POSP` | string (COS Nome) | Proposed land use |
| `POSA` | string (COS Nome) | Current land use |
| `area` | number | Area in m² |
| `entity_name` | string | Entity name |
| `execution_variant` | string | Execution variant |

## Unidades de Transformação attributes

| Field | Type | Description |
|-------|------|-------------|
| `area` | number | Area in m² |
| `cost` | number | Estimated cost (euros) |
| `slope` | categorical | `"< 25%"` or `">= 25%"` only |
| `POSP_ID` | number | Associated POSP polygon ID |
| `POSA` | string (COS Nome) | Current land use |
| `requires_transformation` | boolean | Transformation required |
| `op2_code`, `op5_code`, `op4_code` | string | Operation codes |
| `op3_cost` | number | Operation 3 cost |
| `entity_id` | string | Entity identifier |
| `transformation_id` | number | Transformation unit ID |

`slope` is **exclusive** to this layer. `area` exists on all layers.

## Serviços de Ecossistemas attributes

| Field | Type | Description |
|-------|------|-------------|
| `area` | number | Area in m² |
| `ren` | boolean | In Reserva Ecológica Nacional |
| `ecoSerId` | string | Ecosystem service ID |
| `ecoDesc` | string | Description |
| `ecoSerDetail` | string | Details |
| `landscape_structure` | string | Landscape structure |

## Land-use field selection

| User refers to | Layer | Field |
|----------------|-------|-------|
| proposta, POSP | `POSP` | `POSP` |
| atual, POSA | `POSP` | `POSA` |

Use COS **display names** in filters, never identifiers. Valid: `"Florestas de eucalipto"`. Invalid: `"5.1.1.6"`.
