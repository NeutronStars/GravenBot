package fr.neutronstars.gravenbot.manager;

import fr.neutronstars.gravenbot.jda.JDAManager;

public enum EmoteManager
{
    CONFIRM("{CONFIRM}", "✅"),
    CANCEL("{CANCEL}", "❌"),
    WARNING("{WARN}", "⚠"),

    DOWN("{DOWN}", "⤵");

    private String key;
    private Object value;

    EmoteManager(String key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public Object getValue()
    {
        return value;
    }

    public static String parseEmote(String message)
    {
        for(EmoteManager emote : EmoteManager.values())
        {
            if(!message.contains(emote.key)) continue;
            if(emote.value instanceof Long)
                message = message.replace(emote.key, JDAManager.getShardManager().getEmoteById((long) emote.value).getAsMention());
            else
                message = message.replace(emote.key, String.valueOf(emote.value));
        }
        return message;
    }
}
