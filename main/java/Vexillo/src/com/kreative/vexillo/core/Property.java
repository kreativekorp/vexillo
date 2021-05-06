package com.kreative.vexillo.core;

public enum Property {
	CIVIL_FLAG, STATE_FLAG, WAR_FLAG,
	CIVIL_ENSIGN, STATE_ENSIGN, WAR_ENSIGN,
	AIR_CIVIL_FLAG, AIR_STATE_FLAG, AIR_WAR_FLAG,
	EXT_CIVIL_FLAG, EXT_STATE_FLAG, EXT_WAR_FLAG,
	EXT_CIVIL_ENSIGN, EXT_STATE_ENSIGN, EXT_WAR_ENSIGN,
	ALTERNATE, DE_FACTO, HISTORICAL, NO_FLAG,
	NORMAL, PROPOSAL, RECONSTRUCTION, REVERSE,
	SINISTER_HOIST, TWO_SIDED, VARIANT, VERTICAL,
	AUTHORIZED, REVERSE_MIRRORED,
	REVERSE_EQUAL, REVERSE_UNKNOWN,
	VERTICAL_NORMAL, VERTICAL_ROTATED,
	VERTICAL_UNKNOWN, VERTICAL_INAPPLICABLE;
	
	public static Property parse(String s) {
		s = s.replaceAll("[^A-Za-z0-9]+", "");
		if (s.equalsIgnoreCase("CF")) return CIVIL_FLAG;
		if (s.equalsIgnoreCase("SF")) return STATE_FLAG;
		if (s.equalsIgnoreCase("WF")) return WAR_FLAG;
		if (s.equalsIgnoreCase("CE")) return CIVIL_ENSIGN;
		if (s.equalsIgnoreCase("SE")) return STATE_ENSIGN;
		if (s.equalsIgnoreCase("WE")) return WAR_ENSIGN;
		if (s.equalsIgnoreCase("ACF")) return AIR_CIVIL_FLAG;
		if (s.equalsIgnoreCase("ASF")) return AIR_STATE_FLAG;
		if (s.equalsIgnoreCase("AWF")) return AIR_WAR_FLAG;
		if (s.equalsIgnoreCase("XCF")) return EXT_CIVIL_FLAG;
		if (s.equalsIgnoreCase("XSF")) return EXT_STATE_FLAG;
		if (s.equalsIgnoreCase("XWF")) return EXT_WAR_FLAG;
		if (s.equalsIgnoreCase("XCE")) return EXT_CIVIL_ENSIGN;
		if (s.equalsIgnoreCase("XSE")) return EXT_STATE_ENSIGN;
		if (s.equalsIgnoreCase("XWE")) return EXT_WAR_ENSIGN;
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
		case AIR_CIVIL_FLAG: case AIR_STATE_FLAG: case AIR_WAR_FLAG:
		case EXT_CIVIL_FLAG: case EXT_STATE_FLAG: case EXT_WAR_FLAG:
		case EXT_CIVIL_ENSIGN: case EXT_STATE_ENSIGN: case EXT_WAR_ENSIGN:
			return 0xE200;
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
		case AIR_CIVIL_FLAG: return 0xE300;
		case AIR_STATE_FLAG: return 0xE280;
		case AIR_WAR_FLAG: return 0xE240;
		case EXT_CIVIL_FLAG: return 0xE220;
		case EXT_STATE_FLAG: return 0xE210;
		case EXT_WAR_FLAG: return 0xE208;
		case EXT_CIVIL_ENSIGN: return 0xE204;
		case EXT_STATE_ENSIGN: return 0xE202;
		case EXT_WAR_ENSIGN: return 0xE201;
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
		case VERTICAL: return 0xE04C;
		case AUTHORIZED: return 0xE04E;
		case REVERSE_MIRRORED: return 0xE061;
		case REVERSE_EQUAL: return 0xE062;
		case REVERSE_UNKNOWN: return 0xE063;
		case VERTICAL_NORMAL: return 0xE064;
		case VERTICAL_ROTATED: return 0xE065;
		case VERTICAL_UNKNOWN: return 0xE066;
		case VERTICAL_INAPPLICABLE: return 0xE067;
		default: return 0;
		}
	}
	
	@Override
	public String toString() {
		return name().replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
	}
}