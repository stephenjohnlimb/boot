package com.tinker.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Simple Kafka app to list out the topics on: 192.168.64.90:9094
 */
public class TopicLister {

    public static void main(String[] args) {
        listTopics();

    }

    public static void listTopics() {
        Map<String, List<PartitionInfo>> topics;

        Properties props = new Properties();
        //Lets try and connect to the kafka cluster running in local microk8s.
        props.put("bootstrap.servers", "192.168.64.90:9094");
        props.put("group.id", "test-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        topics = consumer.listTopics();
        System.out.println("Found " + topics.size() + " topics");
        topics.forEach((t, p) -> System.out.println(t));
        consumer.close();
    }
}
