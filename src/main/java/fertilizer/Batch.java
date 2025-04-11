package fertilizer;

public record Batch(String name, double percent, double amount, double scale, String actual)  {
    public static String[] tableHeaders = {"Material Name", "Percent", "Units/Batch", "Scale", "Actual" };
}