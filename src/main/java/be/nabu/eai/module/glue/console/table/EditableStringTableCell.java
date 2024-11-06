/*
* Copyright (C) 2017 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
