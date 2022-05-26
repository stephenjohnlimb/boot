package features.stepDefinitions;

import com.tinker.utils.AnagramDetector;
import io.cucumber.java8.En;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnagramSteps implements En {

  private AnagramDetector underTest;
  private Stream<Character> incoming;

  public AnagramSteps() {

    Given("^a known value of \"([^\"]*)\"$", (String arg0) -> underTest = new AnagramDetector(arg0));

    When("^processing an incoming Stream of characters of \"([^\"]*)\"$",
            (String arg0) -> incoming = arg0.chars().mapToObj(value -> (char) value));

    Then("^(\\d+) anagrams are detected at indexes (\\d+), (\\d+) and (\\d+)$",
            (Integer arg0, Integer arg1, Integer arg2, Integer arg3) -> {

              var expectation = List.of(arg1, arg2, arg3);

              var results = Optional.of(incoming)
                      .map(underTest)
                      .orElse(Stream.of())
                      .collect(Collectors.toList());

              //Now check results
              assertEquals(arg0, results.size());
              assertEquals(expectation, results);
            });
  }
}
