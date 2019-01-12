package fr.neutronstars.gravenbot.command.owner;

import fr.neutronstars.gravenbot.command.Command;
import fr.neutronstars.gravenbot.start.GravenBotStart;
import fr.neutronstars.gravenbot.utils.Language;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class StopCommand extends Command
{
    public StopCommand()
    {
        super("stop", false, true);
    }

    @Override
    public boolean onCommand(User user, Message message, String label, String[] args)
    {
        message.getChannel().sendMessage(Language.getDefaultLanguage().get("stopping")).queue(msg -> GravenBotStart.shutdown());
        return true;
    }
}
