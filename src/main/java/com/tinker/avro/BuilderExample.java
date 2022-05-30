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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class is just a quick demo of the SchemaBuilder approach to using AVRO.
 * <p>
 * This not only provides functions for building but also serialising and deserializing.
 * This is in memory type serialisation, rather than message passing serialisation.
 */
public final class BuilderExample {

  private static final Supplier<Schema> schema = () -> SchemaBuilder.record("User")
          .namespace("example.avro")
          .fields()
          .requiredString("name")
          .name("favorite_number").type().nullable().intType().noDefault()
          .name("favorite_color").type().nullable().stringType().noDefault()
          .endRecord();

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

  public static final Function<GenericRecord, byte[]> toBytes = record -> {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      return recordWriter.apply(record, out).toByteArray();
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
  };

  public static final Function<byte[], GenericRecord> fromBytes = in -> recordReader.apply(new ByteArrayInputStream(in), schema.get());


  /**
   * Use the SchemaBuilder to create a 'User' as defined in the user.avsc.
   * But this is just an example of doing it via code.
   */
  public static final Supplier<String> userSchema = () -> Stream.of(schema)
          .map(Supplier::get)
          .map(v -> v.toString(true))
          .map(s -> s.replace("\r", ""))
          .findFirst()
          .orElseThrow();

  public static final Function<String, GenericRecord> fromName = name -> Stream.of(genericRecord)
            .map(Supplier::get)
            .peek(rec -> rec.put("name", name))
            .findFirst()
            .orElseThrow();

  public GenericRecord populateUser(String name) {
    var rtn = genericRecord.get();
    rtn.put("name", name);
    return rtn;
  }

  public GenericRecord populateUser(String name, int favouriteNumber) {
    var rtn = populateUser(name);
    rtn.put("favorite_number", favouriteNumber);
    return rtn;
  }

  public GenericRecord populateUser(String name, int favouriteNumber, String favouriteColor) {
    var rtn = populateUser(name, favouriteNumber);
    rtn.put("favorite_color", favouriteColor);
    return rtn;
  }
}
