package features.stepDefinitions;

import com.tinker.utils.Histogram;
import com.tinker.utils.HistogramData;
import com.tinker.utils.SplitData;
import io.cucumber.java8.En;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataToHistogram implements En {

  private Histogram<Character> underTest;
  private SplitData<Character> input;

  public DataToHistogram() {

    Given("^a split data to histogram converter$", () -> {
      underTest = new Histogram<>();
    });
    When("^I provide SplitData of (\\d+), \"([^\"]*)\"$", (Integer arg0, String arg1) -> {
      input = new SplitData<Character>(arg0, arg1.chars().mapToObj(value -> (char) value).toList());
    });

    Then("^the HistogramData will have the value of (\\d+), k->(\\d+), j->(\\d+), a->(\\d+) and b->(\\d+)$", (Integer arg0, Integer arg1, Integer arg2, Integer arg3, Integer arg4) -> {
      var expectation = Map.of(
              'k', new AtomicInteger(arg1),
              'j', new AtomicInteger(arg2),
              'a', new AtomicInteger(arg3),
              'b', new AtomicInteger(arg4)
      );
      var result = Stream.of(input).map(incoming -> {
        HistogramData rtn = new HistogramData(incoming.index(), underTest);
        incoming.content().stream().forEach(rtn.content());
        return rtn;
      }).findFirst();
      assertTrue(result.isPresent());

    });
  }
}
