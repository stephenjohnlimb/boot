package com.tinker.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;
import java.util.function.Function;

/**
 * You can run this code and ctrl-C when done.
 * But while running use:
 * kafkacat -P -b 192.168.64.90:9094 -t test-java-topic -K :
 * to put key pairs into topic i.e. type key1:value1\n etc ctrl-c when done
 * Also in another window use:
 * kafkacat -C -b 192.168.64.90:9094 -t test-out-topic -K :
 * And you'll see that same content come out put the values will be upper case.
 */
public class MessageConsumerProducer {

    private static String INCOMING_TOPIC = "test-java-topic";
    private static String OUTGOING_TOPIC = "test-out-topic";

    public static void main(String[] args) {

        var consumer = createConsumer();
        var messageSource = new MessageSource(INCOMING_TOPIC, consumer);

        var businessOperation = new MainBusinessFunction();
        var sink = new MessageSink(OUTGOING_TOPIC, createProducer());
        messageSource.withExceptionsTo(System.err::println).sendMessagesTo(compose(sink, businessOperation));

        System.out.println("Complete");
    }

    private static java.util.function.Consumer<Message> compose(final java.util.function.Consumer<Message> with, final Function<Message, Message> func)
    {
        //So return a consumer that takes a message
        //uses the passed in function to transform the message
        //and then passes that on to the actual final consumer.
        //i.e. compose consumers and functions.
        return message -> with.accept(func.apply(message));
    }

    /** For sending out outgoing messages. */
    private static Producer<String, String> createProducer() {
        final Properties props = new Properties();
        //Lets try and connect to the kafka cluster running in local microk8s.
        props.put("bootstrap.servers", "192.168.64.90:9094");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // Create the consumer using props.
        return new KafkaProducer<String, String>(props);
    }

    /** For accepting incoming messages. */
    private static Consumer<String, String> createConsumer() {
        final Properties props = new Properties();
        //Lets try and connect to the kafka cluster running in local microk8s.
        props.put("bootstrap.servers", "192.168.64.90:9094");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "test-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // Create the consumer using props.
        return new KafkaConsumer<String, String>(props);

    }
}
