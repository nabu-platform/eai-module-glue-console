package be.nabu.eai.module.glue.console;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.resources.api.ResourceContainer;

// need start/stop button (console can be long running)
public class GlueConsole extends JAXBArtifact<GlueConsoleConfiguration> {

	public GlueConsole(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "glue-console.xml", GlueConsoleConfiguration.class);
	}
	
}
