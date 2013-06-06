package com.skytroniks.digibuttons;

import javax.swing.UIManager;

public class DigiButtons {
  public static void main(String[] args) {
    // Mac OS X particulars
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "Digi-Buttons");

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable exception) {
      // fail silently
    }

    MainWindow window = new MainWindow();
    window.setVisible(true);
  }
}
