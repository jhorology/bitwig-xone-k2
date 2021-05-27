package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.github.jhorology.bitwig.control.Layers;
import com.github.jhorology.bitwig.utils.Hook;
import com.github.jhorology.bitwig.utils.Transition;
import com.github.jhorology.bitwig.xone.k2.layer.BaseMixerLayer;
import com.github.jhorology.bitwig.xone.k2.layer.ClipLauncherLayer;
import com.github.jhorology.bitwig.xone.k2.layer.LayerSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XoneK2Extension extends ControllerExtension {
  private static final Logger LOG = LoggerFactory.getLogger(XoneK2Extension.class);
  private static final int TRANSITION_FRAME_RATE = 20;
  private HardwareSurface surface;
  private Layers<XoneK2Control, XoneK2LedState> layers;

  protected XoneK2Extension(final XoneK2ExtensionDefinition definition, final ControllerHost host) {
    super(definition, host);
  }

  @Override
  public void init() {
    ControllerHost host = getHost();
    Hook.init();
    Modules.init(host);
    Transition.init();
    transitionService();
    surface = host.createHardwareSurface();
    BaseMixerLayer test = new BaseMixerLayer(host);
    layers =
        new Layers<>(
            new BaseMixerLayer(host), new LayerSelector(host), new ClipLauncherLayer(host));
    XoneK2Control.init(surface, host.getMidiInPort(0), host.getMidiOutPort(0));
    layers.init();
    layers.open(BaseMixerLayer.class);
    LOG.info("XONE:K2 Initialized.");
  }

  @Override
  public void exit() {
    layers.exit();
    XoneK2Control.exit();
    Transition.exit();
    Modules.exit();
    Hook.exit();
    LOG.info("XONE:K2 Exited.");
  }

  @Override
  public void flush() {
    // Transition.update();
    surface.updateHardware();
  }

  private void transitionService() {
    Transition.update();
    getHost().scheduleTask(this::transitionService, 1000 / TRANSITION_FRAME_RATE);
  }
}
