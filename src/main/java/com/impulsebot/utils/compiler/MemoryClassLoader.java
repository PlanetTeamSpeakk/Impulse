package com.impulsebot.utils.compiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * ClassLoader that loads .class bytes from memory.
 */
public class MemoryClassLoader extends URLClassLoader {

	public MemoryClassLoader() {
		super(toURLs(System.getProperty("java.class.path")), ClassLoader.getSystemClassLoader());
	}

	public Class loadClassFromBytes(byte[] bytes, String className) {
		if (bytes == null || className == null || className.isEmpty())
			return null;
		else try {
			return super.findClass(className);
		} catch (ClassNotFoundException e) {
			return defineClass(className, bytes, 0, bytes.length);
		}
	}

	@Override
	public Class loadClass(String name) throws ClassNotFoundException {
		// Main.print(LogType.DEBUG, "Loaded class", name);
		return super.loadClass(name);
	}

	private static URL[] toURLs(String classPath) {
		if (classPath == null) return new URL[0];

		List<URL> list = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			File file = new File(token);
			if (file.exists())
				try {
					list.add(file.toURI().toURL());
				} catch (MalformedURLException mue) {
				}
			else try {
				list.add(new URL(token));
			} catch (MalformedURLException mue) {
			}
		}
		URL[] res = new URL[list.size()];
		list.toArray(res);
		return res;
	}
}