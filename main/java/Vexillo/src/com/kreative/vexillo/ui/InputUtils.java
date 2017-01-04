package com.kreative.vexillo.ui;

import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class InputUtils {
	public static final int META_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	public static final int ALT_MASK = (META_MASK == InputEvent.ALT_MASK) ? InputEvent.CTRL_MASK : InputEvent.ALT_MASK;
	public static final int SHIFT_MASK = InputEvent.SHIFT_MASK;
	public static final int META_ALT_MASK = META_MASK | ALT_MASK;
	public static final int META_SHIFT_MASK = META_MASK | SHIFT_MASK;
	public static final int META_ALT_SHIFT_MASK = META_MASK | ALT_MASK | SHIFT_MASK;
}