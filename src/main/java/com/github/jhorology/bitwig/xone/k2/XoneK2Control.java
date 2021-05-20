package com.github.jhorology.bitwig.xone.k2;

import static com.github.jhorology.bitwig.xone.k2.XoneK2LedState.*;

import com.bitwig.extension.controller.api.*;
import com.github.jhorology.bitwig.control.Control;
import com.github.jhorology.bitwig.utils.Hook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum XoneK2Control implements Control {
  CH0_ENC0(BUTTON | ENCODER | RELATIVE | LED, 0x34, 0x0),
  CH1_ENC0(BUTTON | ENCODER | RELATIVE | LED, 0x35, 0x1),
  CH2_ENC0(BUTTON | ENCODER | RELATIVE | LED, 0x36, 0x2),
  CH3_ENC0(BUTTON | ENCODER | RELATIVE | LED, 0x37, 0x3),

  CH0_ENC1(BUTTON | ENCODER | LED, 0x30, 0x4),
  CH1_ENC1(BUTTON | ENCODER | LED, 0x31, 0x5),
  CH2_ENC1(BUTTON | ENCODER | LED, 0x32, 0x6),
  CH3_ENC1(BUTTON | ENCODER | LED, 0x33, 0x7),

  CH0_ENC2(BUTTON | ENCODER | LED, 0x2C, 0x8),
  CH1_ENC2(BUTTON | ENCODER | LED, 0x2D, 0x9),
  CH2_ENC2(BUTTON | ENCODER | LED, 0x2E, 0xA),
  CH3_ENC2(BUTTON | ENCODER | LED, 0x2F, 0xB),

  CH0_ENC3(BUTTON | ENCODER | LED, 0x28, 0xC),
  CH1_ENC3(BUTTON | ENCODER | LED, 0x29, 0xD),
  CH2_ENC3(BUTTON | ENCODER | LED, 0x2A, 0xE),
  CH3_ENC3(BUTTON | ENCODER | LED, 0x2B, 0xF),

  CH0_FADER(ENCODER, -1, 0x10),
  CH1_FADER(ENCODER, -1, 0x11),
  CH2_FADER(ENCODER, -1, 0x12),
  CH3_FADER(ENCODER, -1, 0x13),

  A(BUTTON | LED, 0x24, -1),
  B(BUTTON | LED, 0x25, -1),
  C(BUTTON | LED, 0x26, -1),
  D(BUTTON | LED, 0x27, -1),

  E(BUTTON | LED, 0x20, -1),
  F(BUTTON | LED, 0x21, -1),
  G(BUTTON | LED, 0x22, -1),
  H(BUTTON | LED, 0x23, -1),

  I(BUTTON | LED, 0x1C, -1),
  J(BUTTON | LED, 0x1D, -1),
  K(BUTTON | LED, 0x1E, -1),
  L(BUTTON | LED, 0x1F, -1),

  M(BUTTON | LED, 0x18, -1),
  N(BUTTON | LED, 0x19, -1),
  O(BUTTON | LED, 0x1A, -1),
  P(BUTTON | LED, 0x1B, -1),

  LAYER(BUTTON | LED, 0xC, -1),

  NAV1(BUTTON | ENCODER | RELATIVE, 0xD, 0x14),
  NAV2(BUTTON | ENCODER | RELATIVE, 0xE, 0x15),

  SHIFT(BUTTON | LED, 0xF, -1);

  private static final XoneK2Control[][] ENCODERS = {
    {CH0_ENC0, CH1_ENC0, CH2_ENC0, CH3_ENC0},
    {CH0_ENC1, CH1_ENC1, CH2_ENC1, CH3_ENC1},
    {CH0_ENC2, CH1_ENC2, CH2_ENC2, CH3_ENC2},
    {CH0_ENC3, CH1_ENC3, CH2_ENC3, CH3_ENC3}
  };

  private static final XoneK2Control[] FADERS = {CH0_FADER, CH1_FADER, CH2_FADER, CH3_FADER};

  private static final XoneK2Control[][] GRID_BUTTONS = {
    {A, B, C, D},
    {E, F, G, H},
    {I, J, K, L},
    {M, N, O, P}
  };

  private static final Logger LOG = LoggerFactory.getLogger(XoneK2Control.class);
  // TODO MIDI channel should be configurable.
  private static final int MIDI_CH = 0;
  private static final String BUTTON_SUFFIX = "_BTN";
  private static final String ABSOLUTE_SUFFIX = "_ABS";
  private static final String RELATIVE_SUFFIX = "_REL";
  private static final String LED_SUFFIX = "_LED";
  private static final int RELATIVE_VALUE_PER_ROTAION = 30;
  private MidiOut midiOut;
  private HardwareButton button;
  private AbsoluteHardwareKnob absKnob;
  private RelativeHardwareKnob relKnob;
  private MultiStateHardwareLight led;
  private final List<Runnable> pressedHandlers;
  private final List<Runnable> releasedHandlers;
  private final List<Consumer<Double>> absValueHandlers;
  private final List<HardwareBinding> bindings;
  private final List<Hook.Subscription> subscriptions;
  private final int spec;
  private final int note;
  private final int cc;
  private double absValue;
  private boolean pressed;

  /**
   * Initialize.
   *
   * @param surface
   * @param midiIn
   * @param midiOut
   */
  public static void init(HardwareSurface surface, MidiIn midiIn, MidiOut midiOut) {
    Stream.of(values()).forEach(e -> e._init(surface, midiIn, midiOut));
  }

  /** de-initialize */
  public static void exit() {
    Stream.of(values()).forEach(XoneK2Control::clearBindings);
  }

  /**
   * Constructor.
   *
   * @param note Note number of button, LED
   * @param cc Control change number of knob or fader
   */
  private XoneK2Control(int spec, int note, int cc) {
    this.spec = spec;
    this.note = note;
    this.cc = cc;
    this.pressedHandlers = new ArrayList<>();
    this.releasedHandlers = new ArrayList<>();
    this.absValueHandlers = new ArrayList<>();
    this.subscriptions = new ArrayList<>();
    this.bindings = new ArrayList<>();
  }

  private void _init(HardwareSurface surface, MidiIn midiIn, MidiOut midiOut) {
    LOG.trace("initializing control [{}].", name());
    this.midiOut = midiOut;

    if (isButton()) {
      button = createButton(surface, midiIn);
    }
    if (isAbsoluteEncoder()) {
      absKnob = createAbsoluteKnob(surface, midiIn);
    }
    if (isRelativeEncoder()) {
      relKnob = createRelativeKnob(surface, midiIn, button);
    }
    if (isLED()) {
      led = createLed(surface);
    }
  }

  @Override
  public int getSpec() {
    return spec;
  }

  @Override
  public Control onPressed(Runnable handler) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    pressedHandlers.add(handler);
    return this;
  }

  @Override
  public <T extends InternalHardwareLightState> Control onPressed(Runnable handler, T onState) {
    return onPressed(handler, onState, OFF);
  }

  @Override
  public <T extends InternalHardwareLightState> Control onPressed(
      Runnable handler, T onState, T offState) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    pressedHandlers.add(handler);
    releasedHandlers.add(() -> led.state().setValue(offState));
    return this;
  }

  @Override
  public Control onPressed(HardwareActionBindable target) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    bindings.add(target.addBinding(button.pressedAction()));
    return this;
  }

  @Override
  public <T extends InternalHardwareLightState> Control onPressed(
      HardwareActionBindable target, T onState) {
    return onPressed(target, onState, OFF);
  }

  @Override
  public <T extends InternalHardwareLightState> Control onPressed(
      HardwareActionBindable target, T onState, T offState) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support pressed-action.");
    }
    try {
      bindings.add(target.addBinding(button.pressedAction()));
      if (target instanceof BooleanValue) {
        subscriptions.add(Hook.subscribe((BooleanValue) target, v -> led(v ? onState : offState)));
      }
    } catch (Throwable ex) {
      LOG.error("error control[{}]. {}", name(), ex);
    }
    return this;
  }

  @Override
  public Control onReleased(Runnable handler) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support release-action.");
    }
    releasedHandlers.add(handler);
    return this;
  }

  @Override
  public Control onReleased(HardwareActionBindable target) {
    if (!isButton()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support release-action.");
    }
    bindings.add(target.addBinding(button.releasedAction()));
    return this;
  }

  @Override
  public Control onAbsValue(Consumer<Double> handler) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    absValueHandlers.add(handler);
    return this;
  }

  @Override
  public Control onAbsValue(AbsoluteHardwarControlBindable target) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    bindings.add(target.addBinding(absKnob));
    return this;
  }

  @Override
  public Control onAbsValue(AbsoluteHardwarControlBindable target, double min, double max) {
    if (!isAbsoluteEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support absolute-value.");
    }
    bindings.add(target.addBindingWithRange(absKnob, min, max));
    return this;
  }

  @Override
  public Control onRelValue(RelativeHardwarControlBindable target) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBinding(relKnob));
    return this;
  }

  @Override
  public Control onRelValue(RelativeHardwarControlBindable target, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithSensitivity(relKnob, sensitivity));
    return this;
  }

  @Override
  public Control onRelValue(SettableRangedValue target, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithSensitivity(relKnob, sensitivity));
    return this;
  }

  @Override
  public Control onRelValue(
      SettableRangedValue target, double min, double max, double sensitivity) {
    if (!isRelativeEncoder()) {
      throw new UnsupportedOperationException(
          "[" + name() + "] Control doesn't support relative-value.");
    }
    bindings.add(target.addBindingWithRangeAndSensitivity(relKnob, min, max, sensitivity));
    return this;
  }

  @Override
  public <T extends InternalHardwareLightState> Control led(T state) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    led.state().setValue(state);
    return this;
  }

  @Override
  public <T extends InternalHardwareLightState> Control led(BooleanValue value, T onState) {
    return led(value, onState, OFF);
  }

  @Override
  public <T extends InternalHardwareLightState> Control led(
      BooleanValue value, T onState, T offState) {
    if (!isLED()) {
      throw new UnsupportedOperationException("[" + name() + "] Control doesn't have LED.");
    }
    subscriptions.add(Hook.subscribe(value, v -> led.state().setValue(v ? onState : offState)));
    return this;
  }

  @Override
  public boolean isPressed() {
    return pressed;
  }

  @Override
  public double getAbsValue() {
    return absValue;
  }

  @Override
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

  public static XoneK2Control encoder(int ch, int row) {
    return ENCODERS[row][ch];
  }

  public static XoneK2Control fader(int ch) {
    return FADERS[ch];
  }

  public static XoneK2Control grid(int col, int row) {
    return GRID_BUTTONS[row][col];
  }

  public HardwareButton getButton() {
    return button;
  }

  public AbsoluteHardwareKnob getAbsoluteKnob() {
    return absKnob;
  }

  public RelativeHardwareKnob getRelativeKnob() {
    return relKnob;
  }

  public MultiStateHardwareLight getLed() {
    return led;
  }

  private HardwareButton createButton(HardwareSurface surface, MidiIn midiIn) {
    HardwareButton btn = surface.createHardwareButton(name() + BUTTON_SUFFIX);
    btn.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(MIDI_CH, note));
    btn.releasedAction().setActionMatcher(midiIn.createNoteOffActionMatcher(MIDI_CH, note));
    btn.isPressed()
        .addValueObserver(
            pressed -> {
              this.pressed = pressed;
              LOG.trace("[{}] button {}.", name(), pressed ? "pressed" : "released");
              // execute topmost layer's handler
              (pressed ? pressedHandlers : releasedHandlers).forEach(Runnable::run);
            });
    btn.isPressed().markInterested();
    return btn;
  }

  private AbsoluteHardwareKnob createAbsoluteKnob(HardwareSurface surface, MidiIn midiIn) {
    AbsoluteHardwareKnob knob = surface.createAbsoluteHardwareKnob(name() + ABSOLUTE_SUFFIX);
    if (knob != null) {
      knob.setHardwareButton(button);
    }
    knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(MIDI_CH, cc));
    knob.value()
        .addValueObserver(
            value -> {
              absValue = value;
              LOG.trace("[{}] absolute value is changed to [{}].", name(), value);
              // execute topmost layer's handler
              absValueHandlers.forEach(h -> h.accept(value));
            });
    knob.targetValue().markInterested();
    return knob;
  }

  private RelativeHardwareKnob createRelativeKnob(
      HardwareSurface surface, MidiIn midiIn, HardwareButton btn) {
    RelativeHardwareKnob knob = surface.createRelativeHardwareKnob(name() + RELATIVE_SUFFIX);
    if (btn != null) {
      knob.setHardwareButton(btn);
    }
    knob.setAdjustValueMatcher(
        midiIn.createRelative2sComplementCCValueMatcher(MIDI_CH, cc, RELATIVE_VALUE_PER_ROTAION));
    return knob;
  }

  private MultiStateHardwareLight createLed(HardwareSurface surface) {
    MultiStateHardwareLight led = surface.createMultiStateHardwareLight(name() + LED_SUFFIX);
    led.state().onUpdateHardware(this::updateLedState);
    // TODO blinking
    return led;
  }

  private <T extends InternalHardwareLightState> void updateLedState(T state) {
    LOG.trace("[{}] led updated to state [{}].", name(), state);
    int noteOffset = this == LAYER || this == SHIFT ? 4 : 36;
    if (state == XoneK2LedState.OFF) {
      midiOut.sendMidi(0x90 + MIDI_CH, note, 0);
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset, 0);
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset * 2, 0);
    } else if (state == XoneK2LedState.RED) {
      midiOut.sendMidi(0x90 + MIDI_CH, note, 0x7f);
    } else if (state == XoneK2LedState.YELLOW) {
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset, 0x7f);
    } else if (state == XoneK2LedState.GREEN) {
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset * 2, 0x7f);
    }
    // TODO blinking
  }
}
