import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class JoinNewGuild extends ListenerAdapter {
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Member anchorBot = event.getGuild().getMemberById(Main.botId);

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("AnchorBot Joined a New Server!");
        em.addField("Server Name: ", event.getGuild().getName(), false);
        em.addField("Server ID", event.getGuild().getId(), false);
        em.addField("Guild Members: ", NumberFormat.getInstance().format(event.getGuild().retrieveMetaData().complete().getApproximateMembers()), false);
        em.addField("Guild Locale: ",  event.getGuild().getLocale().getLocale(), false);
        Member owner = event.getGuild().retrieveOwner().complete();
        em.addField("Guild Owner Tag", owner.getAsMention(), false);
        em.addField("Guild Owner Raw", owner.getEffectiveName() + "#" + owner.getUser().getDiscriminator(), false);
        em.addField("Guild Owner ID", owner.getId(), false);
        em.addField("Time", OffsetDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)) + " PST", false);
        String avatarUrl = anchorBot == null ? null : anchorBot.getUser().getEffectiveAvatarUrl();
        em.setFooter("AnchorBot is now in " + NumberFormat.getInstance().format(Main.jda.getGuildCache().size()) + " guilds", avatarUrl);
        em.setThumbnail(event.getGuild().getIconUrl());
        em.setColor(Color.GREEN);

        if (Main.jda.getTextChannelById("643974985446326272") != null) {
            Main.jda.getTextChannelById("643974985446326272").sendMessageEmbeds(em.build()).queue();
        }

        //DM server owner info
        event.getGuild().retrieveOwner().queue((u) -> {
            u.getUser().openPrivateChannel().queue((channel) ->
            {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(BotStyle.PRIMARY);
                eb.setTitle("**Thank You For Adding AnchorBot To Your Server!**");
                eb.setDescription("Here are the basics to get you started:");
                eb.addField("Note:", "The pinned message is sent every 5 messages or 15 seconds to comply with discord TOS.", false);
                eb.addField("**Commands:** ", "Do ``?commands`` or ``?help``", false);
                eb.addField("Issues?", "Make sure the bot has permission to send messages, delete messages, and bypass slow mode in pin channels.", false);
                eb.addField("Included Features: ", "-Unlimited Pinned Messages."  +
                        "\n-Use Custom Embeds as Pins." +
                        "\n-Create a pin embed with a custom name and profile pic." +
                        "\n-Custom Prefix." +
                        "\n-Removes \"Pinned Message:\" header." +
                        "\n-Slower Posting Pins." +
                        "\n-All features are free to use." +
                        "\n-More to come!", false);
                eb.setFooter("AnchorBot", BotUtil.botAvatarUrl());
                channel.sendMessageEmbeds(eb.build()).queue();
            });
        });

        if (Main.topggAPI != null) {
            int serverCount = (int) Main.jda.getGuildCache().size();
            Main.topggAPI.setStats(serverCount);
        }

    }
}

