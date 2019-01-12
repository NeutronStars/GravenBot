package fr.neutronstars.gravenbot.utils;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.json.JSONReader;
import fr.neutronstars.gravenbot.json.JSONWriter;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Configuration
{
    private final JSONObject object;
    private final File file;

    public Configuration(File file) throws IOException
    {
        this.file = file;
        if(file.exists())
            object = new JSONReader(file).toJSONObject();
        else
            object = new JSONObject();
    }

    public boolean has(String key)
    {
        return object.has(key);
    }

    public Object get(String key)
    {
        return object.has(key) ? object.get(key) : null;
    }

    public <T> T getOrSetDefault(String key, T defaultValue)
    {
        if(!has(key))
            set(key, defaultValue);
        return (T) get(key);
    }

    public String getString(String key, String defaultString)
    {
        String str = getString(key);
        if(str == null) object.put(key, defaultString == null ? "" : defaultString);
        return object.getString(key);
    }

    public int getInt(String key, int defaultInt)
    {
        int i = getInt(key);
        if(i == Integer.MAX_VALUE) object.put(key, defaultInt);
        return object.getInt(key);
    }

    public String getString(String key)
    {
        return object.has(key) ? object.getString(key) : null;
    }

    public int getInt(String key)
    {
        return object.has(key) ? object.getInt(key) : Integer.MAX_VALUE;
    }

    public void set(String key, Object object)
    {
        this.object.put(key, object);
    }

    public void save()
    {
        GravenBot.getLogger().info("Saving configuration \""+file.getName()+"\"...");
        try(JSONWriter writer = new JSONWriter(file))
        {
            writer.write(object);
            writer.flush();
            GravenBot.getLogger().info("Configuration \""+file.getName()+"\" saved.");
        }catch (IOException e)
        {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }
}
