package fr.neutronstars.gravenbot.command.admin;

import fr.neutronstars.gravenbot.command.Command;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class RoleCommand extends Command
{
    protected RoleCommand()
    {
        super("role", true, true);
    }

    @Override
    public boolean onCommand(User user, Message message, String label, String[] args)
    {
        return false;
    }
}
