package ca.fxco.client_aatool;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.literal;

public class ClientCommands {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static final String PREFIX = "aatool";


    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal(PREFIX)
                .then(literal("clear").executes(ClientCommands::clearCommand))
                .then(CommandManager.argument("toggle", BoolArgumentType.bool())
                        .executes(c -> toggleCommand(c,true)))
                .executes(c -> toggleCommand(c,false));
        dispatcher.register(builder);
    }

    public static int toggleCommand(CommandContext<ServerCommandSource> context, boolean toggle) {
        if (mc.isIntegratedServerRunning()) {
            sendToPlayer("Client_AATool can only be used on servers");
            return 0;
        }
        if (!toggle) {
            sendToPlayer("ClientAATool is current: "+(ClientAATool.shouldLog ? "enabled" : "disabled"));
        } else {
            boolean state = BoolArgumentType.getBool(context,"toggle");
            ClientAATool.shouldLog = state;
            if (mc.getCurrentServerEntry() != null) ClientAATool.servers.put(mc.getCurrentServerEntry().address,state);
            sendToPlayer("ClientAATool is now: "+(state ? "enabled" : "disabled"));
        }
        return 1;
    }

    public static int clearCommand(CommandContext<ServerCommandSource> context) {
        File server = new File(mc.runDirectory, "client_aatool/server");
        if (server.exists()) {
            if (server.delete()) {
                sendToPlayer("Cleared ClientAATool Files");
            } else {
                sendToPlayer("Unable to clear ClientAATool Files!");
            }
        } else {
            sendToPlayer("ClientAATool Files are already cleared!");
        }
        return 1;
    }

    public static void sendToPlayer(String str) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of(str), false);
        }
    }

    public static boolean isClientSideCommand(String[] args) {
        return (args.length > 0 && PREFIX.equals(args[0]));
    }

    public static void executeCommand(StringReader reader) {
        ClientPlayerEntity p = mc.player;
        try {
            if (p != null) p.networkHandler.getCommandDispatcher().execute(reader, new FakeCommandSource(p));
        } catch (Exception e) {
            ClientAATool.LOGGER.error("An issue has happened while attempting to execute client command!",e);
        }
    }

    public static class FakeCommandSource extends ServerCommandSource {
        public FakeCommandSource(ClientPlayerEntity player) {
            super(player, player.getPos(), player.getRotationClient(), null, 0, player.getEntityName(),
                    player.getName(), null, player);
        }

        public Collection<String> getPlayerNames() {
            return mc.getNetworkHandler()
                    .getPlayerList()
                    .stream()
                    .map(e -> e.getProfile().getName())
                    .collect(Collectors.toList());
        }
    }
}
