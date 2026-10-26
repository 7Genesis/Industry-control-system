# Industry Control System

API REST em Java para controle de produtos e matérias-primas de uma indústria. A partir do estoque disponível, ela sugere quanto produzir de cada produto, priorizando os de maior valor.

Projeto desenvolvido como desafio técnico.

## Problema

Uma fábrica tem estoque limitado de matérias-primas, e cada produto consome quantidades diferentes delas. O estoque quase nunca permite produzir tudo. A pergunta é: o que produzir, e quanto, para gerar o maior valor possível com o que existe hoje?

## O que o sistema faz

Tudo abaixo está implementado no módulo `backend`:

- CRUD de produtos (`/products`): nome e preço.
- CRUD de matérias-primas (`/raw-materials`): nome e quantidade em estoque.
- Composição do produto: associar uma matéria-prima a um produto com a quantidade usada por unidade, alterar e remover a associação (`/products/{id}/materials`). A quantidade precisa ser maior que zero.
- Sugestão de produção (`GET /production/suggestion`): devolve, por produto, a quantidade possível, o preço unitário e o valor total, mais o valor total geral.

Exemplo de resposta de `/production/suggestion`:

```json
{
  "suggestions": [
    { "productId": 1, "productName": "...", "quantity": 10, "unitPrice": 50.00, "totalValue": 500.00 }
  ],
  "grandTotal": 500.00
}
```

## Algoritmo de sugestão

Implementado em `ProductionService`. É uma estratégia gulosa:

1. Ordena os produtos por preço, do maior para o menor.
2. Copia o estoque de cada matéria-prima para um mapa virtual (o estoque real no banco não é alterado).
3. Para cada produto, calcula o máximo produzível: o menor valor de `estoque / quantidade usada` entre suas matérias-primas.
4. Se o máximo for maior que zero, desconta do estoque virtual o que essa produção consome e registra o item.
5. Soma os valores de cada item para chegar ao total geral.

Produtos sem matéria-prima associada são ignorados.

## Decisões técnicas

- **Estoque virtual em vez de alterar o banco.** A sugestão é uma consulta, não uma ordem de produção. Calcular sobre uma cópia em memória deixa a chamada sem efeito colateral e repetível, e permite que produtos de menor valor usem apenas o que sobrou dos de maior valor.
- **Algoritmo guloso por preço.** É simples, previsível e fácil de explicar: o produto mais valioso tem prioridade sobre o estoque. A contrapartida é que ele não garante o ótimo global (veja "Estado do projeto").
- **`BigDecimal` para preço.** Evita erro de arredondamento de ponto flutuante em valores monetários. A coluna usa `precision = 10, scale = 2`.
- **Divisão inteira para a quantidade.** Estoque e quantidade usada são inteiros, então só se produzem unidades inteiras.
- **Lógica de cálculo em um service próprio (`ProductionService`).** O resource só expõe o endpoint, o que deixa a regra de negócio separada da camada HTTP e mais fácil de testar.
- **DTOs na sugestão, nas matérias-primas e nas associações.** A resposta da sugestão e a entrada de matérias-primas e associações usam DTOs próprios. Produtos ainda recebem e devolvem a entidade diretamente.
- **Quarkus com Hibernate ORM Panache e PostgreSQL.** Panache reduz o código repetitivo de acesso a dados nas entidades; o PostgreSQL é o banco relacional configurado.
- **`@JsonIgnore` nas referências inversas** (`ProductMaterial.product` e `RawMaterial.products`) para evitar recursão infinita na serialização JSON.

## Como compilar e rodar

Requisitos: Java 17 e um PostgreSQL acessível.

1. Crie o banco `industry_db` e ajuste usuário e senha em `backend/src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=SEU_USUARIO
quarkus.datasource.password=SUA_SENHA
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/industry_db
```

2. Rode em modo de desenvolvimento:

```bash
cd backend
./mvnw quarkus:dev
```

A API sobe em `http://localhost:8080`. O esquema é criado e atualizado automaticamente (`quarkus.hibernate-orm.database.generation=update`). Em modo dev, a Dev UI fica em `http://localhost:8080/q/dev/` e a documentação OpenAPI vem da extensão `quarkus-smallrye-openapi`.

Para apenas compilar: `./mvnw compile`. Para empacotar: `./mvnw package`.

## Testes

Existe só o teste gerado pelo template do Quarkus (`GreetingResourceTest` e `GreetingResourceIT`), que chama `GET /hello`. Esse endpoint não existe no projeto, então o teste falha com `404`. Não há teste do algoritmo de sugestão nem dos endpoints de negócio.

```bash
cd backend
./mvnw test
```

## Estado do projeto

- **Compila** com Java 17 e Quarkus 3.31.4.
- **Frontend:** a pasta `frontend` aparece no repositório, mas está vazia (referência de submódulo sem conteúdo). Não há interface neste repositório; o que existe aqui é a API.
- **Testes:** como descrito acima, a suíte atual falha e não cobre a regra de negócio. Escrever testes para `ProductionService` é o próximo passo mais útil.
- **Limitação do algoritmo:** a escolha gulosa por maior preço não garante o maior valor total possível. Um produto caro pode consumir uma matéria-prima que renderia mais se fosse dividida entre dois produtos mais baratos. Em empate de preço, a ordem é a de leitura do banco. Resolver de forma ótima seria um problema de programação inteira.
- **Validação:** só a associação de matéria-prima a produto valida entrada. Criar e atualizar produtos e matérias-primas não valida campos obrigatórios nem valores negativos.
- **Configuração:** `application.properties` está com o usuário local de desenvolvimento, CORS liberado para qualquer origem e `import.sql` de exemplo sem dados. Não há Docker Compose nem autenticação. Não é uma configuração para produção.
- **Não implementado:** autenticação, paginação, ordem de produção real que baixe o estoque, histórico.
