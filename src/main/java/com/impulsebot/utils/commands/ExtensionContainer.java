package com.impulsebot.utils.commands;

import java.io.File;

public class ExtensionContainer {

	private final Extension	extension;
	private final Class		mainClass;
	private final File		file;

	ExtensionContainer(Extension extension, Class mainClass, File file) {
		this.extension = extension;
		this.mainClass = mainClass;
		this.file = file;
	}

	public Extension getExtension() {
		return extension;
	}

	public Class getMainClass() {
		return mainClass;
	}

	public File getFile() {
		return file;
	}

}
