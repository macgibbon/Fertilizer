package fertilizer;

public class Content {
    double value = 0.0;
    String name = null;

    public Content(String name) {
        super();
        this.name = name;
    }

    public Content(double value) {
        super();
        this.value = value;
    }

    public Content(int value) {
        super();
        this.name = Integer.toString(value);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
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
