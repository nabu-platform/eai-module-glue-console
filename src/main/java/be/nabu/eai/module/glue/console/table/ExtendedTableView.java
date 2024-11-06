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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import be.nabu.glue.core.api.Lambda;
import be.nabu.glue.core.impl.GlueUtils;
import be.nabu.glue.core.impl.methods.v2.SeriesMethods;

@SuppressWarnings("rawtypes")
public class ExtendedTableView extends TableView<Map> {

	private Iterable<?> columns;
	private Iterable<?> rows;
	private Lambda[] aggregators;
	private List rowIds = new ArrayList();
	private Iterator rowIterator;

	@SuppressWarnings("unchecked")
	public ExtendedTableView(Iterable<?> columns, Iterable<?> rows, Lambda...aggregators) {
		autosize();
		setEditable(true);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.columns = columns;
		this.rows = rows;
		this.aggregators = aggregators;
		if (rows != null) {
			rowIterator = rows.iterator();
			TableColumn<Map, String> rowIdentifierColumn = new TableColumn<Map, String>();
			getColumns().add(rowIdentifierColumn);
			rowIdentifierColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<Map, String> cell) {
						int indexOf = -1;
						List items = getItems();
						// need exact map in memory, not just equals()
						for (int i = 0; i < items.size(); i++) {
							if (items.get(i) == cell.getValue()) {
								indexOf = i;
								break;
							}
						}
						if (indexOf >= 0 && indexOf < items.size() - aggregators.length) {
							for (int i = rowIds.size(); i <= indexOf; i++) {
								rowIds.add(rowIterator.next());
							}
							return new SimpleStringProperty(GlueUtils.convert(rowIds.get(indexOf), String.class));
						}
						return new SimpleStringProperty();
					}
				}
			);
			rowIdentifierColumn.setMinWidth(100);
		}
		int counter = 0;
		for (final Object column : columns) {
			TableColumn<Map, String> tableColumn = new TableColumn<Map, String>(GlueUtils.convert(column, String.class));
			tableColumn.setEditable(true);
			tableColumn.setSortable(rows == null);
			tableColumn.setCellFactory(EditableStringTableCell.<Map>forTableColumn());
			final int index = counter;
			tableColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<Map, String> cell) {
						return new SimpleStringProperty(GlueUtils.convert(cell.getValue().get(column), String.class));
					}
				}
			);
			tableColumn.setOnEditCommit(new EventHandler<CellEditEvent<Map, String>>() {
				@Override
				public void handle(CellEditEvent<Map, String> event) {
					Object oldValue = event.getRowValue().get(column);
					event.getRowValue().put(column, event.getNewValue());
					if ((oldValue != null && !oldValue.equals(event.getNewValue())) || (oldValue == null && event.getNewValue() != null)) {
						calculateAggregates(index);
					}
				}
			});
			getColumns().add(tableColumn);
			counter++;
		}
		for (int i = 0; i < aggregators.length; i++) {
			LinkedHashMap linkedHashMap = new LinkedHashMap();
			for (Object column : columns) {
				linkedHashMap.put(column, null);
			}
			getItems().add(FXCollections.observableMap(linkedHashMap));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object getAggregates() {
		List column = (List) getColumn();
		List aggregates = new ArrayList();
		for (int i = 0; i < aggregators	.length; i++) {
			List results = new ArrayList();
			for (int j = 0; j < column.size(); j++) {
				Iterable<?> aggregate = SeriesMethods.aggregate(aggregators[i], ((List) column).get(j));
				results.add(aggregate);
			}
			aggregates.add(results);
		}
		return aggregates;
	}
	
	@SuppressWarnings("unchecked")
	private void calculateAggregates(Integer...columnIndex) {
		if (columnIndex != null && columnIndex.length > 0) {
			Object column = getColumn();
			for (Integer index : columnIndex) {
				for (int i = 0; i < aggregators.length; i++) {
					Iterable<?> aggregate = SeriesMethods.aggregate(aggregators[i], ((List) column).get(index));
					Object value = SeriesMethods.last(aggregate);
					int counter = 0;
					for (Object columnKey : columns) {
						if (index.equals(counter)) {
							getItems().get(getItems().size() - (aggregators.length - i)).put(columnKey, value);
							break;
						}
						counter++;
					}
				}
			}
		}
		refresh();
		// refresh seems to lose focus?
		requestFocus();
	}
	
	@SuppressWarnings("unchecked")
	public void add(Object...object) {
		Iterable<?> series = GlueUtils.toSeries(object);
		Map map = FXCollections.observableMap(new LinkedHashMap());
		Iterator iterator = series.iterator();
		Set<Integer> columnsToRecalculate = new HashSet<Integer>();
		int counter = 0;
		for (Object column : columns) {
			if (!iterator.hasNext()) {
				break;
			}
			Object next = iterator.next();
			map.put(GlueUtils.convert(column, String.class), next);
			if (next != null) {
				columnsToRecalculate.add(counter);
			}
			counter++;
		}
		calculateAggregates(columnsToRecalculate.toArray(new Integer[0]));
		getItems().add(getItems().size() - aggregators.length, map);
	}
	
	@SuppressWarnings("unchecked")
	public void delete(Integer...rows) {
		if (rows == null || rows.length == 0) {
			getItems().clear();
			// add again for aggregates
			LinkedHashMap linkedHashMap = new LinkedHashMap();
			for (Object column : columns) {
				linkedHashMap.put(column, null);
			}
			getItems().add(FXCollections.observableMap(linkedHashMap));
		}
		else {
			delete(Arrays.asList(rows));
		}
	}
	
	@SuppressWarnings("unused")
	private void delete(List<Integer> rows) {
		List<Integer> numbers = new ArrayList<Integer>(rows);
		Collections.sort(numbers);
		Collections.reverse(numbers);
		for (Integer row : numbers) {
			getItems().remove((int) row);
		}
		List<Integer> columns = new ArrayList<Integer>();
		for (Object column : this.columns) {
			columns.add(columns.size());
		}
		calculateAggregates(columns.toArray(new Integer[0]));
	}
	
	public void deleteSelected() {
		ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
		if (!selectedIndices.isEmpty()) {
			delete(selectedIndices);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object getColumn() {
		List list = new ArrayList();
		for (int j = (rows == null ? 0 : 1); j < getColumns().size(); j++) {
			List items = new ArrayList();
			for (int i = 0; i < getItems().size() - aggregators.length; i++) {
				items.add(getColumns().get(j).getCellData(i));
			}
			list.add(items);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public Object getRow() {
		List list = new ArrayList();
		for (int i = 0; i < getItems().size() - aggregators.length; i++) {
			List items = new ArrayList();
			for (int j = (rows == null ? 0 : 1); j < getColumns().size(); j++) {
				items.add(getColumns().get(j).getCellData(i));
			}
			list.add(items);
		}
		return list;
	}
	
	public Iterable<?> getColumnIdentifiers() {
		return columns;
	}
	
	public Iterable<?> getRowIdentifiers() {
		return rows;
	}
}
