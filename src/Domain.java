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

record PerformanceCalculator(Performance performance) { }