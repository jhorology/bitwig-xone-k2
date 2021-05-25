package com.github.jhorology.bitwig.xone.k2.layer;

import static com.github.jhorology.bitwig.xone.k2.SharedModules.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2Control.*;

import com.bitwig.extension.controller.api.ControllerHost;

public class UserControlsLayer extends AbstractLayer {
  protected UserControlsLayer(ControllerHost host) {
    super(host);
  }

  @Override
  protected boolean isOverlay() {
    return false;
  }

  @Override
  protected void setup() {
    int index = 0;
    // knobs
    for (int ch = 0; ch < 4; ch++) {
      for (int row = 0; row < 4; row++) {}
    }
  }
}
