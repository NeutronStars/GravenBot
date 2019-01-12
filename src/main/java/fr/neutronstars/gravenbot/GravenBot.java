package fr.neutronstars.gravenbot;

import fr.neutronstars.gravenbot.utils.Configuration;
import org.json.JSONArray;
import org.slf4j.impl.BotLogger;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.IOException;

public class GravenBot
{
    private static final BotLogger BOT_LOGGER = StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger("GravenBot");
    private static final Configuration CONFIGURATION;

    static {
        Configuration configuration = null;
        getLogger().info("Loading configuration...");
        try {
            configuration = new Configuration(new File("config.json"));
            getLogger().info("Configuration loaded.");
        } catch (IOException e) {
            getLogger().error(e.getMessage(), e);
        }
        CONFIGURATION = configuration;
    }

    public static Configuration getConfig()
    {
        return CONFIGURATION;
    }

    public static BotLogger getLogger()
    {
        return BOT_LOGGER;
    }

    public static void saveConfig()
    {
        if(CONFIGURATION != null)
            CONFIGURATION.save();
    }

    public static void saveLogger()
    {
        if(BOT_LOGGER != null) {
            try {
                StaticLoggerBinder.getSingleton().getLoggerFactory().save();
            } catch (IOException e) {
                GravenBot.getLogger().error(e.getMessage(), e);
            }
        }
    }

    public static boolean hasOwner()
    {
        return getConfig().getOrSetDefault("owners", new JSONArray()).length() > 0;
    }

    public static boolean isOwner(long id)
    {
        return getConfig().getOrSetDefault("owners", new JSONArray()).toList().contains(id);
    }
}
