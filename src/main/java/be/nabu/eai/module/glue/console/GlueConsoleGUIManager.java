package be.nabu.eai.module.glue.console;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseArtifactGUIInstance;
import be.nabu.eai.developer.managers.base.BasePortableGUIManager;
import be.nabu.eai.module.glue.console.table.AceEditorWriter;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.glue.api.Script;
import be.nabu.glue.core.impl.parsers.GlueParserProvider;
import be.nabu.glue.core.impl.providers.StaticJavaMethodProvider;
import be.nabu.glue.core.repositories.DynamicScriptRepository;
import be.nabu.glue.impl.SimpleExecutionEnvironment;
import be.nabu.glue.impl.formatters.SimpleOutputFormatter;
import be.nabu.glue.services.ServiceMethodProvider;
import be.nabu.glue.utils.DynamicScript;
import be.nabu.glue.utils.ScriptRuntime;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class GlueConsoleGUIManager extends BasePortableGUIManager<GlueConsole, BaseArtifactGUIInstance<GlueConsole>> {
	
	private BooleanProperty running = new SimpleBooleanProperty(false);

	public GlueConsoleGUIManager() {
		super("Glue Console", GlueConsole.class, new GlueConsoleManager());
	}

	public void run(GlueConsole artifact) throws IOException, ParseException {
		AnchorPane display = new AnchorPane();
		Tab newTab = MainController.getInstance().newTab(artifact.getId() + ": instance");
		newTab.setContent(display);
		
		AceEditor log = new AceEditor();
		ServiceMethodProvider serviceMethodProvider = new ServiceMethodProvider(artifact.getRepository(), artifact.getRepository(), artifact.getRepository().getServiceRunner());
		GlueParserProvider parserProvider = new GlueParserProvider(serviceMethodProvider, new StaticJavaMethodProvider(new GlueConsoleMethods(display, log.getWebView())));
		DynamicScriptRepository repository = new DynamicScriptRepository(parserProvider);
		DynamicScript script = new DynamicScript(repository, parserProvider, artifact.getConfig().getScript());
		repository.add(script);
		
		Map<String, String> environment = new HashMap<String, String>();
		ScriptRuntime runtime = new ScriptRuntime(script, new SimpleExecutionEnvironment("local", environment), false, new HashMap<String, Object>());
		
		newTab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				runtime.abort();
			}
		});
		
		runtime.setFormatter(new SimpleOutputFormatter(new AceEditorWriter(log)) {

			int depth = 0;
			
			@Override
			public void start(Script script) {
				running.set(true);
				depth++;
				super.start(script);
			}

			@Override
			public void end(Script script, Date started, Date stopped, Exception exception) {
				if (--depth == 0) {
					running.set(false);
				}
				super.end(script, started, stopped, exception);
			}
			
		});
		new Thread(runtime).start();
	}

	@Override
	public void display(MainController controller, AnchorPane pane, GlueConsole artifact) throws IOException, ParseException {
		HBox buttons = new HBox();
		Button start = new Button("Start");
		
		start.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					run(artifact);
				}
				catch (Exception e) {
					MainController.getInstance().notify(e);
				}
			}
		});
		
		buttons.getChildren().addAll(start);
		
		AceEditor editor = new AceEditor();
		editor.setContent("text/x-glue", artifact.getConfig().getScript() == null ? "" : artifact.getConfig().getScript());
		
		editor.subscribe(AceEditor.CHANGE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				String content = editor.getContent();
				if (content == null) {
					content = "";
				}
				if (!content.equals(artifact.getConfig().getScript())) {
					artifact.getConfig().setScript(content);
					MainController.getInstance().setChanged();
				}
			}
		});
		editor.subscribe(AceEditor.CLOSE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				MainController.getInstance().close();
			}
		});
		editor.subscribe(AceEditor.SAVE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				try {
					artifact.getConfig().setScript(editor.getContent());
					MainController.getInstance().save();
				}
				catch (IOException e) {
					MainController.getInstance().notify(e);
				}
			}
		});
		
		VBox box = new VBox();
		box.getChildren().addAll(buttons, editor.getWebView());
		
		VBox.setVgrow(editor.getWebView(), Priority.ALWAYS);
		
		AnchorPane.setBottomAnchor(box, 0d);
		AnchorPane.setTopAnchor(box, 0d);
		AnchorPane.setRightAnchor(box, 0d);
		AnchorPane.setLeftAnchor(box, 0d);
		pane.getChildren().add(box);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected BaseArtifactGUIInstance<GlueConsole> newGUIInstance(Entry entry) {
		return new BaseArtifactGUIInstance<GlueConsole>(this, entry);
	}

	@Override
	protected void setEntry(BaseArtifactGUIInstance<GlueConsole> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}

	@Override
	protected GlueConsole newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new GlueConsole(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<GlueConsole> guiInstance, GlueConsole instance) {
		guiInstance.setArtifact(instance);
	}
	
	@Override
	public String getCategory() {
		return "Reporting";
	}
}
