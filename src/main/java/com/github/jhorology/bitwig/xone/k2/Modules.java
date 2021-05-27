package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.DocumentState;
import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.api.UserControlBank;
import com.github.jhorology.bitwig.xone.k2.Modes.Nav1Mode;
import com.github.jhorology.bitwig.xone.k2.Modes.Nav2Mode;
import java.util.HashMap;
import java.util.Map;

public class Modules {
  public static Transport TRANSPORT;
  public static CursorTrack CURSOR_TRACK;
  public static TrackBank TRACK_BANK;
  public static Map<XoneK2Control, Parameter> USER_CONTROL_MAP;
  public static DocumentState DOCUMENT_STATE;
  public static SettableEnumValue NAV1_MODE;
  public static SettableEnumValue NAV2_MODE;

  static void init(ControllerHost host) {
    TRANSPORT = host.createTransport();
    CURSOR_TRACK = host.createCursorTrack("Xone:K2", "Xone:K2", 3, 4, true);
    TRACK_BANK = host.createTrackBank(4, 3, 4);
    TRACK_BANK.followCursorTrack(CURSOR_TRACK);
    TRACK_BANK.sceneBank().setIndication(true);
    DOCUMENT_STATE = host.getDocumentState();
    NAV1_MODE = DOCUMENT_STATE.getEnumSetting("Nav1", "Navigation Knob mode", Nav1Mode.TEST1);
    NAV2_MODE = DOCUMENT_STATE.getEnumSetting("Nav2", "Navigation Knob mode", Nav2Mode.TEST1);
    // 4 x 4 knobs + 4 faders + 4 x 4 grid buttons
    int numUserControls = 4 * 4 + 4 + 4 * 4;
    UserControlBank controls = host.createUserControls(numUserControls);
    int index = 0;
    // knobs
    USER_CONTROL_MAP = new HashMap<>();
    for (int ch = 0; ch < 4; ch++) {
      for (int row = 0; row < 4; row++) {
        XoneK2Control control = XoneK2Control.knob(ch, row);
        Parameter param = controls.getControl(index++);
        param.setLabel(control.name());
        USER_CONTROL_MAP.put(control, param);
      }
    }
    for (int ch = 0; ch < 4; ch++) {
      XoneK2Control control = XoneK2Control.fader(ch);
      Parameter param = controls.getControl(index++);
      param.setLabel(control.name());
      USER_CONTROL_MAP.put(control, param);
    }
    for (int col = 0; col < 4; col++) {
      for (int row = 0; row < 4; row++) {
        XoneK2Control control = XoneK2Control.grid(col, row);
        Parameter param = controls.getControl(index++);
        param.setLabel(control.name());
        USER_CONTROL_MAP.put(control, param);
      }
    }
  }

  static void exit() {
    TRANSPORT = null;
    CURSOR_TRACK = null;
    TRACK_BANK = null;
    DOCUMENT_STATE = null;
    USER_CONTROL_MAP.clear();
    USER_CONTROL_MAP = null;
    NAV1_MODE = null;
    NAV2_MODE = null;
  }
}
