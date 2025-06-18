package com.eirmax.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class TridentKeyBind {
    public static KeyBinding trident_key;

    public static void registerKeyBinds(){

        trident_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.legendaryweapons2.trident_key",
                GLFW.GLFW_KEY_G,
                "category.legendaryweapons2"
        ));
    }

}
