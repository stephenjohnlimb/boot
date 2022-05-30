package example.avro;

import com.tinker.avro.BuilderExample;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static com.tinker.avro.BuilderExample.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case for the AVRO defined 'User'.
 */
public class UserTest {

  private final BuilderExample underTest = new BuilderExample();

  @Test
  public void testAvroUser() {
    var user = new User();
    assertNotNull(user);
  }

  @Test
  public void testUserJustNameSerialisation() {
    var serialised = Optional.of(underTest.populateUser("Steve", 59, "blue"))
            .map(toBytes)
            .orElseThrow();

    //First as a generic record
    var asGeneric = Optional.of(serialised)
            .map(fromBytes)
            .orElseThrow();

    var name = asGeneric.get("name");
    assertNotNull(name);
    assertEquals("Steve", name.toString());

    assertNotNull(asGeneric.get("favorite_number"));
    assertNotNull(asGeneric.get("favorite_color"));

    try {

      //Now with a specific 'User' generated by AVRO
      DatumReader<User> userDatumReader = new SpecificDatumReader<>(User.class);
      var in = new ByteArrayInputStream(serialised);
      var deserialized = userDatumReader.read(new User(), decoder.apply(in));
      assertNotNull(deserialized);
      assertEquals("Steve", deserialized.getName().toString());
      assertEquals(59, deserialized.getFavoriteNumber());
      assertEquals("blue", deserialized.getFavoriteColor().toString());
    } catch (IOException ioex) {
      fail(ioex);
    }
  }

  @Test
  public void testBuilderExample() {
    //Just need to remove the '\r' to check it against what we expect below.
    String avroString = underTest.userSchema.get();
    var expect = """
            {
              "type" : "record",
              "name" : "User",
              "namespace" : "example.avro",
              "fields" : [ {
                "name" : "name",
                "type" : "string"
              }, {
                "name" : "favorite_number",
                "type" : [ "int", "null" ]
              }, {
                "name" : "favorite_color",
                "type" : [ "string", "null" ]
              } ]
            }""";
    assertEquals(expect, avroString);
  }
}
