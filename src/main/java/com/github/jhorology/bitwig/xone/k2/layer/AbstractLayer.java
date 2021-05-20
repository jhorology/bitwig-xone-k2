package com.github.jhorology.bitwig.xone.k2.layer;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import com.github.jhorology.bitwig.control.Control;

// TODO no ideas
public abstract class AbstractLayer {
  protected ControllerHost host;

  // shared modules
  private static Transport transport;
  private static CursorTrack cursorTrack;
  private static TrackBank trackBank;

  protected AbstractLayer(ControllerHost host) {
    this.host = host;
  }

  protected abstract Control[] use();

  public abstract void start();

  protected Transport getTransport() {
    if (transport == null) {
      transport = host.createTransport();
    }
    return transport;
  }

  protected CursorTrack getCursorTrack() {
    if (cursorTrack == null) {
      cursorTrack = host.createCursorTrack("Xone:K2", "Xone:K2", 3, 4, true);
    }
    return cursorTrack;
  }

  protected TrackBank getTrackBank() {
    if (trackBank == null) {
      trackBank = host.createTrackBank(4, 3, 4);
      trackBank.followCursorTrack(getCursorTrack());
    }
    return trackBank;
  }
}
