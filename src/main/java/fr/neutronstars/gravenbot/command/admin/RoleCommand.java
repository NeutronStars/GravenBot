package fr.neutronstars.gravenbot.command.admin;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.Command;
import fr.neutronstars.gravenbot.jda.JDAManager;
import fr.neutronstars.gravenbot.utils.Configuration;
import fr.neutronstars.gravenbot.utils.Language;
import fr.neutronstars.gravenbot.manager.RoleManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.json.JSONArray;

import java.io.IOException;
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
            message.getChannel().sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove|list|here|anti>").queue();
            return true;
        }

        String arg = getArgsDetect(args[0].toLowerCase(), "add", "remove", "list", "here", "anti");

        if(arg == null)
        {
            message.getChannel().sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove|list|here|anti>").queue();
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
            case "here":
                here(message.getChannel());
                break;
            case "anti":
                anti(message.getChannel(), args.length > 1 ? args[1] : null, !message.getMentionedMembers().isEmpty() ? message.getMentionedMembers().get(0) : null, !message.getMentionedRoles().isEmpty() ? message.getMentionedRoles().get(0) : null);
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

    private void here(MessageChannel channel)
    {
        if(!RoleManager.hasRole())
        {
            channel.sendMessage(Language.getDefaultLanguage().get("not_roles_register")).queue();
            return;
        }

        channel.sendMessage(Language.getDefaultLanguage().get("roles_message")).queue(msg -> {
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
    }

    private void anti(MessageChannel channel, String param, Member member, Role role)
    {
        if(param == null || member == null || role == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove> <@User> <@Role>").queue();
            return;
        }

        if(member.hasPermission(Permission.ADMINISTRATOR))
        {
            channel.sendMessage(Language.getDefaultLanguage().get("cant_add_remove_role_administrator")).queue();
            return;
        }

        String arg = getArgsDetect(param, "add", "remove");

        if(arg == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove> <@User> <@Role>").queue();
            return;
        }

        switch (arg)
        {
            case "add":
                addAnti(channel, member, role);
                break;
            case "remove":
                removeAnti(channel, member, role);
                break;
        }
    }

    private void addAnti(MessageChannel channel, Member member, Role role)
    {
        Configuration configuration = GravenBot.getAntiRoleConfiguration();
        JSONArray array;

        if(!configuration.has(member.getUser().getId()))
            array = new JSONArray();
        else array = (JSONArray) configuration.get(member.getUser().getId());

        member.getGuild().getController().addRolesToMember(member, role).queue(aVoid ->
            channel.sendMessage(Language.getDefaultLanguage().get("add_role_anti_confirm", role.getName(), member.getAsMention())).queue()
        , throwable ->
            channel.sendMessage(Language.getDefaultLanguage().get("add_role_anti_error", role.getName(), member.getAsMention())).queue()
        );

        for(int i = 0; i < array.length(); i++)
        {
            if(array.getLong(i) == role.getIdLong())
                return;
        }

        array.put(role.getIdLong());
        configuration.set(member.getUser().getId(), array);
        configuration.save();
    }

    private void removeAnti(MessageChannel channel, Member member, Role role)
    {
        Configuration configuration = GravenBot.getAntiRoleConfiguration();
        JSONArray array;

        if(!configuration.has(member.getUser().getId()))
            array = new JSONArray();
        else array = (JSONArray) configuration.get(member.getUser().getId());

        member.getGuild().getController().removeRolesFromMember(member, role).queue(aVoid ->
             channel.sendMessage(Language.getDefaultLanguage().get("remove_role_anti_confirm", role.getName(), member.getAsMention())).queue()
        , throwable ->
             channel.sendMessage(Language.getDefaultLanguage().get("remove_role_anti_error", role.getName(), member.getAsMention())).queue()
        );

        for(int i = 0; i < array.length(); i++)
        {
            if(array.getLong(i) == role.getIdLong())
            {
                array.remove(i);
                break;
            }
        }

        if(array.length() == 0)
            configuration.set(member.getUser().getId(), null);
        else
            configuration.set(member.getUser().getId(), array);

        configuration.save();
    }
}
