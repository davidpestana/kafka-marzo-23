FROM ubuntu
RUN apt update && apt install -y nano openjdk-8-jdk wget
RUN wget https://downloads.apache.org/kafka/3.2.3/kafka_2.12-3.2.3.tgz && tar zxvf kafka_2.12-3.2.3.tgz && mv kafka_2.12-3.2.3 kafka
WORKDIR /kafka
CMD ["bash"]