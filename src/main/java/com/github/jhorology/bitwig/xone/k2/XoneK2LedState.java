package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;

public class XoneK2LedState extends InternalHardwareLightState {
  private static final Color GRAY_COLOR = Color.fromRGB(0.5, 0.5, 0.5);
  private static final Color RED_COLOR = Color.fromRGB(1.0, 0.0, 0.0);
  private static final Color YELLOW_COLOR = Color.fromRGB(1.0, 1.0, 0.0);
  private static final Color GREEN_COLOR = Color.fromRGB(0.0, 1.0, 0.0);
  private static final double BLINK_ON_SEC = 0.2; // sec
  private static final double BLINK_OFF_SEC = 0.2; // sec

  /** LED off state. */
  public static final XoneK2LedState OFF =
      new XoneK2LedState("OFF", HardwareLightVisualState.createForColor(GRAY_COLOR));
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
              RED_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          RED);

  /** LED red blinking on beat. */
  public static final XoneK2LedState RED_BEAT =
      new XoneK2LedState(
          "RED_BEAT",
          HardwareLightVisualState.createBlinking(
              RED_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          RED);

  /** LED yellow blinking state. */
  public static final XoneK2LedState YELLOW_BLINK =
      new XoneK2LedState(
          "YELLOW_BLINK",
          HardwareLightVisualState.createBlinking(
              YELLOW_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          YELLOW);

  /** LED yellow blinking on beat. */
  public static final XoneK2LedState YELLOW_BEAT =
      new XoneK2LedState(
          "YELLOW_BEAT",
          HardwareLightVisualState.createBlinking(
              YELLOW_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          YELLOW);

  /** LED green blinking state. */
  public static final XoneK2LedState GREEN_BLINK =
      new XoneK2LedState(
          "GREEN_BLINK",
          HardwareLightVisualState.createBlinking(
              GREEN_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          GREEN);

  /** LED green blinking on beat. */
  public static final XoneK2LedState GREEN_BEAT =
      new XoneK2LedState(
          "GREEN_BEAT",
          HardwareLightVisualState.createBlinking(
              GREEN_COLOR, GRAY_COLOR, BLINK_ON_SEC, BLINK_OFF_SEC),
          GREEN);

  private final String name;
  private final HardwareLightVisualState visualState;
  private final XoneK2LedState blinkOnState;

  private XoneK2LedState(String name, HardwareLightVisualState visualState) {
    this.name = name;
    this.visualState = visualState;
    this.blinkOnState = null;
  }

  private XoneK2LedState(
      String name, HardwareLightVisualState visualState, XoneK2LedState blinkOnState) {
    this.name = name;
    this.visualState = visualState;
    this.blinkOnState = blinkOnState;
  }

  public XoneK2LedState getBlinkOnState() {
    return blinkOnState;
  }

  public boolean isBlinkState() {
    return blinkOnState != null;
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
