package com.curso.kafka.producersconsumers.simple;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.util.TopicCreator;

public class SimpleProducer {

	public static final String BROKER_LIST = "kafka:9092";
    public static final String TOPIC = "topic-simple";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
    	
    	TopicCreator.createTopics(BROKER_LIST, TOPIC);
    	
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

	    Producer<String, String> producer = new KafkaProducer<>(props);

        for (int id = 0; id < 5000; id++) {
            String key = String.format("key[%d]", id);
            String message = String.format("message[%d]", id);
            System.out.println("Sending message with: " + key);
            producer.send(new ProducerRecord<>(TOPIC, key, message));
            Thread.sleep(1000);
        }

        producer.flush();
        producer.close();
    }
}
