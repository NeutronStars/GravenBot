package fr.neutronstars.gravenbot.jda;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.CommandManager;
import fr.neutronstars.gravenbot.utils.Language;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class JDAListener extends ListenerAdapter
{
    @Override
    public void onReady(ReadyEvent event)
    {

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot() || event.getGuild() == null || event.getGuild().getIdLong() != GravenBot.getGuildId()) return;

        String message = event.getMessage().getContentRaw();
        String[] commands = new String[0];

        String expectedPrefix = "!";

        if(message.startsWith(event.getJDA().getSelfUser().getAsMention()))
            commands = message.replaceFirst(event.getJDA().getSelfUser().getAsMention(), "").trim().split(" ");
        else if(message.startsWith(expectedPrefix))
            commands = message.replaceFirst(expectedPrefix, "").trim().split(" ");

        if(commands.length == 0) return;

        if(event.getGuild() != null && !hasPermission(event.getTextChannel(), event.getGuild().getSelfMember())) return;

        if(!CommandManager.hasCommand(commands[0].toLowerCase())) return;

        String[] args = new String[commands.length - 1];
        for(int i = 1; i < commands.length; i++)
            args[i-1] = commands[i];

        if(args.length > 0)
        {
            int realLength = 0;
            for(String arg : args)
                if(arg.length() > 0) realLength++;
            if(realLength != args.length)
            {
                String[] realArgs = new String[realLength];
                int x = 0;
                for(int i = 0; i < args.length; i++)
                {
                    if(args[i].length() == 0) continue;
                    realArgs[x] = args[i];
                    x++;
                }
                args = realArgs;
            }
        }

        try {
            if(!CommandManager.onCommand(event.getAuthor(), event.getMessage(), commands[0].toLowerCase(), args))
                event.getChannel().sendMessage(Language.getDefaultLanguage().get("not_permission")).queue();
        }catch (Throwable t){
            GravenBot.getLogger().error(t.getMessage(), t);
        }
    }

    private boolean hasPermission(TextChannel channel, Member member)
    {
        PermissionOverride override = channel.getPermissionOverride(member);
        if(override != null)
            return override.getAllowed().contains(Permission.MESSAGE_WRITE);

        for(Role role : member.getRoles())
        {
            override = channel.getPermissionOverride(role);
            if(override != null && override.getAllowed().contains(Permission.MESSAGE_WRITE)) return true;
        }

        return member.hasPermission(Permission.MESSAGE_WRITE);
    }
}
