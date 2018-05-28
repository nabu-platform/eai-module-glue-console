package be.nabu.eai.module.glue.console;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseArtifactGUIInstance;
import be.nabu.eai.developer.managers.base.BasePortableGUIManager;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.module.glue.console.table.AceEditorWriter;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.glue.api.ParameterDescription;
import be.nabu.glue.api.Script;
import be.nabu.glue.core.api.MethodProvider;
import be.nabu.glue.core.impl.parsers.GlueParserProvider;
import be.nabu.glue.core.impl.providers.StaticJavaMethodProvider;
import be.nabu.glue.core.repositories.DynamicScriptRepository;
import be.nabu.glue.impl.SimpleExecutionEnvironment;
import be.nabu.glue.impl.formatters.SimpleOutputFormatter;
import be.nabu.glue.services.ServiceMethodProvider;
import be.nabu.glue.utils.DynamicScript;
import be.nabu.glue.utils.ScriptRuntime;
import be.nabu.glue.utils.ScriptUtils;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class GlueConsoleGUIManager extends BasePortableGUIManager<GlueConsole, BaseArtifactGUIInstance<GlueConsole>> {
	
	public GlueConsoleGUIManager() {
		super("Glue Console", GlueConsole.class, new GlueConsoleManager());
	}

	public static Script getScript(GlueConsole artifact) {
		return getScript(artifact, null, null);
	}
	
	public static Script getScript(GlueConsole artifact, AceEditor log, AnchorPane display) {
		if (artifact.getConfig().getScript() == null) {
			return null;
		}
		ServiceMethodProvider serviceMethodProvider = new ServiceMethodProvider(artifact.getRepository(), artifact.getRepository(), artifact.getRepository().getServiceRunner());
		List<MethodProvider> providers = new ArrayList<MethodProvider>();
		providers.add(serviceMethodProvider); 
		providers.add(new StaticJavaMethodProvider(new Class<?> [] { ChartMethods.class }));
		if (log != null && display != null) {
			providers.add(new StaticJavaMethodProvider(new GlueConsoleMethods(artifact, display, log)));
		}
		GlueParserProvider parserProvider = new GlueParserProvider(providers.toArray(new MethodProvider[providers.size()]));
		DynamicScriptRepository repository = new DynamicScriptRepository(parserProvider);
		DynamicScript script;
		try {
			script = new DynamicScript(repository, parserProvider, artifact.getConfig().getScript());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		repository.add(script);
		return script;
	}
	
	public static void run(GlueConsole artifact, List<Object> inputs) throws IOException, ParseException {
		AnchorPane display = new AnchorPane();
		AceEditor log = new AceEditor();
		// make sure we initialize the webview here on the gui thread
		log.getWebView();

		Script script = getScript(artifact, log, display);

		String title = artifact.getConfig().getTitle();
		if (title == null) {
			title = script.getRoot().getContext().getAnnotations().get("title");
		}
		
		Map<String, String> environment = new HashMap<String, String>();
		Map<String, Object> input = new HashMap<String, Object>();
		Iterator<Object> iterator = inputs.iterator();
		for (ParameterDescription description : ScriptUtils.getInputs(script)) {
			input.put(description.getName(), iterator.hasNext() ? iterator.next() : null);
		}
		ScriptRuntime runtime = new ScriptRuntime(script, new SimpleExecutionEnvironment("local", environment), false, input);
		
		runtime.setFormatter(new SimpleOutputFormatter(new AceEditorWriter(log), true, false));
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
					run(artifact, new ArrayList<Object>());
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
		
		TabPane tabs = new TabPane();
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabs.setSide(Side.RIGHT);
		Tab tab = new Tab("Script");
		tab.setContent(box);
		tabs.getTabs().add(tab);
		
		tab = new Tab("Configuration");
		SimplePropertyUpdater createUpdater = EAIDeveloperUtils.createUpdater(artifact.getConfig(), null, "script");
		AnchorPane properties = new AnchorPane();
		MainController.getInstance().showProperties(createUpdater, properties, false);
		tab.setContent(properties);
		tabs.getTabs().add(tab);
		
		AnchorPane.setBottomAnchor(tabs, 0d);
		AnchorPane.setTopAnchor(tabs, 0d);
		AnchorPane.setRightAnchor(tabs, 0d);
		AnchorPane.setLeftAnchor(tabs, 0d);
		pane.getChildren().add(tabs);
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
