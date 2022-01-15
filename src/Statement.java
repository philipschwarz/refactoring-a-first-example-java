import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

record Play(String name, String type) { }

record Performance(String playID, int audience) { }

record EnrichedPerformance(
    String playID,
    Play play,
    int audience,
    Integer amount,
    Integer volumeCredits
) { }

record Invoice(String customer, List<Performance> performances) { }

record StatementData(
  String customer,
  List<EnrichedPerformance> performances,
  Integer totalAmount,
  Integer totalVolumeCredits
) { }

public class Statement {

  static String statement(Invoice invoice, Map<String, Play> plays) {

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
    final var statementData = new StatementData(
      invoice .customer(),
      enrichedPerformances,
      totalAmount.apply(enrichedPerformances),
      totalVolumeCredits.apply(enrichedPerformances));
    return renderPlainText(statementData);
  }

  static String renderPlainText(StatementData data) {

    Function<Integer,String> usd = aNumber -> {
      final var formatter = NumberFormat.getCurrencyInstance(Locale.US);
      formatter.setCurrency(Currency.getInstance(Locale.US));
      return formatter.format(aNumber);
    };

    var result = "Statement for " + data.customer() + "\n";
    for(EnrichedPerformance perf : data.performances()) {
      result += "  " + perf.play().name() + ": " + usd.apply(perf.amount()/100)
                     + " (" + perf.audience() + " seats)\n";
    }

    result += "Amount owed is " + usd.apply(data.totalAmount()/100) + "\n";
    result += "You earned " + data.totalVolumeCredits() + " credits\n";
    return result;
  }

  static final List<Invoice> invoices =
    List.of(
      new Invoice(
        "BigCo",
        List.of(new Performance( "hamlet", 55),
                new Performance("as-like", 35),
                new Performance("othello", 40))));

  static final Map<String,Play> plays = Map.of(
    "hamlet" , new Play("Hamlet", "tragedy"),
    "as-like", new Play("As You Like It", "comedy"),
    "othello", new Play("Othello", "tragedy"));

  public static void main(String[] args) {
    if (!Statement.statement(invoices.get(0), plays).equals(
      """
      Statement for BigCo
        Hamlet: $650.00 (55 seats)
        As You Like It: $580.00 (35 seats)
        Othello: $500.00 (40 seats)
      Amount owed is $1,730.00
      You earned 47 credits
      """
    )) throw new AssertionError();
  }
  
}
