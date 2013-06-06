package com.skytroniks.digibuttons;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;

public class MainWindow extends JFrame {
  private static final long serialVersionUID = -1579583724424467247L;

  private User32.HHOOK hook;
  private int modShift;
  private int modControl;
  private int modAlt;
  private volatile boolean quit;

  public MainWindow() {
    super("Digi-Buttons");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(true);

    final MainPanel mainPanel = new MainPanel();
    setContentPane(mainPanel);
    initializeMenuBar(mainPanel);
    pack();
    setLocationRelativeTo(null);

    modShift = 0;
    modControl = 0;
    modAlt = 0;
    quit = false;

    if (Platform.isWindows()) {
      new Thread(new Runnable() {
        public void run() {
          startWindowsKeyListener(mainPanel);
        }
      }).start();
    } else {
      initializeSwingKeyListener(mainPanel);
    }

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        quit = true;
      }
    });
  }

  private void startWindowsKeyListener(final MainPanel mainPanel) {
    User32.LowLevelKeyboardProc lpfn = new User32.LowLevelKeyboardProc() {
      public LRESULT callback(int nCode, WPARAM wParam,
          User32.KBDLLHOOKSTRUCT lParam) {
        final boolean isKeyDown = (wParam.intValue() == WinUser.WM_KEYDOWN)
            || (wParam.intValue() == WinUser.WM_SYSKEYDOWN);
        switch (lParam.vkCode) {
        case WinUser.VK_CONTROL:
          modControl = (modControl & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LCONTROL:
          modControl = (modControl & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RCONTROL:
          modControl = (modControl & 3) | (isKeyDown ? 4 : 0);
          break;

        case WinUser.VK_SHIFT:
          modShift = (modShift & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LSHIFT:
          modShift = (modShift & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RSHIFT:
          modShift = (modShift & 3) | (isKeyDown ? 4 : 0);
          break;

        case WinUser.VK_MENU:
          modAlt = (modAlt & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LMENU:
          modAlt = (modAlt & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RMENU:
          modAlt = (modAlt & 3) | (isKeyDown ? 4 : 0);
          break;

        default:
          for (KeyStroke hotKey : mainPanel.getSettings().hotKey) {
            final boolean keysMatch = lParam.vkCode == WindowsKeyMap
                .getCode(hotKey);

            if (!keysMatch) {
              continue;
            }

            final boolean modifiersSatisfactory = WindowsKeyMap
                .modifiersSatisfactory(hotKey, modControl > 0, modShift > 0,
                    modAlt > 0);

            if (!isKeyDown || modifiersSatisfactory) {
              mainPanel.processKey(hotKey, isKeyDown);
            }
          }
        }

        return User32.INSTANCE.CallNextHookEx(hook, nCode, wParam,
            lParam.getPointer());
      }
    };

    HMODULE module = Kernel32.INSTANCE.GetModuleHandle(null);
    hook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, lpfn,
        module, 0);

    if (hook == null) {
      return;
    }

    User32.MSG msg = new User32.MSG();

    while (!quit) {
      User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0);
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        // fine, don't sleep then
      }
    }

    User32.INSTANCE.UnhookWindowsHookEx(hook);
  }

  private void initializeSwingKeyListener(final MainPanel mainPanel) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent event) {
        processKeyEvent(event, true);
      }

      @Override
      public void keyReleased(KeyEvent event) {
        processKeyEvent(event, false);
      }

      private void processKeyEvent(KeyEvent event, boolean keyDown) {
        final Settings settings = mainPanel.getSettings();

        for (KeyStroke hotKey : settings.hotKey) {
          if (event.getKeyCode() != hotKey.getKeyCode()) {
            continue;
          }

          final int hotKeyModifiers = hotKey.getModifiers();
          final int eventModifiers = event.getModifiers();
          final boolean control = (eventModifiers & KeyEvent.CTRL_DOWN_MASK | eventModifiers
              & KeyEvent.CTRL_MASK) > 0;
          final boolean shift = (eventModifiers & KeyEvent.SHIFT_DOWN_MASK | eventModifiers
              & KeyEvent.SHIFT_MASK) > 0;
          final boolean alt = (eventModifiers & KeyEvent.ALT_DOWN_MASK | eventModifiers
              & KeyEvent.ALT_MASK) > 0;

          final boolean ignoreModifiers = !keyDown || hotKeyModifiers == 0;
          final boolean modifiersMatch = WindowsKeyMap.modifiersSatisfactory(
              hotKey, control, shift, alt);

          if (ignoreModifiers || modifiersMatch) {
            mainPanel.processKey(hotKey, keyDown);
          }
        }
      }
    });
  }

  private void initializeMenuBar(final MainPanel mainPanel) {
    JMenuBar menuBar = new JMenuBar();

    JMenu overlayMenu = new JMenu("Overlay");
    menuBar.add(overlayMenu);
    mainPanel.addOverlaysToMenu(overlayMenu);

    JMenu backgroundMenu = new JMenu("Background");
    menuBar.add(backgroundMenu);
    mainPanel.addBackgroundsToMenu(backgroundMenu);

    setJMenuBar(menuBar);
  }
}
