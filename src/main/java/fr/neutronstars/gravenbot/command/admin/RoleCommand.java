package fr.neutronstars.gravenbot.command.admin;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.Command;
import fr.neutronstars.gravenbot.jda.JDAManager;
import fr.neutronstars.gravenbot.utils.Language;
import fr.neutronstars.gravenbot.utils.RoleManager;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.Map;

public class RoleCommand extends Command
{
    public RoleCommand()
    {
        super("role", true, true);
    }

    @Override
    public boolean onCommand(User user, Message message, String label, String[] args)
    {
        if(args.length == 0)
        {
            if(!RoleManager.hasRole())
            {
                message.getChannel().sendMessage(Language.getDefaultLanguage().get("not_roles_register")).queue();
                return true;
            }

            message.getChannel().sendMessage(Language.getDefaultLanguage().get("roles_message")).queue(msg -> {
                for(Map.Entry<String, Long> entry : RoleManager.getRoleMap().entrySet())
                {
                    try {
                        Emote emote = JDAManager.getShardManager().getEmoteById(Long.parseLong(entry.getKey()));
                        msg.addReaction(emote).queue();
                    }catch (NumberFormatException nfe){
                        msg.addReaction(entry.getKey()).queue();
                    }
                }
                GravenBot.getConfig().set("role_message", msg.getIdLong());
                GravenBot.saveConfig();
            });

            return true;
        }

        String arg = getArgsDetect(args[0].toLowerCase(), "add", "remove", "list");

        if(arg == null)
        {
            message.getChannel().sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove|list>").queue();
            return true;
        }

        switch (arg)
        {
            case "add":
                add(message.getChannel(), args.length > 1 ? args[1] : null, !message.getMentionedRoles().isEmpty() ? message.getMentionedRoles().get(0) : null);
                break;
            case "remove":
                remove(message.getChannel(), args.length > 1 ? args[1] : null);
                break;
            case "list":
                list(message.getChannel());
                break;
        }
        return true;
    }

    private void add(MessageChannel channel, String key, Role role)
    {
        if(key == null || role == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" add <Emote> <@Role>").queue();
            return;
        }

        RoleManager.addRoles(key, role.getIdLong());
        RoleManager.saveRoles();

        channel.sendMessage(Language.getDefaultLanguage().get("new_role", key, role.getName())).queue();
    }

    private void remove(MessageChannel channel, String key)
    {
        if(key == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" remove <Emote>").queue();
            return;
        }

        if(!RoleManager.hasRole(key))
        {
            channel.sendMessage(Language.getDefaultLanguage().get("not_found_role", key)).queue();
            return;
        }

        RoleManager.removeRoles(key);
        RoleManager.saveRoles();

        channel.sendMessage(Language.getDefaultLanguage().get("remove_role", key)).queue();
    }

    private void list(MessageChannel channel)
    {
        if(!RoleManager.hasRole())
        {
            channel.sendMessage(Language.getDefaultLanguage().get("not_roles_register")).queue();
            return;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(Language.getDefaultLanguage().get("role_view")).append("\n");

        for(Map.Entry<String, Long> entry : RoleManager.getRoleMap().entrySet())
        {
            Emote emote = null;
            Role role = JDAManager.getShardManager().getRoleById(entry.getValue());
            try {
                emote = JDAManager.getShardManager().getEmoteById(Long.parseLong(entry.getKey()));
            }catch (NumberFormatException nfe){}

            builder.append("\n").append(emote != null ? emote.getAsMention() : entry.getKey())
                    .append(" >> ").append(role != null ? role.getAsMention() : entry.getValue());
        }

        channel.sendMessage(builder.toString()).queue();
    }
}
