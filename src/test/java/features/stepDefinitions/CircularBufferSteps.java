package features.stepDefinitions;

import com.tinker.utils.CircularBuffer;
import com.tinker.utils.RandomCharacter;
import io.cucumber.java8.En;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber steps for testing a circular buffer.
 * <p>
 * Move to a much more functional approach in these tests.
 */
public class CircularBufferSteps implements En {

  private final Supplier<Character> randomCharacterGenerator = new RandomCharacter();

  /*
  State for this cucumber test.
   */
  private CircularBuffer<Character> underTest;
  private List<List<Character>> inputs;
  private List<List<Character>> outputs;
  private List<Boolean> incrementalAdditionResults;

  /**
   * Applies each of the Characters in the input to the circular buffer,
   * to produce a finite list out of the buffer.
   * This will be the last N characters that have been applied to the circular buffer.
   * Where N is the size of the circular buffer.
   */
  private final Function<List<Character>, List<Character>> useCircularBuffer = input -> {
    underTest.clear();
    input.forEach(underTest);
    return underTest.list();
  };

  /**
   * Just generates a list of characters that are pseudo random from a finite set.
   */
  private final Function<Integer, List<Character>> listGenerator = size ->
          Stream.generate(randomCharacterGenerator).limit(size).toList();

  /**
   * Makes a list of random lists, so if size is 4, there will be five
   * in the list, but the first will be 0 the second 1, the third 2, etc.
   */
  private final Function<Integer, List<List<Character>>> makeRandomInputs = size -> IntStream
          .rangeClosed(0, size)
          .boxed()
          .map(listGenerator)
          .collect(Collectors.toList());

  /**
   * Just asserts that the lists are not null.
   */
  private final BiConsumer<List<List<Character>>, List<List<Character>>> listValidator = (in, out) -> {
    assertNotNull(in);
    assertNotNull(out);
  };

  /**
   * Asserts that the lists of lists are identical.
   */
  private final BiConsumer<List<List<Character>>, List<List<Character>>> listContentValidator = (in, out) -> {
    assertEquals(in.size(), out.size());
    IntStream.range(0, in.size())
            .boxed()
            .forEach(index -> assertEquals(in.get(index), out.get(index)));
  };
  /**
   * Test case utility function, checking lists match.
   */
  private final BiConsumer<List<List<Character>>, List<List<Character>>> checkInputsAndOutputs = (in, out) -> {
    listValidator.accept(in, out);
    listContentValidator.accept(in, out);
  };

  /**
   * Test case utility function to manually truncate a list, to give the same results as a circular buffer.
   */
  private final BiFunction<Integer, List<List<Character>>, List<List<Character>>> truncateFunction = (toSize, in) -> in.stream()
          .map(items -> items.subList(Math.max(items.size() - toSize, 0), items.size()))
          .toList();

  /**
   * The actual steps used by the cucumber tests.
   */
  public CircularBufferSteps() {

    Given("^a circular buffer of size (\\d+)$", (Integer arg0) -> underTest = new CircularBuffer<>(arg0));

    When("^the input is less than or equal to (\\d+)$",
            (Integer arg0) -> Optional.of(arg0)
                    .map(makeRandomInputs)
                    .ifPresentOrElse(randomValues -> inputs = randomValues, () -> fail("Unable to create random input")));

    Then("^the output is the same as the input$", () -> {
      outputs = inputs.stream().map(useCircularBuffer).toList();
      checkInputsAndOutputs.accept(inputs, outputs);
    });

    Given("^a default sized circular buffer$",
            () -> underTest = new CircularBuffer<>());

    When("^the input is less than or equal to the default size$",
            () -> Optional.of(underTest.getCapacity())
                    .map(makeRandomInputs)
                    .ifPresentOrElse(randomValues -> inputs = randomValues, () -> fail("Unable to create random input")));

    When("^the input is greater than (\\d+)$",
            (Integer arg0) -> Optional.of(arg0 * 2)
                    .map(makeRandomInputs)
                    .ifPresentOrElse(randomValues -> inputs = randomValues, () -> fail("Unable to create random input")));

    Then("^the output only has the last (\\d+) values from the input$", (Integer arg0) -> {
      outputs = inputs.stream().map(useCircularBuffer).toList();
      checkInputsAndOutputs.accept(truncateFunction.apply(arg0, inputs), outputs);
    });

    When("^the input is greater than the default size$",
            () -> Optional.of(underTest.getCapacity() * 3)
                    .map(makeRandomInputs)
                    .ifPresentOrElse(randomValues -> inputs = randomValues, () -> fail("Unable to create random input")));

    Then("^the output only has the last default size values from the input$", () -> {
      outputs = inputs.stream().map(useCircularBuffer).toList();
      checkInputsAndOutputs.accept(truncateFunction.apply(underTest.getCapacity(), inputs), outputs);
    });

    When("^I incrementally add characters$",
            () -> incrementalAdditionResults = Stream.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')
                    .map(value -> underTest.append(value).isFilled())
                    .toList());

    Then("^the buffer only reports filled on and after (\\d+) characters have been added$",
            (Integer arg0) -> assertEquals(List.of(false, false, false, false, true, true, true, true), incrementalAdditionResults));
  }
}
