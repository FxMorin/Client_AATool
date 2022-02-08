package ca.fxco.client_aatool.mixin;

import ca.fxco.client_aatool.ClientAATool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClient_serverChangeMixin {


    @Inject(
            method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V"
            )
    )
    private void onJoinWorld(ClientWorld world, CallbackInfo ci) {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo != null) {
            ClientAATool.shouldLog = ClientAATool.servers.getOrDefault(serverInfo.address, false);
        }
    }
}
