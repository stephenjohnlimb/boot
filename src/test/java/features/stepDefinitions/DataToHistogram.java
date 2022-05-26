package features.stepDefinitions;

import com.tinker.utils.Histogram;
import com.tinker.utils.HistogramData;
import com.tinker.utils.SplitData;
import io.cucumber.java8.En;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DataToHistogram implements En {

    private SplitData<Character> input;

    private Function<SplitData<Character>, HistogramData<Character>> histogramConverter = in -> {
        HistogramData<Character> rtn = new HistogramData<>(in.index(), new Histogram<>());
        in.content().stream().forEach(rtn.content());
        return rtn;
    };

    public DataToHistogram() {

        Given("^a split data to histogram converter$", () -> {
            //Nothing to do here
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
            //TODO Fix broken test
            var result = Stream.of(input).map(histogramConverter).findFirst();
            result
                    .map(histogram -> histogram.content().getResults())
                    .ifPresentOrElse(values -> assertTrue(expectation.equals(values)), () -> fail("No results available"));
        });
    }
}
