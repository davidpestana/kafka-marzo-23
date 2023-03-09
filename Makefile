bootstrap-server = broker1:9092, broker2:9092, broker3:9092

start:
	docker compose -f docker-compose.yaml -f producers.yaml -f consumers.yaml up -d

logs:
	docker compose logs -f

cleanup:
	docker compose down --remove-orphans
	docker rm -f $(docker ps -aq)

topic-create:
	@read -p "Enter topic name: " topic; \
	read -p "Enter partitions number: " partitions; \
	read -p "Enter replication factor: " replication; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-topics.sh --bootstrap-server $(bootstrap-server) --create --partitions $$partitions --replication-factor $$replication --topic $$topic"


topic-delete:
	@read -p "Enter topic name: " topic; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-topics.sh --bootstrap-server $(bootstrap-server) --delete --topic $$topic"

topic-describe:
	@read -p "Enter topic name: " topic; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-topics.sh --bootstrap-server $(bootstrap-server) --describe --topic $$topic"

topic-list:
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-topics.sh --bootstrap-server $(bootstrap-server) --list"

producer-create:
	@read -p "Enter topic name: " topic; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-console-producer.sh --bootstrap-server $(bootstrap-server) --topic $$topic"
	
consumer-create:
	@read -p "Enter topic name: " topic; \
	read -p "Enter group name: " group; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-console-consumer.sh --bootstrap-server $(bootstrap-server) --topic $$topic --group $$group"

consumer-create-fb:
	@read -p "Enter topic name: " topic; \
	read -p "Enter group name: " group; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-console-consumer.sh --bootstrap-server $(bootstrap-server) --topic $$topic --group $$group --from-beginning"

groups-list:
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-consumer-groups.sh --bootstrap-server $(bootstrap-server) --list"


groups-describe:
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-consumer-groups.sh --bootstrap-server $(bootstrap-server) --describe --all-groups"

group-describe:
	@read -p "Enter group name: " group; \
	docker compose -f services.yaml run --rm tools bash -c \
		"./bin/kafka-consumer-groups.sh --bootstrap-server $(bootstrap-server) --describe --group $$group"
tools:
	docker compose -f services.yaml run --rm tools

node:
	docker compose -f producers.yaml run --rm producer-1 bash

install:
	docker compose -f producers.yaml run --rm producer-1 bash -c "npm i"
	docker compose -f consumers.yaml run --rm consumer-1 bash -c "npm i"