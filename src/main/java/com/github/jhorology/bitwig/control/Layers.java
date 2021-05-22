package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.InternalHardwareLightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Layers<T extends Control<T, L>, L extends InternalHardwareLightState> {
  private static final Logger LOG = LoggerFactory.getLogger(Layers.class);
  private final Map<Class<?>, Layer<T, L>> layers;

  // TODO support multiple stacked layer
  // TODO support multiple part of surface
  private Layer<T, L> baseLayer;
  private Layer<T, L> overlay;

  @SafeVarargs
  public Layers(Layer<T, L>... layers) {
    this.layers =
        Stream.of(layers)
          .peek(l -> l.setLayers(this))
          .peek(l -> LOG.trace("Layer[class={}] is registered.", l.getClass()))
          .collect(Collectors.toMap(Layer<T, L>::getClass, l -> l));
    LOG.trace("total [{}] layers are registered.", this.layers.size());
  }

  /**
   *  initialize.
   *  this method should be called at extension's start of lifecycle.
   */
  public void init() {
    layers.values().forEach(Layer::initialize);
  }

  /**
   *  finalize.
   *  this method should be called at extension's end of lifecycle.
   */
  public void exit() {
    if (overlay != null) {
      overlay.dispose();
    }
    if (baseLayer != null) {
      baseLayer.dispose();
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
    Layer<T, L> layer = layers.get(clazz);
    if (layer == null) {
      throw new IllegalStateException("The Layer[class=" + clazz.getName() + "] is not registered.");
    }
    activate(layer);
    LOG.trace("activate layer[{}].", clazz.getName());
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
