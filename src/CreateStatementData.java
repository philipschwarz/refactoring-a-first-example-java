import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

public class CreateStatementData {

  static StatementData createStatementData(Invoice invoice, Map<String, Play> plays) {

    Function<Performance,Play> playFor =
      aPerformance -> plays.get(aPerformance.playID());

    Function<Performance,Integer> amountFor = aPerformance -> {
      var result = 0;
      switch (playFor.apply(aPerformance).type()) {
        case "tragedy" -> {
          result = 40_000;
          if (aPerformance.audience() > 30)
            result += 1_000 * (aPerformance.audience() - 30);
        }
        case "comedy" -> {
          result = 30_000;
          if (aPerformance.audience() > 20)
            result += 10_000 + 500 * (aPerformance.audience() - 20);
          result += 300 * aPerformance.audience();
        }
        default ->
            throw new IllegalArgumentException("unknown type " + playFor.apply(aPerformance).type());
      }
      return result;
    };

    Function<Performance,Integer> volumeCreditsFor = aPerformance -> {
      var result = 0;
      result += Math.max(aPerformance.audience() - 30, 0);
      if ("comedy" == playFor.apply(aPerformance).type())
        result += Math.floor(aPerformance.audience() / 5);
      return result;
    };

    Function<List<EnrichedPerformance>,Integer> totalVolumeCredits = (performances) ->
      performances.stream().collect(reducing(0,EnrichedPerformance::volumeCredits,Integer::sum));

    Function<List<EnrichedPerformance>,Integer> totalAmount = (performances) ->
      performances.stream().collect(reducing(0,EnrichedPerformance::amount,Integer::sum));

    Function<Performance,EnrichedPerformance> enrichPerformance = aPerformance ->
      new EnrichedPerformance(
          aPerformance.playID(),
          playFor.apply(aPerformance),
          aPerformance.audience(),
          amountFor.apply(aPerformance),
          volumeCreditsFor.apply(aPerformance));

    final var enrichedPerformances =
      invoice.performances().stream().map(enrichPerformance::apply).collect(toList());
    return new StatementData(
      invoice.customer(),
      enrichedPerformances,
      totalAmount.apply(enrichedPerformances),
      totalVolumeCredits.apply(enrichedPerformances));
  }

}
