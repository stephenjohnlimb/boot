package features.stepDefinitions;

import com.tinker.utils.Histogram;
import io.cucumber.java8.En;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistogramSteps implements En {

  private Histogram<Character> underTest;
  private Map<Character, AtomicInteger> output;

  public HistogramSteps() {
    Given("^a histogram that can be used to hold Characters$", () -> underTest = new Histogram<>());

    When("^I don't add any Characters to it$", () -> {
      //Nothing to do here
    });

    Then("^the histogram is empty$", () -> assertTrue(underTest.getResults().isEmpty()));

    When("^I add \"([^\"]*)\"$", (String arg0) -> {
      arg0.chars().mapToObj(value -> (char) value).forEach(underTest);
      output = underTest.getResults();
    });

    Then("^the histogram has k->(\\d+), j->(\\d+), a->(\\d+) and b->(\\d+)$", (Integer arg0, Integer arg1, Integer arg2, Integer arg3) -> {
      assertEquals(4, output.size());
      assertEquals(arg0, output.get('k').get());
      assertEquals(arg1, output.get('j').get());
      assertEquals(arg2, output.get('a').get());
      assertEquals(arg3, output.get('b').get());
    });
  }
}
