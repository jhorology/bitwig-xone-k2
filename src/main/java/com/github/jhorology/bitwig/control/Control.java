package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.AbsoluteHardwarControlBindable;
import com.bitwig.extension.controller.api.BooleanValue;
import com.bitwig.extension.controller.api.HardwareActionBindable;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.RelativeHardwarControlBindable;
import com.bitwig.extension.controller.api.SettableRangedValue;
import java.util.function.Consumer;

/** A interface for composite control element of MIDI control surface. */
public interface Control {
  static final int BUTTON = 0x01;
  static final int ENCODER = 0x2;
  static final int RELATIVE = 0x4;
  static final int LED = 0x8;

  /**
   * Add a handler for button pressed event.
   *
   * @param handler a handler
   * @return this instance
   */
  Control onPressed(Runnable handler);

  /**
   * Add a handler for button pressed event.
   *
   * @param handler a handler
   * @param onState led state while pressing
   * @param <T> extends type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control onPressed(Runnable handler, T onState);

  /**
   * Add a handler for button pressed event.
   *
   * @param handler A handler
   * @param onState led state while pressing
   * @param offState led state while not pressing
   * @param <T> extends type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control onPressed(Runnable handler, T onState, T offState);

  /**
   * Add a binding for button-pressed-event.
   *
   * @param target bindable action
   * @return this instance
   */
  Control onPressed(HardwareActionBindable target);

  /**
   * Add a binding for button-pressed-event.
   *
   * @param target bindable action
   * @param onState led state while pressing
   * @param <T> extended type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control onPressed(
      HardwareActionBindable target, T onState);

  /**
   * Add a binding for button-pressed-event.
   *
   * @param target bindable action
   * @param onState led state while pressing
   * @param offState led state while not pressing
   * @param <T> extends type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control onPressed(
      HardwareActionBindable target, T onState, T offState);

  /**
   * Add a handler for button-released-event.
   *
   * @param handler A handler
   * @return this instance
   */
  Control onReleased(Runnable handler);

  /**
   * Add a binding for button-released-event.
   *
   * @param target A bindable target
   * @return this instance
   */
  Control onReleased(HardwareActionBindable target);

  /**
   * Add a consumer for absolute value.
   *
   * @param consumer A handler
   * @return this instance
   */
  Control onAbsValue(Consumer<Double> consumer);

  /**
   * Add a binding for absolute value.
   *
   * @param target A bindable target.
   * @return this instance
   */
  Control onAbsValue(AbsoluteHardwarControlBindable target);

  /**
   * Add a binding for absolute value.
   *
   * @param target A bindable target
   * @param min minimum value of range
   * @param max maximum value of range
   * @return this instance
   */
  Control onAbsValue(AbsoluteHardwarControlBindable target, double min, double max);

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target
   * @return this instance
   */
  Control onRelValue(RelativeHardwarControlBindable target);

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target
   * @param sensitivity sensitivity
   * @return this instance
   */
  Control onRelValue(RelativeHardwarControlBindable target, double sensitivity);

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target.
   * @param sensitivity sensitivity
   * @return this instance
   */
  Control onRelValue(SettableRangedValue target, double sensitivity);

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target.
   * @param min minimum value of range
   * @param max maximum value of range
   * @param sensitivity sensitivity
   * @return this instance
   */
  Control onRelValue(SettableRangedValue target, double min, double max, double sensitivity);

  /**
   * Set the state of LED.
   *
   * @param state A state of LED
   * @param <T> extended type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control led(T state);

  /**
   * Follow the LED state to subscribable boolean value.
   *
   * @param value subscribable boolean value
   * @param onState A LED state while value is true
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control led(BooleanValue value, T onState);

  /**
   * Follow the LED state to subscribable boolean value.
   *
   * @param value subscribable boolean value
   * @param onState A LED state while value is true
   * @param offState A LED state while value is false
   * @param <T> extended type of InternalHardwareLightState
   * @return this instance
   */
  <T extends InternalHardwareLightState> Control led(BooleanValue value, T onState, T offState);

  /**
   * Returns a spec of this control.
   *
   * @return spec of this control
   */
  int getSpec();

  /**
   * Returns a pressed state of this control.
   *
   * @return true while pressing
   */
  boolean isPressed();

  /**
   * Returns a current encoder absolute-value of this control.
   *
   * @return last received value(normalized 0.0-1.0).
   */
  double getAbsValue();

  /** clear all handlers and binding. */
  void clearBindings();

  /**
   * Returns whether or not this control has button.
   *
   * @return true if has button
   */
  default boolean isButton() {
    return (getSpec() & BUTTON) != 0;
  }

  /**
   * Returns whether or not this control has encoder.
   *
   * @return true if has encoder
   */
  default boolean isEncoder() {
    return (getSpec() & ENCODER) != 0;
  }

  /**
   * Returns whether or not this control has absolute-encoder.
   *
   * @return true if has absolute encoder
   */
  default boolean isAbsoluteEncoder() {
    return isEncoder() && (getSpec() & RELATIVE) == 0;
  }

  /**
   * Returns whether or not this control has relative-encoder.
   *
   * @return true if has absolute encoder
   */
  default boolean isRelativeEncoder() {
    return isEncoder() && (getSpec() & RELATIVE) != 0;
  }

  /**
   * Returns whether or not this control has LED.
   *
   * @return true if has LED
   */
  default boolean isLED() {
    return (getSpec() & LED) != 0;
  }
}
