package fr.neutronstars.gravenbot.jda;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.CommandManager;
import fr.neutronstars.gravenbot.utils.Configuration;
import fr.neutronstars.gravenbot.utils.Language;
import fr.neutronstars.gravenbot.manager.QuizManager;
import fr.neutronstars.gravenbot.manager.RoleManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class JDAListener extends ListenerAdapter
{
    @Override
    public void onReady(ReadyEvent event)
    {
        if(event.getJDA().getShardInfo().getShardId()+1 == event.getJDA().getShardInfo().getShardTotal())
            GravenBot.getLogger().info(event.getJDA().getSelfUser().getName()+" is ready !");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot()) return;

        if(event.getGuild() == null)
        {
            if(QuizManager.hasQuizUser(event.getAuthor()))
                QuizManager.receiveResponse(event.getAuthor(), event.getMessage().getContentRaw());
            return;
        }

        if(event.getGuild().getIdLong() != GravenBot.getGuildId()) return;

        String message = event.getMessage().getContentRaw();
        String[] commands = new String[0];

        String expectedPrefix = GravenBot.getConfig().getOrSetDefault("prefix", "!");

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

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event)
    {
        if(event.getUser().isBot() || event.getGuild() == null || event.getGuild().getIdLong() != GravenBot.getGuildId()) return;

        if(event.getChannel().getIdLong() == QuizManager.getQuizChannel())
        {
            if(GravenBot.isOwner(event.getUser().getIdLong()) || event.getMember().hasPermission(Permission.ADMINISTRATOR))
            {
                event.getTextChannel().getMessageById(event.getMessageIdLong()).queue(message -> {
                    if(!message.getAuthor().isBot() || !QuizManager.hasWaitingPlayer(message)) return;
                    Role role = JDAManager.getShardManager().getRoleById(QuizManager.getRoleQuizId());
                    if(role == null) return;
                    Member member = message.getMentionedMembers().get(0);
                    switch (event.getReactionEmote().getName())
                    {
                        case "✅":
                            QuizManager.removeWaitingPlayer(member.getUser());
                            message.getGuild().getController().addRolesToMember(member, role).queue();
                            message.clearReactions().queue();
                            message.editMessage(message.getContentRaw()+"\n\n"+Language.getDefaultLanguage().get("accept", event.getUser().getAsMention())).queue();
                            break;
                        case "❌":
                            QuizManager.removeWaitingPlayer(member.getUser());
                            message.clearReactions().queue();
                            message.editMessage(message.getContentRaw()+"\n\n"+Language.getDefaultLanguage().get("deny", event.getUser().getAsMention())).queue();
                            break;
                    }
                });
            }

            return;
        }

        onEmote(event.getMessageIdLong(), event.getGuild(), event.getMember(), event.getReactionEmote(), true);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event)
    {
        if(event.getUser().isBot() || event.getGuild() == null || event.getGuild().getIdLong() != GravenBot.getGuildId()) return;
        onEmote(event.getMessageIdLong(), event.getGuild(), event.getMember(), event.getReactionEmote(), false);
    }

    private void onEmote(long messageId, Guild guild, Member member, MessageReaction.ReactionEmote reactionEmote, boolean add)
    {
        if(messageId == GravenBot.getConfig().getLong("role_message"))
        {
            RoleManager.apply(guild, member, reactionEmote.getId() != null ? String.valueOf(reactionEmote.getIdLong()) : reactionEmote.getName(),add);
            return;
        }

        if(messageId == GravenBot.getConfig().getLong("message_quiz"))
        {
            QuizManager.startNewQuiz(member);
            return;
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        Configuration configuration = GravenBot.getAntiRoleConfiguration();
        if(!configuration.has(event.getUser().getId())) return;

        JSONArray array = (JSONArray) configuration.get(event.getUser().getId());

        List<Role> roles = new ArrayList<>();
        for(int i = 0; i < array.length(); i++)
        {
            Role role = JDAManager.getShardManager().getRoleById(array.getLong(i));
            if(role != null)
                roles.add(role);
        }

        event.getGuild().getController().addRolesToMember(event.getMember(), roles).queue();
    }
}
