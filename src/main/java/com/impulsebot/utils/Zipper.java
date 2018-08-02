package com.impulsebot.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public class Zipper {

	private Zipper() {}

	public static void unzip(String file, String outputFolder) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		List<ZipEntry> entries = new ArrayList();
		Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
		if (!outputFolder.endsWith("/")) outputFolder += "/";
		if (!new File(outputFolder).exists() || !new File(outputFolder).isDirectory()) new File(outputFolder).mkdirs();
		while (entriesEnum.hasMoreElements())
			entries.add(entriesEnum.nextElement());
		for (ZipEntry entry : entries)
			if (entry.isDirectory())
				new File(outputFolder + "/" + entry.getName()).mkdirs();
		for (ZipEntry entry : entries)
			if (!entry.isDirectory()) {
				InputStream stream = zipFile.getInputStream(entry);
				Files.copy(stream, Paths.get(outputFolder, entry.getName()));
				IOUtils.closeQuietly(stream);
			}
		zipFile.close();
	}

}
