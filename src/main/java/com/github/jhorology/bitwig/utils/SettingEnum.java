package com.github.jhorology.bitwig.utils;

import com.bitwig.extension.controller.api.EnumDefinition;
import com.bitwig.extension.controller.api.EnumValueDefinition;

/**
 * An interface for enumerize EnumValueDefinition
 *
 * @param <E> enum type.
 */
public interface SettingEnum<E extends Enum<E> & EnumValueDefinition> extends EnumValueDefinition {
  /**
   * Returns a concrete enum type.
   *
   * @return this enum type.
   */
  Class<E> getEnumType();

  @SuppressWarnings("unchecked")
  default int getValueIndex() {
    return ((E) this).ordinal();
  }

  @SuppressWarnings("unchecked")
  default String getId() {
    return ((E) this).name();
  }

  @SuppressWarnings("unchecked")
  default String getLimitedDisplayName(int maxLength) {
    return ((E) this).getDisplayName();
  }

  /**
   * Gets the enum definition to which belongs this value.
   *
   * @since API version 11
   */
  default EnumDefinition enumDefinition() {
    return new EnumDefinition() {
      private Class<E> enumType;

      private EnumDefinition enumType(Class<E> enumType) {
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
    }.enumType(getEnumType());
  }
}
