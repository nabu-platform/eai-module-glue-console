package be.nabu.eai.module.glue.console.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EditableStringTableCell<T> extends NullableTextFieldTableCell<T, String> {
	
	public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> forTableColumn() {
		return new Callback<TableColumn<T, String>, TableCell<T, String>>() {
			@Override
			public TableCell<T, String> call(TableColumn<T, String> p) {
				return new EditableStringTableCell<T>();
			}
		};
	}

	@Override
	protected String unmarshal(String value) {
		return value;
	}
	
	@Override
	protected String marshal(String value) {
		return value;
	}
}
