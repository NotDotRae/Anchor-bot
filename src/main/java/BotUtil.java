import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class BotUtil {
    private static final Pattern CUSTOM_EMOJI = Pattern.compile("<a?:[A-Za-z0-9_]{2,32}:\\d{17,20}>");
    private static final Pattern DISCORD_WEBHOOK_PATH = Pattern.compile("/api(?:/v\\d+)?/webhooks/\\d{17,20}/[A-Za-z0-9._-]+");

    private BotUtil() {
    }

    public static boolean shouldIgnore(MessageReceivedEvent event) {
        return !event.isFromGuild() || event.getAuthor().isBot();
    }

    public static String currentPrefix(Guild guild) {
        return Main.mapPrefix.getOrDefault(guild.getId(), Main.prefix);
    }

    public static boolean hasManageMessages(Member member) {
        return member != null && member.hasPermission(Permission.MESSAGE_MANAGE);
    }

    public static String stripCustomEmoji(String text) {
        if (text == null) {
            return "";
        }
        return CUSTOM_EMOJI.matcher(text).replaceAll("").replaceAll(" {2,}", " ").trim();
    }

    public static boolean isDiscordWebhookUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(rawUrl.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) {
                return false;
            }

            host = host.toLowerCase();
            boolean discordHost = host.equals("discord.com")
                    || host.equals("discordapp.com")
                    || host.equals("canary.discord.com")
                    || host.equals("ptb.discord.com");
            return discordHost && DISCORD_WEBHOOK_PATH.matcher(uri.getPath()).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static void react(Message message, String unicodeEmoji) {
        message.addReaction(Emoji.fromUnicode(unicodeEmoji)).queue(null, error -> {});
    }

    public static String channelMention(Guild guild, String channelId) {
        GuildChannel channel = guild.getGuildChannelById(channelId);
        return channel == null ? "<#" + channelId + ">" : channel.getAsMention();
    }

    public static List<String> activeChannelIds(Guild guild, Map<String, String> storedMessages) {
        List<String> channelIds = new ArrayList<>();
        for (String id : storedMessages.keySet()) {
            if (guild.getGuildChannelById(id) != null) {
                channelIds.add(id);
            }
        }
        return channelIds;
    }

    public static String botAvatarUrl() {
        return Main.jda.getShards().stream()
                .findFirst()
                .map(shard -> shard.getSelfUser().getEffectiveAvatarUrl())
                .orElse(null);
    }
}

