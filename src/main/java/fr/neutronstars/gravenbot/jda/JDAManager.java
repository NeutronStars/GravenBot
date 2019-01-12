package fr.neutronstars.gravenbot.jda;

import fr.neutronstars.gravenbot.GravenBot;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class JDAManager
{
    private final static ShardManager SHARD_MANAGER;

    static {
        ShardManager shardManager = null;
        GravenBot.getLogger().info("Connecting JDA Bot...");
        try {
            shardManager = new DefaultShardManagerBuilder()
                    .setToken(GravenBot.getConfig().getOrSetDefault("token","Insert your token here."))
                    .setShardsTotal(GravenBot.getConfig().getOrSetDefault("shards", 1))
                    .addEventListeners(new JDAListener())
                    .build();
        }catch (Exception e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }

        SHARD_MANAGER = shardManager;
    }

    public static ShardManager getShardManager()
    {
        return SHARD_MANAGER;
    }
}
