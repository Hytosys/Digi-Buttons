package com.skytroniks.digibuttons;

import static java.awt.event.KeyEvent.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;

public class WindowsKeyMap {
  private static final Map<Integer, Integer> codeExceptions = new HashMap<Integer, Integer>() {
    private static final long serialVersionUID = -6345628075339651355L;

    {
      put(VK_INSERT, 0x2D);
      put(VK_DELETE, 0x2E);
      put(VK_ENTER, 0x0D);
      put(VK_COMMA, 0xBC);
      put(VK_PERIOD, 0xBE);
      put(VK_PLUS, 0xBB);
      put(VK_MINUS, 0xBD);
      put(VK_SLASH, 0xBF);
      put(VK_SEMICOLON, 0xBA);
      put(VK_PRINTSCREEN, 0x2C);
      put(VK_EQUALS, 0xBB);
    }
  };

  public static int getCode(KeyStroke keyCode) {
    Integer code = codeExceptions.get(keyCode.getKeyCode());
    if (code != null) {
      return code;
    } else {
      return keyCode.getKeyCode();
    }
  }

  public static boolean modifiersSatisfactory(KeyStroke keyCode,
      boolean modControl, boolean modShift, boolean modAlt) {
    if (keyCode != null) {
      if ((keyCode.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
        if (!modShift) {
          return false;
        }
      }

      if ((keyCode.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
        if (!modControl) {
          return false;
        }
      }

      if ((keyCode.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
        if (!modAlt) {
          return false;
        }
      }
    }

    return true;
  }
}
