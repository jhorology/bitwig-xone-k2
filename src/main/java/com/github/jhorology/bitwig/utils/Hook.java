package com.github.jhorology.bitwig.utils;

import com.bitwig.extension.callback.*;
import com.bitwig.extension.controller.api.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple subscription model. */
public class Hook {
  private static final Logger LOG = LoggerFactory.getLogger(Hook.class);

  // TODO  Can Value be used as hash key?
  private static final HashMap<Value<? extends ValueChangedCallback>, List<SubscriptionImpl<?>>>
      hooks = new HashMap<>();

  /** An interface of unsubscribable subscription of Value object. */
  public static interface Subscription {
    void unsubscribe();
  }

  /**
   * An interface o
   *
   * @param <T>
   */
  private static class SubscriptionImpl<T extends ValueChangedCallback> implements Subscription {
    private final Value<T> subscribable;
    private final T observer;

    private SubscriptionImpl(Value<T> subscribable, T observer) {
      this.subscribable = subscribable;
      this.observer = observer;
    }

    @Override
    public void unsubscribe() {
      List<SubscriptionImpl<?>> subscriptions = hooks.get(subscribable);
      if (subscriptions != null) {
        subscriptions.remove(observer);
      }
      if (subscriptions == null || subscriptions.isEmpty()) {
        subscribable.unsubscribe();
      }
      LOG.trace(
          "unsubscribe Value:[{} hashCode:{}]. total {} subscriptions.",
          subscribable,
          subscribable.hashCode(),
          subscriptions.size());
    }
  }

  /**
   * Subscribe the value object.
   *
   * @param subscribable A subscribable value object of Bitwig API.
   * @param observer A instance of extended type of ValuchangedcallBack
   * @param <T> extended type of ValuChangedCallback
   * @return a instance of Subscription.
   */
  public static <T extends ValueChangedCallback> Subscription subscribe(
      Value<T> subscribable, T observer) {
    List<SubscriptionImpl<?>> subscriptions =
        hooks.computeIfAbsent(subscribable, k -> new ArrayList<>());
    SubscriptionImpl<T> subscription = new SubscriptionImpl<T>(subscribable, observer);
    subscriptions.add(subscription);
    if (subscriptions.size() == 1) {
      if (observer instanceof BooleanValueChangedCallback) {
        LOG.trace(
            "subscribe BooleanValue:[{} hashCode:{}]. total {} subscriptions.",
            subscribable,
            subscribable.hashCode(),
            subscriptions.size());
        ((BooleanValue) subscribable)
            .addValueObserver(
                v -> {
                  LOG.trace(
                      "boolean value changed. subscribable:[{}] value:[{}], total {} subscriptions.",
                      subscribable,
                      v,
                      subscriptions.size());
                  subscriptions.forEach(
                      s -> ((BooleanValueChangedCallback) s.observer).valueChanged(v));
                });
      } else if (observer instanceof IntegerValueChangedCallback) {
        LOG.trace(
            "subscribe IntegerValue:[{} hashCode:{}]. total {} subscriptions.",
            subscribable,
            subscribable.hashCode(),
            subscriptions.size());
        ((IntegerValue) subscribable)
            .addValueObserver(
                v -> {
                  LOG.info(
                      "integer value changed. subscribable:[{}] value:[{}], total {} subscriptions.",
                      subscribable,
                      v,
                      subscriptions.size());
                  subscriptions.forEach(
                      s -> ((IntegerValueChangedCallback) s.observer).valueChanged(v));
                });
      } else if (observer instanceof DoubleValueChangedCallback) {
        LOG.trace(
            "subscribe DoubleValue:[{} hashCode:{}], total {} subscriptions.",
            subscribable,
            subscribable.hashCode(),
            subscriptions.size());
        ((DoubleValue) subscribable)
            .addValueObserver(
                v -> {
                  LOG.trace(
                      "double value changed. subscribable:[{}] value:[{}], total {} subscriptions.",
                      subscribable,
                      v,
                      subscriptions.size());
                  subscriptions.forEach(
                      s -> ((DoubleValueChangedCallback) s.observer).valueChanged(v));
                });
      } else if (observer instanceof StringValueChangedCallback) {
        LOG.trace(
            "subscribe StringValue:[{} hashCode:{}], total {} subscriptions.",
            subscribable,
            subscribable.hashCode(),
            subscriptions.size());
        ((StringValue) subscribable)
            .addValueObserver(
                v -> {
                  LOG.info(
                      "string value changed. subscribable:[{}] value:[{}], total {} subscriptions.",
                      subscribable,
                      v,
                      subscriptions.size());
                  subscriptions.forEach(
                      s -> ((StringValueChangedCallback) s.observer).valueChanged(v));
                });
      } else {
        LOG.error(
            "unsupported value tyep [{} hashCode:{}].", subscribable, subscribable.hashCode());
        throw new UnsupportedOperationException("Unsupported Value type[" + subscribable + "].");
      }
      subscribable.subscribe();
    }
    return subscription;
  }
}
