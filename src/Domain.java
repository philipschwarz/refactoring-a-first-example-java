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
    return Math.max(performance().audience() - 30, 0);
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
    final var basicAmount = 40_000;
    final var largeAudiencePremiumAmount =
      performance.audience() <= 30 ? 0 : 1_000 * (performance.audience() - 30);
    return basicAmount + largeAudiencePremiumAmount;
  }
}
record ComedyCalculator(Performance performance, Play play) implements PerformanceCalculator {
  @Override public int amount() {
    final var basicAmount = 30_000;
    final var largeAudiencePremiumAmount =
      performance.audience() <= 20 ? 0 : 10_000 + 500 * (performance.audience() - 20);
    final var audienceSizeAmount = 300 * performance.audience();
    return basicAmount + largeAudiencePremiumAmount + audienceSizeAmount;
  }
  @Override public int volumeCredits() {
    return PerformanceCalculator.super.volumeCredits()
      + (int) Math.floor(performance().audience() / 5);
  }
}