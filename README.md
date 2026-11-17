# product-service

Microsserviço de produtos (Java 25 / Spring Boot 4.1). Dono do schema `products` no PostgreSQL. Serviço interno: só o `gateway-service` fica exposto; o `order-service` usa `GET /products/{id}` para obter o preço (USD) ao criar um pedido.

Enunciado: <https://insper.github.io/platform/2026.2/exercises/product/>

## Estado

CRUD implementado em [`ProductService`](src/main/java/store/product/ProductService.java) e coberto por [`ProductContractTest`](src/test/java/store/product/ProductContractTest.java):

- preço em USD, **maior que zero** (senão 400), guardado com 2 casas;
- nome e unidade sem espaços nas pontas;
- id inexistente: **404** em `GET` e `DELETE`; o id não diferencia maiúsculas, como o `order-service` aceita;
- opcional do enunciado: busca por parte do nome, sem diferenciar maiúsculas (`GET /products?name=`).

Pendentes, opcionais do enunciado: papéis (admin cria/apaga) e cache Redis.

## Contrato HTTP

| Método e caminho | Resultado |
|---|---|
| `POST /products` | 201 + `Location`; corpo `{"name","price","unit"}` → `{"id","name","price","unit"}`; inválido ou `price <= 0` 400 |
| `GET /products` | 200; lista ordenada por nome |
| `GET /products?name=tom` | 200; só os que contêm `tom` no nome, sem diferenciar maiúsculas |
| `GET /products/{id}` | 200; inexistente 404 (o `order-service` transforma em 400 "Product does not exist") |
| `DELETE /products/{id}` | 204; inexistente 404 |
| `GET /products/health-check` | 200 |

`price` é em USD, com no máximo 2 casas decimais (`NUMERIC(19,2)`). Swagger em `/swagger-ui/index.html`. As probes de startup e liveness do Kubernetes chamam `GET /products/health-check`, como pede o `Overview.md`. A readiness e as métricas ficam na porta privada 9091 (`/actuator/health/readiness`, `/actuator/prometheus`).

## Rodar

Testes (H2 em memória, sem Postgres). Sem Maven/JDK 25 local, use o Docker:

```bash
mvn -B -ntp verify
# ou
docker run --rm -v "$PWD":/w -w /w maven:3.9-eclipse-temurin-25 mvn -B -ntp verify
```

Ambiente local com Postgres próprio:

```bash
python3 scripts/init-env.py          # cria .env com senha aleatória (uma vez)
docker network create store-dev      # uma vez; rede compartilhada entre os serviços
docker compose up --build -d --wait
curl -i localhost:8085/products/health-check
```

Na rede `store-dev`, o `order-service` encontra este serviço em `http://product:8080`.

## CI/CD (GitHub Actions)

[`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml):

- **verify** (todo push/PR): `mvn verify`, build da imagem, validação do Compose.
- **deploy** (push em `main`): build `linux/amd64` → push no ECR `product-service` → `kubectl apply` no EKS pelo **digest** → espera o rollout. Só roda quando a variável de repositório `AWS_ROLE_ARN` existe; a autenticação é por OIDC (sem chave AWS no GitHub).

Variáveis do repositório (Settings → Secrets and variables → Actions → Variables): `AWS_ROLE_ARN`, `AWS_REGION` (`us-east-2`), `EKS_CLUSTER_NAME` (`eks-store`), `K8S_NAMESPACE` (`store`).

### Preparação única no cluster

O pipeline não cria nem sobrescreve configuração ou segredos:

```bash
kubectl -n store apply -f k8s/deployer-rbac.yaml
cp k8s/config.example.yaml k8s/config.local.yaml   # preencha o endpoint do RDS
kubectl -n store apply -f k8s/config.local.yaml
```

No EKS do projeto, o Secret `product-secrets` e o usuário do banco `product_svc` são criados pelo bootstrap do RDS (fora deste repositório). O usuário é dono do schema `products` e não tem `CREATE` no banco, então o schema precisa existir antes; o Flyway cria só a tabela. O RDS (`store`) é o mesmo de `account-service` e `order-service`, e cada serviço tem o próprio usuário e schema. O RDS exige TLS: mantenha `DATABASE_SSLMODE=require`.

## Uso de IA

O boilerplate deste repositório (estrutura, configuração, camada HTTP, persistência, testes de contrato, Docker/k8s/CI) foi gerado com assistência de IA (Claude) e revisado pela equipe. As regras de negócio do `ProductService` (criação com regra de preço, listagem e busca por nome, busca por id e exclusão) também foram escritas pelo Claude, em 17/11/2026, por decisão do João, que revisou e é o responsável pelo serviço. Registre aqui o que cada integrante fez.
