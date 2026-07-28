# Available functions reference

The executor calls these after receiving the planner's JSON. **Only `saveFilter` is used** — the `steps` array is always empty.

## `saveFilter(scenarioId, versionId, dto)`

**When:** Agent provides a `filter` object; executor builds `ScenariosFilterInputDTO` and calls `saveFilter`.

**Effect:** Creates and saves a new filter; user activates it in the filters tab.

```java
List<FilterExpression.Layer> layers = List.of(new FilterExpression.Layer("POSP", json));
FilterExpression exp = new FilterExpression("Example Filter", layers, true);
ScenariosFilterInputDTO filterInput = new ScenariosFilterInputDTO(List.of(exp));
```
