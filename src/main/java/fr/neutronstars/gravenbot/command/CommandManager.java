package fr.neutronstars.gravenbot.command;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.command.admin.QuizCommand;
import fr.neutronstars.gravenbot.command.admin.RoleCommand;
import fr.neutronstars.gravenbot.command.owner.StopCommand;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager
{
    public static final Map<String, Command> commandMap = new HashMap<>();

    public static List<Command> getCommands(boolean admin, boolean owner)
    {
        List<Command> commands = new ArrayList<>();
        for(Command command : commandMap.values())
        {
            if((command.isAdmin() && !admin) || (command.isOwner() && !owner)) continue;
            commands.add(command);
        }
        return commands;
    }

    public static List<Command> getCommands()
    {
        return new ArrayList<>(commandMap.values());
    }

    public static void registerCommands()
    {
        GravenBot.getLogger().info("Registering commands...");

        registerCommand(new StopCommand(), new RoleCommand(), new QuizCommand());

        GravenBot.getLogger().info(commandMap.size()+" command(s) registered.");
    }

    private static void registerCommand(Command... commands)
    {
        for(Command command : commands)
            commandMap.put(command.getName(), command);
    }

    public static boolean hasCommand(String label)
    {
        return getCommand(label, null) != null;
    }

    public static boolean onCommand(User user, Message message, String label, String[] args)
    {
        Command command = getCommand(label, message);

        return command != null
                && command.hasPermission(user.getIdLong(), message.getGuild())
                && command.onCommand(user, message, label, args);
    }

    public static Command getCommand(String label, Message message)
    {
        if(commandMap.containsKey(label))
            return commandMap.get(label);
        List<String> commands = new ArrayList<>();
        for(Command command : getCommands())
            if(command.isAutoComplete() && command.getName().startsWith(label))
                commands.add(command.getName());

        if(message != null && commands.size() > 1)
        {
            StringBuilder builder = new StringBuilder();
            for(String str : commands)
            {
                if(builder.length() > 1) builder.append(" | ");
                builder.append(str);
            }
            message.getChannel().sendMessage(message.getAuthor().getAsMention()+"```diff\n- "+builder.toString()+" ?\n```").queue();
        }

        return commands.size() == 1 ? commandMap.get(commands.get(0)) : null;
    }
}
