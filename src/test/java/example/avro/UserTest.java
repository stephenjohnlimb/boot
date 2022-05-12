package example.avro;

import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case for the AVRO defined 'User'.
 */
public class UserTest {

    @Test
    public void testAvroUser()
    {
        var user = new User();
        assertNotNull(user);
    }
}
