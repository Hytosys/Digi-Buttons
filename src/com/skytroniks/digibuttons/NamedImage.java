package com.skytroniks.digibuttons;

public class NamedImage {
  private String name;
  private CroppedImage image;

  public NamedImage(String _name, CroppedImage _image) {
    name = _name;
    image = _image;
  }

  public String getName() {
    return name;
  }

  public CroppedImage getImage() {
    return image;
  }
}
