import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

record Play(String name, String type) { }

record Performance(String playID, int audience) { }

record Invoice(String customer, List<Performance> performances) { }

public class Statement {

  static String statement(Invoice invoice, Map<String, Play> plays) {

    Function<Performance,Play> playFor = aPerformance ->
        plays.get(aPerformance.playID());

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

    Function<Integer,String> usd = aNumber -> {
      final var formatter = NumberFormat.getCurrencyInstance(Locale.US);
      formatter.setCurrency(Currency.getInstance(Locale.US));
      return formatter.format(aNumber);
    };

    var totalAmount = 0;
    var result = "Statement for " + invoice.customer() + "\n";

    for(Performance perf : invoice.performances()) {
      // print line for this order
      result += "  " + playFor.apply(perf).name() + ": " + usd.apply(amountFor.apply(perf)/100)
                     + " (" + perf.audience() + " seats)\n";
      totalAmount += amountFor.apply(perf);
    }

    var volumeCredits = 0;
    for(Performance perf : invoice.performances()) {
      volumeCredits += volumeCreditsFor.apply(perf);
    }

    result += "Amount owed is " + usd.apply(totalAmount/100) + "\n";
    result += "You earned " + volumeCredits + " credits\n";
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
