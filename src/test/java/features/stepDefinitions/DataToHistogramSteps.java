package features.stepDefinitions;

import com.tinker.utils.DataToHistogramConverter;
import com.tinker.utils.HistogramCollector;
import com.tinker.utils.HistogramData;
import com.tinker.utils.SplitData;
import io.cucumber.java8.En;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The Cucumber step tests for checking that data can be converted to a histogram.
 */
public class DataToHistogramSteps implements En {

  private SplitData<Character> input;

  /**
   * As I'm using Atomic Integers I need to write a little function to get the values out to check them.
   */
  private final BiConsumer<Map<Character, AtomicInteger>, Map<Character, AtomicInteger>> assertMapsEqual = (expectation, result) -> {
    assertEquals(expectation.size(), result.size());
    expectation.forEach((k, v) -> assertEquals(v.get(), result.get(k).get()));
  };

  /**
   * A function to validate an expectation against the resulting histogram data.
   */
  private final BiConsumer<Map<Character, AtomicInteger>, Optional<HistogramData<Character>>> validator = (expectation, histogram) ->
          histogram.ifPresentOrElse(value ->
                          Map.of(expectation, value.content().getResults()).forEach(assertMapsEqual),
                  () -> fail("No results to process")
          );
  /**
   * Actually the main sort of processing, takes split data in and produced a Histogram.
   */
  private final Function<SplitData<Character>, HistogramData<Character>> converter = in ->
          new HistogramData<>(in.index(), in.content().stream().collect(new HistogramCollector<>()));

  public DataToHistogramSteps() {

    Given("^a split data to histogram converter$", () -> {
      //Nothing to do here
    });

    When("^I provide SplitData of (\\d+), \"([^\"]*)\"$",
            (Integer arg0, String arg1) -> input = new SplitData<>(arg0, arg1.chars().mapToObj(value -> (char) value).toList()));

    Then("^the HistogramData will have the value of (\\d+), k->(\\d+), j->(\\d+), a->(\\d+) and b->(\\d+)$", (Integer arg0, Integer arg1, Integer arg2, Integer arg3, Integer arg4) -> {
      var expectation = Map.of(
              'k', new AtomicInteger(arg1),
              'j', new AtomicInteger(arg2),
              'a', new AtomicInteger(arg3),
              'b', new AtomicInteger(arg4)
      );

      //A few different ways of doing the same thing.

      //firstly a bit more line.
      var histInLine = Stream
              .of(input)
              .map(in -> new HistogramData<>(in.index(), in.content().stream().collect(new HistogramCollector<>())))
              .findFirst();

      //Then just delegating to a function
      var histFunctionInLine = Stream
              .of(input)
              .map(converter)
              .findFirst();

      //Then using a method.
      var histogram = Stream.of(input).map(histogramConverter()).findFirst();

      //Now let's validate all three mechanisms give the same result
      List.of(histInLine, histFunctionInLine, histogram)
              .forEach(optionalHistogram -> Map.of(expectation, optionalHistogram).forEach(validator));
    });
  }

  private DataToHistogramConverter<Character> histogramConverter() {
    return new DataToHistogramConverter<>();
  }
}
