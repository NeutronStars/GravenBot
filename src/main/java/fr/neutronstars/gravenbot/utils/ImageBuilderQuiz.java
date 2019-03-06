package fr.neutronstars.gravenbot.utils;

import fr.neutronstars.gravenbot.GravenBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class ImageBuilderQuiz
{
    private final String path;

    private int x = 10, maxX = 10, y = 10, maxY = 10;
    private BufferedImage image;

    public ImageBuilderQuiz(String path)
    {
        this.path = path;
    }

    public ImageBuilderQuiz build(Map<String, String> map, int width, int height)
    {
        buildImage(map, 1, 1, true);
        buildImage(map, maxX, maxY, false);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double xMul = (double) width / (double) maxX;
        double yMul = (double) height / (double) maxY;
        double mul = xMul > yMul ? yMul : xMul;

        ((Graphics2D) image.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        image.getGraphics().drawImage(this.image, 0, 0, (int)((double) maxX * mul), (int)((double)maxY * mul), null);
        this.image = image;
        return this;
    }

    private void buildImage(Map<String, String> map, int width, int height, boolean consumerSize)
    {
        x = 10;
        y = 10;
        maxX = 10;
        maxY = 10;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        if(!consumerSize)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
        }

        for(Map.Entry<String, String> entry : map.entrySet())
        {
            g.setColor(Color.CYAN);
            g.setFont(new Font("UTF-8", Font.BOLD, 15));

            buildPrint(g, entry.getKey(), consumerSize);

            maxY+=(y/2);

            g.setColor(Color.WHITE);
            g.setFont(new Font("UTF-8", Font.PLAIN, 15));

            buildPrint(g, "▶ "+entry.getValue(), consumerSize);

            maxY+=(y*2);
        }
    }

    private void buildPrint(Graphics g, String message, boolean consumerSize)
    {
        StringBuilder builder = new StringBuilder();

        int xOffset = 0;
        for(int i = 0; i < message.length(); i++)
        {
            if(xOffset >= 90 && message.charAt(i) == ' ')
            {
                drawString(g, builder.toString(), consumerSize);
                builder = new StringBuilder();
                xOffset = 0;
                continue;
            }
            xOffset++;
            builder.append(message.charAt(i));
        }

        if(builder.length() > 0)
            drawString(g, builder.toString(), consumerSize);
    }

    private void drawString(Graphics g, String message, boolean consumerSize)
    {
        Rectangle2D rectangle2D = getTextBox(message, g);
        if((int)Math.ceil(rectangle2D.getWidth())+(x*2) > maxX)
            maxX = (int)Math.ceil(rectangle2D.getWidth())+(x*2);
        maxY += ((int) Math.ceil(rectangle2D.getHeight()));

        if(!consumerSize)
            g.drawString(message, x, maxY);
    }

    private Rectangle2D getTextBox(String text, Graphics g)
    {
        FontRenderContext context = new FontRenderContext(g.getFont().getTransform(), true, false);
        return g.getFont().getStringBounds(text, context);
    }

    public void sendChannel(MessageChannel channel, String content, Consumer<Message> consumer)
    {
        File folder = new File("tmp");
        if(!folder.exists())
            folder.mkdir();

        File file = new File(folder, path);

        try {
            ImageIO.write(image, "PNG", file);

            channel.sendMessage(content).addFile(file).queue(message -> {
                file.delete();
                consumer.accept(message);
            });
        }catch (IOException e){
            GravenBot.getLogger().error(e.getMessage(), e);
        }
    }
}
