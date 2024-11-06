/*
* Copyright (C) 2017 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
