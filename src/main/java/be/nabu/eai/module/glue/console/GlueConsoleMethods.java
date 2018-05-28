package be.nabu.eai.module.glue.console;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.module.glue.console.table.ExtendedTableView;
import be.nabu.glue.annotations.GlueParam;
import be.nabu.glue.core.api.Lambda;
import be.nabu.glue.core.impl.GlueUtils;
import be.nabu.glue.utils.ScriptRuntime;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.libs.evaluator.annotations.MethodProviderClass;

@MethodProviderClass(namespace = "console")
public class GlueConsoleMethods {

	private Pane target;
	private AceEditor log;
	private GlueConsole artifact;
	private Map<String, Object> views = new HashMap<String, Object>();

	public GlueConsoleMethods(GlueConsole artifact, Pane target, AceEditor log) {
		this.artifact = artifact;
		this.target = target;
		this.log = log;
	}
	
	public void gui(Lambda lambda) {
		ScriptRuntime runtime = ScriptRuntime.getRuntime();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ScriptRuntime current = ScriptRuntime.getRuntime();
				runtime.registerInThread();
				try {
					GlueUtils.calculate(lambda, runtime, new ArrayList<Object>());
				}
				finally {
					if (current != null) {
						current.registerInThread();
					}
					else {
						runtime.unregisterInThread();
					}
				}
			}
		});
	}
	
	public byte [] screenshot(Region node, String format) throws IOException {
		if (format == null) {
			format = "png";
		}
		WritableImage writableImage = new WritableImage((int) node.getWidth(), (int) node.getHeight());
		node.snapshot(new SnapshotParameters(), writableImage);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), format, output);
		return output.toByteArray();
	}
	
	public void print(Region region) {
		if (region == null) {
			region = target;
		}
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {                    
			boolean showPrintDialog = job.showPrintDialog(MainController.getInstance().getStage());
			if (showPrintDialog) {                        
//				region.setScaleX(0.60);
//				region.setScaleY(0.60);
//				region.setTranslateX(-220);
//				region.setTranslateY(-70);
				boolean success = job.printPage(region);
				if (success) {
					job.endJob();
				}
//				region.setTranslateX(0);
//				region.setTranslateY(0);
//				region.setScaleX(1.0);
//				region.setScaleY(1.0);                                              
			}  
		}
	}
	
	public String window(@GlueParam(name = "title") String title, @GlueParam(name = "node") Parent node) {
		String id = UUID.randomUUID().toString().replace("-", "");

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Tab newTab = MainController.getInstance().newTab(title);
				newTab.setContent(node);
				newTab.setOnClosed(new EventHandler<Event>() {
					@Override
					public void handle(Event arg0) {
						ScriptRuntime runtime = ScriptRuntime.getRuntime();
						if (runtime != null) {
							runtime.abort();
						}
					}
				});
				views.put(id, newTab);
			}
		});
		
		return id;
	}
	
	public String popup(@GlueParam(name = "title") String title, @GlueParam(name = "node") Parent node, @GlueParam(name = "modal") Boolean modal, @GlueParam(name = "width") Integer width, @GlueParam(name = "height") Integer height, @GlueParam(name = "fullscreen") Boolean fullscreen) {
		String id = UUID.randomUUID().toString().replace("-", "");
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final Stage stage = new Stage();
				stage.initOwner(MainController.getInstance().getStage());
				if (modal != null && modal) {
					stage.initModality(Modality.WINDOW_MODAL);
				}
				Scene scene = new Scene((Parent) node);
				stage.setScene(scene);
				stage.setTitle(title);
				
				if (width != null) {
					stage.setWidth(width);
				}
				if (height != null) {
					stage.setHeight(height);
				}
				if (fullscreen != null) {
					stage.setFullScreen(fullscreen);
				}
				
				stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent arg0) {
						if (arg0.getCode() == KeyCode.ESCAPE) {
							stage.hide();
						}
					}
				});
				
				stage.show();
				
				views.put(id, stage);
			}
		});
		return id;
	}
	
	public void hide(String...ids) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Collection<String> idsToClose = ids == null || ids.length == 0 ? views.keySet() : Arrays.asList(ids);
				for (String idToClose : idsToClose) {
					Object object = views.get(idToClose);
					if (object != null) {
						if (object instanceof Tab) {
							MainController.getInstance().close((Tab) object);
						}
						else if (object instanceof Stage) {
							((Stage) object).hide();
						}
						views.remove(idToClose);
					}
				}
			}
		});
	}
	
//	private boolean initializedDisplay = false;
//	
//	private void displayInit() {
//		if (!initializedDisplay) {
//			synchronized(this) {
//				if (!initializedDisplay) {
//					String title = artifact.getConfig().getTitle();
//					if (title == null) {
//						title = artifact.getId();
//					}
//					Tab newTab = MainController.getInstance().newTab(title);
//					newTab.setContent(target);
//					newTab.setOnClosed(new EventHandler<Event>() {
//						@Override
//						public void handle(Event arg0) {
//							ScriptRuntime runtime = ScriptRuntime.getRuntime();
//							if (runtime != null) {
//								runtime.abort();
//							}
//						}
//					});
//					initializedDisplay = true;
//				}
//			}
//		}
//	}
	
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
//					displayInit();
					finalTarget.getChildren().add(finalNode);
				}
			});
		}
	}
	
	public Node log() {
		return log.getWebView();
	}
	
	public void clear() {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {
				public void run() {
					log.setContent("text/plain", "");
				}
			});
		}
		else {
			log.setContent("text/plain", "");
		}
	}
	
	public static TabPane tabs() {
		return new TabPane();
	}
	
	public static Tab tab(String title) {
		return new Tab(title);
	}
	
	public static VBox vertical(Node...nodes){
		VBox vbox = new VBox();
		if (nodes != null && nodes.length > 0) {
			vbox.getChildren().addAll(nodes);
		}
		return vbox;
	}
	
	public static HBox horizontal(Node...nodes){
		HBox hbox = new HBox();
		if (nodes != null && nodes.length > 0) {
			hbox.getChildren().addAll(nodes);
		}
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
