package com.curso.kafka.producersconsumers.transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.producersconsumers.partitioner.PartitionProducer;

public class TransactionalProducer {

	public static List<String> CITIES = Arrays.asList("madrid", "barcelona", "burgos");
	public static String TOPIC = "topic-transactions";

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PartitionProducer.BROKER_LIST);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		// Configuración de transacción.
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, TransactionalProducer.class.getName()+TOPIC);
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, TransactionalProducer.class.getName()+TOPIC);
		properties.put(ProducerConfig.RETRIES_CONFIG, 3);
		// Permite garantizar el orden
		properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		//properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 600000);

		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
		// Notificación de gestión de transacciónes
		producer.initTransactions();
		
		Thread thread = new Thread(producer::close);
		Runtime.getRuntime().addShutdownHook(thread);
		int i = 1;
		Random random = new Random();
		while (true) {
			
			try {
				producer.beginTransaction();
				System.out.println("Inicio de transacción ...");
				//HashMap<TopicPartition,OffsetAndMetadata> partitionsWithOffset = new HashMap<>();
				for (int j = 0; j < 5; j++) {
					ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC,
							CITIES.get(random.nextInt(CITIES.size())), "String " + i++);
					// Garantía síncrona.
					producer.send(record).get();
					//System.out.println("Metadatos recibidos : " + recordMetadata);
					//partitionsWithOffset.put(new TopicPartition(TOPIC, recordMetadata.partition()), new OffsetAndMetadata(recordMetadata.offset()));
					System.out.println("Record : " + record.toString());
					
					Thread.sleep(1000);
				}
				//System.out.println(partitionsWithOffset);
				//producer.sendOffsetsToTransaction(partitionsWithOffset, "transactionalConsumer"+ TransactionalProducer.TOPIC);
				producer.commitTransaction();
				System.out.println("Transacción confirmada!");
			} catch (ProducerFencedException e) {
				producer.abortTransaction();
			}
		}
	}
}
