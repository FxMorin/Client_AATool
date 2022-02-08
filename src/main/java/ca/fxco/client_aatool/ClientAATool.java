package ca.fxco.client_aatool;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientAATool implements ClientModInitializer {

    public static boolean shouldLog = false;

    public static final Map<String,Boolean> servers = new HashMap<>();

    public static final Logger LOGGER = LogManager.getLogger("ClientAATool");

    @Override
    public void onInitializeClient() {}
}
