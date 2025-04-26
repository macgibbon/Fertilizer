package fertilizer;

public record Batch(String name, double density, double percent, double amount, double scale, String actual)  {
    public static String[] tableHeaders = {"Material Name", "Density (lb/ft\u00b3)", "Percent", "Units/Batch (lb)", "Scale (lb)", "Actual (lb)" };
}