package fertilizer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math4.legacy.optim.linear.Relationship;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class ContentTableCell extends TableCell<List<Content>, Content> {

    static ObservableList<Relationship> choices = FXCollections
            .observableArrayList(Stream.of(Relationship.values()).collect(Collectors.toList()));
  
    static  TextField createTextField(final ContentTableCell cell) {
          final TextField textField = new TextField(cell.getItem().toString());
          textField.addEventFilter(KeyEvent.ANY, event -> {
              // System.out.println(event);
              if (event.getCode()== javafx.scene.input.KeyCode.ESCAPE) {
                  cell.escapeEdit();
              }           
          });
        textField.setOnAction(event -> {
            Content newContent = Content.update(cell.getItem(), textField.getText());
            cell.commitEdit(newContent);
            event.consume();
        });
        return textField;
    }   
   

    public static StringConverter<Relationship> relationshipConverter = new StringConverter<Relationship>() {
        
        @Override
        public String toString(Relationship r) {
            return r.name();
        }
        
        @Override
        public Relationship fromString(String string) {
            return Relationship.valueOf(string);
        }
    };
    
    static ChoiceBox<Relationship> createChoiceBox(final Cell<Content> cell) {
        ChoiceBox<Relationship> choiceBox = new ChoiceBox<>(choices);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setConverter(relationshipConverter);
        choiceBox.showingProperty().addListener(o -> {
            if (!choiceBox.isShowing()) {
                Relationship r = choiceBox.getSelectionModel().getSelectedItem();
                cell.commitEdit(new Content(r));
            }
        });
        return choiceBox;
    }
    
    static CheckBox createCheckBox(final Cell<Content> cell) {
        CheckBox checkbox = new CheckBox();
        checkbox.setMaxWidth(Double.MAX_VALUE);
        checkbox.setIndeterminate(false);
   //     checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> cell.s(new Content(newValue)));
        return checkbox;
    }
    
    ChoiceBox<Relationship> cb;
    CheckBox cxbox;
    TextField tf;
    private AtomicBoolean infeasible;
    
    public ContentTableCell(AtomicBoolean infeasible) {
        super();
        this.infeasible = infeasible;
    }

    @Override
    public void startEdit() {
        // System.out.println("startEdit");
        super.startEdit();
        Celltype celltype = getItem().celltype;
        switch (celltype) {
        case relationship:
            Relationship r = Relationship.valueOf(getItem().toString());
            setText(null);
            cb = createChoiceBox(this);
            cb.getSelectionModel().select(r);
            setGraphic(cb);
            cb.requestFocus();
            break;
        case enable:
            cxbox = createCheckBox(this);
            Boolean b = getItem().enabled;
            cxbox.setSelected(b);
             setText(null);           
            setGraphic(cxbox);
            cxbox.requestFocus();
            cxbox.selectedProperty().addListener(observer -> this.commitEdit(new Content(cxbox.isSelected())));
            break;  

        default:
            String oldText = getItem().toString();
            setText(null);
            tf = createTextField(this);
            tf.setText(oldText);
            setGraphic(tf);
            tf.selectAll();
            tf.requestFocus();
        }
    }

    @Override
    public void commitEdit(Content newValue) {
        // System.out.println("commitEdit " + newValue.toString());
        super.commitEdit(newValue);
        setGraphic(null);
        setText(newValue.toString());      
    }

    @Override
    public void cancelEdit() {
        // System.out.println("cancelEdit " + getItem().toString());
        Content newValue = Content.update(getItem(), tf.getText());
        super.commitEdit(newValue);
        setGraphic(null);
        setText(newValue.toString());
    }    
    
    private void escapeEdit() {
        // System.out.println("escapeEdit " + getItem().toString());
        super.cancelEdit();
        setGraphic(null);
        setText(getItem().toString());        
    }

    @Override
    public void updateSelected(boolean selected) {
        // System.out.println("selected " + selected);
        super.updateSelected(selected);
    }

    @Override
    public void updateItem(Content content, boolean empty) {
        super.updateItem(content, empty);
        if (empty || content == null) {
            setText(null);
        } else {
            setGraphic(null);
            setText(content.toString());
            Celltype celltype = content.celltype;
            switch (celltype) {
            case actualAmount:
            case whitespace:
            case solutionPrice:
            case totalAmount:
            case ingredientAmount:
            case name:
                setEditable(false);
                getStyleClass().add("readonly");
                if (infeasible.get()) {
                    this.getStyleClass().add("infeasible");
                } else {
                    this.getStyleClass().remove("infeasible");
                }
                break;
            default:
                break;
            }

        }
    }
}
