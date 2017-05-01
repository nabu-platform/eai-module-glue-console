package be.nabu.eai.module.glue.console;

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

public class GlueConsoleMenuEntry implements MainMenuEntry {

	@Override
	public void populate(MenuBar menuBar) {
		Menu findOrCreate = EAIDeveloperUtils.findOrCreate(menuBar, "Plugins");
		
		List<GlueConsole> artifacts = MainController.getInstance().getRepository().getArtifacts(GlueConsole.class);
		Collections.sort(artifacts, new Comparator<GlueConsole>() {
			@Override
			public int compare(GlueConsole o1, GlueConsole o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		
		for (GlueConsole artifact : artifacts) {
			MenuItem item = new MenuItem(artifact.getId());
			item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						GlueConsoleGUIManager.run(artifact);
					}
					catch (Exception e) {
						MainController.getInstance().notify(e);
					}
				}
			});
			findOrCreate.getItems().add(item);
		}
	}

}
