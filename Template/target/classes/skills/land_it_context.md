# LAND IT — Contexto para o agente planeador

## O que é o LAND IT

Sistema de apoio à decisão espacial (SADE) web para planear **reordenamento do território** no âmbito das **AIGP** (Áreas Integradas de Gestão da Paisagem). Apoia equipas multidisciplinares a desenhar e comparar **cenários** de ocupação do solo.

## Hierarquia de dados

```
AIGP
├── POSA (ocupação atual, partilhada) — camada de leitura
├── Camadas AIGP (estrutura de resiliência, ecológica, etc.)
└── Cenário
    └── Versão (árvore de versões)
        ├── POSP — proposta editável
        ├── Unidades de Transformação — custo, declive, etc.
        └── Serviços de Ecossistemas — REN, etc.
```

- **Cenário**: proposta de desenho da paisagem.
- **Versão**: iteração dentro do cenário.
- **POSP**: polígonos com uso do solo proposto (campo `POSP`).
- **Transformação**: custo, declive, tipo de unidade.
- **Ecossistema**: flag REN, remuneração, etc.

## Filtros

O utilizador define filtros por camada com condições JsonLogic. O agente gera `plan.filter`; o executor persiste via `saveFilter`.

## Taxonomia COS

Valores de uso do solo seguem a **Carta de Ocupação do Solo (COS)**. Backend guarda Identificadores; filtros usam **Nomes**. Ver `cos_land_use_catalog.md`.

## Glossário

| Termo | Filtro |
|-------|--------|
| Proposta / POSP | `layerName: "POSP"`, campo `POSP` |
| Atual / POSA | `layerName: "POSP"`, campo `POSA` |
| Transformação | `layerName: "Unidades de Transformação"` |
| Ecossistema | `layerName: "Serviços de Ecossistemas"` |
