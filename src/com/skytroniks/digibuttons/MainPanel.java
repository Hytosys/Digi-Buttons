package com.skytroniks.digibuttons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
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

  private CroppedImage layoutUnpressed;
  private ArrayList<NamedImage> overlays;
  private ArrayList<NamedImage> backgrounds;
  private Settings settings;
  private CroppedImage[] pressed;
  private boolean[] isPressed;
  private int overlayIndex;
  private int backgroundIndex;
  private BufferedImage backgroundCache;
  private BufferedImage overlayCache;
  private Dimension previousPanelSize;

  public MainPanel() {
    Dimension windowSize = new Dimension(629, 365);
    this.setPreferredSize(windowSize);

    layoutUnpressed = ResourceLoader.loadImage("layout_unpressed", false);
    settings = ResourceLoader.loadSettings();
    overlays = ResourceLoader.loadOverlays();
    backgrounds = ResourceLoader.loadBackgrounds();
    overlayIndex = -1;
    backgroundIndex = -1;
    backgroundCache = null;
    overlayCache = null;
    previousPanelSize = new Dimension(0, 0);

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

    pressed = new CroppedImage[pressedNames.length];
    isPressed = new boolean[pressedNames.length];

    for (int index = 0; index < pressed.length; index++) {
      isPressed[index] = false;
      pressed[index] = ResourceLoader.loadImage(pressedNames[index], true);
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
          parent.cacheOverlay();
        } else if (type.compareTo("background") == 0) {
          backgroundIndex = -1;
          parent.cacheBackground();
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
            parent.cacheOverlay();
          } else if (type.compareTo("background") == 0) {
            backgroundIndex = finalIndex;
            parent.cacheBackground();
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
    final Dimension size = g.getClipBounds().getSize();

    if (previousPanelSize.width != size.width
        || previousPanelSize.height != size.height) {
      cacheBackground();
      cacheOverlay();
      
      for (int index = 0; index < pressed.length; index++) {
        pressed[index].cache(size);
      }
    }

    if (backgroundCache != null) {
      g.drawImage(backgroundCache, 0, 0, null);
    }

    for (int index = 0; index < pressed.length; index++) {
      if (!isPressed[index]) {
        continue;
      }

      BufferedImage cache = pressed[index].getCache();
      Point location = pressed[index].getCacheRect().getLocation();
      g.drawImage(cache, location.x, location.y, null);
    }

    if (overlayCache != null) {
      g.drawImage(overlayCache, 0, 0, null);
    }
  }

  private void cacheBackground() {
    final Dimension size = this.getSize();

    if (size.width == 0 || size.height == 0) {
      backgroundCache = null;
      return;
    }

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();

    BufferedImage finalImage = gc.createCompatibleImage(size.width,
        size.height, Transparency.TRANSLUCENT);
    
    Graphics g = finalImage.getGraphics();

    if (backgroundIndex > -1 && backgroundIndex < backgrounds.size()) {
      final CroppedImage croppedImage = backgrounds.get(backgroundIndex)
          .getImage();
      final BufferedImage image = croppedImage.getImage();
      drawScaledImage(size, g, image);
    }

    drawScaledImage(size, g, layoutUnpressed.getImage());

    backgroundCache = finalImage;
  }

  private void cacheOverlay() {
    final Dimension size = this.getSize();

    if (size.width == 0 || size.height == 0) {
      overlayCache = null;
      return;
    }

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();

    BufferedImage finalImage = gc.createCompatibleImage(size.width,
        size.height, Transparency.TRANSLUCENT);
    Graphics g = finalImage.getGraphics();

    if (overlayIndex > -1 && overlayIndex < overlays.size()) {
      final CroppedImage croppedImage = overlays.get(overlayIndex).getImage();
      final BufferedImage image = croppedImage.getImage();
      drawScaledImage(size, g, image);
      overlayCache = finalImage;
    } else {
      overlayCache = null;
    }
  }

  public static double getScaleFactor(int masterSize, int targetSize) {
    return (double) targetSize / (double) masterSize;
  }

  public static double getScaleFactorToFit(Dimension masterSize,
      Dimension targetSize) {
    double scaleWidth = getScaleFactor(masterSize.width, targetSize.width);
    double scaleHeight = getScaleFactor(masterSize.height, targetSize.height);

    return Math.min(scaleHeight, scaleWidth);
  }

  public static void drawScaledImage(Dimension size, Graphics g,
      BufferedImage image) {
    final Dimension originalSize = new Dimension(image.getWidth(),
        image.getHeight());
    final double scaleFactor = getScaleFactorToFit(originalSize, size);

    final int scaledWidth = (int) Math.round(originalSize.width * scaleFactor);
    final int scaledHeight = (int) Math
        .round(originalSize.height * scaleFactor);
    final int midWidth = size.width - 1 - scaledWidth;
    final int midHeight = size.height - 1 - scaledHeight;
    final int x = midWidth / 2;
    final int y = midHeight / 2;

    g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
  }
}
