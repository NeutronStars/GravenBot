package fr.neutronstars.gravenbot.start;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.CommandManager;
import fr.neutronstars.gravenbot.jda.JDAManager;
import fr.neutronstars.gravenbot.sql.SQLManager;
import fr.neutronstars.gravenbot.utils.Language;
import fr.neutronstars.gravenbot.manager.QuizManager;
import fr.neutronstars.gravenbot.manager.RoleManager;
import net.dv8tion.jda.bot.sharding.ShardManager;

public class GravenBotStart
{
    private static volatile boolean running;

    public static void main(String... args)
    {
        Language.register();
        CommandManager.registerCommands();
        RoleManager.loadRoles();
        QuizManager.loadQuestion();
        QuizManager.loadWaitingPlayer();

        boolean restart = false;

        if(!GravenBot.hasOwner())
        {
            restart = true;
            GravenBot.getLogger().warn("Please put at least 1 owner in the \"config.json\".");
        }

        if(!GravenBot.hasGuild())
        {
            restart = true;
            GravenBot.getLogger().warn("Please put your guild id in the \"config.json\".");
        }

        if(!restart)
        {
            ShardManager shardManager = JDAManager.getShardManager();
            if(shardManager == null)
            {
                restart = true;
                GravenBot.getLogger().warn("Insert your token in the \"config.json\".");
            }
            else restart = !SQLManager.loadSQL();
        }


        if(restart)
        {
            save();
            return;
        }

        run();
    }

    private static void run()
    {
        running = true;

        while (running)
        {

        }

        stop();
    }

    public static void shutdown()
    {
        running = false;
    }

    private static void stop()
    {
        GravenBot.getLogger().info("Stopping JDA Bot...");
        JDAManager.getShardManager().shutdown();
        GravenBot.getLogger().info("JDA Bot stopped.");
        save();
    }

    private static void save()
    {
        RoleManager.saveRoles();
        QuizManager.saveQuestion();
        QuizManager.saveWaitingPlayer();
        GravenBot.saveAntiRoleConfiguration();
        GravenBot.saveConfig();
        GravenBot.saveLogger();
    }
}
