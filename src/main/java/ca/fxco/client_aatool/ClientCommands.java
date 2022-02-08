package ca.fxco.client_aatool;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
                .requires((s) -> !mc.isIntegratedServerRunning())
                .then(literal("clear").executes(ClientCommands::clearCommand))
                .then(CommandManager.argument("toggle", BoolArgumentType.bool())
                        .executes(ClientCommands::toggleCommand))
                .executes(ClientCommands::toggleCommand);
        dispatcher.register(builder);
    }

    public static int toggleCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Boolean state = tryGet(
                () -> BoolArgumentType.getBool(context,"toggle"),
                null
        );
        ServerCommandSource source = context.getSource();
        if (state == null) {
            String str = ClientAATool.shouldLog ? "enabled" : "disabled";
            source.sendFeedback(Text.of("ClientAATool is current: "+str),false);
        } else {
            ClientAATool.shouldLog = state;
            if (mc.getCurrentServerEntry() != null) ClientAATool.servers.put(mc.getCurrentServerEntry().address,state);
            String str = state ? "enabled" : "disabled";
            source.sendFeedback(Text.of("ClientAATool is now: "+str),false);
        }
        return 1;
    }

    public static int clearCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        File server = new File(mc.runDirectory, "client_aatool/server");
        ServerCommandSource source = context.getSource();
        if (server.exists()) {
            if (server.delete()) {
                source.sendFeedback(Text.of("Cleared ClientAATool Files"),false);
            } else {
                source.sendFeedback(Text.of("Unable to clear ClientAATool Files!"), false);
            }
        } else {
            source.sendFeedback(Text.of("ClientAATool Files are already cleared!"),false);
        }
        return 1;
    }

    public static boolean isClientSideCommand(String[] args) {
        return (args.length > 0 && PREFIX.equals(args[0]));
    }

    public static void executeCommand(StringReader reader) {
        ClientPlayerEntity player = mc.player;
        try {
            player.networkHandler.getCommandDispatcher().execute(reader, new FakeCommandSource(player));
        } catch (Exception e) {
            ClientAATool.LOGGER.error("An issue has happened while attempting to execute client command!",e);
        }
    }

    public static class FakeCommandSource extends ServerCommandSource {
        public FakeCommandSource(ClientPlayerEntity p) {
            super(p, p.getPos(), p.getRotationClient(), null, 0, p.getEntityName(), p.getName(), null, p);
        }

        public Collection<String> getPlayerNames() {
            return mc.getNetworkHandler()
                    .getPlayerList()
                    .stream()
                    .map(e -> e.getProfile().getName())
                    .collect(Collectors.toList());
        }
    }

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws CommandSyntaxException;
    }

    public static <T> T tryGet(SupplierWithException<T> a, T defaultValue) throws CommandSyntaxException {
        try {
            return a.get();
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
