import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetStickCommand extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String prefix = "?";
        String guildID = event.getGuild().getId();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (Main.mapPrefix.containsKey(event.getGuild().getId())) {
            prefix = Main.mapPrefix.get(event.getGuild().getId());
        }



        if (args[0].equalsIgnoreCase(prefix + "getstick") || args[0].equalsIgnoreCase(prefix + "getsticks") || args[0].equalsIgnoreCase(prefix + "getstickies")) {

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.getMessage().reply("Whoops! You need the `Manage Messages` permission to use this command.").queue();
                return;
            }

            if (getClassicPinChannels(guildID).isEmpty() && getSlowAnchorChannels(guildID).isEmpty() && getEmbedPinChannels(guildID).isEmpty() && getWebhookPinChannels(guildID).isEmpty()) {
                event.getMessage().reply("No active pins in this server!\nYou can make one with `" + prefix + "stick` or use `" + prefix + "help` for a full list of commands.").queue();
                return;
            }

            Member anchorBot = event.getGuild().getMemberById(Main.botId);
            EmbedBuilder emb = new EmbedBuilder();
            emb.setColor(BotStyle.PRIMARY)
                    .setTitle("-Active Pins in **" + event.getGuild().getName() + "**-")
                    .setFooter("AnchorBot", BotUtil.botAvatarUrl());


            if (!getClassicPinChannels(guildID).isEmpty()) {
                //Add classic stickies to embed
                for (String channelID : getClassicPinChannels(guildID)) {
                    emb.addField("Classic Pin:", "Channel: " + BotUtil.channelMention(event.getGuild(), channelID) + "\n__Pinned Message:__```\n" + Main.mapMessage.get(channelID) + "```", false);
                }
            }
            if (!getSlowAnchorChannels(guildID).isEmpty()) {
                //Add Slow Pin to embed
                for (String channelID : getSlowAnchorChannels(guildID)) {
                    emb.addField("Slow Pin:", "Channel: " + BotUtil.channelMention(event.getGuild(), channelID) + "\n__Pinned Message:__```\n" + Main.mapMessageSlow.get(channelID) + "```", false);
                }
            }
            if (!getEmbedPinChannels(guildID).isEmpty()) {
                //Add pin embeds to embed
                for (String channelID : getEmbedPinChannels(guildID)) {
                    emb.addField("Pin Embed:", "Channel: " + BotUtil.channelMention(event.getGuild(), channelID) + "__\nPinned Message:__```\n" + Main.mapMessageEmbed.get(channelID) + "```", false);
                }
            }
            if (!getWebhookPinChannels(guildID).isEmpty()) {
                //Add pin WebHook to embed
                for (String channelID : getWebhookPinChannels(guildID)) {
                    emb.addField("WebHook Pin Embed:", "Channel: " + BotUtil.channelMention(event.getGuild(), channelID) + "__\nPinned Message:__```\n" + Main.webhookMessage.get(channelID) + "```", false);
                }
            }

            event.getMessage().replyEmbeds(emb.build()).queue();

        }
    }


    public List<String> getClassicPinChannels(String guildId) {
        return BotUtil.activeChannelIds(Main.jda.getGuildById(guildId), Main.mapMessage);
    }

    public List<String> getSlowAnchorChannels(String guildId) {
        return BotUtil.activeChannelIds(Main.jda.getGuildById(guildId), Main.mapMessageSlow);
    }

    public List<String> getEmbedPinChannels(String guildId) {
        return BotUtil.activeChannelIds(Main.jda.getGuildById(guildId), Main.mapMessageEmbed);
    }

    public List<String> getWebhookPinChannels(String guildId) {
        return BotUtil.activeChannelIds(Main.jda.getGuildById(guildId), Main.webhookMessage);
    }

}

