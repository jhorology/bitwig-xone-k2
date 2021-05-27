package com.github.jhorology.bitwig.utils;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.ColorValueChangedCallback;
import com.bitwig.extension.callback.DoubleValueChangedCallback;
import com.bitwig.extension.callback.EnumValueChangedCallback;
import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.callback.ObjectValueChangedCallback;
import com.bitwig.extension.callback.StringArrayValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.callback.ValueChangedCallback;
import com.bitwig.extension.controller.api.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple subscription model. */
@SuppressWarnings("unchecked")
public class Hook {
  private static final Logger LOG = LoggerFactory.getLogger(Hook.class);
  private static HashMap<Value<?>, List<SubscriptionImpl<?>>> hooks;

  /** An interface of unsubscribable subscription of Value object. */
  public static interface Subscription<T> extends Supplier<T> {
    void unsubscribe();
  }

  /** An interface o */
  private static class SubscriptionImpl<T> implements Subscription<T>, Consumer<T> {
    private final Value<?> value;
    private final Consumer<T> consumer;
    private T currentValue;

    private SubscriptionImpl(Value<?> value, Consumer<T> consumer) {
      this.value = value;
      this.consumer = consumer;
    }

    @Override
    public void unsubscribe() {
      List<SubscriptionImpl<?>> subscriptions = hooks.get(value);

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
        value.unsubscribe();
      }
    }

    @Override
    public void accept(T currentValue) {
      consumer.accept(currentValue);
      this.currentValue = currentValue;
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public T get() {
      return currentValue;
    }
  }

  public static void init() {
    hooks = new HashMap<>();
  }

  public static void exit() {
    hooks.clear();
    hooks = null;
  }

  private static interface CallbackFactory<T extends ValueChangedCallback>
      extends Function<List<SubscriptionImpl<?>>, T> {}
  ;

  private static final List<Pair<Class<? extends ValueChangedCallback>, CallbackFactory<?>>>
      CALLBACK_FACTORIES = new ArrayList<>();

  static {
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            BooleanValueChangedCallback.class,
            list ->
                (BooleanValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<Boolean>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            ColorValueChangedCallback.class,
            list ->
                (ColorValueChangedCallback)
                    (r, g, b) ->
                        list.forEach(
                            s -> ((SubscriptionImpl<Color>) s).accept(Color.fromRGB(r, g, b)))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            DoubleValueChangedCallback.class,
            list ->
                (DoubleValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<Double>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            EnumValueChangedCallback.class,
            list ->
                (EnumValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<String>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            IntegerValueChangedCallback.class,
            list ->
                (IntegerValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<Integer>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            StringArrayValueChangedCallback.class,
            list ->
                (StringArrayValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<String[]>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            StringValueChangedCallback.class,
            list ->
                (StringValueChangedCallback)
                    v -> list.forEach(s -> ((SubscriptionImpl<String>) s).accept(v))));
    CALLBACK_FACTORIES.add(
        new ImmutablePair<>(
            ObjectValueChangedCallback.class,
            list ->
                (ObjectValueChangedCallback<?>)
                    v -> list.forEach(s -> ((SubscriptionImpl<Object>) s).accept(v))));
  }

  public static <T extends ValueChangedCallback> void use(Value<T> value) {
    CallbackFactory<T> factory = getCallbackFactory(value);
    if (factory == null) {
      throw new UnsupportedOperationException("Unsupported value type [" + value + "]");
    }
    hooks.computeIfAbsent(
        value,
        k -> {
          List<SubscriptionImpl<?>> list = new ArrayList<>();
          value.addValueObserver(factory.apply(list));
          return list;
        });
  }

  public static <T extends ValueChangedCallback, C> Subscription<C> subscribe(
      Value<T> value, Consumer<C> consumer) {
    CallbackFactory<T> factory = getCallbackFactory(value);
    if (factory == null) {
      throw new UnsupportedOperationException("Unsupported value type [" + value + "]");
    }
    List<SubscriptionImpl<?>> subscriptions =
        hooks.computeIfAbsent(
            value,
            k -> {
              List<SubscriptionImpl<?>> list = new ArrayList<>();
              value.addValueObserver(factory.apply(list));
              return list;
            });
    if (subscriptions.size() == 0) {
      value.subscribe();
    }
    SubscriptionImpl<C> subscription = new SubscriptionImpl<>(value, consumer);
    subscriptions.add(subscription);
    LOG.trace("subscribe(): value[{}]. subscriptions total {}.", value, subscriptions.size());
    return subscription;
  }

  @SuppressWarnings("unchecked")
  private static <T extends ValueChangedCallback> CallbackFactory<T> getCallbackFactory(
      Value<T> value) {
    return CALLBACK_FACTORIES.stream()
        .filter(
            p -> {
              try {
                value.getClass().getMethod("addValueObserver", p.getLeft());
                return true;
              } catch (NoSuchMethodException | SecurityException ex) {
                return false;
              }
            })
        .map(p -> (CallbackFactory<T>) p.getRight())
        .findFirst()
        .orElse(null);
  }
}
