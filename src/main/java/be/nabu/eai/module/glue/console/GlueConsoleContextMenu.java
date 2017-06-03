package be.nabu.eai.module.glue.console;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.repository.api.Entry;

public class GlueConsoleContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		Menu plugins = new Menu("Plugins");
		GlueConsoleMenuEntry.populate(plugins, entry);
		return plugins;
	}

}
