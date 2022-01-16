import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statement {

  static String statement(Invoice invoice, Map<String, Play> plays) {
    return renderPlainText(CreateStatementData.createStatementData(invoice,plays));
  }

  static String htmlStatement(Invoice invoice, Map<String, Play> plays) {
    return renderHtml(CreateStatementData.createStatementData(invoice, plays));
  }
  
  static String renderPlainText(StatementData data) {
    var result = "Statement for " + data.customer() + "\n";
    for(EnrichedPerformance perf : data.performances()) {
      result += "  " + perf.play().name() + ": " + usd(perf.amount()/100)
                     + " (" + perf.audience() + " seats)\n";
    }

    result += "Amount owed is " + usd(data.totalAmount()/100) + "\n";
    result += "You earned " + data.totalVolumeCredits() + " credits\n";
    return result;
  }

  static String renderHtml(StatementData data) {
    var result = "<h1>Statement for "+data.customer()+"</h1>\n";
    result += "<table>\n";
    result += "<tr><th>play</th><th>seats</th><th>cost</th></tr>\n";
    for (EnrichedPerformance perf : data.performances()) {
      result += "<tr><td>" + perf.play().name() + "</td><td>" + perf.audience() + "</td>";
      result += "<td>" + usd(perf.amount() / 100) + "</td></tr>\n";
    }
    result += "</table>\n";
    result += "<p>Amount owed is <em>" + usd(data.totalAmount()/100) + "</em></p>\n";
    result += "<p>You earned <em>"+ data.totalVolumeCredits() +"</em> credits</p>\n";
    return result;
  }

  static String usd(int aNumber) {
    final var formatter = NumberFormat.getCurrencyInstance(Locale.US);
    formatter.setCurrency(Currency.getInstance(Locale.US));
    return formatter.format(aNumber);
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
    if (!Statement.htmlStatement(invoices.get(0), plays).equals(
      """
      <h1>Statement for BigCo</h1>
      <table>
      <tr><th>play</th><th>seats</th><th>cost</th></tr>
      <tr><td>Hamlet</td><td>55</td><td>$650.00</td></tr>
      <tr><td>As You Like It</td><td>35</td><td>$580.00</td></tr>
      <tr><td>Othello</td><td>40</td><td>$500.00</td></tr>
      </table>
      <p>Amount owed is <em>$1,730.00</em></p>
      <p>You earned <em>47</em> credits</p>
      """
    )) throw new AssertionError();
  }
  
}
