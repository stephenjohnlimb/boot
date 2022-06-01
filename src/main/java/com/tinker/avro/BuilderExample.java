package com.tinker.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class is just a quick demo of the SchemaBuilder approach to using AVRO.
 * <p>
 * This not only provides functions for building but also serialising and deserializing.
 * This is in memory type serialisation, rather than message passing serialisation.
 * <p>
 * I've also used some ideas of composition and higher-level functions.
 * See populateUser for more details on this.
 */
public final class BuilderExample {

  /**
   * Create the schema, this is the same as user.avsc - but in code form.
   */
  private static final Supplier<Schema> schema = () -> SchemaBuilder.record("User")
          .namespace("example.avro")
          .fields()
          .requiredString("name")
          .name("favorite_number").type().nullable().intType().noDefault()
          .name("favorite_color").type().nullable().stringType().noDefault()
          .endRecord();

  /**
   * Make a new record using the schema above.
   */
  private static final Supplier<GenericRecord> genericRecord = () -> new GenericData.Record(schema.get());

  public static final Function<ByteArrayOutputStream, BinaryEncoder> encoder = out -> EncoderFactory.get().directBinaryEncoder(out, null);

  public static final Function<ByteArrayInputStream, BinaryDecoder> decoder = in -> DecoderFactory.get().directBinaryDecoder(in, null);

  private static final BiFunction<GenericRecord, ByteArrayOutputStream, ByteArrayOutputStream> recordWriter = (record, out) ->
  {
    try {
      var writer = new GenericDatumWriter<GenericRecord>(record.getSchema());
      writer.write(record, encoder.apply(out));
      return out;
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
  };

  private static final BiFunction<ByteArrayInputStream, Schema, GenericRecord> recordReader = (in, schema) -> {
    try {
      var reader = new GenericDatumReader<GenericRecord>(schema);
      return reader.read(null, decoder.apply(in));
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
  };

  /**
   * Function to convert a generic record to a byte array.
   */
  public static final Function<GenericRecord, byte[]> toBytes = record -> {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      //I could do this below, but it's a bit contrived
      //return Pairs.from(Map.of(record, out)).map(recordWriter).findFirst().orElseThrow().toByteArray();
      return recordWriter.apply(record, out).toByteArray();
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
  };

  /**
   * Function to convert a byte array back into a generic record.
   */
  public static final Function<byte[], GenericRecord> fromBytes = in -> recordReader.apply(new ByteArrayInputStream(in), schema.get());

  /**
   * Use the SchemaBuilder to create a 'User' as defined in the user.avsc.
   * But this is just an example of doing it via code.
   * <p>
   * The output is a String representation of that schema.
   */
  public static final Supplier<String> userSchema = () -> Stream.of(schema)
          .map(Supplier::get)
          .map(v -> v.toString(true))
          .map(s -> s.replace("\r", ""))
          .findFirst()
          .orElseThrow();

  /**
   * Useful for setting fields on the generic record.
   * Makes a deep copy of the object each time so as not to interfere with any other processing.
   * This is a higher level function - i.e. a function that returns a function.
   */
  private static final BiFunction<String, Object, Function<GenericRecord, GenericRecord>> addValue = (key, value) -> in -> {
    var rtn = new GenericData().deepCopy(schema.get(), in);
    rtn.put(key, value);
    return rtn;
  };

  /**
   * Accepts a populator that can enrich a generic record with additional properties.
   */
  public GenericRecord populateUser(final Function<GenericRecord, GenericRecord> populator) {
    return Stream.of(genericRecord)
            .map(Supplier::get)
            .map(populator)
            .findFirst()
            .orElseThrow();
  }

  public GenericRecord populateUser(final String name) {
    return populateUser(addValue.apply("name", name));
  }

  public GenericRecord populateUser(final String name, final int favouriteNumber, final String favouriteColor) {
    Map<String, Object> toUpdate = Map.of(
            "name", name,
            "favorite_number", favouriteNumber,
            "favorite_color", favouriteColor
    );

    //So stream the pairs of names and values that we want to put into the GenericRecord
    //Call the high-level function to take the name, value pair that outputs a Function.
    //That series this then reduced via being composed into a single function.
    //That function is then used above to populate a generic record.
    return populateUser(Pairs.from(toUpdate).map(addValue).reduce(Function.identity(), Function::andThen));
  }
}
