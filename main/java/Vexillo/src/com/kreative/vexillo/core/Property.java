package com.kreative.vexillo.core;

public enum Property {
	CIVIL_FLAG, STATE_FLAG, WAR_FLAG,
	CIVIL_ENSIGN, STATE_ENSIGN, WAR_ENSIGN,
	ALTERNATE, DE_FACTO, HISTORICAL, NO_FLAG,
	NORMAL, PROPOSAL, RECONSTRUCTION, REVERSE,
	SINISTER_HOIST, TWO_SIDED, VARIANT, VERTICAL;
	
	public static Property parse(String s) {
		s = s.replaceAll("[^A-Za-z0-9]+", "");
		if (s.equalsIgnoreCase("CF")) return CIVIL_FLAG;
		if (s.equalsIgnoreCase("SF")) return STATE_FLAG;
		if (s.equalsIgnoreCase("WF")) return WAR_FLAG;
		if (s.equalsIgnoreCase("CE")) return CIVIL_ENSIGN;
		if (s.equalsIgnoreCase("SE")) return STATE_ENSIGN;
		if (s.equalsIgnoreCase("WE")) return WAR_ENSIGN;
		for (Property value : values()) {
			String n = value.name().replaceAll("[^A-Za-z0-9]+", "");
			if (s.equalsIgnoreCase(n)) return value;
		}
		throw new IllegalArgumentException("Unknown property: " + s);
	}
	
	public int getCodePointGroup() {
		switch (this) {
		case CIVIL_FLAG: case STATE_FLAG: case WAR_FLAG:
		case CIVIL_ENSIGN: case STATE_ENSIGN: case WAR_ENSIGN:
			return 0xE000;
		default:
			return 0;
		}
	}
	
	public int getCodePoint() {
		switch (this) {
		case CIVIL_FLAG: return 0xE020;
		case STATE_FLAG: return 0xE010;
		case WAR_FLAG: return 0xE008;
		case CIVIL_ENSIGN: return 0xE004;
		case STATE_ENSIGN: return 0xE002;
		case WAR_ENSIGN: return 0xE001;
		case ALTERNATE: return 0xE040;
		case DE_FACTO: return 0xE041;
		case HISTORICAL: return 0xE042;
		case NO_FLAG: return 0xE043;
		case NORMAL: return 0xE044;
		case PROPOSAL: return 0xE045;
		case RECONSTRUCTION: return 0xE046;
		case REVERSE: return 0xE047;
		case SINISTER_HOIST: return 0xE048;
		case TWO_SIDED: return 0xE049;
		case VARIANT: return 0xE04A;
		case VERTICAL: return 0xE04B;
		default: return 0;
		}
	}
	
	@Override
	public String toString() {
		return name().replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
	}
}