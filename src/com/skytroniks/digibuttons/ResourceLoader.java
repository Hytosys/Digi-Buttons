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

  public static CroppedImage loadImage(String name, boolean crop) {
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
    
    CroppedImage croppedImage = new CroppedImage();
    croppedImage.initialize(image, crop);

    return croppedImage;
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

    String defaultBackground = properties.getProperty("default_background");
    if (defaultBackground != null) {
      settings.defaultBackground = defaultBackground;
    }

    return settings;
  }

  public static ArrayList<NamedImage> loadOverlays() {
    return loadNamedImages("overlay");
  }

  public static ArrayList<NamedImage> loadBackgrounds() {
    return loadNamedImages("background");
  }

  private static ArrayList<NamedImage> loadNamedImages(String type) {
    ArrayList<NamedImage> images = new ArrayList<NamedImage>();

    File folder = new File("./res/" + type + "s/");
    File[] files = folder.listFiles();
    if (files == null) {
      return images;
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
        System.out.println("Unable to load " + type + " " + filename);
        return images;
      }

      if (image != null) {
        CroppedImage croppedImage = new CroppedImage();
        croppedImage.initialize(image, false);
        
        images.add(new NamedImage(filename, croppedImage));
      }
    }

    return images;
  }
}
