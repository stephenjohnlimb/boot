## Some Kafka Development and packaging

I thought I'd have a tinker around with different ways of processing some Kafka messages
now I have [deployed a Kafka cluster in `microk8s`](../../../../BitnamiKafkaHelm.md).

So I had a couple of goes both producing and consuming messages with Kafka.

The first is sort of not very Spring-Boot and just uses the `org.apache.kafka.clients` API.


### Kafka Testing and just using the client API
[KafkaTest](../../../test/java/com/tinker/kafka/KafkaTest.java) just provides an example
of mocking out the Kafka Consumer and Producer.

It also develops the idea of a [Message Source](../../../main/java/com/tinker/kafka/MessageSource.java) and
a [Message Sink](../../../main/java/com/tinker/kafka/MessageSink.java), I also added in a Java record
[Message](../../../main/java/com/tinker/kafka/Message.java) that defines an immutable message that can be used.

The key/value pair used in Kafka is mapped 'in to' and 'out of' the Message.
See [MessageConsumerProducer](../../../main/java/com/tinker/kafka/MessageConsumerProducer.java) for how that is wired
together. This source file has a 'main' method associated with it, so you can just run it up and try it.

Finally, the actual business functionality of all this is just to convert a message value to 'uppercase'.
yes that's it, total overkill - but I'm not interested in the business functionality; I'm interested in the
overall technologies involved and experimenting with how they can be employed.

To try it out and actually process messages, it uses two `topics`.
- test-java-topic - as a consumer
- test-out-topic - as a producer

So if you start two `terminal` sessions and connect to those `topics` you will be able to inject messages in
and see them get process.

#### Consumer terminal
Just run `kafkacat -C -b 192.168.64.90:9094 -t test-out-topic -K :` - this will just print out the key/value
pairs as they are written to the `test-out-topic` by the `MessageConsumerProducer`.

#### Producer terminal
Just run `kafkacat -P -b 192.168.64.90:9094 -t test-java-topic -K :` - this will now wait for you to enter key/value
pairs (delimit by using a ':'). So after the command write:
- first:value1
- second:value2
- CTRL-C

#### The MessageConsumerProducer
Just run this java application from the IDE or the command line.
Then just watch as the messages are consumed from `test-java-topic` and processed. They will come out
in the `Consumer terminal` as:
- first:VALUE1
- second:VALUE2

### Spring-Boot Kafka Annotations
I then thoughts I'd do the same thing, but by using the kafka Annotations and a 
[spring boot application](../../../main/java/com/tinker/kafka/SpringBootKafkaApplication.java) with
a [kafka producer](../../../main/java/com/tinker/kafka/SpringBootKafkaProducer.java) and a
[kafka consumer](../../../main/java/com/tinker/kafka/SpringBootKafkaConsumer.java).

I had to alter the [application.properties](../../../main/resources/application.properties) as below:
```
# This is where I run my kafka cluster (inside microk8s)
spring.kafka.bootstrap-servers=192.168.64.90:9094

# For the consumer
spring.kafka.consumer.group-id=test-consumer-group
spring.kafka.consumer.client-id=test-consumer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# For the producer
spring.kafka.producer.client-id=test-consumer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

But this now caused my maven builds to fail, because Spring-Boot really only expects one Spring-Boot application
to be in a project (not on reasonably).
So I modified by pom.xml and added in a couple of profiles and a switchable property. Really these applications should
be in separate projects.

Modifications to the [pom.xml](../../../../pom.xml) are as follows:
```
<properties>
		<java.version>17</java.version>
		<start-class>com.tinker.boot.BootApplication</start-class>
	</properties>

	<profiles>
		<profile>
			<id>just-boot</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<start-class>com.tinker.boot.BootApplication</start-class>
			</properties>
		</profile>
		<profile>
			<id>kafka-boot</id>
			<activation>
				<activeByDefault>false</activeByDefault>
			</activation>
			<properties>
				<start-class>com.tinker.kafka.SpringBootKafkaApplication</start-class>
			</properties>
		</profile>
	</profiles>

...
    <!-- For trying out with kafka -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.2.0</version>
    </dependency>
...
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <!-- Need to specify the main as I've added a second 'main' application -->
            <mainClass>${start-class}</mainClass>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>repackage</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
```

Now I can do two separate types of build - as I am specifying the class that Spring-Boot should use.
- For `com.tinker.boot.BootApplication` - use `mvn clean install -Pjust-boot`
- For `com.tinker.kafka.SpringBootKafkaApplication` - use `mvn clean install -Pkafka-boot`

You can then do the normal, docker build stuff (which is a pain, I'll look at using 'jib' next).
See [the Dockerfile](Dockerfile) - which is just the same as the other docker file for the spring-boot
application.

```
docker build -f src/main/microk8s/kafka-boot/Dockerfile -t kafka-boot .
docker tag kafka-boot:latest  172.18.235.245:32000/kafka-boot:2.6.1
docker push  172.18.235.245:32000/kafka-boot:2.6.1
```

Then you can use the [helm charts](../../../main/helm/kafka-boot) I created using:
`helm create kafka-boot`. I modified:
- [values.yaml](../../../main/helm/kafka-boot/values.yaml)
- [Chart.yaml](../../../main/helm/kafka-boot/Chart.yaml)

Updating the image to pull and also the `version` of the chart and the `appVersion` of the application.
(Hence the tagging of kafka-boot with version 2.6.1).

You can now deploy that image with `helm install kafka-boot ./src/main/helm/kafka-boot` or do 'dry runs'.
Then you can try the consumer terminal and producer terminal commands above and you should get the same outcome.

## Summary
There are a number of ways to work with Spring-Boot, Kafka, Kubernetes and Helm. but I can't help but see some
disconnects from a developer point of view.

I suppose it comes down to what 'deliverables' and documentation the development team need to provide to the
staff that will actually do the deployments in live production systems.

It used to be that a Java development team would provide a `war file` and maybe also some configuration.

I think that a development team now, should probably provide:
- A versioned docker image in a central repository
- A versioned helm chart in a central repository (that references the docker image)

My reason for this thought is that, the helm chart provides a nice standard way to document what parameters
are configurable and the set of dependencies in terms of persistence and the like.
But it enables the deployment engineers the capability of altering the values on a per customer/environment basis.

### Versions
I'm still not that clear on how best to manager versions. Because we have:
- Source repository tags and versions via branches
- Maven pom versions
- Docker built tagged versions
- Helm Chart versions

At the moment, with what I have been doing; all of these versions move at different speeds and are not really 
connected.

If there was a bug in a system, you would need to know:
- Helm Chart version - good easy - `helm list`
- Now you will know the `App Version` - from the Chart and hence the version of the docker image
- But how do you know which branch/tag or source version that docker image was built from?

In these examples, I've just arbitrarily tagged the docker image with version `2.6.1` but this should
really be tagged based on the version from our source code (specifically from some type of `project version`).

Normally the `project version` number will be bumped `major.minor.patch` at significant points in time
and the source code repository (say git) will have tags added at that point in time of tagging.

In this way you can, checkout the version of the source code for a specific build. 

So I may have a go with [JIB](https://www.baeldung.com/jib-dockerizing). 



