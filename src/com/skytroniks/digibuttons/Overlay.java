package com.skytroniks.digibuttons;

import java.awt.image.BufferedImage;

public class Overlay {
  private String name;
  private BufferedImage image;
  
  public Overlay(String _name, BufferedImage _image) {
    name = _name;
    image = _image;
  }
  
  public String getName() {
    return name;
  }
  
  public BufferedImage getImage() {
    return image;
  }
}
