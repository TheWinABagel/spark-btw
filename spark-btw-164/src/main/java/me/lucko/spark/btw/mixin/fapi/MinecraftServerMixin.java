package me.lucko.spark.btw.mixin.fapi;

import me.lucko.spark.fabricapi.ServerEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities()V"), method = "tick")
    private void onStartTick(CallbackInfo ci) {
        ServerEvents.START_SERVER_TICK.invoker().onStartTick((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onEndTick(CallbackInfo ci) {
        ServerEvents.END_SERVER_TICK.invoker().onEndTick((MinecraftServer) (Object) this);
    }
}
