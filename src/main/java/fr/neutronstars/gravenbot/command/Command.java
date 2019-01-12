package fr.neutronstars.gravenbot.command;

import fr.neutronstars.gravenbot.GravenBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public abstract class Command
{
    private final String name;
    private final boolean admin, owner;

    private boolean autoComplete;

    protected Command(String name, boolean admin, boolean owner)
    {
        this.name = name;
        this.admin = admin;
        this.owner = owner;
    }

    public String getName()
    {
        return name;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public boolean isOwner()
    {
        return owner;
    }

    public boolean isAutoComplete()
    {
        return autoComplete;
    }

    public void setAutoComplete(boolean autoComplete)
    {
        this.autoComplete = autoComplete;
    }

    public boolean hasPermission(long id, Guild guild)
    {
        return owner ? GravenBot.isOwner(id) : !admin || (guild != null && guild.getMemberById(id).hasPermission(Permission.ADMINISTRATOR));
    }

    public String getArgsDetect(String arg, String... args)
    {
        List<String> list = new ArrayList<>();
        for(String str : args)
        {
            if(str.equalsIgnoreCase(arg)) return str;
            if(str.startsWith(arg)) list.add(str);
        }
        return list.size() == 1 ? list.get(0) : null;
    }

    public abstract boolean onCommand(User user, Message message, String label, String[] args);
}
