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
    private static final Configuration CONFIGURATION, ANTI_ROLE_CONFIGURATION;

    static {
        Configuration configuration = null;
        Configuration antiRoleConfiguration = null;
        try {
            getLogger().info("Loading configuration...");
            configuration = new Configuration(new File("config.json"));
            getLogger().info("Configuration loaded.");

            getLogger().info("Loading anti role configuration...");
            antiRoleConfiguration = new Configuration(new File("anti-role.json"));
            getLogger().info("Anti Role configuration loaded.");
        } catch (IOException e) {
            getLogger().error(e.getMessage(), e);
        }
        CONFIGURATION = configuration;
        ANTI_ROLE_CONFIGURATION = antiRoleConfiguration;
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

    public static boolean hasGuild()
    {
        return getConfig().getOrSetDefault("guildId", 0L) != 0L;
    }

    public static long getGuildId()
    {
        return (long) getConfig().get("guildId");
    }

    public static Configuration getAntiRoleConfiguration()
    {
        return ANTI_ROLE_CONFIGURATION;
    }

    public static void saveAntiRoleConfiguration()
    {
        if(ANTI_ROLE_CONFIGURATION != null)
            ANTI_ROLE_CONFIGURATION.save();
    }
}
