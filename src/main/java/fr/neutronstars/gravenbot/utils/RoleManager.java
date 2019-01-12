package fr.neutronstars.gravenbot.utils;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.jda.JDAManager;
import fr.neutronstars.gravenbot.json.JSONReader;
import fr.neutronstars.gravenbot.json.JSONWriter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RoleManager
{
    private static final Map<String, Long> ROLE_MAP = new HashMap<>();
    private static final File FILE = new File("roles.json");

    public static void loadRoles()
    {
        if(!FILE.exists()) return;
        GravenBot.getLogger().info("Loading roles...");
        try {
            JSONObject object = new JSONReader(FILE).toJSONObject();
            for(String key : object.keySet())
                ROLE_MAP.put(key, object.getLong(key));
            GravenBot.getLogger().info("Roles loaded.");
        } catch (IOException e) {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static Map<String, Long> getRoleMap()
    {
        return new HashMap<>(ROLE_MAP);
    }

    public static boolean hasRole()
    {
        return !ROLE_MAP.isEmpty();
    }

    public static boolean hasRole(String key)
    {
        return ROLE_MAP.containsKey(key);
    }

    public static void addRoles(String key, Long value)
    {
        ROLE_MAP.put(key, value);
    }

    public static void removeRoles(String key)
    {
        ROLE_MAP.remove(key);
    }

    public static void saveRoles()
    {
        if(ROLE_MAP.isEmpty())
        {
            if(FILE.exists()) FILE.delete();
            return;
        }

        GravenBot.getLogger().info("Saving roles...");

        JSONObject object = new JSONObject();
        for(Map.Entry<String, Long> entry : ROLE_MAP.entrySet())
            object.put(entry.getKey(), entry.getValue());

        try (JSONWriter writer = new JSONWriter(FILE)){
            writer.write(object);
            writer.flush();
            GravenBot.getLogger().info("Roles saved.");
        }catch (IOException e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static void apply(Guild guild, Member member, String key, boolean add)
    {
        if(!hasRole(key)) return;
        Role role = JDAManager.getShardManager().getRoleById(ROLE_MAP.get(key));
        if(role == null) return;
        if(add)
            guild.getController().addRolesToMember(member, role).queue();
        else
            guild.getController().removeRolesFromMember(member, role).queue();
    }
}
