import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DisconnectEventMessages extends ListenerAdapter {

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        TextChannel log = Main.jda.getTextChannelById("853879746698018838");
        if (log == null) {
            return;
        }
        EmbedBuilder emb = new EmbedBuilder();

        emb.setColor(Color.RED)
                .setTitle("SHARD DISCONNECTED")
                .setFooter("Time: " + event.getTimeDisconnected().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        if (event.isClosedByServer()) {
            var frame = event.getServiceCloseFrame();
            emb.setDescription(
                    "```" + event.getJDA().getShardInfo().getShardString() + "DISCONNECT [SERVER]\nCODE: [" + (frame == null ? "N/A" : frame.getCloseCode()) + "]\nREASON: " + (frame == null ? "N/A" : frame.getCloseReason()) +
                            "\nSHARD STATUS: " + event.getJDA().getStatus()
                            + "```");
        } else {
            var frame = event.getClientCloseFrame();
            emb.setDescription(
                    "```" + event.getJDA().getShardInfo().getShardString() + "DISCONNECT [CLIENT]\nCODE: [" + (frame == null ? "N/A" : frame.getCloseCode()) + "]\nREASON: " + (frame == null ? "N/A" : frame.getCloseReason()) +
                            "\nSHARD STATUS: " + event.getJDA().getStatus() +
                            "```");
        }
        log.sendMessageEmbeds(emb.build()).queue();
    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {
        TextChannel log = Main.jda.getTextChannelById("853879746698018838");
        if (log == null) {
            return;
        }
        EmbedBuilder emb = new EmbedBuilder();
        emb.setColor(Color.GREEN)
                .setTitle("SHARD RESUMED")
                .setDescription("```" + event.getJDA().getShardInfo().getShardString() + "\nSHARD STATUS: " + event.getJDA().getStatus() + "```");
        log.sendMessageEmbeds(emb.build()).queue();
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        TextChannel log = Main.jda.getTextChannelById("853879746698018838");
        if (log == null) {
            return;
        }
        EmbedBuilder emb = new EmbedBuilder();
        emb.setColor(Color.GREEN)
                .setTitle("SHARD RECONNECTED")
                .setDescription("```" + event.getJDA().getShardInfo().getShardString() + "\nSHARD STATUS: " + event.getJDA().getStatus() + "```");
        log.sendMessageEmbeds(emb.build()).queue();
    }
}

