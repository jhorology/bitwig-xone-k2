package com.github.jhorology.bitwig.utils;

import com.bitwig.extension.controller.api.EnumDefinition;
import com.bitwig.extension.controller.api.EnumValueDefinition;
import java.lang.reflect.ParameterizedType;

/**
 * An interface for enumerize EnumValueDefinition
 *
 * @param <E> enum type. (recursive generic type)
 */
@SuppressWarnings("unchecked")
public interface SettingEnum<E extends Enum<E> & EnumValueDefinition> extends EnumValueDefinition {

  /**
   * Returns a concrete enum type.
   *
   * @return this enum type.
   */
  default Class<E> getEnumType() {
    // TODO always correct ?
    return (Class<E>)
        ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  @Override
  default int getValueIndex() {
    return ((E) this).ordinal();
  }

  @Override
  default String getId() {
    return ((E) this).name();
  }

  @Override
  default String getDisplayName() {
    return getId();
  }

  @Override
  default String getLimitedDisplayName(int maxLength) {
    // TODO Will Bitwig Studio do something?
    return ((E) this).getDisplayName();
  }

  /**
   * Gets the enum definition to which belongs this value.
   *
   * @since API version 11
   */
  @Override
  default EnumDefinition enumDefinition() {
    return new EnumDefinition() {
      private Class<E> enumType;

      private EnumDefinition of(Class<E> enumType) {
        this.enumType = enumType;
        return this;
      }

      /**
       * Gets the number of entries in the enum, must be greater than 0.
       *
       * @since API version 11
       */
      @Override
      public int getValueCount() {
        return enumType.getEnumConstants().length;
      }

      /**
       * Gets the {@Link EnumValueDefinition} for the given index.
       *
       * @param valueIndex must be in the range 0 .. {@link #getValueCount()} - 1.
       * @return null if not found
       * @since API version 11
       */
      @Override
      public EnumValueDefinition valueDefinitionAt(int valueIndex) {
        return enumType.getEnumConstants()[valueIndex];
      }

      /**
       * Gets the {@Link EnumValueDefinition} for the given enum id.
       *
       * @param id
       * @return null if not found
       * @since API version 11
       */
      @Override
      public EnumValueDefinition valueDefinitionFor(String id) {
        return Enum.valueOf(enumType, id);
      }
    }.of(getEnumType());
  }
}
