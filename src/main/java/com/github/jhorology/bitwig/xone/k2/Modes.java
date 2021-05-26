package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.controller.api.EnumValue;
import com.bitwig.extension.controller.api.EnumValueDefinition;
import com.github.jhorology.bitwig.utils.SettingEnum;

public class Modes {
  public static enum Nav1Mode implements SettingEnum<Nav1Mode> {
    TEST1("Test 1"),
    TEST2("Test 2"),
    TEST3("Test 2")
    ;


    private final String displayName;

    private Nav1Mode(String displayName) {
      this.displayName = displayName;
    }
    /**
     * Returns a concrete enum type.
     *
     * @return this enum type.
     */
    @Override
    public Class<Nav1Mode> getEnumType() {
      return Nav1Mode.class;
    }

    /**
     * This is a string that is suitable for display.
     *
     * @since API version 11
     */
    @Override
    public String getDisplayName() {
      return displayName;
    }
  }

  public static enum Nav2Mode implements SettingEnum {
    TEST1("Test 1"),
    TEST2("Test 2"),
    TEST3("Test 2")
    ;


    private final String displayName;

    private Nav2Mode(String displayName) {
      this.displayName = displayName;
    }
    /**
     * Returns a concrete enum type.
     *
     * @return this enum type.
     */
    @Override
    public Class<Nav2Mode> getEnumType() {
      return Nav2Mode.class;
    }

    /**
     * This is a string that is suitable for display.
     *
     * @since API version 11
     */
    @Override
    public String getDisplayName() {
      return displayName;
    }
  }
}
