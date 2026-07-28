# Catálogo COS das propostas de uso do solo

Valores válidos para `POSP` e `POSA`. O backend guarda o **Identificador**, mas **os filtros usam os Nomes** (strings). Ex: `"Florestas de eucalipto"`, não `"5.1.1.6"`.

## Estrutura

- **Identificador**: valor interno — não usar em filtros.
- **Nome**: valor a usar em filtros (`==`, `in`).
- **COS nível 1**: pai imediato na hierarquia.

Para «todos os pomares» usar `in` com a lista de Nomes do grupo.

## Catálogo completo (106 entradas)

| Identificador | Nome | COS nível 1 |
|---------------|------|-------------|
| 1.1.1.1 | Áreas edificadas residenciais contínuas predominantemente verticais | 1.1.1.1 |
| 1.1.1.2 | Áreas edificadas residenciais contínuas predominantemente horizontais | 1.1.1.2 |
| 1.1.2.1 | Áreas edificadas residenciais descontínuas | 1.1.2.1 |
| 1.1.2.2 | Áreas edificadas residenciais descontínuas esparsas | 1.1.2.2 |
| 1.2.1.1 | Indústria e logística | 1.2.1.1 |
| 1.2.1.2 | Comércio e serviços | 1.2.1.2 |
| 1.2.2.1 | Instalações agrícolas e pecuárias | 1.2.2.1 |
| 1.2.2.1.1 | Instalações agrícolas e pecuárias: Apiário | 1.2.2.1 |
| 1.3.1.1 | Equipamentos culturais | 1.3.1.1 |
| 1.3.2.1 | Equipamentos desportivos | 1.3.2.1 |
| 1.3.2.2 | Equipamentos de lazer | 1.3.2.2 |
| 1.3.2.3 | Campos de golfe | 1.3.2.3 |
| 1.3.2.4 | Parques de campismo e de caravanismo | 1.3.2.4 |
| 1.3.3.1 | Cemitérios | 1.3.3.1 |
| 1.3.4.1 | Outros equipamentos e instalações turísticas | 1.3.4.1 |
| 1.4.1.1 | Infraestruturas de produção de energia hídrica | 1.4.1.1 |
| 1.4.1.2 | Infraestruturas de produção de energia solar | 1.4.1.2 |
| 1.4.1.3 | Outras Infraestruturas de produção de energia renovável | 1.4.1.3 |
| 1.4.2.1 | Infraestruturas de produção de energia de fonte fóssil | 1.4.2.1 |
| 1.4.3.1 | Subestações e postos de transformação de energia | 1.4.3.1 |
| 1.4.4.1 | Infraestruturas de captação e tratamento de águas para consumo | 1.4.4.1 |
| 1.4.4.2 | Infraestruturas de drenagem e tratamento de águas residuais | 1.4.4.2 |
| 1.4.5.1 | Aterros | 1.4.5.1 |
| 1.4.5.2 | Outras infraestruturas de resíduos | 1.4.5.2 |
| 1.4.6.1 | Outras infraestruturas | 1.4.6.1 |
| 1.5.1.1 | Rede rodoviária | 1.5.1.1 |
| 1.5.1.1.1 | Rede rodoviária: Construção | 1.5.1.1 |
| 1.5.1.1.2 | Rede rodoviária: Manutenção | 1.5.1.1 |
| 1.5.1.2 | Rede ferroviária | 1.5.1.2 |
| 1.5.2.1 | Terminais portuários de mar e de rio | 1.5.2.1 |
| 1.5.2.2 | Estaleiros navais e docas secas | 1.5.2.2 |
| 1.5.2.3 | Marinas e docas pesca | 1.5.2.3 |
| 1.5.3.1 | Aeroportos | 1.5.3.1 |
| 1.5.3.2 | Aeródromos | 1.5.3.2 |
| 1.5.4.1 | Áreas de estacionamento | 1.5.4.1 |
| 1.6.1.1 | Minas a céu aberto | 1.6.1.1 |
| 1.6.1.2 | Pedreiras | 1.6.1.2 |
| 1.7.1.1 | Vazios sem construção | 1.7.1.1 |
| 1.7.1.2 | Áreas em construção | 1.7.1.2 |
| 1.8.1.1 | Espaços verdes | 1.8.1.1 |
| 2.1.1.1 | Culturas temporárias de sequeiro e regadio | 2.1.1.1 |
| 2.1.1.2 | Arrozais | 2.1.1.2 |
| 2.2.1.1 | Vinhas | 2.2.1.1 |
| 2.2.2.1 | Pomares | 2.2.2.1 |
| 2.2.2.1.1 | Pomares: Citrinos | 2.2.2.1 |
| 2.2.2.1.2 | Pomares: Marmeleiros | 2.2.2.1 |
| 2.2.2.1.3 | Pomares: Nogueira | 2.2.2.1 |
| 2.2.2.1.4 | Pomares: Amendoal | 2.2.2.1 |
| 2.2.2.1.5 | Pomares: Medronhal | 2.2.2.1 |
| 2.2.2.1.6 | Pomares: Pinheiro Manso | 2.2.2.1 |
| 2.2.3.1 | Olivais | 2.2.3.1 |
| 2.3.1.1 | Culturas temporárias e/ou pastagens melhoradas associadas a vinha | 2.3.1.1 |
| 2.3.1.2 | Culturas temporárias e/ou pastagens melhoradas associadas a pomar | 2.3.1.2 |
| 2.3.1.3 | Culturas temporárias e/ou pastagens melhoradas associadas a olival | 2.3.1.3 |
| 2.3.2.1 | Mosaicos culturais e parcelares complexos | 2.3.2.1 |
| 2.3.3.1 | Agricultura com espaços naturais e seminaturais | 2.3.3.1 |
| 2.4.1.1 | Agricultura e viveiros protegidos | 2.4.1.1 |
| 3.1.1.1 | Pastagens melhoradas | 3.1.1.1 |
| 3.1.2.1 | Pastagens espontâneas | 3.1.2.1 |
| 4.1.1.1 | Superfícies agrossilvícolas de sobreiro | 4.1.1.1 |
| 4.1.1.2 | Superfícies agrossilvícolas de azinheira | 4.1.1.2 |
| 4.1.1.3 | Superfícies agrossilvícolas de outros carvalhos | 4.1.1.3 |
| 4.1.1.4 | Superfícies agrossilvícolas de outras folhosas | 4.1.1.4 |
| 4.1.2.1 | Superfícies agrossilvícolas de pinheiro manso | 4.1.2.1 |
| 4.1.2.2 | Superfícies agrossilvícolas de outras resinosas | 4.1.2.2 |
| 4.2.1.1 | Superfícies silvopastoris de sobreiro | 4.2.1.1 |
| 4.2.1.1.1 | Superfícies silvopastoris de sobreiro: Alguma Regeneração de Sb, Md, Pb | 4.2.1.1 |
| 4.2.1.2 | Superfícies silvopastoris de azinheira | 4.2.1.2 |
| 4.2.1.3 | Superfícies silvopastoris de outros carvalhos | 4.2.1.3 |
| 4.2.1.4 | Superfícies silvopastoris de outras folhosas | 4.2.1.4 |
| 4.2.2.1 | Superfícies silvopastoris de pinheiro manso | 4.2.2.1 |
| 4.2.2.2 | Superfícies silvopastoris de outras resinosas | 4.2.2.2 |
| 5.1.1.1 | Florestas de sobreiro | 5.1.1.1 |
| 5.1.1.2 | Florestas de azinheira | 5.1.1.2 |
| 5.1.1.3 | Florestas de outros carvalhos | 5.1.1.3 |
| 5.1.1.4 | Florestas de castanheiro | 5.1.1.4 |
| 5.1.1.5 | Florestas de alfarrobeira | 5.1.1.5 |
| 5.1.1.6 | Florestas de eucalipto | 5.1.1.6 |
| 5.1.1.7 | Florestas de acácias | 5.1.1.7 |
| 5.1.1.7.1 | Florestas de acácias: Galeria Ripícola | 5.1.1.7 |
| 5.1.1.8 | Florestas de outras folhosas | 5.1.1.8 |
| 5.1.1.8.1 | Florestas de outras folhosas: Galeria Ripícola | 5.1.1.8 |
| 5.1.1.8.2 | Florestas de outras folhosas: Medronhal | 5.1.1.8 |
| 5.1.2.1 | Florestas de pinheiro bravo | 5.1.2.1 |
| 5.1.2.2 | Florestas de pinheiro manso | 5.1.2.2 |
| 5.1.2.3 | Florestas de outras resinosas | 5.1.2.3 |
| 6.1.1.1 | Matos | 6.1.1.1 |
| 7.1.1.1 | Praias, dunas e areais interiores | 7.1.1.1 |
| 7.1.1.2 | Praias, dunas e areais costeiros | 7.1.1.2 |
| 7.1.2.1 | Espaços rochosos | 7.1.2.1 |
| 7.1.3.1 | Vegetação esparsa | 7.1.3.1 |
| 8.1.1.1 | Pauis e turfeiras | 8.1.1.1 |
| 8.1.2.1 | Sapais | 8.1.2.1 |
| 8.1.2.2 | Zonas entremarés | 8.1.2.2 |
| 9.1.1.1 | Cursos de água naturais | 9.1.1.1 |
| 9.1.1.2 | Cursos de água modificados ou artificializados | 9.1.1.2 |
| 9.1.2.1 | Lagos e lagoas interiores artificiais | 9.1.2.1 |
| 9.1.2.2 | Lagos e lagoas interiores naturais | 9.1.2.2 |
| 9.1.2.3 | Albufeiras de barragens | 9.1.2.3 |
| 9.1.2.4 | Albufeiras de represas ou de açudes | 9.1.2.4 |
| 9.1.2.5 | Charcas | 9.1.2.5 |
| 9.2.1.1 | Aquicultura | 9.2.1.1 |
| 9.3.1.1 | Salinas | 9.3.1.1 |
| 9.3.2.1 | Lagoas costeiras | 9.3.2.1 |
| 9.3.3.1 | Desembocaduras fluviais | 9.3.3.1 |
| 9.3.4.1 | Oceano | 9.3.4.1 |

## Grupos úteis para operador `in`

**Pomares:** `"Pomares"`, `"Pomares: Citrinos"`, `"Pomares: Marmeleiros"`, `"Pomares: Nogueira"`, `"Pomares: Amendoal"`, `"Pomares: Medronhal"`, `"Pomares: Pinheiro Manso"`

**Florestas:** `"Florestas de sobreiro"`, `"Florestas de azinheira"`, `"Florestas de outros carvalhos"`, `"Florestas de castanheiro"`, `"Florestas de alfarrobeira"`, `"Florestas de eucalipto"`, `"Florestas de acácias"`, `"Florestas de acácias: Galeria Ripícola"`, `"Florestas de outras folhosas"`, `"Florestas de outras folhosas: Galeria Ripícola"`, `"Florestas de outras folhosas: Medronhal"`, `"Florestas de pinheiro bravo"`, `"Florestas de pinheiro manso"`, `"Florestas de outras resinosas"`

**Rede rodoviária:** `"Rede rodoviária"`, `"Rede rodoviária: Construção"`, `"Rede rodoviária: Manutenção"`

**Florestas de outras folhosas:** `"Florestas de outras folhosas"`, `"Florestas de outras folhosas: Galeria Ripícola"`, `"Florestas de outras folhosas: Medronhal"`

**Silvopastoris de sobreiro:** `"Superfícies silvopastoris de sobreiro"`, `"Superfícies silvopastoris de sobreiro: Alguma Regeneração de Sb, Md, Pb"`

**Instalações agrícolas:** `"Instalações agrícolas e pecuárias"`, `"Instalações agrícolas e pecuárias: Apiário"`
