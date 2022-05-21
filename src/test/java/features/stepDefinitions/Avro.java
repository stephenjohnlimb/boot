package features.stepDefinitions;

import example.avro.User;
import io.cucumber.java8.En;
import static org.junit.jupiter.api.Assertions.*;

public class Avro implements En {
    public Avro() {
        Given("^User avro type exists$", () -> assertNotNull(Class.forName("example.avro.User")));
        When("^serialization is required$", () -> {
            var user = new User("Steve", 59, "Blue");
            var asBytes = user.toByteBuffer();
            assertNotNull(asBytes);
        });
        Then("^the user can be serialized and deserialized$", () -> {
            var user1 = new User("Steve", 59, "Blue");
            var asBytes = user1.toByteBuffer();
            var user2 = User.fromByteBuffer(asBytes);
            assertEquals(user1, user2);
        });
    }
}
