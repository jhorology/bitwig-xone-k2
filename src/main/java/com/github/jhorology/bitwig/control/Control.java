package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.AbsoluteHardwarControlBindable;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher;
import com.bitwig.extension.controller.api.BooleanValue;
import com.bitwig.extension.controller.api.HardwareAction;
import com.bitwig.extension.controller.api.HardwareActionBindable;
import com.bitwig.extension.controller.api.HardwareActionBinding;
import com.bitwig.extension.controller.api.HardwareActionMatcher;
import com.bitwig.extension.controller.api.HardwareBinding;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;
import com.bitwig.extension.controller.api.RelativeHardwarControlBindable;
import com.bitwig.extension.controller.api.RelativeHardwareKnob;
import com.bitwig.extension.controller.api.RelativeHardwareValueMatcher;
import com.bitwig.extension.controller.api.SettableRangedValue;
import com.github.jhorology.bitwig.utils.Hook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base skeleton class for composite control element of MIDI control surface.
 *
 * @param <T> extended control type(recursive generics type).
 * @param <L> extended type of InternalHardwareLightState.
 */
public abstract class Control<T extends Control<T, L>, L extends InternalHardwareLightState> {
  private static final Logger LOG = LoggerFactory.getLogger(Control.class);

  private static final String BUTTON_SUFFIX = "_BTN";
  private static final String ABSOLUTE_SUFFIX = "_ABS";
  private static final String RELATIVE_SUFFIX = "_REL";
  private static final String LED_SUFFIX = "_LED";

  private final List<Runnable> pressedHandlers;
  private final List<Runnable> releasedHandlers;
  private final List<Consumer<Double>> absValueHandlers;
  private final List<HardwareBinding> bindings;
  private final List<Hook.Subscription> subscriptions;
  private final List<Hook.Subscription> internalSubscriptions;
  //
  private double absValue;
  private boolean pressed;

  private MidiOut midiOut;

  /** Control has behavior of button. */
  protected static final int BUTTON = 0x01;

  /** Control has behavior of encoder. */
  protected static final int ENCODER = 0x2;

  /** encoders value is relative. */
  protected static final int RELATIVE = 0x4;

  /** Control has behavior of LED. */
  protected static final int LED = 0x8;

  /** common control, exclude from layer management. */
  protected static final int COMMON = 0x10;

  /** An instance of HardwareButton. */
  protected HardwareButton button;

  /** An instance of AbsoluteHardwareKnob. */
  protected AbsoluteHardwareKnob absKnob;

  /** An instance of RelativeHardwareKnob. */
  protected RelativeHardwareKnob relKnob;

  /** An instance of MultiStateHardwareLight. */
  protected MultiStateHardwareLight led;

  private static class ConditionalActionBindable implements HardwareActionBindable {
    private final Supplier<Boolean> condition;
    private final HardwareActionBindable bindable;

    private ConditionalActionBindable(
        HardwareActionBindable bindable, Supplier<Boolean> condition) {
      this.condition = condition;
      this.bindable = bindable;
    }

    @Override
    public HardwareActionBinding addBinding(HardwareAction action) {
      return bindable.addBinding(action);
    }

    @Override
    public void invoke() {
      LOG.info("#### bindable action invoked condition={}", condition.get());
      if (condition.get()) {
        bindable.invoke();
      }
    }
  }

  /** Constructor. */
  protected Control() {
    this.pressedHandlers = new ArrayList<>();
    this.releasedHandlers = new ArrayList<>();
    this.absValueHandlers = new ArrayList<>();
    this.subscriptions = new ArrayList<>();
    this.internalSubscriptions = new ArrayList<>();
    this.bindings = new ArrayList<>();
  }

  /**
   * Returns a name of this control. name should be unique.
   *
   * @return name of this control
   */
  protected abstract String name();

  /**
   * Returns a default LED off state.
   *
   * @return LED state.
   */
  protected abstract L getDefaultLedOffState();

  /**
   * Returns a spec of this control.
   *
   * @return spec of this control
   */
  protected abstract int getSpec();

  /**
   * Create a matcher for button-pressed action.
   *
   * @param midiIn a Midi input port.
   * @return a matcher
   */
  protected abstract HardwareActionMatcher createPressedActionMatcher(MidiIn midiIn);

  /**
   * Create a matcher for button-released action.
   *
   * @param midiIn a Midi input port.
   * @return a matcher
   */
  protected abstract HardwareActionMatcher createReleasedActionMatcher(MidiIn midiIn);

  /**
   * Create a matcher for absolute encoder.
   *
   * @param midiIn a Midi input port.
   * @return a matcher
   */
  protected abstract AbsoluteHardwareValueMatcher createAbsValueMatcher(MidiIn midiIn);

  /**
   * Create a matcher for relative encoder.
   *
   * @param midiIn a Midi input port.
   * @return a matcher
   */
  protected abstract RelativeHardwareValueMatcher createRelValueMatcher(MidiIn midiIn);

  /** send MIDI messages to control LED. */
  protected abstract void sendLedState(L state, MidiOut midiOut);

  /**
   * initialize. this method should be called in inherited class at extension's start of lifecycle.
   *
   * @param surface
   * @param midiIn A MIDI input port
   * @param midiOut A MIDI output port, nullable when control doesn't have LED.
   */
  protected void initialize(HardwareSurface surface, MidiIn midiIn, MidiOut midiOut) {
    this.midiOut = midiOut;
    if (isButton()) {
      LOG.trace("[{}] control is button.", name());
      this.button = createButton(surface, midiIn);
    }
    if (isAbsoluteEncoder()) {
      LOG.trace("[{}] control is absolute encoder.", name());
      this.absKnob = createAbsoluteKnob(surface, midiIn);
    }
    if (isRelativeEncoder()) {
      LOG.trace("[{}] control is relative encoder.", name());
      this.relKnob = createRelativeKnob(surface, midiIn, button);
    }
    if (isLED()) {
      LOG.trace("[{}] control is LED.", name());
      this.led = createLed(surface);
    }
  }

  /** finalize. this method should be called in inherited class at extension's end of lifecycle. */
  protected void dispose() {
    clearBindings();
    clearInternalSubscriptions();
  }

  /**
   * Set a LED state for button pressed event.
   *
   * @param state the LED state when button pressed.
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onPressed(L state) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    return onPressed(() -> led.state().setValue(state));
  }

  /**
   * Add a handler for button pressed event.
   *
   * @param handler a handler
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onPressed(Runnable handler) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    pressedHandlers.add(handler);
    return (T) this;
  }

  /**
   * Add a binding for button-pressed-event.
   *
   * @param target bindable action
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onPressed(HardwareActionBindable target) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    bindings.add(target.addBinding(button.pressedAction()));
    return (T) this;
  }

  /**
   * Add a handler for button pressed event.
   *
   * @param condition execution condition of handler
   * @param handler a handler
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onPressed(Supplier<Boolean> condition, Runnable handler) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    pressedHandlers.add(
        () -> {
          if (condition.get()) handler.run();
        });
    return (T) this;
  }

  /**
   * Add a binding for button-pressed-event.
   *
   * @param condition execution condition of handler
   * @param target bindable action
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onPressed(Supplier<Boolean> condition, HardwareActionBindable target) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    pressedHandlers.add(
        () -> {
          if (condition.get()) target.invoke();
        });
    return (T) this;
  }

  /**
   * Set a LED state for button released event.
   *
   * @param state the LED state when button released.
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onReleased(L state) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    return onReleased(() -> led.state().setValue(state));
  }

  /**
   * Add a handler for button-released-event.
   *
   * @param handler A handler
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onReleased(Runnable handler) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support release-action.");
    }
    releasedHandlers.add(handler);
    return (T) this;
  }

  /**
   * Add a binding for button-released-event.
   *
   * @param target A bindable target
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onReleased(HardwareActionBindable target) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support release-action.");
    }
    bindings.add(target.addBinding(button.releasedAction()));
    return (T) this;
  }

  /**
   * Add a consumer for absolute value.
   *
   * @param handler A handler
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onAbsValue(Consumer<Double> handler) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    absValueHandlers.add(handler);
    return (T) this;
  }

  /**
   * Add a binding for absolute value.
   *
   * @param target A bindable target.
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onAbsValue(AbsoluteHardwarControlBindable target) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    bindings.add(target.addBinding(absKnob));
    return (T) this;
  }

  /**
   * Add a binding for absolute value.
   *
   * @param target A bindable target
   * @param min minimum value of range
   * @param max maximum value of range
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onAbsValue(AbsoluteHardwarControlBindable target, double min, double max) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    bindings.add(target.addBindingWithRange(absKnob, min, max));
    return (T) this;
  }

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onRelValue(RelativeHardwarControlBindable target) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBinding(relKnob));
    return (T) this;
  }

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target
   * @param sensitivity sensitivity
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onRelValue(RelativeHardwarControlBindable target, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithSensitivity(relKnob, sensitivity));
    return (T) this;
  }

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target.
   * @param sensitivity sensitivity
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onRelValue(SettableRangedValue target, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithSensitivity(relKnob, sensitivity));
    return (T) this;
  }

  /**
   * Add a binding for relative value.
   *
   * @param target A bindable target.
   * @param min minimum value of range
   * @param max maximum value of range
   * @param sensitivity sensitivity
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T onRelValue(SettableRangedValue target, double min, double max, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithRangeAndSensitivity(relKnob, min, max, sensitivity));
    return (T) this;
  }

  /**
   * Set the state of LED.
   *
   * @param state A state of LED
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T led(L state) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    led.state().setValue(state);
    return (T) this;
  }

  /**
   * Follow the LED state to subscribable boolean value.
   *
   * @param value subscribable boolean value
   * @param onState A LED state while value is true
   * @return this instance
   */
  public T led(BooleanValue value, L onState) {
    return led(value, onState, getDefaultLedOffState());
  }

  /**
   * Follow the LED state to subscribable boolean value.
   *
   * @param value subscribable boolean value
   * @param onState A LED state while value is true
   * @param offState A LED state while value is false
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public T led(BooleanValue value, L onState, L offState) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    subscriptions.add(Hook.subscribe(value, v -> led.state().setValue(v ? onState : offState)));
    return (T) this;
  }

  /**
   * Returns a pressed state of this control.
   *
   * @return true while pressing
   */
  public boolean isPressed() {
    return pressed;
  }

  /**
   * Returns a pressed state of this control.
   *
   * @return true while pressing
   */
  public boolean isReleased() {
    return !pressed;
  }

  /**
   * Returns a current encoder absolute-value of this control.
   *
   * @return last received value(normalized 0.0-1.0).
   */
  public double getAbsValue() {
    return absValue;
  }

  /** clear all handlers and binding. */
  public void clearBindings() {
    pressedHandlers.clear();
    releasedHandlers.clear();
    absValueHandlers.clear();
    Collections.reverse(bindings);
    bindings.forEach(HardwareBinding::removeBinding);
    bindings.clear();
    Collections.reverse(subscriptions);
    subscriptions.forEach(Hook.Subscription::unsubscribe);
    subscriptions.clear();
  }

  /**
   * Returns whether or not this control has button.
   *
   * @return true if has button
   */
  public boolean isButton() {
    return (getSpec() & BUTTON) != 0;
  }

  /**
   * Returns whether or not this control has encoder.
   *
   * @return true if has encoder
   */
  public boolean isEncoder() {
    return (getSpec() & ENCODER) != 0;
  }

  /**
   * Returns whether or not this control has absolute-encoder.
   *
   * @return true if has absolute encoder
   */
  public boolean isAbsoluteEncoder() {
    return isEncoder() && (getSpec() & RELATIVE) == 0;
  }

  /**
   * Returns whether or not this control has relative-encoder.
   *
   * @return true if has absolute encoder
   */
  public boolean isRelativeEncoder() {
    return isEncoder() && (getSpec() & RELATIVE) != 0;
  }

  /**
   * Returns whether or not this control has LED.
   *
   * @return true if has LED
   */
  public boolean isLED() {
    return (getSpec() & LED) != 0;
  }

  public boolean isCommon() {
    return (getSpec() & COMMON) != 0;
  }

  private HardwareButton createButton(HardwareSurface surface, MidiIn midiIn) {
    HardwareButton btn = surface.createHardwareButton(name() + BUTTON_SUFFIX);
    btn.pressedAction().setActionMatcher(createPressedActionMatcher(midiIn));
    btn.releasedAction().setActionMatcher(createReleasedActionMatcher(midiIn));
    internalSubscriptions.add(
        Hook.subscribe(
            btn.isPressed(),
            pressed -> {
              this.pressed = pressed;
              (pressed ? pressedHandlers : releasedHandlers).forEach(Runnable::run);
            }));
    return btn;
  }

  private AbsoluteHardwareKnob createAbsoluteKnob(HardwareSurface surface, MidiIn midiIn) {
    AbsoluteHardwareKnob knob = surface.createAbsoluteHardwareKnob(name() + ABSOLUTE_SUFFIX);
    if (knob != null) {
      knob.setHardwareButton(button);
    }
    knob.setAdjustValueMatcher(createAbsValueMatcher(midiIn));
    internalSubscriptions.add(
        Hook.subscribe(
            knob.value(),
            value -> {
              absValue = value;
              absValueHandlers.forEach(h -> h.accept(value));
            }));
    return knob;
  }

  private RelativeHardwareKnob createRelativeKnob(
      HardwareSurface surface, MidiIn midiIn, HardwareButton btn) {
    RelativeHardwareKnob knob = surface.createRelativeHardwareKnob(name() + RELATIVE_SUFFIX);
    if (btn != null) {
      knob.setHardwareButton(btn);
    }
    knob.setAdjustValueMatcher(createRelValueMatcher(midiIn));
    return knob;
  }

  @SuppressWarnings("unchecked")
  private MultiStateHardwareLight createLed(HardwareSurface surface) {
    MultiStateHardwareLight led = surface.createMultiStateHardwareLight(name() + LED_SUFFIX);
    led.state().onUpdateHardware(state -> sendLedState((L) state, midiOut));
    // TODO blinking
    return led;
  }

  private void clearInternalSubscriptions() {
    Collections.reverse(internalSubscriptions);
    internalSubscriptions.forEach(Hook.Subscription::unsubscribe);
    internalSubscriptions.clear();
  }
}
