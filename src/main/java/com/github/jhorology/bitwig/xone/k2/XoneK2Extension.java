package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;
import com.github.jhorology.bitwig.xone.k2.layer.BaseMixerLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XoneK2Extension extends ControllerExtension {
  private static final Logger LOG = LoggerFactory.getLogger(XoneK2Extension.class);
  private HardwareSurface surface;

  protected XoneK2Extension(final XoneK2ExtensionDefinition definition, final ControllerHost host) {
    super(definition, host);
  }

  @Override
  public void init() {
    ControllerHost host = getHost();
    surface = host.createHardwareSurface();
    XoneK2Control.init(surface, host.getMidiInPort(0), host.getMidiOutPort(0));
    new BaseMixerLayer(host).start();
    LOG.info("XONE:K2 Initialized.");
  }

  @Override
  public void exit() {
    XoneK2Control.exit();
    LOG.info("XONE:K2 Exited.");
  }

  @Override
  public void flush() {
    surface.updateHardware();
  }
}
