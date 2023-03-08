bootstrap-server = broker1:9092, broker2:9092, broker3:9092

start:
	docker compose up -d
	 

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