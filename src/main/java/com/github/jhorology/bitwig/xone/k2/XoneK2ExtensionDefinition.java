package com.github.jhorology.bitwig.xone.k2;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;
import java.util.UUID;

public class XoneK2ExtensionDefinition extends ControllerExtensionDefinition {
  private static final UUID DRIVER_ID = UUID.fromString("3ac1ceb2-248f-4c52-b5e5-98e86f2226cc");

  public XoneK2ExtensionDefinition() {}

  @Override
  public String getName() {
    return "XONE:K2";
  }

  @Override
  public String getAuthor() {
    return "Masafumi";
  }

  @Override
  public String getVersion() {
    return "0.1-SNAPSHOT";
  }

  @Override
  public UUID getId() {
    return DRIVER_ID;
  }

  @Override
  public String getHardwareVendor() {
    return "Alen & Heath";
  }

  @Override
  public String getHardwareModel() {
    return "XONE:K2";
  }

  @Override
  public int getRequiredAPIVersion() {
    return 13;
  }

  @Override
  public int getNumMidiInPorts() {
    return 1;
  }

  @Override
  public int getNumMidiOutPorts() {
    return 1;
  }

  @Override
  public void listAutoDetectionMidiPortNames(
      final AutoDetectionMidiPortNamesList list, final PlatformType platformType) {
    if (platformType == PlatformType.WINDOWS) {
      // TODO: Set the correct names of the ports for auto detection on Windows platform here
      // and uncomment this when port names are correct.
      // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
    } else if (platformType == PlatformType.MAC) {
      list.add(new String[] {"XONE:K2"}, new String[] {"XONE:K2"});
    } else if (platformType == PlatformType.LINUX) {
      // TODO: Set the correct names of the ports for auto detection on Windows platform here
      // and uncomment this when port names are correct.
      // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
    }
  }

  @Override
  public XoneK2Extension createInstance(final ControllerHost host) {
    return new XoneK2Extension(this, host);
  }
}
