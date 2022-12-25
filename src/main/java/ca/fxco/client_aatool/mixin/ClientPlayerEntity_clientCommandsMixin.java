package ca.fxco.client_aatool.mixin;

import ca.fxco.client_aatool.ClientCommands;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntity_clientCommandsMixin {


    @Inject(
            at = @At("HEAD"),
            method = "sendCommand(Ljava/lang/String;Lnet/minecraft/text/Text;)V",
            cancellable = true
    )
    private void onSendCommand(String message, Text text, CallbackInfo ci) {
        if (message.startsWith("/")) {
            StringReader reader = new StringReader(message);
            reader.skip();
            int cursor = reader.getCursor();
            reader.setCursor(cursor);
            if (ClientCommands.isClientSideCommand(message.substring(1).split(Pattern.quote(" ")))) {
                ClientCommands.executeCommand(reader);
                ci.cancel();
            }
        }
    }
}
