package me.lucko.spark.btw.mixin.fapi;

import me.lucko.spark.fabricapi.ClientEvents;
import net.minecraft.src.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("HEAD"), method = "runTick")
    private void onStartTick(CallbackInfo info) {
        ClientEvents.START_CLIENT_TICK.invoker().onStartTick((Minecraft) (Object) this);
    }

    @Inject(at = @At("RETURN"), method = "runTick")
    private void onEndTick(CallbackInfo info) {
        ClientEvents.END_CLIENT_TICK.invoker().onEndTick((Minecraft) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/src/ILogAgent;logInfo(Ljava/lang/String;)V", shift = At.Shift.AFTER, remap = false), method = "shutdownMinecraftApplet")
    private void onStopping(CallbackInfo ci) {
        ClientEvents.CLIENT_STOPPING.invoker().onClientStopping((Minecraft) (Object) this);
    }
}
