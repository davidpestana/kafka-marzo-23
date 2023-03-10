package com.curso.kafka.avro;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.curso.kafka.avro.model.Ciudad;
import com.curso.kafka.avro.model.Clima;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

public class SchemaRegistryConsumerRecordNameStrategy {
	private static final AtomicBoolean closed = new AtomicBoolean(false);
	
	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Apagando");
				closed.set(true);
			}
		});
		// Configs
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,SchemaRegistryProducer.BROKER_LIST);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");// __consumer_offsets
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "SchemaRegistryConsumerTopicStrategy");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		
		KafkaConsumer<Ciudad, Clima> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(SchemaRegistryProducer.TOPIC_BASE));
		while(!closed.get()) {
			ConsumerRecords<Ciudad, Clima> records = consumer.poll(Duration.ofSeconds(1));
			for(ConsumerRecord<Ciudad, Clima> record: records) {
				System.out.printf("particion = %2d offset = %5d key = %7s ts = %8s value %12s\n",
						record.partition(),
						record.offset(),
						record.key(),
						String.valueOf(record.timestamp()),
						record.value()
						);
			}
		}
		consumer.close();
	}
	

}
