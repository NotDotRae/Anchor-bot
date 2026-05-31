import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Objects;

public class WebHookAnchor extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String prefix = "?";


        if (Main.mapPrefix.containsKey(event.getGuild().getId())) {
            prefix = Main.mapPrefix.get(event.getGuild().getId());
        }


        if (args[0].equalsIgnoreCase(prefix + "setwebhook")) {
            //If there is already a URL, remove it
            if (Main.webhookURL.containsKey(event.getChannel().getId())) {
                Main.webhookURL.remove(event.getChannel().getId());
                removeDBurl(event.getChannel().getId());
            }

            if (args.length != 2) {
                event.getMessage().reply("**Whoops!**\nPlease use this format:\n`" + prefix + "setwebhook URL HERE`").queue();
                return;
            }

            String o = event.getMessage().getContentRaw();
            String [] arr = o.split(" ", 2);

            String url = arr[1].trim();
            if (!BotUtil.isDiscordWebhookUrl(url)) {
                event.getMessage().reply("**Whoops!**\nPlease provide a valid Discord webhook URL.").queue();
                return;
            }

            Main.webhookURL.put(event.getChannel().getId(), url);
            addDBurl(event.getChannel().getId(), url);
            event.getMessage().delete().queue(null,
                    error -> event.getMessage().reply("*(I am missing `Manage Messages` permission, so just make sure to keep your WebHook URL a secret from server members)*").queue());
            event.getChannel().sendMessage(event.getMember().getAsMention() + " DONE! WebHook URL has been set for this channel.\n*(If the WebHook gets deleted you will need to make a new one and set the WebHook URL again).*").queue();

        }

        if (args[0].equalsIgnoreCase(prefix + "stickwebhook")) {

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.getMessage().reply(event.getMember().getAsMention() + "**Whoops!**\n You need the `MANAGE_MESSAGES` permission to use this command!").queue();
                BotUtil.react(event.getMessage(), "❌");
                return;
            }

            //if webhook URL is not yet set.
            if (!Main.webhookURL.containsKey(event.getChannel().getId())) {
                EmbedBuilder emb = new EmbedBuilder();
                emb.setDescription("**Whoops!**\nYou need to set the WebHook URL for this channel first!" +
                        "\nYou can do this by going to:\n`Channel Settings -> Integrations -> WebHooks -> New WebHook -> Copy WebHook URL`"
                    + "\nThen add the link by doing:\n`" + prefix + "setwebhook URL HERE` in the channel.")
                        .setColor(BotStyle.PRIMARY);
                BotUtil.react(event.getMessage(), "❌");
                event.getMessage().replyEmbeds(emb.build()).queue();
                return;
            }
            if (!BotUtil.isDiscordWebhookUrl(Main.webhookURL.get(event.getChannel().getId()))) {
                event.getMessage().reply("**Whoops!**\nThe stored webhook URL is not valid anymore. Set a new Discord webhook URL with `" + prefix + "setwebhook URL HERE`.").queue();
                return;
            }


            String o = event.getMessage().getContentRaw();
            String[] arr = o.split(" ", 2);
            if (arr.length < 2 || arr[1].trim().isEmpty()) {
                event.getMessage().reply("**Whoops!**\nPlease provide a message:\n`" + prefix + "stickwebhook YOUR MESSAGE HERE`").queue();
                return;
            }

            String message = BotUtil.stripCustomEmoji(arr[1]);

            Main.webhookMessage.put(event.getChannel().getId(), message);
            addDBmessage(event.getChannel().getId(), message);

            sendStoredWebhookPin(event.getChannel().getId());

            BotUtil.react(event.getMessage(), "✅");
            return;
        }

        if (args[0].equalsIgnoreCase(prefix + "webhookstop")) {
            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.getMessage().reply(event.getMember().getAsMention() + "**Whoops!**\n You need the `MANAGE_MESSAGES` permission to use this command!").queue();
                BotUtil.react(event.getMessage(), "❌");
                return;
            }

            if (Main.webhookMessage.containsKey(event.getChannel().getId())) {
                String channelId = event.getChannel().getId();
                event.getChannel().getHistory().retrievePast(8).queue(history -> {
                    int limit = Math.min(history.size(), 5);
                    for (int i = 0; i < limit; i++) {
                        Message m = history.get(i);
                        if (isStoredWebhookPin(m, channelId)) {
                            m.delete().queue();
                        }
                    }
                });

                Main.webhookMessage.remove(event.getChannel().getId());
                removeDBmessage(event.getChannel().getId());
                BotUtil.react(event.getMessage(), "✅");
            }
        }



        //do pin work
        if (Main.webhookMessage.containsKey(event.getChannel().getId())) {
            String channelId = event.getChannel().getId();


            event.getChannel().getHistory().retrievePast(8).queue(history -> {

                Boolean check = false;

                try {
                    int recentLimit = Math.min(history.size(), 5);
                    for (int i = 0; i < recentLimit; i++) {
                        Message m = history.get(i);
                        //if message is pin message
                        if (isStoredWebhookPin(m, channelId)) {
                            check = true;
                            //if message is older then 30 sec
                            if (m.getTimeCreated().compareTo(OffsetDateTime.now().minusSeconds(15)) < 0) {
                                m.delete().queue(null, (error) -> {});
                                sendStoredWebhookPin(channelId);
                            }
                            break;
                        }
                    }

                    //if check = true (is set to true if 1 of last 5 are the pin)
                    if (!check) {
                            int cleanupLimit = Math.min(history.size(), 7);
                            for (int i = 0; i < cleanupLimit; i++) {
                                Message m = history.get(i);
                                if (isStoredWebhookPin(m, channelId)) {
                                    m.delete().queue(null, (error) -> {});
                                }
                            }

                        sendStoredWebhookPin(channelId);
                    }

                } catch (Exception e) {
                    System.err.println("Webhook pin repost failed: " + e.getMessage());
                }
                });
        }

    }

    private boolean isStoredWebhookPin(Message message, String channelId) {
        return message.isWebhookMessage()
                && !message.getEmbeds().isEmpty()
                && Objects.equals(message.getEmbeds().get(0).getDescription(), Main.webhookMessage.get(channelId));
    }

    private void sendStoredWebhookPin(String channelId) {
        String webhookUrl = Main.webhookURL.get(channelId);
        if (!BotUtil.isDiscordWebhookUrl(webhookUrl) || !Main.webhookMessage.containsKey(channelId)) {
            return;
        }

        WebhookClientBuilder builder = new WebhookClientBuilder(webhookUrl);
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("webhookThread");
            thread.setDaemon(true);
            return thread;
        });
        builder.setWait(true);

        WebhookClient client = builder.build();
        try {
            WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
                    .setColor(BotStyle.PRIMARY_RAW)
                    .setDescription(Main.webhookMessage.get(channelId));

            if (Main.mapImageLinkEmbed.containsKey(channelId)) {
                embed.setThumbnailUrl(Main.mapImageLinkEmbed.get(channelId));
            }
            if (Main.mapBigImageLinkEmbed.containsKey(channelId)) {
                embed.setImageUrl(Main.mapBigImageLinkEmbed.get(channelId));
            }

            client.send(SilentMessages.webhook(embed.build()));
        } finally {
            client.close();
        }
    }

    public void addDBurl(String channelId, String url) {
        ConvexDb.upsert(DbKinds.WEBHOOK_URL, channelId, url);
    }


    public void removeDBurl(String channelId) {
        ConvexDb.delete(DbKinds.WEBHOOK_URL, channelId);
    }

    public void addDBmessage(String channelId, String message) {
        ConvexDb.upsert(DbKinds.WEBHOOK_MESSAGE, channelId, message);
    }


    public void removeDBmessage(String channelId) {
        ConvexDb.delete(DbKinds.WEBHOOK_MESSAGE, channelId);
    }

}

