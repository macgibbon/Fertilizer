package fertilizer;

public class Content {
    public double value = 0.0;
    public String name = null;

    public Content(String name) {
        super();
        this.name = name;
    }

    public Content(double value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        if (name != null)
            return name;
        else
            return String.format("%.2f", value);
    }

    public static double convertDouble(String str) {
        double value = 0.0;
        try {
            value = Double.valueOf(str);
        } catch (Throwable t) {
        }
        return value;
    }

}
