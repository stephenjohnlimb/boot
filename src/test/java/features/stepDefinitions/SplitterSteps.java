package features.stepDefinitions;

import com.tinker.utils.RandomCharacter;
import com.tinker.utils.SplitData;
import com.tinker.utils.Splitter;
import io.cucumber.java8.En;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitterSteps implements En {

  private final Supplier<Character> randomCharacterGenerator = new RandomCharacter();

  private Splitter<Character> underTest;
  private List<Character> inputs;

  private final Supplier<SplitData<Character>> emptyValueSupplier = () -> new SplitData<>(0, List.of());

  public SplitterSteps() {

    Given("^a splitter configured to split at size (\\d+)$",
            (Integer arg0) -> underTest = new Splitter<>(arg0));

    When("^the splitter input is less than (\\d+)$",
            (Integer arg0) -> inputs = Stream.generate(randomCharacterGenerator).limit(arg0 - 1).toList());

    Then("^the output of the splitter is a Stream of empty lists$", () -> {
      var outputs = inputs.stream().map(underTest).toList();
      var expected = Stream.generate(emptyValueSupplier).limit(inputs.size()).toList();
      assertEquals(expected, outputs);
    });

    When("^the splitter input is greater than (\\d+)$",
            (Integer arg0) -> inputs = Stream.generate(randomCharacterGenerator).limit(arg0 * 3L).toList());

    Then("^the output of the splitter first (\\d+) lists are empty and subsequent list contain just the last (\\d+) inputs$", (Integer arg0, Integer arg1) -> {
      var outputs = inputs.stream().map(underTest).toList();
      var expectedEmpty = Stream.generate(emptyValueSupplier).limit(arg0).toList();
      var firstNOutputs = outputs.stream().limit(arg0).toList();
      assertEquals(expectedEmpty, firstNOutputs);

      //Now get the outputs that should have an index and actual content
      var lastOutputs = outputs.stream().skip(arg0).toList();

      //So use the index value to check the sublist matches the right part of the input.
      lastOutputs.forEach(output -> assertEquals(inputs.subList(output.index(), output.index() + arg1), output.content()));
    });
  }
}
