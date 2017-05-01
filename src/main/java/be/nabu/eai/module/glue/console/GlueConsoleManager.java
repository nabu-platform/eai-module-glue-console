package be.nabu.eai.module.glue.console;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class GlueConsoleManager extends JAXBArtifactManager<GlueConsoleConfiguration, GlueConsole> {

	public GlueConsoleManager() {
		super(GlueConsole.class);
	}

	@Override
	protected GlueConsole newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new GlueConsole(id, container, repository);
	}

}
