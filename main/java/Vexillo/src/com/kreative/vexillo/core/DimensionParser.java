package com.kreative.vexillo.core;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class DimensionParser {
	private final CharacterIterator it;
	
	public DimensionParser(String s) {
		this.it = new StringCharacterIterator(s);
	}
	
	private Dimension parseFactor() {
		char ch = it.current();
		while (Character.isWhitespace(ch)) ch = it.next();
		if (Character.isDigit(ch) || ch == '.') {
			StringBuffer s = new StringBuffer();
			while (Character.isDigit(ch) || ch == '.') {
				s.append(ch);
				ch = it.next();
			}
			String ns = s.toString();
			try { return new Dimension.Constant(Double.parseDouble(ns)); }
			catch (NumberFormatException e) { throw expected("a number", ns); }
		} else if (Character.isLetter(ch)) {
			StringBuffer s = new StringBuffer();
			while (Character.isLetter(ch) || Character.isDigit(ch)) {
				s.append(ch);
				ch = it.next();
			}
			String id = s.toString();
			if (id.equalsIgnoreCase("abs")) return new Dimension.Abs(parseFactor());
			if (id.equalsIgnoreCase("sqrt")) return new Dimension.Sqrt(parseFactor());
			if (id.equalsIgnoreCase("cbrt")) return new Dimension.Cbrt(parseFactor());
			if (id.equalsIgnoreCase("sin")) return new Dimension.Sin(parseFactor());
			if (id.equalsIgnoreCase("cos")) return new Dimension.Cos(parseFactor());
			if (id.equalsIgnoreCase("tan")) return new Dimension.Tan(parseFactor());
			if (id.equalsIgnoreCase("sinh")) return new Dimension.Sinh(parseFactor());
			if (id.equalsIgnoreCase("cosh")) return new Dimension.Cosh(parseFactor());
			if (id.equalsIgnoreCase("tanh")) return new Dimension.Tanh(parseFactor());
			if (id.equalsIgnoreCase("asin")) return new Dimension.Asin(parseFactor());
			if (id.equalsIgnoreCase("acos")) return new Dimension.Acos(parseFactor());
			if (id.equalsIgnoreCase("atan")) return new Dimension.Atan(parseFactor());
			if (id.equalsIgnoreCase("ln")) return new Dimension.Ln(parseFactor());
			if (id.equalsIgnoreCase("exp")) return new Dimension.Exp(parseFactor());
			if (id.equalsIgnoreCase("log10")) return new Dimension.Log10(parseFactor());
			if (id.equalsIgnoreCase("exp10")) return new Dimension.Exp10(parseFactor());
			if (id.equalsIgnoreCase("pow")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Pow(d[0], d[1]); }
			if (id.equalsIgnoreCase("root")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Root(d[0], d[1]); }
			if (id.equalsIgnoreCase("log")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Log(d[0], d[1]); }
			if (id.equalsIgnoreCase("min")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Min(d[0], d[1]); }
			if (id.equalsIgnoreCase("max")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Max(d[0], d[1]); }
			if (id.equalsIgnoreCase("hypot")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Hypot(d[0], d[1]); }
			if (id.equalsIgnoreCase("atan2")) { Dimension[] d = parseArgs(new Dimension[2]); return new Dimension.Atan2(d[0], d[1]); }
			return new Dimension.Variable(id);
		} else switch (ch) {
			case '+': it.next(); return new Dimension.Pos(parseFactor());
			case '-': it.next(); return new Dimension.Neg(parseFactor());
			case '(':
				it.next();
				Dimension d = parseSum();
				ch = it.current();
				while (Character.isWhitespace(ch)) ch = it.next();
				if (ch != ')') throw expected(")", ch);
				it.next();
				return d;
			default: throw expected("a number or variable", ch);
		}
	}
	
	private Dimension[] parseArgs(Dimension[] array) {
		char ch = it.current();
		while (Character.isWhitespace(ch)) ch = it.next();
		if (ch != '(') throw expected("(", ch);
		it.next();
		array[0] = parseSum();
		for (int i = 1; i < array.length; i++) {
			ch = it.current();
			while (Character.isWhitespace(ch)) ch = it.next();
			if (ch != ',') throw expected(",", ch);
			it.next();
			array[i] = parseSum();
		}
		ch = it.current();
		while (Character.isWhitespace(ch)) ch = it.next();
		if (ch != ')') throw expected(")", ch);
		it.next();
		return array;
	}
	
	private Dimension parseProduct() {
		Dimension d = parseFactor();
		for (;;) {
			char ch = it.current();
			while (Character.isWhitespace(ch)) ch = it.next();
			switch (ch) {
				case '*': it.next(); d = new Dimension.Mul(d, parseFactor()); break;
				case '/': it.next(); d = new Dimension.Div(d, parseFactor()); break;
				case '%': it.next(); d = new Dimension.Mod(d, parseFactor()); break;
				default: return d;
			}
		}
	}
	
	private Dimension parseSum() {
		Dimension d = parseProduct();
		for (;;) {
			char ch = it.current();
			while (Character.isWhitespace(ch)) ch = it.next();
			switch (ch) {
				case '+': it.next(); d = new Dimension.Add(d, parseProduct()); break;
				case '-': it.next(); d = new Dimension.Sub(d, parseProduct()); break;
				default: return d;
			}
		}
	}
	
	public Dimension parse() {
		Dimension d = parseSum();
		char ch = it.current();
		while (Character.isWhitespace(ch)) ch = it.next();
		if (ch != CharacterIterator.DONE) throw expected("end of expression", ch);
		return d;
	}
	
	public boolean hasNext() {
		char ch = it.current();
		while (Character.isWhitespace(ch) || ch == ',') ch = it.next();
		return (ch != CharacterIterator.DONE);
	}
	
	public Dimension parseNext() {
		char ch = it.current();
		while (Character.isWhitespace(ch) || ch == ',') ch = it.next();
		return parseSum();
	}
	
	private static IllegalArgumentException expected(String e, String f) {
		return new IllegalArgumentException(
			"Expected " + e + " but found " + f + "."
		);
	}
	
	private static IllegalArgumentException expected(String e, char f) {
		return new IllegalArgumentException(
			"Expected " + e + " but found " + (
				(f == CharacterIterator.DONE) ?
				"end of expression" : Character.toString(f)
			) + "."
		);
	}
}