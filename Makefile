run:
	./mvnw spring-boot:run

build:
	./mvnw clean install -DskipTests

test:
	./mvnw test

db-up:
	docker-compose up -d

db-down:
	docker-compose down

db-reset:
	docker-compose down -v && docker-compose up -d

clean:
	./mvnw clean