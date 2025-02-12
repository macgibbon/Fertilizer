package fertilizer;


import org.apache.commons.math4.legacy.optim.linear.Relationship;
import static fertilizer.Celltype.*;

public class Content {
    public double value = 0.0;
    public String name = null;
    public Boolean enabled;
    public Celltype celltype = whitespace;
    
    public Content() {
        super();
        this.name = "";
    }

    public Content(String name, Celltype celltype) {
        super();
        this.name = name;
        this.celltype = celltype;
    }

    public Content(double value, Celltype celltype) {
        super();
        this.value = value;
        this.celltype = celltype;
    }
    
    public Content(Boolean b) {
        super();
        this.enabled = b;
        celltype = enable;
    }

    public Content(Relationship relationship) {
        this.name = relationship.toString();
        celltype = Celltype.relationship;        
    }

    @Override
    public String toString() {
        if (name != null)
            return name;
        else if(enabled != null)
            return enabled.toString();
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
