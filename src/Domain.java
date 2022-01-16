import java.util.List;

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

record PerformanceCalculator(Performance performance, Play play) {
  int amount() {
    var result = 0;
    switch (play.type()) {
      case "tragedy" -> {
        result = 40_000;
        if (performance.audience() > 30) 
          result += 1_000 * (performance.audience() - 30); }
      case "comedy" -> {
        result = 30_000;
        if (performance.audience() > 20) 
          result += 10_000 + 500 * (performance.audience() - 20);
        result += 300 * performance.audience(); }
      default -> throw new IllegalArgumentException(
            "unknown type " + play.type());
    }
    return result;
  }
  int volumeCredits() {
    var result = 0;
    result += Math.max(performance.audience() - 30, 0);
    if ("comedy" == play.type()) 
      result += Math.floor(performance.audience() / 5);  
    return result; 
  }  
}