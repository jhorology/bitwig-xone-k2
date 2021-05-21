package com.github.jhorology.bitwig.xone.k2.layer;

import static com.github.jhorology.bitwig.xone.k2.XoneK2Control.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2LedState.*;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMixerLayer extends AbstractLayer {
  private static final Logger LOG = LoggerFactory.getLogger(BaseMixerLayer.class);

  public BaseMixerLayer(ControllerHost host) {
    super(host);
  }

  @Override
  protected boolean isOverlay() {
    return false;
  }

  @Override
  protected void setup() {
    TrackBank trackBank = getTrackBank();
    Transport transport = getTransport();
    for (int ch = 0; ch < 4; ch++) {
      Track track = trackBank.getItemAt(ch);
      use(
          knob(ch, 0).onRelValue(track.pan()).onPressed(() -> track.pan().reset(), RED, GREEN),
          knob(ch, 1)
              .onAbsValue(track.sendBank().getItemAt(0))
              .onPressed(() -> track.sendBank().getItemAt(0).reset(), RED, GREEN),
          knob(ch, 2)
              .onAbsValue(track.sendBank().getItemAt(1))
              .onPressed(() -> track.sendBank().getItemAt(1).reset(), RED, GREEN),
          knob(ch, 3)
              .onAbsValue(track.sendBank().getItemAt(2))
              .onPressed(() -> track.sendBank().getItemAt(2).reset(), RED, GREEN),
          fader(ch).onAbsValue(track.volume()),
          grid(ch, 0).onPressed(track.arm(), RED),
          grid(ch, 1).onPressed(track.solo(), YELLOW),
          grid(ch, 2).onPressed(track.mute(), YELLOW));
    }
    use(
        M.onPressed(transport.playAction()),
        N.onPressed(transport.recordAction()),
        O.onPressed(transport.stopAction()));
    // TODO SHIFT action.
    // TODO multi function encoder for NAV1, NAV2
    //  - pressed + rotate select mode.
  }
}
