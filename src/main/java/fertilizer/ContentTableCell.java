package fertilizer;

import java.util.List;
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
import javafx.scene.input.KeyCode;

public class ContentTableCell extends TableCell<List<Content>, Content> {

    static ObservableList<String> choices = FXCollections
            .observableArrayList(Stream.of(Relationship.values()).map(r -> r.name()).collect(Collectors.toList()));

    static  TextField createTextField(final Cell<Content> cell) {
          final TextField textField = new TextField(cell.getItem() == null ? "" : cell.getItem().toString());

        textField.setOnAction(event -> {
            Content newContent = Content.update(cell.getItem(), textField.getText());
            cell.commitEdit(newContent);
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }
    
    ChoiceBox<String> cb = new ChoiceBox<String>(choices);
    CheckBox cxbox = new CheckBox();
    TextField tf;
    

    public ContentTableCell() {
        super();
    }

    @Override
    public void startEdit() {
        super.startEdit();
        System.out.println("startEdit");
        String oldText=getItem().toString();
        setText(null);
        tf = createTextField(this);
        tf.setText(oldText);
        setGraphic(tf);
        tf.selectAll();
        tf.requestFocus();
    }

    @Override
    public void commitEdit(Content newValue) {
        System.out.println("commitEdit");
        super.commitEdit(newValue);
        setGraphic(null);
        setText(tf.getText());      
    }

    @Override
    public void cancelEdit() {
        System.out.println("cancelEdit");
        super.cancelEdit();
        setGraphic(null);
        setText(getItem().toString());
    }

    @Override
    public void updateSelected(boolean selected) {
        System.out.println("selected " + selected);
        super.updateSelected(selected);
    }

    @Override
    public void updateItem(Content content, boolean empty) {
        super.updateItem(content, empty);
        if (empty || content == null) {
            setText(null);
        } else {
            Celltype celltype = content.celltype;
            switch (celltype) {
            
            case analysis:
            case price:
            case constraintAmount:
                setGraphic(null);
                setText(content.toString());
                break;
            
            case relationship:
                cb.setValue(content.name);
                setGraphic(cb);
                setText(null);
                break;
            case enable:
                cxbox.setIndeterminate(false);
                cxbox.setSelected(content.enabled);
                setGraphic(cxbox);
                setText(null);
                break;

            case actualAmount:
            case whitespace:
            case solutionPrice:
            case totalAmount:
            case ingredientAmount:
            case name:
                setGraphic(null);
                setText(content.toString());
                getStyleClass().add("readonly");
                break;
           //
           //     setGraphic(null);
             //   setText(content.toString());
           }
            if (empty && isSelected()) {
                updateSelected(false);
            }
        }
    }
}
