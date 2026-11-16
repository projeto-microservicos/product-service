# product-service

Microsserviço de produtos (Java 25 / Spring Boot 4.1). Dono do schema `products` no PostgreSQL. Serviço interno: só o `gateway-service` fica exposto; o `order-service` usa `GET /products/{id}` para obter o preço (USD) ao criar um pedido.

Enunciado: <https://insper.github.io/platform/2026.2/exercises/product/>

## Estado da implementação local

CRUD implementado em `ProductService`, com nomes/unidades normalizados, preço estritamente positivo e 404 para consulta/exclusão de identificador inexistente. Os testes de contrato estão ativos. Busca por nome, papéis administrativos e cache de produtos não fazem parte desta implementação.

A validação integrada usa o Compose da raiz do agregador. Essa alteração local ainda precisa ser publicada no repositório do serviço antes de atualizar o gitlink.

## Contrato HTTP

| Método e caminho | Resultado |
|---|---|
| `POST /products` | 201 + `Location`; corpo `{"name","price","unit"}` → `{"id","name","price","unit"}`; inválido 400 |
| `GET /products` | 200; lista de produtos |
| `GET /products/{id}` | 200; inexistente 404 (o `order-service` transforma em 400 "Product does not exist") |
| `DELETE /products/{id}` | 204 |
| `GET /products/health-check` | 200 |

`price` é em USD, com no máximo 2 casas decimais (`NUMERIC(19,2)`). Swagger em `/swagger-ui/index.html`. Saúde e métricas na porta privada 9091 (`/actuator/health`, `/actuator/prometheus`).

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
read -rsp 'Senha do RDS: ' DB_PASSWORD; echo
# printf é builtin do shell: a senha não aparece na lista de processos
printf 'DATABASE_USERNAME=store\nDATABASE_PASSWORD=%s\n' "$DB_PASSWORD" |
  kubectl -n store create secret generic product-secrets --from-env-file=/dev/stdin
unset DB_PASSWORD
```

O banco `store` precisa existir; o Flyway cria o schema e a tabela. Pode ser o mesmo RDS do `account-service` e do `order-service`: cada serviço usa o próprio schema.

## Uso de IA

O boilerplate deste repositório (estrutura, configuração, camada HTTP, persistência, testes de contrato desabilitados, Docker/k8s/CI) foi gerado com assistência de IA (Claude) e revisado pela equipe. As regras de negócio marcadas `TODO(product)` são implementadas pela equipe. Registre aqui o que cada integrante fez.

A implementação funcional e a ativação dos testes nesta revisão foram feitas com assistência do Codex em 16/11/2026. As referências a TODO no histórico de geração acima descrevem o boilerplate original.
