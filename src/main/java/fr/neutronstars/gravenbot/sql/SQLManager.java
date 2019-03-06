package fr.neutronstars.gravenbot.sql;

import fr.neutronstars.gravenbot.GravenBot;

import java.sql.*;

public class SQLManager
{
    private static final String URL, USER, PASSWORD;
    private static Connection connection;

    static {
        URL = GravenBot.getConfig().getString("databaseUrl", null);
        USER = GravenBot.getConfig().getString("databaseUser", null);
        PASSWORD = GravenBot.getConfig().getString("databasePassword", null);

        if(URL == null || USER == null || PASSWORD == null)
            throw new IllegalArgumentException("Please, set the data for the database in the \"config.json\"");

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static boolean loadSQL()
    {
        GravenBot.getLogger().info("Connecting to the database...");
        reconnect();
        if(connection == null)
        {
            GravenBot.getLogger().warn("Failed to connect to the database.");
            return false;
        }

        GravenBot.getLogger().info("Login successfully on the database.");



        return true;
    }

    public static void reconnect()
    {
        try {
            disconnect();
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }catch (SQLException e)
        {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static void disconnect()
    {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                GravenBot.getLogger().error(e.getMessage(), e);
            }
        }
    }

    public static void close(ResultSet resultSet)
    {
        try {
            resultSet.getStatement().close();
        } catch (SQLException e) {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static void execute(String sql, boolean update)
    {
        executeQuery(sql, update, true);
    }

    public static void executeQuery(String sql)
    {
        execute(sql, false);
    }

    public static void executeUpdate(String sql) {
        execute(sql, true);
    }

    private static void executeQuery(String sql, boolean update, boolean reconnect)
    {
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            if(update) statement.executeUpdate();
            else statement.executeQuery();
        }catch (SQLException e){
            if(reconnect){
                reconnect();
                executeQuery(sql, update, false);
            }else GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static ResultSet getResult(String sql)
    {
        return getResult(sql, true);
    }

    private static ResultSet getResult(String sql, boolean reconnect)
    {
        try {
            return connection.prepareStatement(sql).executeQuery();
        }catch (Exception e){
            if(reconnect)
            {
                reconnect();
                return getResult(sql,false);
            }else GravenBot.getLogger().error(e.getMessage(), e);
        }
        return null;
    }
}
