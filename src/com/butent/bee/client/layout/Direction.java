package com.butent.bee.client.layout;

/**
 * Contains available directions for layout elements creation.
 */

public enum Direction {
  NORTH, EAST, SOUTH, WEST, CENTER;

  public String brief() {
    return name().substring(0, 1);
  }

  public boolean isHorizontal() {
    return this == EAST || this == WEST;
  }

  public boolean isVertical() {
    return this == NORTH || this == SOUTH;
  }
}
