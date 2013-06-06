package com.skytroniks.digibuttons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.KeyStroke;

public class ResourceLoader {
  private static final String[] hotKeyNames = new String[] { "left", "right",
      "up", "down", "light_punch", "medium_punch", "heavy_punch", "light_kick",
      "medium_kick", "heavy_kick", "l_1", "l_2" };

  public static BufferedImage loadImage(String name) {
    BufferedImage image = null;

    InputStream stream;

    try {
      stream = (InputStream) new FileInputStream("./res/" + name + ".png");
    } catch (FileNotFoundException exception) {
      stream = ResourceLoader.class.getResourceAsStream(name + ".png");
    }

    if (stream == null) {
      System.out.println("Unable to load " + name);
      return null;
    }

    try {
      image = ImageIO.read(stream);
    } catch (IOException exception) {
      System.out.println("Unable to load " + name);
      return null;
    }

    try {
      stream.close();
    } catch (IOException exception) {
      // ignore, might just be a package resource
    }

    return image;
  }

  public static Settings loadSettings() {
    Settings settings = new Settings();
    Properties properties = new Properties();

    try {
      FileInputStream stream = new FileInputStream("./res/config.properties");
      properties.load(stream);
    } catch (FileNotFoundException exception) {
      return settings;
    } catch (IOException exception) {
      System.out.println("Properties file invalid");
      return settings;
    }

    for (int index = 0; index < hotKeyNames.length; index++) {
      String name = hotKeyNames[index];
      String setting = properties.getProperty(name);
      if (setting == null) {
        continue;
      }

      KeyStroke keyStroke = KeyStroke.getKeyStroke(setting);
      if (keyStroke == null) {
        System.out.println("Hotkey setting for " + name + " is invalid");
        continue;
      }

      settings.hotKey[index] = keyStroke;
    }
    
    String defaultOverlay = properties.getProperty("default_overlay");
    if (defaultOverlay != null) {
      settings.defaultOverlay = defaultOverlay;
    }

    return settings;
  }

  public static ArrayList<Overlay> loadOverlays() {
    ArrayList<Overlay> overlays = new ArrayList<Overlay>();

    File folder = new File("./res/overlays/");
    File[] files = folder.listFiles();
    if (files == null) {
      return overlays;
    }

    for (File file : files) {
      if (!file.isFile() || !file.canRead()) {
        continue;
      }

      String filename = file.getName();

      if (!filename.toLowerCase().endsWith(".png")) {
        continue;
      }

      BufferedImage image = null;

      try {
        image = ImageIO.read(file);
      } catch (IOException exception) {
        System.out.println("Unable to load overlay " + filename);
        return overlays;
      }

      if (image != null) {
        overlays.add(new Overlay(filename, image));
      }
    }

    return overlays;
  }
}
