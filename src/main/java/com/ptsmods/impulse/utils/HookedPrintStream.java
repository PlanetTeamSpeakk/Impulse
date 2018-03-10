package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class HookedPrintStream extends PrintStream {

	private final PrintHook hook;

	public HookedPrintStream(OutputStream out, PrintHook hook) {
		super(out);
		this.hook = hook;
	}

	public HookedPrintStream(String fileName, PrintHook hook) throws FileNotFoundException {
		super(fileName);
		this.hook = hook;
	}

	public HookedPrintStream(File file, PrintHook hook) throws FileNotFoundException {
		super(file);
		this.hook = hook;
	}

	public HookedPrintStream(OutputStream out, boolean autoFlush, PrintHook hook) {
		super(out, autoFlush);
		this.hook = hook;
	}

	public HookedPrintStream(String fileName, String csn, PrintHook hook) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		this.hook = hook;
	}

	public HookedPrintStream(File file, String csn, PrintHook hook) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
		this.hook = hook;
	}

	public HookedPrintStream(OutputStream out, boolean autoFlush, String encoding, PrintHook hook) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
		this.hook = hook;
	}

	@Override
	public void print(boolean b) {
		print(b ? "true" : "false");
	}

	@Override
	public void print(char c) {
		hook.run(c);
		super.print(c);
	}

	@Override
	public void print(int i) {
		hook.run(i);
		super.print(i);
	}

	@Override
	public void print(long l) {
		hook.run(l);
		super.print(l);
	}

	@Override
	public void print(float f) {
		hook.run(f);
		super.print(f);
	}

	@Override
	public void print(double d) {
		hook.run(d);
		super.print(d);
	}

	@Override
	public void print(char s[]) {
		hook.run(s);
		super.print(s);
	}

	@Override
	public void print(String s) {
		hook.run(s);
		super.print(s);
	}

	@Override
	public PrintStream printf(String format, Object ... args) {
		return format(format, args);
	}

	@Override
	public PrintStream printf(Locale l, String format, Object ... args) {
		hook.run(String.format(l, format, args));
		return format(l, format, args);
	}

	public static abstract class PrintHook {

		private void run(Object o) {
			onPrint(String.valueOf(o));
		}

		/**
		 * @param string The string that has been printed, if {@code null} was printed a string containing {@code "null"} is passed.
		 */
		public abstract void onPrint(String string);

	}

}
