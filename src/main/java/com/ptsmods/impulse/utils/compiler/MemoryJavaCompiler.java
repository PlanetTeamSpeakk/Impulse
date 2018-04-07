package com.ptsmods.impulse.utils.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.ptsmods.impulse.miscellaneous.Main;
import com.ptsmods.impulse.utils.AtomicObject;
import com.ptsmods.impulse.utils.HookedPrintStream;
import com.ptsmods.impulse.utils.HookedPrintStream.PrintHook;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 */
public class MemoryJavaCompiler {
	private static JavaCompiler				tool;
	private static StandardJavaFileManager	sjfm;
	private static MemoryClassLoader		classLoader	= new MemoryClassLoader();

	private MemoryJavaCompiler() {
	}

	static {
		tool = ToolProvider.getSystemJavaCompiler();
		if (tool == null) throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
		sjfm = tool.getStandardFileManager(null, null, null);
	}

	/**
	 * Compiles the code to and returns the method in it.
	 *
	 * @throws CompilationException
	 *             If an error occurs during compilation.
	 */
	public static Method compileStaticMethod(final String methodName, final String className, final String source) throws ClassNotFoundException, CompilationException {
		Class clazz = compileClass(className + ".java", source);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods)
			if (method.getName().equals(methodName)) {
				if (!method.isAccessible()) method.setAccessible(true);
				return method;
			}
		throw new NoSuchMethodError(methodName);
	}

	public static Class compileClass(String fileName, String source) throws ClassNotFoundException, CompilationException {
		return classLoader.loadClassFromBytes(compile(fileName, source), fileName.split("\\.")[0]);
	}

	public static byte[] compile(String fileName, String source) throws CompilationException {
		return compile(fileName, source, null, System.getProperty("java.class.path"));
	}

	/**
	 * Compile given String source and return bytecodes as a Map.
	 *
	 * @param fileName
	 *            source fileName to be used for error messages etc.
	 * @param source
	 *            Java source as String
	 * @param sourcePath
	 *            Location of additional .java source files
	 * @param classPath
	 *            Location of additional .class files
	 * @throws CompilationException
	 *             If an error occurs during the compilation.
	 */
	private static byte[] compile(String fileName, String source, String sourcePath, String classPath) throws CompilationException {
		MemoryJavaFileManager fileManager = new MemoryJavaFileManager(sjfm);
		List<JavaFileObject> compUnits = new ArrayList<>(1);
		compUnits.add(fileManager.makeStringSource(fileName, source));
		return compile(compUnits, fileManager, sourcePath, classPath);
	}

	private static byte[] compile(final List<JavaFileObject> compUnits, final MemoryJavaFileManager fileManager, String sourcePath, String classPath) throws CompilationException {
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		List<String> options = new ArrayList<>();
		options.add("-Xlint:all");
		options.add("-deprecation");
		if (sourcePath != null) {
			options.add("-sourcepath");
			options.add(sourcePath);
		}

		if (classPath != null) {
			options.add("-classpath");
			options.add(classPath);
		}
		AtomicObject<String> error = new AtomicObject("");
		JavaCompiler.CompilationTask task = tool.getTask(new PrintWriter(new HookedPrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}, new PrintHook() {
			@Override
			public void onPrint(String string, boolean newLine) {
				error.set(error.get() + string + (newLine ? "\n" : ""));
			}
		})), fileManager, diagnostics, options, null, compUnits);
		if (task.call() == false) {
			for (Diagnostic diagnostic : diagnostics.getDiagnostics())
				error.set(error.get() + diagnostic);
			char[] chars = error.get().split("\n")[2].toCharArray();
			int character = 0;
			for (int i : Main.range(chars.length))
				if (chars[i] == '^') {
					character = i;
					break;
				}
			throw new CompilationException(error.get().split("\n")[1], character, "Error:" + error.get().split("\n")[0].split(": error:")[1]);
		}
		Map<String, byte[]> classBytes = fileManager.getClassBytes();
		try {
			fileManager.close();
		} catch (IOException exp) {
		}
		return classBytes.get(new ArrayList(classBytes.keySet()).get(0));
	}
}