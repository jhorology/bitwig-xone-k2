package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;

public class XoneK2LedState extends InternalHardwareLightState {
  public static final Color OFF_COLOR = Color.fromRGB(0.5, 0.5, 0.5);
  public static final Color RED_COLOR = Color.fromRGB(1.0, 0.0, 0.0);
  public static final Color YELLOW_COLOR = Color.fromRGB(1.0, 1.0, 0.0);
  public static final Color GREEN_COLOR = Color.fromRGB(0.0, 1.0, 0.0);

  private static final double BLINK_ON_SEC = 0.2; // sec
  private static final double BLINK_OFF_SEC = 0.2; // sec

  /** LED off state. */
  public static final XoneK2LedState OFF =
      new XoneK2LedState("OFF", HardwareLightVisualState.createForColor(OFF_COLOR));
  /** LED red state. */
  public static final XoneK2LedState RED =
      new XoneK2LedState("RED", HardwareLightVisualState.createForColor(RED_COLOR));
  /** LED yellow state. */
  public static final XoneK2LedState YELLOW =
      new XoneK2LedState("YELLOW", HardwareLightVisualState.createForColor(YELLOW_COLOR));

  /** LED green state. */
  public static final XoneK2LedState GREEN =
      new XoneK2LedState("GREEN", HardwareLightVisualState.createForColor(GREEN_COLOR));

  /** LED red blinking state. */
  public static final XoneK2LedState RED_BLINK =
      new XoneK2LedState(
          "RED_BLINK",
          HardwareLightVisualState.createBlinking(
              RED_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  /** LED red blinking on beat. */
  public static final XoneK2LedState RED_BEAT =
      new XoneK2LedState(
          "RED_BEAT",
          HardwareLightVisualState.createBlinking(
              RED_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  /** LED yellow blinking state. */
  public static final XoneK2LedState YELLOW_BLINK =
      new XoneK2LedState(
          "YELLOW_BLINK",
          HardwareLightVisualState.createBlinking(
              YELLOW_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  /** LED yellow blinking on beat. */
  public static final XoneK2LedState YELLOW_BEAT =
      new XoneK2LedState(
          "YELLOW_BEAT",
          HardwareLightVisualState.createBlinking(
              YELLOW_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  /** LED green blinking state. */
  public static final XoneK2LedState GREEN_BLINK =
      new XoneK2LedState(
          "GREEN_BLINK",
          HardwareLightVisualState.createBlinking(
              GREEN_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  /** LED green blinking on beat. */
  public static final XoneK2LedState GREEN_BEAT =
      new XoneK2LedState(
          "GREEN_BEAT",
          HardwareLightVisualState.createBlinking(
              GREEN_COLOR, OFF_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC));

  private final String name;
  private final HardwareLightVisualState visualState;

  private XoneK2LedState(String name, HardwareLightVisualState visualState) {
    this.name = name;
    this.visualState = visualState;
  }

  @Override
  public HardwareLightVisualState getVisualState() {
    return visualState;
  }

  @Override
  public boolean equals(Object obj) {
    // all instances are static final
    return this == obj;
  }

  @Override
  public String toString() {
    return name;
  }
}
