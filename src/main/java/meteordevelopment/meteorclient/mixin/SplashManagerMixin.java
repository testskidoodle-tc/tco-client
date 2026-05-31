/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.tco.TcoSplashes;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(SplashManager.class)
public abstract class SplashManagerMixin {
    private static final Random RANDOM = new Random();

    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashRenderer> cir) {
        // Hide vanilla yellow splash — TcoTitleOverlay draws animated custom splashes
        cir.setReturnValue(new SplashRenderer(Component.empty()));
    }
}
