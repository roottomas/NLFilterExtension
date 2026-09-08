# Available functions reference

O executor (`NLQueryFunction`) chama estas funções do `BackendService` depois de receber o JSON do planeador. O array `steps` do plano está sempre vazio (não usado nesta versão) — o executor age diretamente sobre `plan`.

## Como o `plan` mapeia para as funções

| Situação no plano | Função chamada |
|-------------------|----------------|
| `plan.filter` presente **sem** `filterId` (ou `filterId <= 0`) | `saveFilter` (criação) |
| `plan.filterId > 0` + `plan.filter` | `updateFilter` (atualização) |
| `need_filters: true` | `getUserFilters` (obter lista) e reenviar prompt ao agente |

## `getUserFilters(scenarioId, versionId)`

**Quando:** o agente devolve `need_filters: true`.

**Efeito:** devolve os filtros existentes do cenário/versão. O executor injeta a lista (`EXISTING FILTERS`: ID, Title, Description, Layers) num novo prompt e reenvia ao agente, que identifica o filtro e devolve `plan.filterId`.

## `saveFilter(scenarioId, versionId, dto)`

**Quando:** o agente fornece `plan.filter` sem `filterId`.

**Efeito:** cria e guarda um novo filtro; o utilizador ativa-o no separador de filtros.

```java
List<FilterExpression.Layer> layers = List.of(new FilterExpression.Layer("POSP", ruleJson));
FilterExpression exp = new FilterExpression("Example Filter", layers, true);
ScenariosFilterInputDTO dto = new ScenariosFilterInputDTO(List.of(exp));
backendService.saveFilter(scenarioId, versionId, dto);
```

## `updateFilter(scenarioId, versionId, filterId, dto)`

**Quando:** o agente fornece `plan.filterId > 0` e `plan.filter` (fluxo de update). O executor força `activated: true` no filtro atualizado.

**Efeito:** substitui as condições do filtro identificado por `filterId`. As condições não mencionadas pelo utilizador devem ser preservadas no `plan.filter` (ver `agent_role_and_output.md`).
