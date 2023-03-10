package com.curso.kafka.streams.windowed;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class KTableWindowProducer {
	
	public static String BROKER_LIST = "localhost:9092";

	public static List<String> operaciones = Arrays.asList("CONEXION","INSERCION","BORRADO","DESCONEXION");
	public static List<String> usuarios = Arrays.asList("Luis","Ruben","Alberto","Rodrigo");
	
	
	public static void main(String[] args) throws InterruptedException {
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		KafkaProducer<String, String> producer = new KafkaProducer<>(props);
		Random random = new Random();
		for(int i = 0;i < 100000; i++) {
			String key = operaciones.get(random.nextInt(operaciones.size()));
			String value = usuarios.get(random.nextInt(operaciones.size()));
			
			ProducerRecord<String, String> record = new ProducerRecord<>(Ejemplo04KTableWindowed.TOPIC_ORIGEN, key, value);
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(100);
		}
		producer.flush();
		producer.close();
	}

}
