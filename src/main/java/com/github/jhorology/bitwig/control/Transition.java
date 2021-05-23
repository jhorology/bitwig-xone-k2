package com.github.jhorology.bitwig.control;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Transition implements Supplier<Double> {
  public static final Function<Double, Double> DEFAULT = t -> 1.0;
  public static final Function<Double, Double> LINEAR = t -> t;
  public static final Function<Double, Double> BEZIER = t -> t * t * (3.0 - 2.0 * t);
  // TODO need more functions

  // default ON=100ms OFF=100ms
  public static class Params {
    private double startValue = 1.0;
    private int delay = 0;
    private int endDelay = 100;
    private int duration = 100;
    private boolean loop = true;
    private boolean globalSync = true;
    private Function<Double, Double> fn = t -> 0.0;

    public double getStartValue() {
      return startValue;
    }

    public void setStartValue(double startValue) {
      this.startValue = startValue;
    }

    public int getDelay() {
      return delay;
    }

    public void setDelay(int delay) {
      this.delay = delay;
    }

    public int getEndDelay() {
      return endDelay;
    }

    public void setEndDelay(int endDelay) {
      this.endDelay = endDelay;
    }

    public int getDuration() {
      return duration;
    }

    public void setDuration(int duration) {
      this.duration = duration;
    }

    public boolean isLoop() {
      return loop;
    }

    public void setLoop(boolean loop) {
      this.loop = loop;
    }

    public boolean isGlobalSync() {
      return globalSync;
    }

    public void setGlobalSync(boolean globalSync) {
      this.globalSync = globalSync;
    }

    public Function<Double, Double> getFn() {
      return fn;
    }

    public void setFn(Function<Double, Double> fn) {
      this.fn = fn;
    }
  }

  private static List<Transition> transitionList;
  private final Params params;
  private final Consumer<Double> consumer;
  private final long startTime;

  private double value = Double.NaN;
  private boolean removed;

  public static Transition create(Consumer<Double> consumer) {
    return new Transition(new Params(), consumer);
  }

  public static Transition create(Params params, Consumer<Double> consumer) {
    return new Transition(params, consumer);
  }

  private Transition(Params params, Consumer<Double> consumer) {
    this.params = params;
    this.consumer = consumer;
    this.startTime = System.currentTimeMillis();
  }

  /**
   * initialize
   * this method should be called at ControllerExtension#init()
   */
  public static void init() {
    transitionList = new ArrayList<>();
  }

  /**
   * update transitions
   */
  public static void update() {
    long currentTime = System.currentTimeMillis();
    transitionList.remove(
        transitionList.stream().filter(t -> t._update(currentTime)).collect(Collectors.toList()));
  }

  /**
   * finalize.
   * this method should be called at ControllerExtension#exit()
   */
  public static void exit() {
    transitionList.clear();
    transitionList = null;
  }

  @Override
  public Double get() {
    return value;
  }

  private boolean _update(long currentTime) {
    if (removed) {
      return true;
    }
    long cycleDuration = params.delay + params.duration + params.endDelay;
    long elapsedTime = currentTime - startTime;
    long cycleElapsedTime =
        params.globalSync ? (currentTime % cycleDuration) : (elapsedTime % cycleDuration);

    double value;
    if (cycleElapsedTime < params.delay || Double.isNaN(this.value)) {
      // in delay
      value = params.startValue;
    } else if (cycleElapsedTime <= (params.delay + params.duration)) {
      // in transition
      value = params.fn.apply((double) (cycleElapsedTime - params.delay) / params.duration);
    } else {
      // in end delay
      value = this.value;
    }
    if (this.value != value) {
      this.consumer.accept(value);
    }
    this.removed = !params.loop && elapsedTime >= cycleDuration;
    return this.removed;
  }

  public void remove() {
    this.removed = true;
  }
}
