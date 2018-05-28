package be.nabu.eai.module.glue.console;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "glueConsole")
public class GlueConsoleConfiguration {

	private String script, title;
	private ConsoleType type;

	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ConsoleType getType() {
		return type;
	}
	public void setType(ConsoleType type) {
		this.type = type;
	}
	public enum ConsoleType {
		PLUGIN, APPLICATION
	}
}
