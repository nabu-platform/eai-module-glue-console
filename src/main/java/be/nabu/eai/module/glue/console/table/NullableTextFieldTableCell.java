package be.nabu.eai.module.glue.console.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

abstract public class NullableTextFieldTableCell<T, S> extends TableCell<T, S> {
	
	private TextField textField;
	
	@Override
	public void startEdit() {
		super.startEdit();
		
		if (textField == null)
			createTextField();
		
		setText(null);
		setGraphic(textField);
		textField.selectAll();
		textField.requestFocus();
	}

	@Override
	public void cancelEdit() {
		cancelEdit(false);
	}
	
	private void cancelEdit(boolean hard) {
		super.cancelEdit();
		if (!hard) {
			commitEdit(unmarshal(textField.getText()));
		}
		else {
			setText(marshal(getItem()));
		}
		setGraphic(null);
	}

	@Override
	public void updateItem(S item, boolean empty) {
//		super.updateItem(item, empty);
		super.updateItem(item, false);
		if (empty) {
			setText(null);
			setGraphic(null);
		}
		else {
			if (isEditing()) {
				if (textField != null)
					textField.setText(getString());
				setText(null);
				setGraphic(textField);
			} 
			else {
				setText(getString());
				setGraphic(null);
			}
		}
	}
	
	abstract protected S unmarshal(String value);
	
	abstract protected String marshal(S value);

	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (arg2 == null || !arg2) {
					commitEdit(textField.getText() == null || textField.getText().isEmpty() ? null : unmarshal(textField.getText()));
				}
			}
		});
		textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
					List<TableColumn<T, ?>> columns = new ArrayList<TableColumn<T, ?>>(getTableView().getColumns());
					int current = columns.indexOf(getTableColumn());
					List<TableColumn<T, ?>> search = columns.subList(current + 1, columns.size());
					search.addAll(columns.subList(0, current));
					if (event.isShiftDown()) {
						Collections.reverse(search);
					}
					TableColumn<T, ?> columnToEdit = getTableColumn();
					for (TableColumn<T, ?> column : search) {
						if (column.isEditable()) {
							columnToEdit = column;
							break;
						}
					}
					int index = columns.indexOf(columnToEdit);
					int row = getTableRow().getIndex();
					if (!event.isShiftDown() && index <= current) {
						row++;
					}
					else if (event.isShiftDown() && index >= current) {
						row--;
					}
					commitEdit(textField.getText() == null || textField.getText().isEmpty() ? null : unmarshal(textField.getText()));
					if (row >= 0 && row < getTableView().getItems().size()) {
						getTableView().edit(row, columnToEdit);
					}
					event.consume();
				}
				else if (event.getCode() == KeyCode.ENTER) {
					commitEdit(textField.getText() == null || textField.getText().isEmpty() ? null : unmarshal(textField.getText()));
					event.consume();
				}
				else if (event.getCode() == KeyCode.ESCAPE) {
					cancelEdit(true);
					event.consume();
				}
			}
		});
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}
	
	
}
