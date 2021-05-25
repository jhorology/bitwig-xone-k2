package com.github.jhorology.bitwig.utils;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.callback.*;
import com.bitwig.extension.controller.api.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple subscription model. */
public class Hook {
  private static final Logger LOG = LoggerFactory.getLogger(Hook.class);

  // TODO  Can Value be used as hash key?
  private static final HashMap<Value<?>, List<SubscriptionImpl>> hooks = new HashMap<>();

  /** An interface of unsubscribable subscription of Value object. */
  public static interface Subscription {
    void unsubscribe();
  }

  /** An interface o */
  private static class SubscriptionImpl implements Subscription {
    private final Value<?> value;
    private final Consumer<?> consumer;

    private SubscriptionImpl(Value<?> value, Consumer<?> consumer) {
      this.value = value;
      this.consumer = consumer;
    }

    @Override
    public void unsubscribe() {
      List<SubscriptionImpl> subscriptions = hooks.get(value);

      if (subscriptions == null) {
        LOG.error("unsubscribe(): value[{}] not found!!", value);
        return;
      }

      boolean removed = subscriptions.remove(this);
      if (!removed) {
        LOG.error(
            "unsubscribe(): subscription not found!! value:[{}]. subscriptions total {}.",
            value,
            subscriptions.size());
        return;
      }
      LOG.trace(
          "unsubscribe(): subscription was removed, value[{}] subscriptions total {}",
          value,
          subscriptions.size());

      if (subscriptions.isEmpty()) {
        //              hooks.remove(value);
        value.unsubscribe();
        //              LOG.trace(
        //                  "unsubscribe(): value[{}] was removed. subscribing values total {}.",
        //                  value,
        //                  hooks.keySet().size());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeBoolean(
      Value<BooleanValueChangedCallback> value, Consumer<Boolean> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  v -> list.forEach(s -> ((Consumer<Boolean>) s.consumer).accept(v)));
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeInteger(
      Value<IntegerValueChangedCallback> value, Consumer<Integer> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  (int v) -> list.forEach(s -> ((Consumer<Integer>) s.consumer).accept(v)));
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeDouble(
      Value<DoubleValueChangedCallback> value, Consumer<Double> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  (double v) -> list.forEach(s -> ((Consumer<Double>) s.consumer).accept(v)));
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeString(
      Value<ObjectValueChangedCallback<String>> value, Consumer<String> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  (String v) -> list.forEach(s -> ((Consumer<String>) s.consumer).accept(v)));
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeColor(
      Value<ColorValueChangedCallback> value, Consumer<Color> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  (float red, float green, float blue) -> {
                    list.forEach(
                        s ->
                            ((Consumer<Color>) s.consumer).accept(Color.fromRGB(red, green, blue)));
                  });
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  public static Subscription subscribeStringArray(
      Value<ObjectValueChangedCallback<String[]>> value, Consumer<String[]> consumer) {
    List<SubscriptionImpl> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl> list = new ArrayList<>();
              value.addValueObserver(
                  (String[] v) -> list.forEach(s -> ((Consumer<String[]>) s.consumer).accept(v)));
              value.subscribe();
              return list;
            });
    SubscriptionImpl subscription = new SubscriptionImpl(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }
}
