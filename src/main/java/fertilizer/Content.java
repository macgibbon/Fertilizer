package fertilizer;


import org.apache.commons.math4.legacy.optim.linear.Relationship;
import static fertilizer.Celltype.*;

public class Content {
    public double value = 0.0;
    public String name = null;
    public Boolean enabled = null;
    public Celltype celltype = whitespace;
    
    public Content() {
        this.name = "";
    }

    public Content(String name, Celltype celltype) {
        super();      
        this.celltype = celltype;
        this.name = name;
    }

    public Content(double value, Celltype celltype) {
        this.value = value;
        this.celltype = celltype;
    }
    
    public Content(Boolean b) {
       this.enabled = b;
        celltype = enable;
    }

    public Content(Relationship relationship) {
        this.name = relationship.name();
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
    
    public static Content update(Content oldContent, String newText) {
        switch (oldContent.celltype) {
        case name:
            return new Content(newText, oldContent.celltype);

        case analysis:
        case price:
        case constraintAmount:
            double d = Double.valueOf(newText);
            return new Content(d, oldContent.celltype);
        case enable:
            boolean b = Boolean.valueOf(newText);
            return new Content(b);
        case relationship:
        case ingredientAmount:
        case actualAmount:
        case solutionPrice:
        case totalAmount:
        case whitespace:
            throw new RuntimeException("Unexpected Entry");
        default:
            return new Content("",Celltype.name);
        }
    }

}
