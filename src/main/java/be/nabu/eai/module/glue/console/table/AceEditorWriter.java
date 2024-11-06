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

package be.nabu.eai.module.glue.console.table;

import java.io.IOException;
import java.io.Writer;

import be.nabu.jfx.control.ace.AceEditor;
import javafx.application.Platform;

public class AceEditorWriter extends Writer {

	private AceEditor aceEditor;

	public AceEditorWriter(AceEditor aceEditor) {
		this.aceEditor = aceEditor;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		final String string = new String(cbuf, off, len);
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					aceEditor.append(string);
	//				textarea.setScrollTop(Double.MAX_VALUE);
				}
			});
		}
		else {
			aceEditor.append(string);
		}
	}

	@Override
	public void flush() throws IOException {
		// do nothing
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}
}
