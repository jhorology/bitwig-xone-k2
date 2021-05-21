package com.github.jhorology.bitwig.xone.k2.layer;

import com.bitwig.extension.controller.api.ControllerHost;
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
    // TODO
  }
}
