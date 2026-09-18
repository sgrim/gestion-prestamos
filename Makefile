# Gestión de préstamos bancarios — comandos habituales
# Uso: make <objetivo>   (make help para ver la lista)

COMPOSE ?= docker compose
MAVEN_IMAGE ?= maven:3.9-eclipse-temurin-21
NODE_IMAGE ?= node:22-alpine
# Timeout corto + reintentos: una conexión colgada no bloquea el build (por defecto Maven espera 30 min)
MVN_NET_OPTS ?= -Daether.connector.http.requestTimeout=30000 -Daether.connector.http.retryHandler.count=5

# Carga .env si existe (para conocer los puertos en los mensajes)
-include .env
FRONTEND_PORT ?= 4200
BACKEND_PORT ?= 8081

.DEFAULT_GOAL := help
.PHONY: help env up down restart build logs ps clean test test-back test-front db-shell run-back run-front

help: ## Muestra esta ayuda
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

env: ## Crea .env a partir de .env.example (si no existe)
	@test -f .env || (cp .env.example .env && echo ".env creado: revisa los secretos antes de desplegar")

up: ## Construye y levanta todo (db + backend + frontend)
	$(COMPOSE) up -d --build
	@echo ""
	@echo "  Frontend : http://localhost:$(FRONTEND_PORT)"
	@echo "  API      : http://localhost:$(BACKEND_PORT)/actuator/health"
	@echo "  Usuarios : usuario@test.com / admin@test.com  (clave: 123)"

down: ## Detiene los contenedores (conserva los datos)
	$(COMPOSE) down

restart: down up ## Reinicia todo

build: ## Reconstruye las imágenes sin levantar
	$(COMPOSE) build

logs: ## Sigue los logs (make logs s=backend para uno solo)
	$(COMPOSE) logs -f $(s)

ps: ## Estado de los servicios
	$(COMPOSE) ps

clean: ## Detiene todo y BORRA los datos de la base de datos
	$(COMPOSE) down -v --remove-orphans

test: test-back test-front ## Ejecuta todos los tests

test-back: ## Tests del backend (JUnit 5) dentro de un contenedor Maven
	docker run --rm -v "$(CURDIR)/backend":/app -v prestamos-m2:/root/.m2 -w /app $(MAVEN_IMAGE) mvn -B $(MVN_NET_OPTS) test

test-front: ## Tests del frontend dentro de un contenedor Node
	docker run --rm -v "$(CURDIR)/frontend":/app -v prestamos-node-modules:/app/node_modules -w /app $(NODE_IMAGE) sh -c "npm ci && npm test -- --watch=false"

db-shell: ## Abre psql en la base de datos
	$(COMPOSE) exec db sh -c 'psql -U $$POSTGRES_USER -d $$POSTGRES_DB'

run-back: ## Backend en local (requiere Java 21 y la BD levantada: docker compose up -d db)
	cd backend && DB_URL=jdbc:postgresql://localhost:$${DB_PORT:-5433}/$${POSTGRES_DB:-prestamos} \
		DB_USER=$${POSTGRES_USER:-prestamos} DB_PASSWORD=$${POSTGRES_PASSWORD:-prestamos} mvn spring-boot:run

run-front: ## Frontend en modo desarrollo (requiere Node; proxy hacia localhost:8081)
	cd frontend && npm install && npm start
