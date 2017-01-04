package com.kreative.vexillo.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Dimension {
	public abstract double value(Map<String, Dimension> namespace);
	
	public static final class Constant extends Dimension {
		private final double value;
		public Constant(double value) { this.value = value; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return value;
		}
	}
	
	public static final class Variable extends Dimension {
		private final String name;
		private final Set<Thread> lockedThreads;
		public Variable(String name) {
			this.name = name;
			this.lockedThreads = new HashSet<Thread>();
		}
		@Override
		public double value(Map<String, Dimension> ns) {
			Thread currentThread = Thread.currentThread();
			if (lockedThreads.contains(currentThread)) {
				throw new IllegalArgumentException(
					"Circular reference to " + name + " in dimensions."
				);
			} else if (!ns.containsKey(name)) {
				throw new IllegalArgumentException(
					"Undefined reference to " + name + " in dimensions."
				);
			} else {
				lockedThreads.add(currentThread);
				try { return ns.get(name).value(ns); }
				finally { lockedThreads.remove(currentThread); }
			}
		}
	}
	
	public static final class Pos extends Dimension {
		private final Dimension a;
		public Pos(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return +a.value(ns);
		}
	}
	
	public static final class Neg extends Dimension {
		private final Dimension a;
		public Neg(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return -a.value(ns);
		}
	}
	
	public static final class Add extends Dimension {
		private final Dimension a, b;
		public Add(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return a.value(ns) + b.value(ns);
		}
	}
	
	public static final class Sub extends Dimension {
		private final Dimension a, b;
		public Sub(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return a.value(ns) - b.value(ns);
		}
	}
	
	public static final class Mul extends Dimension {
		private final Dimension a, b;
		public Mul(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return a.value(ns) * b.value(ns);
		}
	}
	
	public static final class Div extends Dimension {
		private final Dimension a, b;
		public Div(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return a.value(ns) / b.value(ns);
		}
	}
	
	public static final class Mod extends Dimension {
		private final Dimension a, b;
		public Mod(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return a.value(ns) % b.value(ns);
		}
	}
	
	public static final class Pow extends Dimension {
		private final Dimension a, b;
		public Pow(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.pow(a.value(ns), b.value(ns));
		}
	}
	
	public static final class Root extends Dimension {
		private final Dimension a, b;
		public Root(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.pow(a.value(ns), 1.0 / b.value(ns));
		}
	}
	
	public static final class Log extends Dimension {
		private final Dimension a, b;
		public Log(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.log(a.value(ns)) / Math.log(b.value(ns));
		}
	}
	
	public static final class Abs extends Dimension {
		private final Dimension a;
		public Abs(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.abs(a.value(ns));
		}
	}
	
	public static final class Sqrt extends Dimension {
		private final Dimension a;
		public Sqrt(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.sqrt(a.value(ns));
		}
	}
	
	public static final class Cbrt extends Dimension {
		private final Dimension a;
		public Cbrt(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.cbrt(a.value(ns));
		}
	}
	
	public static final class Sin extends Dimension {
		private final Dimension a;
		public Sin(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.sin(Math.toRadians(a.value(ns)));
		}
	}
	
	public static final class Cos extends Dimension {
		private final Dimension a;
		public Cos(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.cos(Math.toRadians(a.value(ns)));
		}
	}
	
	public static final class Tan extends Dimension {
		private final Dimension a;
		public Tan(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.tan(Math.toRadians(a.value(ns)));
		}
	}
	
	public static final class Sinh extends Dimension {
		private final Dimension a;
		public Sinh(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.sinh(a.value(ns));
		}
	}
	
	public static final class Cosh extends Dimension {
		private final Dimension a;
		public Cosh(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.cosh(a.value(ns));
		}
	}
	
	public static final class Tanh extends Dimension {
		private final Dimension a;
		public Tanh(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.tanh(a.value(ns));
		}
	}
	
	public static final class Asin extends Dimension {
		private final Dimension a;
		public Asin(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.toDegrees(Math.asin(a.value(ns)));
		}
	}
	
	public static final class Acos extends Dimension {
		private final Dimension a;
		public Acos(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.toDegrees(Math.acos(a.value(ns)));
		}
	}
	
	public static final class Atan extends Dimension {
		private final Dimension a;
		public Atan(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.toDegrees(Math.atan(a.value(ns)));
		}
	}
	
	public static final class Ln extends Dimension {
		private final Dimension a;
		public Ln(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.log(a.value(ns));
		}
	}
	
	public static final class Exp extends Dimension {
		private final Dimension a;
		public Exp(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.exp(a.value(ns));
		}
	}
	
	public static final class Log10 extends Dimension {
		private final Dimension a;
		public Log10(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.log10(a.value(ns));
		}
	}
	
	public static final class Exp10 extends Dimension {
		private final Dimension a;
		public Exp10(Dimension a) { this.a = a; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.pow(10.0, a.value(ns));
		}
	}
	
	public static final class Min extends Dimension {
		private final Dimension a, b;
		public Min(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.min(a.value(ns), b.value(ns));
		}
	}
	
	public static final class Max extends Dimension {
		private final Dimension a, b;
		public Max(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.max(a.value(ns), b.value(ns));
		}
	}
	
	public static final class Hypot extends Dimension {
		private final Dimension a, b;
		public Hypot(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.hypot(a.value(ns), b.value(ns));
		}
	}
	
	public static final class Atan2 extends Dimension {
		private final Dimension a, b;
		public Atan2(Dimension a, Dimension b) { this.a = a; this.b = b; }
		@Override
		public double value(Map<String, Dimension> ns) {
			return Math.toDegrees(Math.atan2(a.value(ns), b.value(ns)));
		}
	}
}