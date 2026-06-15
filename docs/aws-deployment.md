# ☁️ AWS Deployment — IntelliOps Enterprise Platform

> **Phase 6** — Dockerization, AWS deployment, and production readiness for the IntelliOps AI-Powered Enterprise Operations Co-Pilot.

---

## 📋 Deployment Architecture

```
                         ┌─────────────────────────────┐
                         │    CloudFront (CDN)          │
                         │    Angular S3 Bucket         │
                         └──────────┬──────────────────┘
                                    │
                         ┌──────────▼──────────────────┐
                         │   Application Load Balancer │
                         └──┬─────┬─────┬──────┬──────┘
                            │     │     │      │
                    ┌───────▼┐ ┌──▼───┐ ┌▼─────┐ ┌▼──────┐
                    │ Auth   │ │Order │ │Copilot│ │Billing│
                    │ Service│ │Svc   │ │AI Svc │ │Svc    │
                    │ ECS    │ │ECS   │ │ECS    │ │ECS    │
                    └───┬────┘ └──┬───┘ └──┬────┘ └──┬────┘
                        │         │        │         │
              ┌─────────▼─────────▼────────▼─────────▼──┐
              │           RDS (PostgreSQL)              │
              │   intellops_auth · intellops_order       │
              └─────────┬───────────────────────────────┘
                        │
              ┌─────────▼───────────────────────────────┐
              │        Elasticache (Redis)              │
              └─────────┬───────────────────────────────┘
                        │
              ┌─────────▼───────────────────────────────┐
              │     MSK (Managed Kafka)                 │
              └─────────────────────────────────────────┘
```

---

## 🐳 Dockerization

### Multi-Stage Docker Builds

Each microservice uses a multi-stage Docker build for minimal image size:

```dockerfile
# Stage 1: Build with Maven
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime with JRE only
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose (Local Dev)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports: ["5432:5432"]

  auth-service:
    build: ./backend/auth-service
    ports: ["8080:8080"]
    depends_on: [postgres]

  order-service:
    build: ./backend/order-service
    ports: ["8081:8081"]
    depends_on: [postgres, kafka]

  # ... additional services
```

---

## ☁️ AWS Service Configuration

### 1. ECS (Elastic Container Service) — Fargate

```hcl
resource "aws_ecs_service" "auth_service" {
  name            = "auth-service"
  cluster         = aws_ecs_cluster.intellops.id
  task_definition = aws_ecs_task_definition.auth.arn
  launch_type     = "FARGATE"
  desired_count   = 2

  network_configuration {
    subnets         = var.private_subnets
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.auth.arn
    container_name   = "auth-service"
    container_port   = 8080
  }
}
```

### 2. RDS (PostgreSQL)

```hcl
resource "aws_db_instance" "postgres" {
  identifier     = "intellops-postgres"
  engine         = "postgres"
  engine_version = "16.3"
  instance_class = "db.t3.medium"
  allocated_storage = 100

  db_name  = "intellops_order"
  username = "postgres"
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period = 30
  enabled_cloudwatch_logs_exports = ["postgresql"]
  deletion_protection = true
}
```

### 3. MSK (Managed Streaming for Kafka)

```hcl
resource "aws_msk_cluster" "kafka" {
  cluster_name           = "intellops-kafka"
  kafka_version          = "3.7.0"
  number_of_broker_nodes = 3

  broker_node_group_info {
    instance_type   = "kafka.m5.large"
    client_subnets  = var.private_subnets
    security_groups = [aws_security_group.kafka.id]
  }
}
```

### 4. ElastiCache (Redis)

```hcl
resource "aws_elasticache_cluster" "redis" {
  cluster_id      = "intellops-cache"
  engine          = "redis"
  node_type       = "cache.t3.micro"
  num_cache_nodes = 1
  parameter_group_name = "default.redis7"
  subnet_group_name    = aws_elasticache_subnet_group.main.name
}
```

---

## 🔐 Environment Configuration

### AWS Secrets Manager

```bash
# Store secrets
aws secretsmanager create-secret \
  --name /intellops/production/jwt-secret \
  --secret-string "your-jwt-secret-key-here"

aws secretsmanager create-secret \
  --name /intellops/production/db-password \
  --secret-string "your-db-password"
```

### ECS Task Definition Environment Variables

```json
[
  { "name": "SPRING_DATASOURCE_URL",   "value": "jdbc:postgresql://intellops-postgres.xxx.us-east-1.rds.amazonaws.com:5432/intellops_order" },
  { "name": "SPRING_DATASOURCE_USERNAME", "value": "postgres" },
  { "name": "SPRING_DATASOURCE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:us-east-1:xxx:secret:/intellops/production/db-password" },
  { "name": "SPRING_PROFILES_ACTIVE",     "value": "aws" },
  { "name": "JWT_SECRET",                 "valueFrom": "arn:aws:secretsmanager:us-east-1:xxx:secret:/intellops/production/jwt-secret" },
  { "name": "SPRING_KAFKA_BOOTSTRAP_SERVERS", "value": "b-1.intellops.xxx.kafka.us-east-1.amazonaws.com:9092" }
]
```

---

## 🚀 CI/CD Pipeline (GitHub Actions)

```yaml
name: Deploy to AWS ECS

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag, and push image to Amazon ECR
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: intellops-auth
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG -f Dockerfile.auth .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

      - name: Deploy to ECS
        run: |
          aws ecs update-service --cluster intellops --service auth-service \
            --force-new-deployment
```

---

## 📊 Monitoring & Observability

### CloudWatch Dashboard

```hcl
resource "aws_cloudwatch_dashboard" "intellops" {
  dashboard_name = "IntelliOps-Production"

  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric",
        properties = {
          metrics = [
            ["AWS/ECS", "CPUUtilization", { stat = "Average" }],
            ["AWS/ECS", "MemoryUtilization", { stat = "Average" }]
          ],
          period = 300,
          stat   = "Average",
          region = "us-east-1",
          title  = "ECS Service Health"
        }
      }
    ]
  })
}
```

### Alarms

| Alarm | Metric | Threshold | Action |
|-------|--------|-----------|--------|
| High CPU | CPUUtilization | > 80% for 5 min | SNS → Email |
| High Memory | MemoryUtilization | > 85% for 5 min | SNS → Email |
| HTTP 5xx | HTTPCode_Target_5XX | > 10 for 5 min | SNS → PagerDuty |
| DB Connections | DatabaseConnections | > 80% max | SNS → Email |

---

## 🔒 Security Best Practices

1. **Network Security**
   - All services in private subnets
   - Only ALB in public subnets
   - Security groups with least-privilege access
   - VPC flow logs enabled

2. **Data Protection**
   - RDS encryption at rest (AES-256)
   - EBS encryption for ECS tasks
   - TLS 1.3 for all in-transit traffic
   - Secrets stored in AWS Secrets Manager

3. **IAM Roles**
   - ECS task roles with minimal permissions
   - Separate roles per service
   - Regular access key rotation

4. **Compliance**
   - CloudTrail enabled for audit logging
   - Backup retention: 30 days
   - Multi-AZ deployment for HA
   - Regular security patching

---

## 📈 Production Readiness Checklist

- [x] Multi-stage Docker builds (minimal image size)
- [x] Health check endpoints (/api/actuator/health)
- [x] Graceful shutdown configuration
- [x] Connection pooling (HikariCP)
- [ ] AWS ECS Fargate deployment
- [ ] RDS PostgreSQL with Multi-AZ
- [ ] MSK Kafka cluster
- [ ] ElastiCache Redis for caching
- [ ] CloudFront CDN for static assets
- [ ] WAF for API protection
- [ ] Auto-scaling policies
- [ ] Blue/Green deployment strategy
- [ ] Database migration automation
- [ ] Log aggregation (CloudWatch)
- [ ] Distributed tracing (X-Ray)
- [ ] Synthetic monitoring (CloudWatch Synthetics)

---

## 📚 References

- [AWS ECS Fargate Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-aws/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Docker Multi-Stage Builds](https://docs.docker.com/build/building/multi-stage/)
