package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Layer<T extends Control<T, L>, L extends InternalHardwareLightState> {
  protected Set<Control<T, L>> controls;
  protected final ControllerHost host;
  private boolean initializing;
  protected Layers<T, L> layers;

  protected Layer(ControllerHost host) {
    this.host = host;
  }

  protected abstract boolean isOverlay();

  protected abstract void setup();

  void setLayers(Layers<T, L> layers) {
    this.layers = layers;
  }

  void initialize() {
    initializing = true;
    controls = new HashSet<>();
    // call setup() within ControllerExtension#init():
    // - create all needed modules. trackBank, transport, etc...
    // - registering all needed observer for Value#addValueObserver.
    // - determines the controls that are used by this layer
    setup();
    clearBindings();
    initializing = false;
  }

  void dispose(){
    clearBindings();
  }

  protected void use(Control<T, L> control) {
    if (initializing) {
      controls.add(control);
    }
  }

  protected void use(Control<T, L>... controls) {
    if (initializing) {
      this.controls.addAll(Arrays.asList(controls));
    }
  }

  Set<Control<T, L>> getControls() {
    return controls;
  }

  void clearBindings() {
    clearBindings(false);
  }

  void clearBindings(boolean excludeCommonControls) {
    controls.stream()
        .filter(c -> !excludeCommonControls || !c.isCommon())
        .forEach(Control::clearBindings);
  }
}
