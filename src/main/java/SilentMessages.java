import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public final class SilentMessages {
    private static final int SUPPRESS_NOTIFICATIONS = 1 << 12;

    private SilentMessages() {
    }

    public static MessageCreateAction send(MessageChannel channel, String content) {
        return channel.sendMessage(BotUtil.stripCustomEmoji(content)).setSuppressedNotifications(true);
    }

    public static MessageCreateAction send(MessageChannel channel, MessageEmbed embed) {
        return channel.sendMessageEmbeds(embed).setSuppressedNotifications(true);
    }

    public static WebhookMessage webhook(WebhookEmbed embed) {
        SilentWebhookMessageBuilder message = new SilentWebhookMessageBuilder();
        message.suppressNotifications();
        message.addEmbeds(embed);
        return message.build();
    }

    private static final class SilentWebhookMessageBuilder extends WebhookMessageBuilder {
        private void suppressNotifications() {
            this.flags |= SUPPRESS_NOTIFICATIONS;
        }
    }
}

