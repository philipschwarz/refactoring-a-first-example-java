import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class Statement {

  static String statement(Invoice invoice, Map<String, Play> plays) {
    return renderPlainText(CreateStatementData.createStatementData(invoice,plays));
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
