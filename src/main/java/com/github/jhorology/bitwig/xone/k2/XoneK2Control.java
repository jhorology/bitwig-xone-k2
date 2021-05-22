package com.github.jhorology.bitwig.xone.k2;

import static com.github.jhorology.bitwig.xone.k2.XoneK2LedState.*;

import com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher;
import com.bitwig.extension.controller.api.HardwareActionMatcher;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.RelativeHardwareValueMatcher;
import com.github.jhorology.bitwig.control.Control;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XoneK2Control extends Control<XoneK2Control, XoneK2LedState> {
  private static final Logger LOG = LoggerFactory.getLogger(XoneK2Control.class);

  public static final XoneK2Control CH0_ENC0 =
      create(BUTTON | ENCODER | RELATIVE | LED, 0x34, 0x0, "CH0_ENC0");
  public static final XoneK2Control CH1_ENC0 =
      create(BUTTON | ENCODER | RELATIVE | LED, 0x35, 0x1, "CH1_ENC0");
  public static final XoneK2Control CH2_ENC0 =
      create(BUTTON | ENCODER | RELATIVE | LED, 0x36, 0x2, "CH2_ENC0");
  public static final XoneK2Control CH3_ENC0 =
      create(BUTTON | ENCODER | RELATIVE | LED, 0x37, 0x3, "CH3_ENC0");

  public static final XoneK2Control CH0_ENC1 =
      create(BUTTON | ENCODER | LED, 0x30, 0x4, "CH0_ENC1");
  public static final XoneK2Control CH1_ENC1 =
      create(BUTTON | ENCODER | LED, 0x31, 0x5, "CH1_ENC1");
  public static final XoneK2Control CH2_ENC1 =
      create(BUTTON | ENCODER | LED, 0x32, 0x6, "CH2_ENC1");
  public static final XoneK2Control CH3_ENC1 =
      create(BUTTON | ENCODER | LED, 0x33, 0x7, "CH3_ENC1");

  public static final XoneK2Control CH0_ENC2 =
      create(BUTTON | ENCODER | LED, 0x2C, 0x8, "CH0_ENC2");
  public static final XoneK2Control CH1_ENC2 =
      create(BUTTON | ENCODER | LED, 0x2D, 0x9, "CH1_ENC2");
  public static final XoneK2Control CH2_ENC2 =
      create(BUTTON | ENCODER | LED, 0x2E, 0xA, "CH2_ENC2");
  public static final XoneK2Control CH3_ENC2 =
      create(BUTTON | ENCODER | LED, 0x2F, 0xB, "CH3_ENC2");

  public static final XoneK2Control CH0_ENC3 =
      create(BUTTON | ENCODER | LED, 0x28, 0xC, "CH0_ENC3");
  public static final XoneK2Control CH1_ENC3 =
      create(BUTTON | ENCODER | LED, 0x29, 0xD, "CH1_ENC3");
  public static final XoneK2Control CH2_ENC3 =
      create(BUTTON | ENCODER | LED, 0x2A, 0xE, "CH2_ENC3");
  public static final XoneK2Control CH3_ENC3 =
      create(BUTTON | ENCODER | LED, 0x2B, 0xF, "CH3_ENC3");

  public static final XoneK2Control CH0_FADER = create(ENCODER, -1, 0x10, "CH0_FADER");
  public static final XoneK2Control CH1_FADER = create(ENCODER, -1, 0x11, "CH1_FADER");
  public static final XoneK2Control CH2_FADER = create(ENCODER, -1, 0x12, "CH2_FADER");
  public static final XoneK2Control CH3_FADER = create(ENCODER, -1, 0x13, "CH3_FADER");

  public static final XoneK2Control A = create(BUTTON | LED, 0x24, -1, "A");
  public static final XoneK2Control B = create(BUTTON | LED, 0x25, -1, "B");
  public static final XoneK2Control C = create(BUTTON | LED, 0x26, -1, "C");
  public static final XoneK2Control D = create(BUTTON | LED, 0x27, -1, "D");

  public static final XoneK2Control E = create(BUTTON | LED, 0x20, -1, "E");
  public static final XoneK2Control F = create(BUTTON | LED, 0x21, -1, "F");
  public static final XoneK2Control G = create(BUTTON | LED, 0x22, -1, "G");
  public static final XoneK2Control H = create(BUTTON | LED, 0x23, -1, "H");

  public static final XoneK2Control I = create(BUTTON | LED, 0x1C, -1, "I");
  public static final XoneK2Control J = create(BUTTON | LED, 0x1D, -1, "J");
  public static final XoneK2Control K = create(BUTTON | LED, 0x1E, -1, "K");
  public static final XoneK2Control L = create(BUTTON | LED, 0x1F, -1, "L");

  public static final XoneK2Control M = create(BUTTON | LED, 0x18, -1, "M");
  public static final XoneK2Control N = create(BUTTON | LED, 0x19, -1, "N");
  public static final XoneK2Control O = create(BUTTON | LED, 0x1A, -1, "O");
  public static final XoneK2Control P = create(BUTTON | LED, 0x1B, -1, "P");

  public static final XoneK2Control LAYER = create(BUTTON | LED | COMMON, 0xC, -1, "LAYER");

  public static final XoneK2Control NAV1 =
      create(BUTTON | ENCODER | RELATIVE | COMMON, 0xD, 0x14, "NAV1");
  public static final XoneK2Control NAV2 =
      create(BUTTON | ENCODER | RELATIVE | COMMON, 0xE, 0x15, "NAV2");

  public static final XoneK2Control SHIFT = create(BUTTON | LED | COMMON, 0xF, -1, "SHIFT");

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

  private static final XoneK2Control[] ALL = {
    CH0_ENC0, CH1_ENC0, CH2_ENC0, CH3_ENC0,
    CH0_ENC1, CH1_ENC1, CH2_ENC1, CH3_ENC1,
    CH0_ENC2, CH1_ENC2, CH2_ENC2, CH3_ENC2,
    CH0_ENC3, CH1_ENC3, CH2_ENC3, CH3_ENC3,
    CH0_FADER, CH1_FADER, CH2_FADER, CH3_FADER,
    A, B, C, D,
    E, F, G, H,
    I, J, K, L,
    M, N, O, P,
    LAYER, NAV1, NAV2, SHIFT
  };

  // TODO MIDI channel should be configurable.
  private static final int MIDI_CH = 0;
  private static final int RELATIVE_AMOUNT_PER_ROTATION = 30;
  private final int spec;
  private final int note;
  private final int cc;
  private final String name;

  private static XoneK2Control create(int spec, int note, int cc, String name) {
    return new XoneK2Control(spec, note, cc, name);
  }

  private XoneK2Control(int spec, int note, int cc, String name) {
    super();
    this.spec = spec;
    this.note = note;
    this.cc = cc;
    this.name = name;
  }

  /**
   * initialize.
   */
  public static void init(HardwareSurface surface, MidiIn midiIn, MidiOut midiOut) {
    Stream.of(ALL).forEach(c -> c.initialize(surface, midiIn, midiOut));
  }

  /**
   * finalize.
   */
  public static void exit() {
    Stream.of(ALL).forEach(XoneK2Control::dispose);
  }

  /**
   * Returns a knob control.
   *
   * @param ch channel index (0-3).
   * @param row row index of knob section (0-3).
   * @return A knob control
   */
  public static XoneK2Control knob(int ch, int row) {
    return ENCODERS[row][ch];
  }

  /**
   * Returns a fader control.
   *
   * @param ch channel index (0-3).
   * @return A fader control
   */
  public static XoneK2Control fader(int ch) {
    return FADERS[ch];
  }

  /**
   * Returns a button control of grid section.
   *
   * @param col column index (0-3).
   * @param row row index (0-3).
   * @return A grid button control
   */
  public static XoneK2Control grid(int col, int row) {
    return GRID_BUTTONS[row][col];
  }

  /** {@inheritDoc} */
  @Override
  protected String name() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  protected XoneK2LedState getDefaultLedOffState() {
    return OFF;
  }

  /** {@inheritDoc} */
  @Override
  protected int getSpec() {
    return spec;
  }

  /** {@inheritDoc} */
  @Override
  protected HardwareActionMatcher createPressedActionMatcher(MidiIn midiIn) {
    return midiIn.createNoteOnActionMatcher(MIDI_CH, note);
  }

  /** {@inheritDoc} */
  @Override
  protected HardwareActionMatcher createReleasedActionMatcher(MidiIn midiIn) {
    return midiIn.createNoteOffActionMatcher(MIDI_CH, note);
  }

  /** {@inheritDoc} */
  @Override
  protected AbsoluteHardwareValueMatcher createAbsValueMatcher(MidiIn midiIn) {
    return midiIn.createAbsoluteCCValueMatcher(MIDI_CH, cc);
  }

  /** {@inheritDoc} */
  @Override
  protected RelativeHardwareValueMatcher createRelValueMatcher(MidiIn midiIn) {
    LOG.trace("## [{}] createRelValueMatcher() cc={}", name(), cc);
    return midiIn.createRelative2sComplementCCValueMatcher(MIDI_CH, cc, RELATIVE_AMOUNT_PER_ROTATION);
  }

  /** {@inheritDoc} */
  @Override
  protected void sendLedState(XoneK2LedState state, MidiOut midiOut) {
    LOG.trace("[{}] led updated to state [{}].", name(), state);
    int noteOffset = this == LAYER || this == SHIFT ? 4 : 36;
    if (state == OFF) {
      midiOut.sendMidi(0x90 + MIDI_CH, note, 0);
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset, 0);
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset * 2, 0);
    } else if (state == RED) {
      midiOut.sendMidi(0x90 + MIDI_CH, note, 0x7f);
    } else if (state == YELLOW) {
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset, 0x7f);
    } else if (state == GREEN) {
      midiOut.sendMidi(0x90 + MIDI_CH, note + noteOffset * 2, 0x7f);
    }
    // TODO blinking
  }
}
