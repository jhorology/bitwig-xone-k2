package com.github.jhorology.bitwig.control;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.github.jhorology.bitwig.utils.Hook.Subscription;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Layer<T extends Control<T, L>, L extends InternalHardwareLightState> {
  private static final Logger LOG = LoggerFactory.getLogger(Layer.class);
  protected Set<T> controls;
  protected List<Subscription> subscriptions;
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
    LOG.debug("Layer[{}] start initializing.", this.getClass());
    subscriptions = new ArrayList<>();
    initializing = true;
    controls = new HashSet<>();
    // call setup() within ControllerExtension#init():
    // - registering all needed observer for Value#addValueObserver.
    // - determines the controls that are used by this layer
    setup();
    clearBindings();
    initializing = false;
    onInitialize();
    LOG.debug("Layer[{}] initialized. controls.size()=[{}]", this.getClass(), this.controls.size());
  }

  void dispose() {
    onDispose();
    clearBindings();
  }

  /** initialize delegation point for inherited class. */
  protected void onInitialize() {}

  /** dispose delegation point for inherited class. */
  protected void onDispose() {}

  protected final void use(T control) {
    if (initializing) {
      this.controls.add(control);
    }
  }

  @SafeVarargs
  protected final void use(T... controls) {
    if (initializing) {
      this.controls.addAll(Arrays.asList(controls));
    }
  }

  Set<T> getControls() {
    return controls;
  }

  void clearBindings() {
    clearBindings(false);
  }

  void clearBindings(boolean excludeCommonControls) {
    LOG.debug("Layer[{}] clearBindings. controls=[{}]", this.getClass(), controls);
    controls.stream()
        .filter(c -> !excludeCommonControls || !c.isCommon())
        .forEach(Control::clearBindings);
    subscriptions.forEach(Subscription::unsubscribe);
    subscriptions.clear();
  }
}
