package com.tinker.pact.consumer;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "dataProviderKafka", providerType = ProviderType.ASYNCH)
public class ConsumerUserPactTest {

  @Pact(consumer = "dataConsumerKafka")
  public MessagePact validDataMessageFromKafkaProvider(MessagePactBuilder builder) {
    //here we are stating that the consumer will need these fields, even though more fields can and do
    //exist we are not using them. That's the PACT.
    return builder
            .expectsToReceive("a valid data from kafka provider")
            .withContent(LambdaDsl.newJsonBody(lambdaDslJsonBody -> {
              lambdaDslJsonBody.stringType("name");
              lambdaDslJsonBody.numberType("favorite_number");
              //We don't need the favourite color.
            }).build())
            .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "validDataMessageFromKafkaProvider")
  public void testValidDataFromProvider(List<Message> messages) throws JSONException {
    assertThat(messages).isNotEmpty();
    var contents = messages.get(0).contentsAsString();
    var asJSON = new JSONObject(contents);
    assertNotNull(asJSON.get("favorite_number"));
    assertNotNull(asJSON.get("name"));
  }
}