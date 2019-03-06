package fr.neutronstars.gravenbot.command.admin;

import fr.neutronstars.gravenbot.command.Command;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class ChallengeCommand extends Command
{
    protected ChallengeCommand()
    {
        super("challenge", true, false);
    }

    @Override
    public boolean onCommand(User user, Message message, String label, String[] args)
    {
        return false;
    }
}
