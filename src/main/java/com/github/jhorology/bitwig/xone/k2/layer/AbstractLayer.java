package com.github.jhorology.bitwig.xone.k2.layer;

import com.bitwig.extension.controller.api.ControllerHost;
import com.github.jhorology.bitwig.control.Layer;
import com.github.jhorology.bitwig.xone.k2.XoneK2Control;
import com.github.jhorology.bitwig.xone.k2.XoneK2LedState;

abstract class AbstractLayer extends Layer<XoneK2Control, XoneK2LedState> {
  protected AbstractLayer(ControllerHost host) {
    super(host);
  }

  @Override
  protected void onInitialize() {}

  @Override
  protected void onDispose() {}
}
