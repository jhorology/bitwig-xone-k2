package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.InternalHardwareLightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Layers<T extends Control<T, L>, L extends InternalHardwareLightState> {
  private static final Logger LOG = LoggerFactory.getLogger(Layers.class);
  private final Map<String, Layer<T, L>> layers;

  // TODO support multiple stacked layer
  // TODO support multiple part of surface
  private Layer<T, L> baseLayer;
  private Layer<T, L> overlay;

  public Layers(Layer<T, L>... layers) {
    this.layers =
        Stream.of(layers)
          .peek(l -> l.setLayers(this))
          .peek(l -> LOG.trace("Layer[class={}] is registered.", l.getClass()))
          .collect(Collectors.toMap(l-> l.getClass().getName(), l -> l));
    LOG.trace("total [{}] layers are registered.", this.layers.size());
  }

  public void init() {
    layers.values().forEach(Layer::init);
  }

  public void exit() {
    if (overlay != null) {
      overlay.clearBindings();
    }
    if (baseLayer != null) {
      baseLayer.clearBindings();
    }
    layers.clear();
    baseLayer = null;
    overlay = null;
  }

  public void closeOverlay() {
    if (overlay != null) {
      overlay.clearBindings();
      if (baseLayer == null) {
        throw new IllegalStateException("Base layer doesn't exist.");
      }
      baseLayer.clearBindings();
      baseLayer.setup();
    }
  }

  public void activate(Class<?> clazz) {
    LOG.trace("activate layer[{}]", clazz.getName());
    Layer<T, L> layer = layers.get(clazz.getName());
    if (layer == null) {
      throw new IllegalStateException("The Layer[class=" + clazz.getName() + "] doesn't exist.");
    }
    activate(layer);
  }

  public void activate(Layer<T, L> layer) {
    if (overlay != null) {
      overlay.clearBindings();
    }
    if (baseLayer != null) {
      baseLayer.clearBindings();
    }
    if (layer.isOverlay()) {
      if (baseLayer == null) {
        throw new IllegalStateException("Base layer doesn't exist.");
      }
      baseLayer.setup();
      layer.getControls().forEach(Control::clearBindings);
      layer.setup();
      overlay = layer;
    } else {
      layer.setup();
      baseLayer = layer;
    }
  }
}
