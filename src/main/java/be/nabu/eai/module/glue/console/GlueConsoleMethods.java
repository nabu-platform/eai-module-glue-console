package be.nabu.eai.module.glue.console;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import be.nabu.eai.module.glue.console.table.ExtendedTableView;
import be.nabu.glue.annotations.GlueParam;
import be.nabu.glue.core.api.Lambda;
import be.nabu.glue.core.impl.GlueUtils;
import be.nabu.glue.utils.ScriptRuntime;
import be.nabu.libs.evaluator.annotations.MethodProviderClass;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@MethodProviderClass(namespace = "console")
public class GlueConsoleMethods {

	private Pane target;
	private Node log;

	public GlueConsoleMethods(Pane target, Node log) {
		this.target = target;
		this.log = log;
	}
	
	public void display(@GlueParam(name = "node") Object node, @GlueParam(name = "target") Node target, @GlueParam(name = "expand") Boolean expand) {
		if (target instanceof TabPane) {
			if (!(node instanceof Tab)) {
				Tab tab = new Tab("Unnamed");
				tab.setContent((Node) node);
				node = tab;
			}
			final TabPane finalTarget = (TabPane) target;
			final Tab finalNode = (Tab) node;
			Platform.runLater(new Runnable() {
				public void run() {
					finalTarget.getTabs().add(finalNode);
				}
			});
		}
		else {
			if (target == null) {
				target = GlueConsoleMethods.this.target;
			}
			if (target instanceof AnchorPane && (expand == null || expand)) {
				AnchorPane.setBottomAnchor((Node) node, 0d);
				AnchorPane.setTopAnchor((Node) node, 0d);
				AnchorPane.setRightAnchor((Node) node, 0d);
				AnchorPane.setLeftAnchor((Node) node, 0d);
			}
			final Pane finalTarget = (Pane) target;
			final Node finalNode = (Node) node;
			Platform.runLater(new Runnable() {
				public void run() {
					finalTarget.getChildren().add(finalNode);
				}
			});
		}
	}
	
	public Node log() {
		return log;
	}
	
	public static TabPane tabs() {
		return new TabPane();
	}
	
	public static Tab tab(String title) {
		return new Tab(title);
	}
	
	public static VBox vertical(Node...nodes){
		VBox vbox = new VBox();
		vbox.getChildren().addAll(nodes);
		return vbox;
	}
	
	public static HBox horizontal(Node...nodes){
		HBox hbox = new HBox();
		hbox.getChildren().addAll(nodes);
		return hbox;
	}
	
	public Button button(String title, final Lambda lambda) {
		Button button = new Button(title);
		ScriptRuntime runtime = ScriptRuntime.getRuntime();
		button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void handle(ActionEvent event) {
				GlueUtils.calculate(lambda, runtime, new ArrayList());
			}
		});
		return button;
	}

	public static TextInputControl text(@GlueParam(name = "text") String text, @GlueParam(name = "prompt") String prompt, @GlueParam(name = "change") Lambda lambda) {
		TextField textField = new TextField(text);
		textField.setPromptText(prompt);
		if (lambda != null) {
			ScriptRuntime runtime = ScriptRuntime.getRuntime();
			textField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
					GlueUtils.calculate(lambda, runtime, Arrays.asList(arg2));
				}
			});
		}
		return textField;
	}
	
	public static TextInputControl textarea(@GlueParam(name = "text") String text, @GlueParam(name = "prompt") String prompt, @GlueParam(name = "change") Lambda lambda) {
		TextArea area = new TextArea(text);
		area.setPromptText(prompt);
		if (lambda != null) {
			ScriptRuntime runtime = ScriptRuntime.getRuntime();
			area.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
					GlueUtils.calculate(lambda, runtime, Arrays.asList(arg2));
				}
			});
		}
		return area;
	}
	
	public static Label label(@GlueParam(name = "text") String text) {
		return new Label(text);
	}
	
	public static CheckBox checkbox(@GlueParam(name = "text") String text, @GlueParam(name = "checked") Boolean checked, @GlueParam(name = "change") Lambda lambda) {
		CheckBox checkBox = new CheckBox(text);
		checkBox.setSelected(checked != null && checked);
		if (lambda != null) {
			ScriptRuntime runtime = ScriptRuntime.getRuntime();
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					GlueUtils.calculate(lambda, runtime, Arrays.asList(arg2));
				}
			});
		}
		return checkBox;
	}
	
	public static ExtendedTableView table(Iterable<?> columns, Iterable<?> rows, Lambda...aggregators) {
		return new ExtendedTableView(columns, rows, aggregators);
	}
	
	public static Node fxml(String content) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.load(new ByteArrayInputStream(content.getBytes("UTF-8")));
		return loader.getRoot();
	}
}
