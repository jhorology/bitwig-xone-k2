package com.github.jhorology.bitwig.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *  Simple transition generator.
 *  for visual, modulation, etc..any purpose.
 *
 *         1.0 |
 *  startValue |--------------!\  fn(t)
 *             |                 \----------\
 *             |                             \
 *             |                              \_____________
 *             |
 *             |            t=0              t=1.0
 *         0.0 |-------------|----------------|-------------!
 *               delay           duration         endDelay
 */
public class Transition implements Supplier<Double> {
  public static final Function<Double, Double> LINEAR = t -> t;
  public static final Function<Double, Double> BEZIER = t -> t * t * (3.0 - 2.0 * t);
  // TODO need more functions

  /**
   * A POJO class for parameters of transition.
   */
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
  private boolean ended;

  // TODO need more static methods for easy use
  public static Transition blink(int onDuration, int offDuration, Consumer<Double> consumer) {
    Params params = new Params();
    params.setDelay(onDuration);
    params.setDuration(offDuration);
    return new Transition(params, consumer);
  }

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
    transitionList.add(this);
  }

  /** initialize. this method should be called at ControllerExtension#init() */
  public static void init() {
    transitionList = new ArrayList<>();
  }

  /**
   * update transitions.
   * this method should be called interval.
   */
  public static void update() {
    long currentTime = System.currentTimeMillis();
    transitionList.forEach(t -> t._update(currentTime));
    transitionList.removeIf(Transition::isEnded);
  }

  /** finalize. this method should be called at ControllerExtension#exit() */
  public static void exit() {
    transitionList.clear();
    transitionList = null;
  }

  @Override
  public Double get() {
    return value;
  }

  private boolean isEnded() {
    return ended;
  }

  private void _update(long currentTime) {
    if (this.isEnded()) {
      return;
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
      value = params.fn.apply(1.0);
    }
    if (this.value != value) {
      this.consumer.accept(value);
      this.value = value;
    }
    this.ended = !params.loop && elapsedTime >= cycleDuration;
  }

  public void remove() {
    this.ended = true;
  }
}
