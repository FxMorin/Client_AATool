package ca.fxco.client_aatool.mixin;

import ca.fxco.client_aatool.ClientAATool;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandler_saveMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    private File statisticsFile = null;


    @Inject(
            method = "onStatistics(Lnet/minecraft/network/packet/s2c/play/StatisticsS2CPacket;)V",
            at = @At("RETURN")
    )
    public void onStatisticsSave(StatisticsS2CPacket packet, CallbackInfo ci) {
        if (ClientAATool.shouldLog && !this.client.isIntegratedServerRunning() && this.client.player != null) {
            if (statisticsFile == null) {
                String uuid = client.player.getUuidAsString();
                statisticsFile = new File(client.runDirectory, "client_aatool/server/stats/"+ uuid + ".json");
                try {
                    statisticsFile.getParentFile().mkdirs();
                    statisticsFile.createNewFile();
                } catch (IOException err) {
                    ClientAATool.LOGGER.error("Couldn't create stat file", err);
                }
            }
            save(asString(this.client.player.getStatHandler()));
        }
    }

    private void save(String string) {
        try {
            FileUtils.writeStringToFile(statisticsFile, string,Charset.defaultCharset(), false);
        } catch (IOException err) {
            ClientAATool.LOGGER.error("Couldn't save stats", err);
        }
    }

    private String asString(StatHandler statHandler) {
        Map<StatType<?>, JsonObject> map = Maps.newHashMap();

        StatHandlerAccessor accessor = (StatHandlerAccessor)statHandler;
        for (Object2IntMap.Entry<Stat<?>> statEntry : accessor.getStatMap().object2IntEntrySet()) {
            Stat<?> stat = statEntry.getKey();
            (map.computeIfAbsent(stat.getType(), (statType) -> {
                return new JsonObject();
            })).addProperty(getStatId(stat).toString(), statEntry.getIntValue());
        }

        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<StatType<?>, JsonObject> statTypeJsonObjectEntry : map.entrySet()) {
            jsonObject.add(Registry.STAT_TYPE.getId(
                    statTypeJsonObjectEntry.getKey()).toString(),
                    statTypeJsonObjectEntry.getValue()
            );
        }
        JsonObject entry = new JsonObject();
        entry.add("stats", jsonObject);
        entry.addProperty("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        return entry.toString();
    }

    private static <T> Identifier getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }
}
