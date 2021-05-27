package com.github.jhorology.bitwig.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simple transition generator. for visual, modulation, etc..any purpose.
 *
 * <pre>
 *             1.0 |
 * start = fn(0.0) |________________   fn(t)
 *                 |                \___________
 *                 |                            \
 *                 |                             \_____________ end = fn(1.0)
 *                 |
 *                 |            t=0.0            t=1.0
 *             0.0 |-------------|----------------|-------------!
 *                    delay(ms)     duration(ms)     endDelay(ms)
 * </pre>
 */
public class Transition implements Supplier<Double> {
  private static final Function<Double, Double> PULSE = t -> t == 1.0 ? 0 : 1.0;
  private static final Function<Double, Double> LINEAR = t -> t;
  private static final Function<Double, Double> BEZIER_EASE_IN = t -> t * t * (3.0 - 2.0 * t);
  // TODO need more functions

  /**
   * A POJO class for parameters of transition.
   *
   * <p>defaultL: pulse on=200ms, off=200ms
   *
   * <pre>
   *   delay = 0
   *   endDelay = 200
   *   duration  = 200
   *   loop = true
   *   triggerable = false
   *   globalSync = true
   *   fn = t -> t == 1.0 ? 0 : 1.0;
   * </pre>
   */
  public static class Params {
    private int delay = 0;
    private int endDelay = 200;
    private int duration = 200;
    private boolean loop = true;
    private boolean triggerable = false;
    private boolean globalSync = true;
    private Function<Double, Double> fn = PULSE;

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

    public boolean isTriggerable() {
      return triggerable;
    }

    public void setTriggerable(boolean triggerable) {
      this.triggerable = triggerable;
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
  private long startTime;
  private final double startValue;
  private final double endValue;
  private double value = Double.NaN;
  private long lastCycleElapsedTime;
  private boolean paused;
  private boolean ended;
  private boolean canceled;

  // TODO need more static methods for easy use
  public static Transition blink(int onDuration, int offDuration, Consumer<Double> consumer) {
    Params params = new Params();
    params.setDuration(onDuration);
    params.setEndDelay(offDuration);
    return new Transition(params, consumer);
  }

  public static Transition triggerablePulse(int pulseDuration, Consumer<Double> consumer) {
    Params params = new Params();
    params.setTriggerable(true);
    params.setDuration(pulseDuration);
    params.setEndDelay(0);
    params.setGlobalSync(false);
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
    this.startValue = params.fn.apply(0.0);
    this.endValue = params.fn.apply(1.0);
    this.paused = params.triggerable;
    transitionList.add(this);
  }

  /**
   * initialize.
   *
   * <p>this method should be called at ControllerExtension#init()
   */
  public static void init() {
    transitionList = new ArrayList<>();
  }

  /**
   * finalize.
   *
   * <p>this method should be called at ControllerExtension#exit()
   */
  public static void exit() {
    transitionList.clear();
    transitionList = null;
  }

  public void trigger() {
    if (params.triggerable) {
      paused = false;
      value = Double.NaN;
      startTime = System.currentTimeMillis();
      _update(startTime);
    }
  }

  /**
   * update transitions.
   *
   * <p>this method should be called interval.
   */
  public static void update() {
    long currentTime = System.currentTimeMillis();
    transitionList.forEach(t -> t._update(currentTime));
    transitionList.removeIf(t -> t.ended || t.canceled);
  }

  @Override
  public Double get() {
    return value;
  }

  private void _update(long currentTime) {
    if (canceled || paused) {
      return;
    }
    long cycleDuration = params.delay + params.duration + params.endDelay;
    long elapsedTime = currentTime - startTime;
    long cycleElapsedTime =
        params.globalSync ? (currentTime % cycleDuration) : (elapsedTime % cycleDuration);
    boolean cycleUp = cycleElapsedTime < lastCycleElapsedTime;
    lastCycleElapsedTime = cycleElapsedTime;
    if (cycleUp) {
      // fn(1.0)
      notifyValue(endValue);
      if (!params.loop) {
        ended = true;
        return;
      }
      if (params.triggerable) {
        paused = true;
        return;
      }
    }

    // start
    if (cycleUp || Double.isNaN(this.value)) {
      // fn(0.0)
      notifyValue(startValue);
    }

    double value;
    if (cycleElapsedTime < params.delay) {
      // in delay
      value = startValue;
    } else if (cycleElapsedTime <= (params.delay + params.duration)) {
      // in transition
      value = params.fn.apply((double) (cycleElapsedTime - params.delay) / params.duration);
    } else {
      // in end delay
      value = endValue;
    }
    notifyValue(value);
  }

  private void notifyValue(double value) {
    if (this.value != value) {
      this.consumer.accept(value);
      this.value = value;
    }
  }

  /**
   * Remove the transition from service task.
   *
   * <p>if Parametes.isLoop() == true, this method should be called when it becomes unnecessary.
   */
  public void remove() {
    canceled = true;
  }

  public boolean isEnded() {
    return paused;
  }

  public boolean isPaused() {
    return paused;
  }
}
