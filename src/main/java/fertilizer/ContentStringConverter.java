package fertilizer;

import javafx.util.StringConverter;

public class ContentStringConverter extends StringConverter<Content> {

    @Override
    public String toString(Content content) {        
        return content.toString();
    }

    @Override
    public Content fromString(String string) {
       return new Content(string, Celltype.name);
    }

}
