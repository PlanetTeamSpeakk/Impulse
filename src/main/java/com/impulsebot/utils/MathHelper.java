package com.impulsebot.utils;

import java.util.ArrayList;
import java.util.List;

public class MathHelper {

	public static Percentage percentage(double max, double amount) {
		return percentage(max, amount, 100D);
	}

	/**
	 * To calculate how much 25% of 6800 is use
	 * {@link com.impulsebot.Main#percentage(double, double, double)
	 * percentage(100, 6800, 25)} where only the last 2 arguments are edited.
	 * To calculate how much 4800 of 6800 is use
	 * {@link com.impulsebot.Main#percentage(double, double, double)
	 * percentage(6800, 4800, 100)} where only the first 2 arguments are edited.
	 */
	public static Percentage percentage(double max, double amount, double percentage) {
		return new Percentage((float) (amount / max * percentage));
	}

	public static float average(float... floats) {
		float average = 0F;
		for (float f : floats)
			average += f;
		return average / floats.length;
	}

	public static double factorial(double d) {
		Double[] doubles = Main.range(d);
		doubles = Main.castDoubleArray(Main.removeArgs(doubles, 0, 1));
		org.apache.commons.lang3.ArrayUtils.reverse(doubles);
		for (double x : doubles)
			d *= x;
		return d;
	}

	/**
	 * Evaluates a math equation in a String.
	 * It does addition, subtraction, multiplication, division, exponentiation
	 * (using the ^ symbol), factorial (! <b>before</b> a number), and a few basic
	 * functions like sqrt, cbrt, sin, cos, and tan. It supports grouping using
	 * (...), and it gets the operator precedence and associativity rules correct.
	 * 
	 * @param str
	 * @return The answer to the equation.
	 * @author Boann
	 */
	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = ++pos < str.length() ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ')
					nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected character: " + (char) ch);
				return x;
			}

			double parseExpression() {
				double x = parseTerm();
				while (true)
					if (eat('+'))
						x += parseTerm(); // addition
					else if (eat('-'))
						x -= parseTerm(); // subtraction
					else return x;
			}

			double parseTerm() {
				double x = parseFactor();
				while (true)
					if (eat('*'))
						x *= parseFactor(); // multiplication
					else if (eat('/'))
						x /= parseFactor(); // division
					else if (eat('%'))
						x %= parseFactor(); // modulo
					else return x;
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if (ch >= '0' && ch <= '9' || ch == '.') { // numbers
					while (ch >= '0' && ch <= '9' || ch == '.')
						nextChar();
					x = Double.parseDouble(str.substring(startPos, pos));
				} else if (ch >= 'a' && ch <= 'z' || ch == '!') { // functions
					while (ch >= 'a' && ch <= 'z' || ch == '!')
						nextChar();
					String func = str.substring(startPos, pos);
					x = parseFactor();
					if (func.equals("sqrt"))
						x = Math.sqrt(x);
					else if (func.equals("cbrt"))
						x = Math.cbrt(x);
					else if (func.equals("sin"))
						x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos"))
						x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan"))
						x = Math.tan(Math.toRadians(x));
					else if (func.equals("pi"))
						x = Math.PI * (x == 0D ? 1D : x);
					else if (func.equals("!") && x > 0 && x <= 170)
						x = factorial(x);
					else if (func.equals("!"))
						throw new RuntimeException("Cannot factorialize numbers higher than 170 or lower than 1.");
					else throw new RuntimeException("Unknown function: " + func);
				} else if (ch != -1)
					throw new RuntimeException("Unexpected character: " + (char) ch);
				else x = 0D;

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
				return x;
			}
		}.parse();
	}

	public static int min(Integer... ints) {
		List<Double> doubles = new ArrayList();
		for (int i : ints)
			doubles.add((double) i);
		return (int) min(doubles.toArray(new Double[] {}));
	}

	public static double min(Double... doubles) {
		double min = Double.MAX_VALUE;
		for (double d : doubles)
			if (d < min) min = d;
		return min;
	}

	public static int max(Integer... ints) {
		List<Double> doubles = new ArrayList();
		for (int i : ints)
			doubles.add((double) i);
		return (int) max(doubles.toArray(new Double[] {}));
	}

	public static double max(Double... doubles) {
		double max = Double.MIN_VALUE;
		for (double d : doubles)
			if (d > max) max = d;
		return max;
	}

	public static class Percentage extends Number {
		public static final Percentage	MAX_VALUE			= new Percentage(100F);
		public static final Percentage	MIN_VALUE			= new Percentage(0F);
		private static final long		serialVersionUID	= 2937645220837168877L;
		private float					value;

		public Percentage(float value) {
			if (value < 0) throw new IllegalArgumentException("The given value, " + value + ", was than 0.");
			this.value = value;
		}

		public Percentage(double value) {
			this((float) value);
		}

		@Override
		public int intValue() {
			return (int) value;
		}

		@Override
		public long longValue() {
			return (long) value;
		}

		@Override
		public float floatValue() {
			return value;
		}

		@Override
		public double doubleValue() {
			return value;
		}

		@Override
		public String toString() {
			return Float.toString(value) + "%";
		}

		public void add(float arg1) {
			if (value + arg1 < 100 && value + arg1 > 0)
				value += arg1;
			else if (value + arg1 > 100)
				value = 100F;
			else value = 0F;
		}

		public void add(Percentage arg1) {
			add(arg1.floatValue());
		}

		public void subtract(float arg1) {
			if (value - arg1 < 100 && value - arg1 > 0)
				value -= arg1;
			else if (value - arg1 > 100)
				value = 100F;
			else value = 0F;
		}

		public void subtract(Percentage arg1) {
			subtract(arg1.floatValue());
		}

	}

}
