package com.skytroniks.digibuttons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public class CroppedImage {
  private BufferedImage image;
  private Rectangle clip;
  private Dimension originalSize;
  private BufferedImage cache;
  private Rectangle cacheRect;

  public CroppedImage() {
    cache = null;
  }

  public void initialize(BufferedImage source, boolean crop) {
    final int width = source.getWidth();
    final int height = source.getHeight();

    if (!crop) {
      originalSize = new Dimension(width, height);
      clip = new Rectangle(width, height);
      image = source;

      return;
    }

    originalSize = new Dimension(width, height);

    int x0 = width;
    int x1 = 0;
    int y0 = height;
    int y1 = 0;

    int[] argb = source.getRGB(0, 0, width, height, null, 0, width);

    for (int x = 0; x < source.getWidth(); x++) {
      for (int y = 0; y < source.getHeight(); y++) {
        final int pixel = argb[y * width + x];
        final boolean opaque = (pixel & 0xFF000000) > 0;
        if (!opaque) {
          continue;
        }

        if (x < x0) {
          x0 = x;
        }

        if (x > x1) {
          x1 = x;
        }

        if (y < y0) {
          y0 = y;
        }

        if (y > y1) {
          y1 = y;
        }
      }
    }

    if (x1 > x0 && y1 > y0) {
      clip = new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
      image = source.getSubimage(x0, y0, clip.width, clip.height);
    } else {
      clip = new Rectangle(width, height);
      image = source;
    }
  }

  public Rectangle getClip() {
    return clip;
  }

  public BufferedImage getImage() {
    return image;
  }

  public Dimension getOriginalSize() {
    return originalSize;
  }

  public BufferedImage getCache() {
    return cache;
  }

  public Rectangle getCacheRect() {
    return cacheRect;
  }

  public void cache(Dimension panelSize) {
    if (image == null || panelSize.width == 0 || panelSize.height == 0) {
      cache = null;
      return;
    }

    final double scaleFactor = MainPanel.getScaleFactorToFit(originalSize,
        panelSize);

    final int sheetScaledWidth = (int) Math.round(originalSize.width
        * scaleFactor);
    final int sheetScaledHeight = (int) Math.round(originalSize.height
        * scaleFactor);
    final int midWidth = panelSize.width - 1 - sheetScaledWidth;
    final int midHeight = panelSize.height - 1 - sheetScaledHeight;
    final int offsetX = midWidth / 2;
    final int offsetY = midHeight / 2;
    final int scaledWidth = (int) Math.round(clip.getWidth() * scaleFactor);
    final int scaledHeight = (int) Math.round(clip.getHeight() * scaleFactor);
    final int scaledX = (int) Math.round(clip.getX() * scaleFactor);
    final int scaledY = (int) Math.round(clip.getY() * scaleFactor);
    final int x = offsetX + scaledX;
    final int y = offsetY + scaledY;

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();

    BufferedImage finalImage = gc.createCompatibleImage(scaledWidth,
        scaledHeight, Transparency.TRANSLUCENT);
    Graphics g = finalImage.getGraphics();

    g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);

    cache = finalImage;
    cacheRect = new Rectangle(x, y, scaledWidth, scaledHeight);
  }
}
