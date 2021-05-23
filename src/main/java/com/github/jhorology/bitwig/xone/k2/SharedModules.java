package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;

public class SharedModules {
  public static Transport TRANSPORT;
  public static CursorTrack CURSOR_TRACK;
  public static TrackBank TRACK_BANK;

  static void init(ControllerHost host) {
    TRANSPORT = host.createTransport();
    CURSOR_TRACK = host.createCursorTrack("Xone:K2", "Xone:K2", 3, 4, true);
    TRACK_BANK = host.createTrackBank(4, 3, 4);
    TRACK_BANK.followCursorTrack(CURSOR_TRACK);
    TRACK_BANK.sceneBank().setIndication(true);
  }

  static void exit() {
    TRANSPORT = null;
    CURSOR_TRACK = null;
    TRACK_BANK = null;
  }
}
