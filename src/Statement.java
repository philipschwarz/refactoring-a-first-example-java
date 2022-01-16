import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Statement {

  static String statement(Invoice invoice, Map<String, Play> plays) {
    return renderPlainText(CreateStatementData.createStatementData(invoice,plays));
  }

  static String htmlStatement(Invoice invoice, Map<String, Play> plays) {
    return renderHtml(CreateStatementData.createStatementData(invoice, plays));
  }

  static String renderPlainText(StatementData data) {
    return
      "Statement for %s\n".formatted(data.customer()) +
        data.performances()
            .stream()
            .map(p ->
                "  %s: %s (%d seats)\n".formatted(
                  p.play().name(), usd(p.amount()/100), p.audience())
            ).collect(Collectors.joining()) +
        """
        Amount owed is %s
        You earned %d credits
        """.formatted(usd(data.totalAmount()/100), data.totalVolumeCredits());
  }

  static String renderHtml(StatementData data) {
    return
      """
      <h1>Statement for %s</h1>
      <table>
      <tr><th>play</th><th>seats</th><th>cost</th></tr>
      """.formatted(data.customer()) +
        data
          .performances()
          .stream()
          .map(p -> "<tr><td>%s</td><td>%d</td><td>%s</td></tr>\n"
              .formatted(p.play().name(),p.audience(),usd(p.amount()/100))
          ).collect(Collectors.joining()) +
      """
      </table>
      <p>Amount owed is <em>%s</em></p>
      <p>You earned <em>%d</em> credits</p>
      """.formatted(usd(data.totalAmount()/100), data.totalVolumeCredits());
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
