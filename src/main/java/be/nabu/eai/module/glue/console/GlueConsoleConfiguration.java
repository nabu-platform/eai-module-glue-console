package be.nabu.eai.module.glue.console;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "glueConsole")
public class GlueConsoleConfiguration {
	private String script;

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
}
