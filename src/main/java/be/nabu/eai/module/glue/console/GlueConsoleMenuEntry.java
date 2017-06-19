package be.nabu.eai.module.glue.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.MainMenuEntry;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.api.Entry;
import be.nabu.glue.api.Script;
import be.nabu.glue.utils.ScriptUtils;

public class GlueConsoleMenuEntry implements MainMenuEntry {

	@Override
	public void populate(MenuBar menuBar) {
		Menu plugins = EAIDeveloperUtils.findOrCreate(menuBar, "Plugins");
		populate(plugins, null);
	}

	public static void populate(Menu plugins, Entry entry) {
		List<GlueConsole> artifacts = MainController.getInstance().getRepository().getArtifacts(GlueConsole.class);
		
		Comparator<MenuItem> comparator = new Comparator<MenuItem>() {
			@Override
			public int compare(MenuItem o1, MenuItem o2) {
				return o1.getText().compareTo(o2.getText());
			}
		};
		
		for (GlueConsole artifact : artifacts) {
			String path = getPath(artifact, entry != null);
			if (path == null) {
				continue;
			}
			int index = path.indexOf('/');
			Menu target = null;
			if (index >= 0) {
				for (MenuItem item : plugins.getItems()) {
					if (item instanceof Menu && item.getText().equals(path.substring(0, index))) {
						target = (Menu) item;
						break;
					}
				}
				if (target == null) {
					target = new Menu(path.substring(0, index));
					plugins.getItems().add(target);
					Collections.sort(plugins.getItems(), comparator);
				}
			}
			else {
				target = plugins;
			}
			MenuItem item = new MenuItem(index >= 0 ? path.substring(index + 1) : path);
			item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						List<Object> inputs = new ArrayList<Object>();
						if (entry != null) {
							inputs.add(entry);
						}
						GlueConsoleGUIManager.run(artifact, inputs);
					}
					catch (Exception e) {
						MainController.getInstance().notify(e);
					}
				}
			});
			Collections.sort(target.getItems(), comparator);
			target.getItems().add(item);
		}
	}
	
	private static String getPath(GlueConsole console, boolean withParam) {
		Script script = GlueConsoleGUIManager.getScript(console);
		if (script == null) {
			return null;
		}
		String path = null;
		String category = null;
		try {
			boolean hasParam = !ScriptUtils.getInputs(script).isEmpty();
			if (!hasParam && withParam) {
				return null;
			}
			else if (hasParam && !withParam) {
				return null;
			}
			if (script.getRoot() != null) {
				path = script.getRoot().getContext().getAnnotations().get("title");
				category = script.getRoot().getContext().getAnnotations().get("category");
			}
			if (path == null) {
				path = console.getId();
			}
			if (category != null) {
				path = category.replace("/", "|") + "/" + path;
			}
			return path;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
