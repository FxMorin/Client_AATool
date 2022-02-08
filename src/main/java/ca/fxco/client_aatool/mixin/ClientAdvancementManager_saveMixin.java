package ca.fxco.client_aatool.mixin;

import ca.fxco.client_aatool.ClientAATool;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

@Mixin(ClientAdvancementManager.class)
public class ClientAdvancementManager_saveMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Final
    @Shadow
    private Map<Advancement, AdvancementProgress> advancementProgresses;

    private File advancementFile = null;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer())
            .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .setPrettyPrinting()
            .create();


    @Inject(
            method = "onAdvancements(Lnet/minecraft/network/packet/s2c/play/AdvancementUpdateS2CPacket;)V",
            at = @At("RETURN")
    )
    public void onAdvancementsSave(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        if (ClientAATool.shouldLog && !this.client.isIntegratedServerRunning()) {
            if (advancementFile == null) {
                String uuid = client.player != null ? client.player.getUuidAsString() : "advancements";
                advancementFile = new File(client.runDirectory, "client_aatool/server/advancements/" + uuid + ".json");
                try {
                    advancementFile.getParentFile().mkdirs();
                    advancementFile.createNewFile();
                } catch (IOException err) {
                    ClientAATool.LOGGER.error("Couldn't save advancements", err);
                }
            }
            save();
        }
    }


    private void save() {
        Map<Identifier, AdvancementProgress> map = Maps.newHashMap();
        for (Map.Entry<Advancement, AdvancementProgress> entry : advancementProgresses.entrySet()) {
            AdvancementProgress advancementProgress = entry.getValue();
            if (advancementProgress.isAnyObtained()) {
                map.put((entry.getKey()).getId(), advancementProgress);
            }
        }
        JsonElement jsonElement = GSON.toJsonTree(map);
        int version = SharedConstants.getGameVersion().getWorldVersion();
        jsonElement.getAsJsonObject().addProperty("DataVersion", version);
        try {
            FileOutputStream entry = new FileOutputStream(this.advancementFile);
            try {
                OutputStreamWriter advancementProgress = new OutputStreamWriter(entry, Charsets.UTF_8.newEncoder());
                try {
                    GSON.toJson(jsonElement, advancementProgress);
                } catch (Throwable gsonErr) {
                    try {
                        advancementProgress.close();
                    } catch (Throwable closeErr) {
                        gsonErr.addSuppressed(closeErr);
                    }
                    throw gsonErr;
                }
                advancementProgress.close();
            } catch (Throwable closeErr) {
                try {
                    entry.close();
                } catch (Throwable fileErr) {
                    closeErr.addSuppressed(fileErr);
                }
                throw closeErr;
            }
            entry.close();
        } catch (IOException err) {
            ClientAATool.LOGGER.error("Couldn't save player advancements to {}\n{}", this.advancementFile, err);
        }
    }
}
