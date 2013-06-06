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

  private BufferedImage layoutUnpressed;
  private ArrayList<NamedImage> overlays;
  private ArrayList<NamedImage> backgrounds;
  private Settings settings;
  private BufferedImage[] pressed;
  private boolean[] isPressed;
  private int overlayIndex;
  private int backgroundIndex;

  public MainPanel() {
    Dimension windowSize = new Dimension(629, 365);
    this.setPreferredSize(windowSize);

    layoutUnpressed = ResourceLoader.loadImage("layout_unpressed");
    settings = ResourceLoader.loadSettings();
    overlays = ResourceLoader.loadOverlays();
    backgrounds = ResourceLoader.loadBackgrounds();
    overlayIndex = -1;
    backgroundIndex = -1;

    if (settings.defaultOverlay != null) {
      for (int index = 0; index < overlays.size(); index++) {
        if (overlays.get(index).getName()
            .compareToIgnoreCase(settings.defaultOverlay) == 0) {
          overlayIndex = index;
          break;
        }
      }
    }

    if (settings.defaultBackground != null) {
      for (int index = 0; index < backgrounds.size(); index++) {
        if (backgrounds.get(index).getName()
            .compareToIgnoreCase(settings.defaultBackground) == 0) {
          backgroundIndex = index;
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
    addNamedImagesToMenu(menu, "overlay");
  }

  public void addBackgroundsToMenu(JMenu menu) {
    addNamedImagesToMenu(menu, "background");
  }

  private void addNamedImagesToMenu(JMenu menu, final String type) {
    final MainPanel parent = this;

    final ArrayList<NamedImage> images;
    if (type.compareTo("overlay") == 0) {
      images = overlays;
    } else if (type.compareTo("background") == 0) {
      images = backgrounds;
    } else {
      images = new ArrayList<NamedImage>();
    }

    final int imageIndex;
    if (type.compareTo("overlay") == 0) {
      imageIndex = overlayIndex;
    } else if (type.compareTo("background") == 0) {
      imageIndex = backgroundIndex;
    } else {
      imageIndex = -1;
    }

    ButtonGroup buttonGroup = new ButtonGroup();
    JMenuItem item = new JRadioButtonMenuItem("No " + type, true);
    buttonGroup.add(item);
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        if (type.compareTo("overlay") == 0) {
          overlayIndex = -1;
        } else if (type.compareTo("background") == 0) {
          backgroundIndex = -1;
        }
        parent.repaint();
      }
    });
    menu.add(item);

    menu.addSeparator();

    for (int index = 0; index < images.size(); index++) {
      final int finalIndex = index;
      NamedImage image = images.get(index);
      item = new JRadioButtonMenuItem(image.getName(), false);
      buttonGroup.add(item);
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          if (type.compareTo("overlay") == 0) {
            overlayIndex = finalIndex;
          } else if (type.compareTo("background") == 0) {
            backgroundIndex = finalIndex;
          }
          parent.repaint();
        }
      });

      if (index == imageIndex) {
        item.setSelected(true);
      }

      menu.add(item);
    }

    if (images.size() == 0) {
      item = new JMenuItem("Add PNG " + type + "s to res/" + type + "s/");
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

    if (backgroundIndex > -1 && backgroundIndex < backgrounds.size()) {
      final BufferedImage image = backgrounds.get(backgroundIndex).getImage();
      drawScaledImage(g, image);
    }

    drawScaledImage(g, layoutUnpressed);

    for (int index = 0; index < pressed.length; index++) {
      if (!isPressed[index]) {
        continue;
      }

      drawScaledImage(g, pressed[index]);
    }

    if (overlayIndex > -1 && overlayIndex < overlays.size()) {
      final BufferedImage image = overlays.get(overlayIndex).getImage();
      drawScaledImage(g, image);
    }
  }

  private static double getScaleFactor(int masterSize, int targetSize) {
    return (double) targetSize / (double) masterSize;
  }

  private static double getScaleFactorToFit(Dimension masterSize,
      Dimension targetSize) {
    double scaleWidth = getScaleFactor(masterSize.width, targetSize.width);
    double scaleHeight = getScaleFactor(masterSize.height, targetSize.height);

    return Math.min(scaleHeight, scaleWidth);
  }

  private static void drawScaledImage(Graphics g, BufferedImage image) {
    Dimension panelSize = g.getClipBounds().getSize();
    Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());

    final double scaleFactor = getScaleFactorToFit(imageSize, panelSize);
    final int scaledWidth = (int) Math.round(image.getWidth() * scaleFactor);
    final int scaledHeight = (int) Math.round(image.getHeight() * scaleFactor);
    final int width = panelSize.width - 1 - scaledWidth;
    final int height = panelSize.height - 1 - scaledHeight;
    final int x = width / 2;
    final int y = height / 2;

    g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
  }
}
