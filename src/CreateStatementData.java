import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

public class CreateStatementData {

  static StatementData createStatementData(Invoice invoice, Map<String, Play> plays) {

    Function<Performance, Play> playFor =
      aPerformance -> plays.get(aPerformance.playID());

    Function<Performance, Integer> volumeCreditsFor = aPerformance ->
      new PerformanceCalculator(aPerformance,playFor.apply(aPerformance)).volumeCredits();

    Function<List<EnrichedPerformance>, Integer> totalVolumeCredits = (performances) ->
      performances.stream().collect(reducing(0, EnrichedPerformance::volumeCredits, Integer::sum));

    Function<List<EnrichedPerformance>, Integer> totalAmount = (performances) ->
      performances.stream().collect(reducing(0, EnrichedPerformance::amount, Integer::sum));

    Function<Performance, EnrichedPerformance> enrichPerformance = aPerformance -> {
      final var calculator = new PerformanceCalculator(aPerformance,playFor.apply(aPerformance));
      return new EnrichedPerformance(
        aPerformance.playID(),
        calculator.play(),
        aPerformance.audience(),
        calculator.amount(),
        volumeCreditsFor.apply(aPerformance));
    };

    final var enrichedPerformances =
      invoice.performances().stream().map(enrichPerformance::apply).collect(toList());
    return new StatementData(
      invoice.customer(),
      enrichedPerformances,
      totalAmount.apply(enrichedPerformances),
      totalVolumeCredits.apply(enrichedPerformances));
  }

}
