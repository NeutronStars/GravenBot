package fr.neutronstars.gravenbot.utils;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.json.JSONReader;
import fr.neutronstars.gravenbot.manager.EmoteManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Language
{
    private static final Map<String, Language> LANGUAGE_MAP = new HashMap<>();

    public static void register(){
        GravenBot.getLogger().info("Loading language...");

        try {
            LANGUAGE_MAP.put("FR", new Language("FR", new JSONReader(new InputStreamReader(Language.class.getResourceAsStream("/assets/gravenbot/language/FR_fr.json"))).toJSONObject(), true));
            GravenBot.getLogger().info("Language \"FR_fr\" loaded.");
        }catch (IOException e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static Language getLanguage(String name)
    {
        Language language = LANGUAGE_MAP.get(name);
        return language != null ? language : getDefaultLanguage();
    }

    public static Language getLanguageOrNull(String name) {
        return LANGUAGE_MAP.get(name);
    }

    public static Language getDefaultLanguage()
    {
        return LANGUAGE_MAP.get("FR");
    }

    private final String name;
    private final JSONObject object;
    private final boolean isDefault;

    private Language(String name, JSONObject object)
    {
        this(name, object, false);
    }

    private Language(String name, JSONObject object, boolean isDefault)
    {
        this.name = name;
        this.object = object;
        this.isDefault = isDefault;
    }

    public String getName()
    {
        return name;
    }

    public String get(String path, String... args)
    {
        JSONObject object = this.object;

        String[] paths = path.split("\\.");

        for(int i = 0; i < paths.length - 1; i++)
        {
            if(!object.has(paths[i])) return isDefault ? path : getDefaultLanguage().get(path, args);
            Object obj = object.get(paths[i]);
            if(!(obj instanceof JSONObject)) return isDefault ? path : getDefaultLanguage().get(path, args);
            object = (JSONObject) obj;
        }

        if(!object.has(paths[paths.length - 1])) return isDefault ? path : getDefaultLanguage().get(path, args);
        Object obj = object.get(paths[paths.length - 1]);
        return obj instanceof String ? EmoteManager.parseEmote(String.format(String.valueOf(obj), args)) : isDefault ? path : getDefaultLanguage().get(path, args);
    }

    public static Collection<Language> getLanguages()
    {
        return new ArrayList<>(LANGUAGE_MAP.values());
    }

    public static String[] getLanguagesByName()
    {
        return new ArrayList<>(LANGUAGE_MAP.keySet()).toArray(new String[LANGUAGE_MAP.size()]);
    }

    public boolean isDefault()
    {
        return isDefault;
    }
}
