## Cucumber getting started

### Use with maven
To get started using cucumber you need to add in a couple of dependencies.
```
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java8</artifactId>
    <version>7.3.4</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>7.3.4</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <version>5.9.0-M1</version>
    <scope>test</scope>
</dependency>
```

Now you must add in that junit vintage engine, and you must also add in a
java class that can be triggered from junit as detailed below.

### Junit Class to trigger Cucumber
```
package features;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/test/resources/features"}, glue = {"features.stepDefinitions"})
public class TestCucumberRunner
{
}
```

#### CucumberOptions
So the name of the file must have `Test` in it! and it must be located in a package somewhere in the
`src\test\java` directory (I've used the features package here).

It's aso important that you use the annotation `@RunWith(Cucumber.class)`. This tells junt how to run the test.

But then you also need to tell cucumber where to find files that hold the `features`.
These **feature** files contain the definitions of the cucumber test, i.e. the human-readable test definitions.

I've created one called `Avro.feature` and put it in directory `src/test/resources/features`.

Finally, there's the `glue` (great name , I had no idea what this was).
Basically this just tells Cucumber which **package** the actual Java code that implements the cucumber test is
defined in.

Here is my very basic implementation:
```
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
```

### Summary
I can see that Cucumber provides some value in terms of ensuring there is a human-readable
definition of what you are trying to test (so better than just javadoc).
So this does enforce a behaviour/test driven design.

I can also see that is leads to fairly small and well-defined tests.

In the end it does just come back down to some junit tests, but rather than just be rambling
and varying (depending on the developer) they are more consistent.

I might give it a go of a while and see if I like it.

