package fr.neutronstars.gravenbot.command.admin;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.Command;
import fr.neutronstars.gravenbot.utils.EmoteManager;
import fr.neutronstars.gravenbot.utils.Language;
import fr.neutronstars.gravenbot.utils.QuizManager;
import net.dv8tion.jda.core.entities.*;

import java.util.Map;

public class QuizCommand extends Command
{
    public QuizCommand()
    {
        super("quiz", true, true);
    }

    @Override
    public boolean onCommand(User user, Message message, String label, String[] args)
    {
        if(args.length == 0)
        {
            if(!QuizManager.hasQuestion())
            {
                message.getChannel().sendMessage(Language.getDefaultLanguage().get("not_question")).queue();
                return true;
            }

            if(!QuizManager.hasRoleQuiz())
            {
                message.getChannel().sendMessage(Language.getDefaultLanguage().get("not_role")).queue();
                return true;
            }

            if(!QuizManager.hasQuizChannel())
            {
                message.getChannel().sendMessage(Language.getDefaultLanguage().get("not_channel")).queue();
                return true;
            }

            message.getChannel().sendMessage(Language.getDefaultLanguage().get("message_quiz")).queue(msg -> {
                msg.addReaction(String.valueOf(EmoteManager.CONFIRM.getValue())).queue();
                GravenBot.getConfig().set("message_quiz", msg.getIdLong());
                GravenBot.saveConfig();
            });

            return true;
        }

        String arg = getArgsDetect(args[0], "add", "remove", "set", "replace", "move", "clear", "show", "role", "channel","perm");

        if(arg == null)
        {
            message.getChannel().sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" <add|remove|set|replace|move|clear|show|role|channel|perm>").queue();
            return true;
        }

        switch (arg){
            case "add":
                add(message.getChannel(), args);
                break;
            case "remove":
                removeQuestion(message.getChannel(), args.length > 1 ? args[1] : null);
                break;
            case "set":
                set(message.getChannel(), args.length > 1 ? args[1] : null, args);
                break;
            case "replace":
                replace(message.getChannel(), args.length > 1 ? args[1] : null, args);
                break;
            case "move":
                move(message.getChannel(), args.length > 1 ? args[1] : null, args.length > 2 ? args[2] : null);
                break;
            case "clear":
                clear(message.getChannel());
                break;
            case "show":
                show(message.getChannel());
                break;
            case "role":
                role(message.getChannel(), !message.getMentionedRoles().isEmpty() ? message.getMentionedRoles().get(0) : null);
                break;
            case "channel":
                channel(message.getChannel(), !message.getMentionedChannels().isEmpty() ? message.getMentionedChannels().get(0) : null);
                break;
        }

        return true;
    }

    private void add(MessageChannel channel, String[] args)
    {
        if(args.length == 1)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" add <Your Question>").queue();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for(int i = 1; i < args.length; i++)
            builder.append(args[i]).append(" ");

        int id = QuizManager.addQuestion(builder.toString().trim());
        QuizManager.saveQuestion();

        channel.sendMessage(Language.getDefaultLanguage().get("add_question", builder.toString().trim(), String.valueOf(id))).queue();
    }

    private void removeQuestion(MessageChannel channel, String questionId)
    {
        if(questionId == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" remove <QuestionId>").queue();
            return;
        }

        int id = 0;

        try {
            id = Integer.parseInt(questionId);
        }catch (NumberFormatException nfe){
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" remove <QuestionId>").queue();
            return;
        }

        if(!QuizManager.hasQuestion(id))
        {
            channel.sendMessage(Language.getDefaultLanguage().get("not_question_id")).queue();
            return;
        }

        QuizManager.removeQuestion(id);
        QuizManager.saveQuestion();
        channel.sendMessage(Language.getDefaultLanguage().get("remove_question", String.valueOf(id))).queue();
    }

    private void set(MessageChannel channel, String questionId, String[] args)
    {
        if(questionId == null || args.length == 2)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" set <QuestionId> <Your Question>").queue();
            return;
        }

        int id = 0;

        try {
            id = Integer.parseInt(questionId);
        }catch (NumberFormatException nfe){
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" set <QuestionId> <Your Question>").queue();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for(int i = 2; i < args.length; i++)
            builder.append(args[i]).append(" ");

        QuizManager.setQuestion(id, builder.toString().trim());
        QuizManager.saveQuestion();

        channel.sendMessage(Language.getDefaultLanguage().get("add_question", builder.toString().trim(), String.valueOf(id))).queue();
    }

    private void move(MessageChannel channel, String questionId, String newPosition)
    {
        if(questionId == null || newPosition == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" move <QuestionId> <NewPosition>").queue();
            return;
        }

        int id = 0, position = 0;
        try {
            id = Integer.parseInt(questionId);
            position = Integer.parseInt(newPosition);
        }catch (NumberFormatException nfe){
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" move <QuestionId> <NewPosition>").queue();
            return;
        }

        if(!QuizManager.hasQuestion(id))
        {
            channel.sendMessage(Language.getDefaultLanguage().get("not_question_id")).queue();
            return;
        }

        QuizManager.moveQuestion(id, position);
        QuizManager.saveQuestion();

        channel.sendMessage(Language.getDefaultLanguage().get("move_question", questionId, newPosition)).queue();
    }

    private void replace(MessageChannel channel, String questionId, String[] args)
    {
        if(questionId == null || args.length == 2)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" replace <QuestionId> <Your Question>").queue();
            return;
        }

        int id = 0;

        try {
            id = Integer.parseInt(questionId);
        }catch (NumberFormatException nfe){
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" replace <QuestionId> <Your Question>").queue();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for(int i = 2; i < args.length; i++)
            builder.append(args[i]).append(" ");

        if(!QuizManager.replaceQuestion(id, builder.toString().trim()))
        {
            channel.sendMessage(Language.getDefaultLanguage().get("cant_replace")).queue();
            return;
        }
        QuizManager.saveQuestion();

        channel.sendMessage(Language.getDefaultLanguage().get("replace_question", builder.toString().trim(), String.valueOf(id))).queue();
    }

    private void clear(MessageChannel channel)
    {
        QuizManager.clearQuestion();
        QuizManager.saveQuestion();
        channel.sendMessage(Language.getDefaultLanguage().get("clear_question")).queue();
    }

    private void show(MessageChannel channel)
    {
        Map<Integer, String> map = QuizManager.getQuizMap();
        StringBuilder builder = new StringBuilder();
        builder.append("```");
        for(Map.Entry<Integer, String> entry : map.entrySet())
        {
            builder.append("\n[").append(entry.getKey()).append("] ").append(entry.getValue());

            if(builder.length() > 1000)
            {
                channel.sendMessage(builder.append("\n```").toString()).queue();
                builder = new StringBuilder().append("```");
            }
        }

        if(builder.length() > 3)
            channel.sendMessage(builder.append("\n```").toString()).queue();
    }

    private void role(MessageChannel channel, Role role)
    {
        if(role == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" role <@Role>").queue();
            return;
        }

        QuizManager.setRoleQuizId(role);
        channel.sendMessage(Language.getDefaultLanguage().get("set_role_question", role.getName())).queue();
    }

    private void channel(MessageChannel channel, TextChannel textChannel)
    {
        if(textChannel == null)
        {
            channel.sendMessage(GravenBot.getConfig().getString("prefix")+getName()+" channel <#Channel>").queue();
            return;
        }

        QuizManager.setQuizChannel(textChannel);
        channel.sendMessage(Language.getDefaultLanguage().get("set_channel_question", textChannel.getName())).queue();
    }
}
