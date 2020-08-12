package io.flutter.plugins;

import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.inapppurchase.InAppPurchasePlugin;
import com.pinto.pay.PayPlugin;

/**
 * Generated file. Do not edit.
 */
public final class GeneratedPluginRegistrant {
  public static void registerWith(PluginRegistry registry) {
    if (alreadyRegisteredWith(registry)) {
      return;
    }
    InAppPurchasePlugin.registerWith(registry.registrarFor("io.flutter.plugins.inapppurchase.InAppPurchasePlugin"));
    PayPlugin.registerWith(registry.registrarFor("com.pinto.pay.PayPlugin"));
  }

  private static boolean alreadyRegisteredWith(PluginRegistry registry) {
    final String key = GeneratedPluginRegistrant.class.getCanonicalName();
    if (registry.hasPlugin(key)) {
      return true;
    }
    registry.registrarFor(key);
    return false;
  }
}
