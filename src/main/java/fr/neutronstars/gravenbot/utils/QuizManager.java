package fr.neutronstars.gravenbot.utils;

import fr.neutronstars.gravenbot.GravenBot;
import fr.neutronstars.gravenbot.jda.JDAManager;
import fr.neutronstars.gravenbot.json.JSONReader;
import fr.neutronstars.gravenbot.json.JSONWriter;
import net.dv8tion.jda.core.entities.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuizManager
{
    private static final Map<Integer, String> QUIZ_MAP = new HashMap<>();
    private static final Map<Long, QuizPlayer> QUIZ_PLAYER_MAP = new HashMap<>();
    private static final Map<Long, Long> WAITING_PLAYER = new HashMap<>();

    public static boolean hasQuestion()
    {
        return !QUIZ_MAP.isEmpty();
    }

    public static Map<Integer, String> getQuizMap()
    {
        return new HashMap<>(QUIZ_MAP);
    }

    public static boolean hasQuestion(int questionId)
    {
        return QUIZ_MAP.containsKey(questionId);
    }

    public static void removeQuestion(int questionId)
    {
        QUIZ_MAP.remove(questionId);
        refresh();
    }

    public static int addQuestion(String question)
    {
        int questionId = 1;
        while (QUIZ_MAP.containsKey(questionId))
            questionId++;
        QUIZ_MAP.put(questionId, question);
        return questionId;
    }

    public static void setQuestion(int questionId, String question)
    {
        if(hasQuestion(questionId))
        {
            List<Integer> list = new ArrayList<>(QUIZ_MAP.keySet());
            list.sort(Collections.reverseOrder());
            for(int id : list)
            {
                if(id < questionId) break;
                QUIZ_MAP.put(id+1, QUIZ_MAP.get(id));
            }
        }
        QUIZ_MAP.put(questionId, question);
        refresh();
    }

    public static boolean replaceQuestion(int questionId, String question)
    {
        if(!hasQuestion(questionId))
            return false;
        QUIZ_MAP.put(questionId, question);
        return true;
    }

    public static void clearQuestion()
    {
        QUIZ_MAP.clear();
    }

    public static boolean moveQuestion(int questionId, int position)
    {
        if(!hasQuestion(questionId)) return false;
        String question = QUIZ_MAP.get(questionId);
        removeQuestion(questionId);
        setQuestion(position, question);
        return true;
    }

    public static void refresh()
    {
        Map<Integer, String> map = new HashMap<>();
        List<Integer> list = new ArrayList<>(QUIZ_MAP.keySet());
        Collections.sort(list);
        int newId = 1;
        for(int id : list)
        {
            map.put(newId, QUIZ_MAP.get(id));
            newId++;
        }
        QUIZ_MAP.clear();
        QUIZ_MAP.putAll(map);
    }

    public static boolean hasRoleQuiz()
    {
        return GravenBot.getConfig().getLong("role_quiz") != 0L;
    }

    public static long getRoleQuizId()
    {
        return GravenBot.getConfig().getLong("role_quiz");
    }

    public static void setRoleQuizId(Role role)
    {
        GravenBot.getConfig().set("role_quiz", role.getIdLong());
        GravenBot.getConfig().save();
    }

    public static boolean hasQuizChannel()
    {
        return GravenBot.getConfig().getLong("channel_quiz") != 0L;
    }

    public static long getQuizChannel()
    {
        return GravenBot.getConfig().getLong("channel_quiz");
    }

    public static void setQuizChannel(TextChannel textChannel)
    {
        GravenBot.getConfig().set("channel_quiz", textChannel.getIdLong());
        GravenBot.saveConfig();
    }

    public static boolean hasQuizUser(User user)
    {
        return QUIZ_PLAYER_MAP.containsKey(user.getIdLong());
    }

    public static void startNewQuiz(Member member)
    {
        if(QUIZ_MAP.isEmpty() || hasQuizUser(member.getUser())) return;
        Role role = JDAManager.getShardManager().getRoleById(getRoleQuizId());
        if(role == null || member.getRoles().contains(role)) return;
        refresh();

        member.getUser().openPrivateChannel().queue(privateChannel -> {
            QUIZ_PLAYER_MAP.put(member.getUser().getIdLong(), new QuizPlayer(member, privateChannel));
            QUIZ_PLAYER_MAP.get(member.getUser().getIdLong()).startQuiz();
        }, throwable -> {});
    }

    public static void receiveResponse(User user, String response)
    {
        if(hasQuizUser(user))
            QUIZ_PLAYER_MAP.get(user.getIdLong()).receiveResponse(response);
    }

    public static void loadQuestion()
    {
        File file = new File("question.json");
        if(!file.exists()) return;
        GravenBot.getLogger().info("Loading questions...");
        QUIZ_MAP.clear();
        try {
            JSONObject object = new JSONReader(file).toJSONObject();
            for(String key : object.keySet())
                QUIZ_MAP.put(Integer.parseInt(key), object.getString(key));
            GravenBot.getLogger().info("Questions loaded.");
        } catch (Exception e) {
            GravenBot.getLogger().error(e.getMessage(), e);
        }

        refresh();
    }

    public static void saveQuestion()
    {
        File file = new File("question.json");
        if(QUIZ_MAP.isEmpty())
        {
            if(file.exists()) file.delete();
            return;
        }

        GravenBot.getLogger().info("Saving questions...");

        JSONObject object = new JSONObject();
        for(Map.Entry<Integer, String> entry : QUIZ_MAP.entrySet())
            object.put(String.valueOf(entry.getKey()), entry.getValue());

        try (JSONWriter writer = new JSONWriter(file)){
            writer.write(object);
            writer.flush();
            GravenBot.getLogger().info("Questions saved.");
        }catch (IOException e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static boolean hasWaitingPlayer(Message message)
    {
        return !message.getMentionedUsers().isEmpty() && WAITING_PLAYER.containsKey(message.getMentionedUsers().get(0).getIdLong());
    }

    private static void addWaitingPlayer(User user, Message message)
    {
        WAITING_PLAYER.put(user.getIdLong(), message.getIdLong());
        saveWaitingPlayer();
    }

    public static void removeWaitingPlayer(User user)
    {
        WAITING_PLAYER.remove(user.getIdLong());
        saveWaitingPlayer();
    }

    public static void loadWaitingPlayer()
    {
        File file = new File("waiting_users.json");
        if(!file.exists()) return;
        GravenBot.getLogger().info("Loading Waiting Users...");
        WAITING_PLAYER.clear();
        try {
            JSONObject object = new JSONReader(file).toJSONObject();
            for(String key : object.keySet())
                WAITING_PLAYER.put(Long.parseLong(key), object.getLong(key));
            GravenBot.getLogger().info("Waiting uses loaded.");
        } catch (Exception e) {
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static void saveWaitingPlayer()
    {
        File file = new File("waiting_users.json");
        if(WAITING_PLAYER.isEmpty())
        {
            if(file.exists()) file.delete();
            return;
        }

        GravenBot.getLogger().info("Saving waiting users...");

        JSONObject object = new JSONObject();
        for(Map.Entry<Long, Long> entry : WAITING_PLAYER.entrySet())
            object.put(String.valueOf(entry.getKey()), entry.getValue());

        try (JSONWriter writer = new JSONWriter(file)){
            writer.write(object);
            writer.flush();
            GravenBot.getLogger().info("Waiting users saved.");
        }catch (IOException e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }

    public static class QuizPlayer
    {
        private final Map<String, String> responseMap = new LinkedHashMap<>();
        private final Member member;
        private final PrivateChannel channel;

        private int stateQuiz;

        public QuizPlayer(Member member, PrivateChannel channel)
        {
            this.member = member;
            this.channel = channel;
        }

        public void startQuiz()
        {
            stateQuiz = 1;
            channel.sendMessage(Language.getDefaultLanguage().get("start_quiz") +"\n\n"+QUIZ_MAP.get(stateQuiz)).queue();
        }

        public void receiveResponse(String response)
        {
            responseMap.put(QUIZ_MAP.get(stateQuiz), response);
            stateQuiz++;

            if(isFinish()){
                channel.sendMessage(Language.getDefaultLanguage().get("end_quiz")).queue();
                QUIZ_PLAYER_MAP.remove(member.getUser().getIdLong());

                TextChannel textChannel = JDAManager.getShardManager().getTextChannelById(getQuizChannel());
                if(textChannel == null) return;
                StringBuilder builder = new StringBuilder();
                builder.append(member.getUser().getAsMention());

                for(Map.Entry<String, String> entry : responseMap.entrySet())
                    builder.append("\n\n**").append(entry.getKey()).append("**\n").append(entry.getValue());

                textChannel.sendMessage(builder.toString()).queue(message -> {
                    message.addReaction(String.valueOf(EmoteManager.CONFIRM.getValue())).queue();
                    message.addReaction(String.valueOf(EmoteManager.CANCEL.getValue())).queue();

                    addWaitingPlayer(member.getUser(), message);
                });
            }else
                channel.sendMessage(QUIZ_MAP.get(stateQuiz)).queue();
        }

        public boolean isFinish()
        {
            return stateQuiz != 0 && !QUIZ_MAP.containsKey(stateQuiz);
        }
    }
}
