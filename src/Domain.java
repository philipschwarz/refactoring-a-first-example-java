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

sealed interface PerformanceCalculator {
  Performance performance();
  Play play();
  int amount();
  default int volumeCredits() {
    var result = 0;
    result += Math.max(performance().audience() - 30, 0);
    if ("comedy" == play().type())
      result += Math.floor(performance().audience() / 5);
    return result; 
  }
  static PerformanceCalculator instance(Performance aPerformance, Play aPlay) {
    return switch (aPlay.type()) {
      case "tragedy" -> new TragedyCalculator(aPerformance, aPlay);
      case "comedy" -> new ComedyCalculator(aPerformance, aPlay);
      default -> throw new IllegalArgumentException(
        String.format("unknown type '%s'", aPlay.type()));
    };
  }
}
record TragedyCalculator(Performance performance, Play play) implements PerformanceCalculator {
  @Override public int amount() {
    var result = 40_000;
    if (performance().audience() > 30) 
      result += 1_000 * (performance().audience() - 30);
    return result;
  }
}
record ComedyCalculator(Performance performance, Play play) implements PerformanceCalculator {
  @Override public int amount() {
    var result = 30_000;
    if (performance().audience() > 20) 
      result += 10_000 + 500 * (performance().audience() - 20);
    result += 300 * performance().audience(); 
    return result;
  }
}