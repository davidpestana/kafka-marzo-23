package com.curso.kafka.producersconsumers.transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import com.curso.kafka.producersconsumers.partitioner.PartitionProducer;
import com.curso.kafka.util.TopicCreator;

public class TransactionalConsumer {

    private static final AtomicBoolean closed = new AtomicBoolean(false);//para cerrar al matar el proceso

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Shutting down");
                closed.set(true);
            }
        });
        TopicCreator.createTopics(PartitionProducer.BROKER_LIST, TransactionalProducer.TOPIC);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PartitionProducer.BROKER_LIST);
        
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "transactionalConsumer"+ TransactionalProducer.TOPIC);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");// default "read_uncommitted"
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TransactionalProducer.TOPIC));

        while (!closed.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("partition = %2d   offset = %5d   key = %7s timestamp = %8s  value = %12s\n",
                        record.partition(), record.offset(), record.key(), String.valueOf(record.timestamp()), record.value());
            HashMap<TopicPartition,OffsetAndMetadata> partitionsWithOffset = new HashMap<>();
            for (TopicPartition partition: records.partitions()) {
            	List<ConsumerRecord<String, String>> list = records.records(partition);
            	long offset = list.get(list.size() -1).offset();
            	partitionsWithOffset.put(partition, new OffsetAndMetadata(offset+1));
            }
            consumer.commitSync(partitionsWithOffset);
        }

        consumer.close();
    }
}
