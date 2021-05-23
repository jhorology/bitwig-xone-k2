package com.github.jhorology.bitwig.xone.k2.layer;

import static com.github.jhorology.bitwig.xone.k2.SharedModules.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2Control.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2LedState.*;

import com.bitwig.extension.controller.api.ClipLauncherSlot;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClipLauncherLayer extends AbstractLayer {
  private static final Logger LOG = LoggerFactory.getLogger(ClipLauncherLayer.class);

  public ClipLauncherLayer(ControllerHost host) {
    super(host);
  }

  @Override
  protected boolean isOverlay() {
    return true;
  }

  @Override
  protected void setup() {
    for (int col = 0; col < 4; col++) {
      Track track = TRACK_BANK.getItemAt(col);
      for (int row = 0; row < 4; row++) {
        ClipLauncherSlot slot = track.clipLauncherSlotBank().getItemAt(row);
        use(
            grid(col, row)
                .onPressed(slot.launchAction())
                .led(slot.exists(), GREEN, OFF)
                .led(slot.isPlaying(), YELLOW, GREEN));
      }
    }
  }
}
