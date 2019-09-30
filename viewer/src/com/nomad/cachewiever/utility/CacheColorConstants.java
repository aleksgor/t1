package com.nomad.cachewiever.utility;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

public class CacheColorConstants implements ColorConstants {

  public static Color listenerBackGround = new Color(null, 230, 230, 255);
  public static Color darkGray = new Color(null, 10, 10, 10);

  public static Color getOrangeColor(int i) {
    i = i > 17 ? 17 : i;
    return new Color(null, 255, 196, i * 15);
  }

  public static Color getCyanColor(int i) {
    i = i > 17 ? 17 : i;
    return new Color(null, i * 15, 196, 255);
  }
  
}
