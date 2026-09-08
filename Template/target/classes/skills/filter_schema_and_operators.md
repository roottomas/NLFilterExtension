# Filter schema and JsonLogic operators

Home único das regras de estrutura do `ruleJson` e operadores. Campos e camadas: `layers_and_attributes.md`. Valores de uso do solo (Nomes COS): `cos_land_use_catalog.md`.

## Estrutura do `filter`

O `plan.filter` tem sempre:

| Campo | Obrigatório | Notas |
|-------|-------------|-------|
| `title` | sim | Título curto do filtro. |
| `description` | sim (só quando há filtro) | Regras em `agent_role_and_output.md` (§Description rules). |
| `activated` | sim | `true` por omissão. |
| `layers` | sim | Array não vazio; cada layer tem `layerName` + `ruleJson`. |

## Estrutura do `ruleJson`

O UI do LAND IT espera que cada `ruleJson` seja envolvido por **um único** operador lógico de topo:

```
{ "or":  [ { <op>: [ { "var": "CAMPO" }, VALOR ] }, ... ] }
{ "and": [ { <op>: [ { "var": "CAMPO" }, VALOR ] }, ... ] }
```

- Usar **`or`** quando o utilizador quer UMA **ou** outra condição (ex: "eucalipto ou sobreiro").
- Usar **`and`** quando o utilizador quer AMBAS as condições (ex: "eucalipto **e** área > 5 ha").

## Operadores suportados

| Operador | Uso |
|----------|-----|
| `==` | Igualdade (um valor). |
| `in` | Pertence a lista de valores. |
| `>`, `>=`, `<`, `<=` | Comparações numéricas (`area`, `cost`). |
| `and`, `or` | Composição lógica. |

### Vários valores → `in`

Usar sempre `in` com **array**, nunca uma string:

```json
{ "in": [{ "var": "POSP" }, ["Olivais", "Florestas de sobreiro"]] }
```

Nunca: `"in [Olivais, Florestas de sobreiro]"` como string.

### Exceção `slope` (campo categórico binário)

`slope` (camada `Unidades de Transformação`) só aceita **dois valores**, sempre via `==`:

```json
{ "==": [{ "var": "slope" }, ">= 25%"] }
{ "==": [{ "var": "slope" }, "< 25%"] }
```

Nunca usar `>` / `<` / `numeric_threshold` para `slope`. Semântica de linguagem natural → valor: `domain_semantics.md`.

## Conversão de unidades — `area`

O campo `area` está em **m²**. Converter hectares antes de filtrar: `1 ha = 10 000 m²` (ex: 5 ha → `50000`).

## Grupos `__GROUP__`

**NUNCA** incluir `__GROUP__` no `ruleJson`. Um valor `__GROUP__:<grupo>` (vindo de uma opção de clarificação) deve ser **expandido** para a lista completa de Nomes COS reais do grupo, retirada de `cos_land_use_catalog.md`, e usado com `in`.

Exemplo: `__GROUP__:Florestas` → `["Florestas de sobreiro", "Florestas de azinheira", "Florestas de eucalipto", ...]` (lista completa do grupo).

## Filtros com várias camadas

Quando a query mistura atributos de camadas diferentes (ex: uso do solo POSP + declive/custo em `Unidades de Transformação`), criar **um único `filter` com várias `layers`**, cada uma com o seu `layerName` e `ruleJson`.

- **NUNCA** combinar condições de camadas diferentes no mesmo `ruleJson`.
- **NUNCA** criar dois filtros separados.

Exemplo completo: `planning_examples.md` (Exemplo 22).
