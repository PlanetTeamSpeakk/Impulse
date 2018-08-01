package com.impulsebot.utils;

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
		hook.run(c, false);
		super.print(c);
	}

	@Override
	public void print(int i) {
		hook.run(i, false);
		super.print(i);
	}

	@Override
	public void print(long l) {
		hook.run(l, false);
		super.print(l);
	}

	@Override
	public void print(float f) {
		hook.run(f, false);
		super.print(f);
	}

	@Override
	public void print(double d) {
		hook.run(d, false);
		super.print(d);
	}

	@Override
	public void print(char s[]) {
		hook.run(s, false);
		super.print(s);
	}

	@Override
	public void print(String s) {
		hook.run(s, false);
		super.print(s);
	}

	@Override
	public PrintStream printf(String format, Object ... args) {
		return format(format, args);
	}

	@Override
	public PrintStream printf(Locale l, String format, Object ... args) {
		hook.run(String.format(l, format, args), false);
		return format(l, format, args);
	}

	@Override
	public void println(boolean b) {
		println(b ? "true" : "false");
	}

	@Override
	public void println(char c) {
		hook.run(c, true);
		super.println(c);
	}

	@Override
	public void println(int i) {
		hook.run(i, true);
		super.println(i);
	}

	@Override
	public void println(long l) {
		hook.run(l, true);
		super.println(l);
	}

	@Override
	public void println(float f) {
		hook.run(f, true);
		super.println(f);
	}

	@Override
	public void println(double d) {
		hook.run(d, true);
		super.println(d);
	}

	@Override
	public void println(char s[]) {
		hook.run(s, true);
		super.println(s);
	}

	@Override
	public void println(String s) {
		hook.run(s, true);
		super.println(s);
	}

	public static abstract class PrintHook {

		private void run(Object o, boolean printLn) {
			onPrint(String.valueOf(o), printLn);
		}

		/**
		 * @param string The string that has been printed, if {@code null} was printed a string containing {@code "null"} is passed.
		 * @param newLine {@code true} if a new line was printed afterwards, {@code false} otherwise. This does <b>not</b> mean that the string doesn't end with a newline character.
		 */
		public abstract void onPrint(String string, boolean newLine);

	}

}
