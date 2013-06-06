package com.skytroniks.digibuttons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public class MainPanel extends JPanel {
  private static final long serialVersionUID = 6924976121549861760L;
  private static final String[] pressedNames = new String[] { "left", "right",
      "up", "down", "light-punch", "medium-punch", "heavy-punch", "light-kick",
      "medium-kick", "heavy-kick", "l-1", "l-2" };

  private BufferedImage background;
  private BufferedImage layoutUnpressed;
  private ArrayList<Overlay> overlays;
  private Settings settings;
  private BufferedImage[] pressed;
  private boolean[] isPressed;
  private int overlayIndex;

  public MainPanel() {
    Dimension windowSize = new Dimension(629, 365);
    this.setPreferredSize(windowSize);

    background = ResourceLoader.loadImage("background");
    layoutUnpressed = ResourceLoader.loadImage("layout_unpressed");
    settings = ResourceLoader.loadSettings();
    overlays = ResourceLoader.loadOverlays();
    overlayIndex = -1;

    if (settings.defaultOverlay != null) {
      for (int index = 0; index < overlays.size(); index++) {
        if (overlays.get(index).getName().compareToIgnoreCase(settings.defaultOverlay) == 0) {
          overlayIndex = index;
          break;
        }
      }
    }
    
    pressed = new BufferedImage[pressedNames.length];
    isPressed = new boolean[pressedNames.length];

    for (int index = 0; index < pressed.length; index++) {
      isPressed[index] = false;
      pressed[index] = ResourceLoader.loadImage(pressedNames[index]);
    }
  }

  public void processKey(KeyStroke hotKey, boolean keyDown) {
    boolean changed = false;

    for (int index = 0; index < settings.hotKey.length; index++) {
      if (settings.hotKey[index] == hotKey) {
        isPressed[index] = keyDown;
        changed = true;
      }
    }

    if (changed) {
      repaint();
    }
  }

  public void addOverlaysToMenu(JMenu menu) {
    final MainPanel parent = this;
    ButtonGroup buttonGroup = new ButtonGroup();
    JMenuItem item = new JRadioButtonMenuItem("No overlay", true);
    buttonGroup.add(item);
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        overlayIndex = -1;
        parent.repaint();
      }
    });
    menu.add(item);

    menu.addSeparator();

    for (int index = 0; index < overlays.size(); index++) {
      final int finalIndex = index;
      Overlay overlay = overlays.get(index);
      item = new JRadioButtonMenuItem(overlay.getName(), false);
      buttonGroup.add(item);
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          overlayIndex = finalIndex;
          parent.repaint();
        }
      });
      
      if (index == overlayIndex) {
        item.setSelected(true);
      }
      
      menu.add(item);
    }

    if (overlays.size() == 0) {
      item = new JMenuItem("Add PNG overlays to res/overlays/");
      item.setEnabled(false);
      menu.add(item);
    }
  }

  public Settings getSettings() {
    return settings;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    g.drawImage(background, 0, 0, null);
    g.drawImage(layoutUnpressed, 0, 0, null);

    for (int index = 0; index < pressed.length; index++) {
      if (!isPressed[index]) {
        continue;
      }

      g.drawImage(pressed[index], 0, 0, null);
    }

    if (overlayIndex > -1 && overlayIndex < overlays.size()) {
      g.drawImage(overlays.get(overlayIndex).getImage(), 0, 0, null);
    }
  }
}
