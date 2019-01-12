package org.slf4j.impl;

import fr.neutronstars.gravenbot.GravenBot;
import org.slf4j.ILoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Origin Code by SLF4J-Simple [Link=https://github.com/qos-ch/slf4j/tree/master/slf4j-simple]
 * Modified by NeutronStars
 */

public class BotLoggerFactory implements ILoggerFactory
{
    private final ConcurrentMap<String, BotLogger> loggerMap = new ConcurrentHashMap<>();
    private final List<String> logList = new ArrayList<>();

    public BotLogger getLogger(String name)
    {
        BotLogger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            BotLogger newInstance = new BotLogger(name, this);
            BotLogger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            if(oldInstance == null)
                newInstance.info("New Logger : "+newInstance.getName());
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    protected void log(String log)
    {
        logList.add(log);
    }

    public void save() throws IOException
    {
        GravenBot.getLogger().info("Saving logger...");
        File folder = new File("logs");
        if(!folder.exists()){
            GravenBot.getLogger().info("Creating folder \""+folder.getName()+"\"...");
            if(folder.mkdir())
                GravenBot.getLogger().info("Folder \""+folder.getName()+"\" created.");
            else
                GravenBot.getLogger().error("Folder \""+folder.getName()+"\" can't be created !");
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("logs/"+ BotLogger.simpleDate.format(new Date()).replace(":", "-")+".log"), "UTF-8"));

        for(String str : logList) {
            writer.write(str);
            writer.newLine();
        }
        GravenBot.getLogger().info("Logger saved.");
        writer.write(logList.get(logList.size()-1));

        writer.flush();
    }
}
