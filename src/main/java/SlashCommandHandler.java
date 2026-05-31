import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

public class SlashCommandHandler extends ListenerAdapter {
    public static Collection<CommandData> commandData() {
        List<CommandData> commands = new ArrayList<>();

        add(commands, simple("help", "Show AnchorBot commands."));
        add(commands, simple("commands", "Show AnchorBot commands."));
        add(commands, message("stick", "Create a channel pin message.", "message", "Pin text."));
        add(commands, simple("stickstop", "Stop pins in this channel."));
        add(commands, simple("unstick", "Stop pins in this channel."));
        add(commands, simple("getstick", "List active pins in this server."));
        add(commands, simple("getsticks", "List active pins in this server."));
        add(commands, simple("getstickies", "List active pins in this server."));
        add(commands, message("stickembed", "Create a pinned embed.", "message", "Embed pin text."));
        add(commands, message("stickslow", "Create a slower channel pin.", "message", "Pin text."));
        add(commands, url("setimage", "Set the small image for pin embeds."));
        add(commands, simple("removeimage", "Remove the small image for pin embeds."));
        add(commands, simple("getimage", "Show the current image settings."));
        add(commands, url("setbigimage", "Set the large image for pin embeds."));
        add(commands, simple("removebigimage", "Remove the large image for pin embeds."));
        add(commands, simple("getbigimage", "Show the current large image setting."));
        add(commands, url("setwebhook", "Set the channel webhook URL."));
        add(commands, message("stickwebhook", "Create a pinned webhook embed.", "message", "Webhook pin text."));
        add(commands, simple("webhookstop", "Stop the webhook pin in this channel."));
        add(commands, message("prefix", "Set the server prefix.", "prefix", "New prefix."));
        add(commands, simple("resetprefix", "Reset the server prefix to ?."));
        add(commands, simple("ping", "Show gateway ping."));
        add(commands, simple("about", "Show AnchorBot information."));
        add(commands, simple("serverinfo", "Show server information."));
        add(commands, simple("uptime", "Show AnchorBot uptime."));
        add(commands, simple("invite", "Show the bot invite link."));
        add(commands, message("poll", "Create a yes/no poll.", "question", "Poll question."));
        add(commands, message("apoll", "Create a multiple-choice poll.", "poll", "Question, option1, option2..."));
        add(commands, message("advancedpoll", "Create a multiple-choice poll.", "poll", "Question, option1, option2..."));
        add(commands, user("userinfo", "Show information for a member."));
        add(commands, simple("website", "Show the website link."));
        add(commands, simple("site", "Show the website link."));
        add(commands, simple("support", "Show the support server link."));
        add(commands, simple("supportserver", "Show the support server link."));
        add(commands, simple("features", "Show AnchorBot features."));
        add(commands, simple("anchorbotfeatures", "Show AnchorBot features."));
        add(commands, message("embed", "Send your text as an embed.", "message", "Embed text."));
        add(commands, simple("docs", "Show documentation link."));
        add(commands, simple("permcheck", "Check AnchorBot channel permissions."));
        add(commands, simple("helpstaff", "Show staff commands."));
        add(commands, message("getshard", "Get the shard for a server ID.", "server_id", "Server ID."));
        SlashCommandData restartShard = simple("restartshard", "Restart a shard.");
        restartShard.addOption(OptionType.INTEGER, "shard", "Shard ID.", true);
        add(commands, restartShard);
        add(commands, message("manualstop", "Stop a pin by channel ID.", "channel_id", "Channel ID."));
        add(commands, simple("adminstats", "Show owner-only runtime stats."));
        add(commands, simple("topservers", "Show owner-only top servers."));
        add(commands, simple("shutdown", "Owner-only shutdown."));
        add(commands, simple("restart", "Owner-only restart."));
        add(commands, simple("shard", "Show shard info."));
        add(commands, simple("shards", "Show shard info."));
        add(commands, simple("shardping", "Show shard ping info."));
        return commands;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            replyError(event, "AnchorBot commands can only be used in a server.");
            return;
        }

        switch (event.getName()) {
            case "help":
            case "commands":
                replyHelp(event);
                break;
            case "stick":
                startTextPin(event, Main.mapMessage, Main.mapDeleteId, DbKinds.STICKY, "message");
                break;
            case "stickslow":
                startTextPin(event, Main.mapMessageSlow, Main.mapDeleteId2, DbKinds.SLOW_STICKY, "message");
                break;
            case "stickembed":
                startEmbedPin(event);
                break;
            case "stickstop":
            case "unstick":
                stopPins(event);
                break;
            case "getstick":
            case "getsticks":
            case "getstickies":
                replyActivePins(event);
                break;
            case "setimage":
                setMapValue(event, Main.mapImageLinkEmbed, DbKinds.EMBED_IMAGE, "url", "Small image set for pin embeds.");
                break;
            case "setbigimage":
                setMapValue(event, Main.mapBigImageLinkEmbed, DbKinds.BIG_EMBED_IMAGE, "url", "Large image set for pin embeds.");
                break;
            case "removeimage":
                removeMapValue(event, Main.mapImageLinkEmbed, DbKinds.EMBED_IMAGE, "Small image removed for pin embeds.");
                break;
            case "removebigimage":
                removeMapValue(event, Main.mapBigImageLinkEmbed, DbKinds.BIG_EMBED_IMAGE, "Large image removed for pin embeds.");
                break;
            case "getimage":
            case "getbigimage":
                replyImageSettings(event);
                break;
            case "setwebhook":
                setWebhook(event);
                break;
            case "stickwebhook":
                startWebhookPin(event);
                break;
            case "webhookstop":
                stopWebhookPin(event);
                break;
            case "prefix":
                setPrefix(event);
                break;
            case "resetprefix":
                resetPrefix(event);
                break;
            case "ping":
                replyPing(event);
                break;
            case "about":
                replyAbout(event);
                break;
            case "serverinfo":
                replyServerInfo(event);
                break;
            case "uptime":
                event.reply("Uptime: ``" + uptime() + "``").queue();
                break;
            case "invite":
                event.reply(Main.INVITE_URL.isBlank() ? "Invite links are not configured." : Main.INVITE_URL).setEphemeral(true).queue();
                break;
            case "poll":
                createPoll(event);
                break;
            case "apoll":
            case "advancedpoll":
                createAdvancedPoll(event);
                break;
            case "userinfo":
                replyUserInfo(event);
                break;
            case "website":
            case "site":
                event.reply(Main.WEBSITE_URL.isBlank() ? "This command is not configured." : Main.WEBSITE_URL).setEphemeral(true).queue();
                break;
            case "support":
            case "supportserver":
                event.reply(Main.SUPPORT_URL.isBlank() ? "This command is not configured." : Main.SUPPORT_URL).setEphemeral(true).queue();
                break;
            case "features":
            case "anchorbotfeatures":
                replyFeatures(event);
                break;
            case "embed":
                createEmbed(event);
                break;
            case "docs":
                event.reply(Main.DOCS_URL.isBlank() ? "This command is not configured." : Main.DOCS_URL).setEphemeral(true).queue();
                break;
            case "permcheck":
                replyPermCheck(event);
                break;
            case "helpstaff":
                replyStaffHelp(event);
                break;
            case "getshard":
                replyShardForServer(event);
                break;
            case "restartshard":
                restartShard(event);
                break;
            case "manualstop":
                manualStop(event);
                break;
            case "adminstats":
                replyAdminStats(event);
                break;
            case "topservers":
                replyTopServers(event);
                break;
            case "shutdown":
                shutdown(event);
                break;
            case "restart":
                restart(event);
                break;
            case "shard":
            case "shards":
                replyShardInfo(event);
                break;
            case "shardping":
                replyShardPing(event);
                break;
            default:
                replyError(event, "Unknown command.");
        }
    }

    private static void add(List<CommandData> commands, SlashCommandData command) {
        commands.add(command);
    }

    private static SlashCommandData simple(String name, String description) {
        return net.dv8tion.jda.api.interactions.commands.build.Commands.slash(name, description);
    }

    private static SlashCommandData message(String name, String description, String optionName, String optionDescription) {
        return simple(name, description).addOption(OptionType.STRING, optionName, optionDescription, true);
    }

    private static SlashCommandData url(String name, String description) {
        return message(name, description, "url", "URL.");
    }

    private static SlashCommandData user(String name, String description) {
        return simple(name, description).addOption(OptionType.USER, "user", "Member.", false);
    }

    private void startTextPin(SlashCommandInteractionEvent event, Map<String, String> messages, Map<String, String> deleteIds, String dbKind, String optionName) {
        if (!requireManageMessages(event)) {
            return;
        }

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        String channelId = channel.getId();
        String message = option(event, optionName);
        if (message.isBlank()) {
            replyError(event, "Please provide pin text.");
            return;
        }

        deleteIfPresent(channel, deleteIds, channelId);
        messages.put(channelId, message);
        ConvexDb.upsert(dbKind, channelId, message);
        SilentMessages.send(channel, message).queue(sent -> deleteIds.put(channelId, sent.getId()));
        event.reply("Pin started in " + channel.getAsMention() + ".").setEphemeral(true).queue();
    }

    private void startEmbedPin(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        String channelId = channel.getId();
        String message = option(event, "message");
        if (message.isBlank()) {
            replyError(event, "Please provide pin embed text.");
            return;
        }

        EmbedBuilder embed = pinEmbed(channelId, message);
        Main.mapMessageEmbed.put(channelId, message);
        ConvexDb.upsert(DbKinds.EMBED_STICKY, channelId, message);
        deleteIfPresent(channel, Main.mapDeleteIdEmbed, channelId);
        SilentMessages.send(channel, embed.build()).queue(sent -> Main.mapDeleteIdEmbed.put(channelId, sent.getId()));
        event.reply("Pin embed started in " + channel.getAsMention() + ".").setEphemeral(true).queue();
    }

    private void stopPins(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        String channelId = channel.getId();
        deleteIfPresent(channel, Main.mapDeleteId, channelId);
        deleteIfPresent(channel, Main.mapDeleteId2, channelId);
        deleteIfPresent(channel, Main.mapDeleteIdEmbed, channelId);
        Main.mapMessage.remove(channelId);
        Main.mapMessageSlow.remove(channelId);
        Main.mapMessageEmbed.remove(channelId);
        Main.webhookMessage.remove(channelId);
        ConvexDb.delete(DbKinds.STICKY, channelId);
        ConvexDb.delete(DbKinds.SLOW_STICKY, channelId);
        ConvexDb.delete(DbKinds.EMBED_STICKY, channelId);
        ConvexDb.delete(DbKinds.WEBHOOK_MESSAGE, channelId);
        event.reply("Pins stopped in " + channel.getAsMention() + ".").setEphemeral(true).queue();
    }

    private void replyActivePins(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }

        Guild guild = event.getGuild();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("-Active Pins in **" + guild.getName() + "**-")
                .setFooter("AnchorBot", BotUtil.botAvatarUrl());

        addPinFields(embed, guild, "Classic Pin:", Main.mapMessage);
        addPinFields(embed, guild, "Slow Pin:", Main.mapMessageSlow);
        addPinFields(embed, guild, "Pin Embed:", Main.mapMessageEmbed);
        addPinFields(embed, guild, "Webhook Pin Embed:", Main.webhookMessage);

        if (embed.getFields().isEmpty()) {
            event.reply("No active pins in this server.").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void setMapValue(SlashCommandInteractionEvent event, Map<String, String> map, String dbKind, String optionName, String response) {
        if (!requireManageMessages(event)) {
            return;
        }
        String channelId = event.getChannel().getId();
        String value = option(event, optionName);
        map.put(channelId, value);
        ConvexDb.upsert(dbKind, channelId, value);
        event.reply(response).setEphemeral(true).queue();
    }

    private void removeMapValue(SlashCommandInteractionEvent event, Map<String, String> map, String dbKind, String response) {
        if (!requireManageMessages(event)) {
            return;
        }
        String channelId = event.getChannel().getId();
        map.remove(channelId);
        ConvexDb.delete(dbKind, channelId);
        event.reply(response).setEphemeral(true).queue();
    }

    private void replyImageSettings(SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("Current image settings for pin embeds");
        boolean hasImage = false;
        if (Main.mapImageLinkEmbed.containsKey(channelId)) {
            embed.addField("Small Image Link:", Main.mapImageLinkEmbed.get(channelId), false);
            embed.setThumbnail(Main.mapImageLinkEmbed.get(channelId));
            hasImage = true;
        }
        if (Main.mapBigImageLinkEmbed.containsKey(channelId)) {
            embed.addField("Large Image Link:", Main.mapBigImageLinkEmbed.get(channelId), false);
            embed.setImage(Main.mapBigImageLinkEmbed.get(channelId));
            hasImage = true;
        }
        if (!hasImage) {
            event.reply("No image is set for pin embeds in this channel.").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void setWebhook(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }
        String channelId = event.getChannel().getId();
        String url = option(event, "url");
        if (!BotUtil.isDiscordWebhookUrl(url)) {
            replyError(event, "Please provide a valid Discord webhook URL.");
            return;
        }
        Main.webhookURL.put(channelId, url);
        ConvexDb.upsert(DbKinds.WEBHOOK_URL, channelId, url);
        event.reply("Webhook URL set for this channel.").setEphemeral(true).queue();
    }

    private void startWebhookPin(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }
        String channelId = event.getChannel().getId();
        if (!Main.webhookURL.containsKey(channelId)) {
            replyError(event, "Set the channel webhook URL first with `/setwebhook`.");
            return;
        }
        String message = option(event, "message");
        Main.webhookMessage.put(channelId, message);
        ConvexDb.upsert(DbKinds.WEBHOOK_MESSAGE, channelId, message);
        sendWebhookPin(channelId);
        event.reply("Webhook pin started in " + event.getChannel().asGuildMessageChannel().getAsMention() + ".").setEphemeral(true).queue();
    }

    private void stopWebhookPin(SlashCommandInteractionEvent event) {
        if (!requireManageMessages(event)) {
            return;
        }
        String channelId = event.getChannel().getId();
        Main.webhookMessage.remove(channelId);
        ConvexDb.delete(DbKinds.WEBHOOK_MESSAGE, channelId);
        event.reply("Webhook pin stopped in this channel.").setEphemeral(true).queue();
    }

    private void setPrefix(SlashCommandInteractionEvent event) {
        if (!requireManageServer(event)) {
            return;
        }
        String prefix = option(event, "prefix");
        Main.mapPrefix.put(event.getGuild().getId(), prefix);
        ConvexDb.upsert(DbKinds.PREFIX, event.getGuild().getId(), prefix);
        event.reply("Prefix set to `" + prefix + "`.").setEphemeral(true).queue();
    }

    private void resetPrefix(SlashCommandInteractionEvent event) {
        if (!requireManageServer(event)) {
            return;
        }
        Main.mapPrefix.remove(event.getGuild().getId());
        ConvexDb.delete(DbKinds.PREFIX, event.getGuild().getId());
        event.reply("Prefix reset to `?`.").setEphemeral(true).queue();
    }

    private void replyPing(SlashCommandInteractionEvent event) {
        int shardId = (int) ((event.getGuild().getIdLong() >>> 22) % Main.jda.getShardsTotal());
        JDA shard = Main.jda.getShardById(shardId);
        String shardPing = shard == null ? "unavailable" : shard.getGatewayPing() + "ms";
        event.reply(">>> **Pong!**\nThis Shard: (`" + shardId + "`) Ping: `" + shardPing + "`."
                + "\nAll Shards Average Ping: `" + Main.jda.getAverageGatewayPing() + "`ms.").queue();
    }

    private void replyAbout(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("**-AnchorBot Information-**")
                .addField("Developed By:", "the bot owner\n(`0`)", false)
                .addField("Ping:", Main.jda.getAverageGatewayPing() + "ms", false)
                .addField("Uptime:", "``" + uptime() + "``", true)
                .addField("Shards:", "Shard **" + ((event.getGuild().getIdLong() >>> 22) % Main.jda.getShardsTotal()) + " of " + Main.jda.getShardsTotal() + "**", false)
                .addField("Guilds:", "AnchorBot is in **" + NumberFormat.getInstance().format(Main.jda.getGuildCache().size()) + "** Guilds", false)
                .addField("Included Features:", "All pin and embed features are free to use.", false)
                .setFooter("AnchorBot is Made with Java & JDA", BotUtil.botAvatarUrl());
        event.replyEmbeds(embed.build()).queue();
    }

    private void replyServerInfo(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        String creationDate = guild.getTimeCreated().getMonth() + " " + guild.getTimeCreated().getDayOfMonth() + ", " + guild.getTimeCreated().getYear();
        event.deferReply().queue(hook -> guild.retrieveOwner().queue(owner -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotStyle.PRIMARY)
                    .setThumbnail(guild.getIconUrl())
                    .setTitle("**-Server Info-**")
                    .addField("Info for " + guild.getName(),
                            "**Server ID:** ``" + guild.getId() + "``\n" +
                                    "**Creation Date:** " + creationDate + " *(" + DAYS.between(guild.getTimeCreated(), OffsetDateTime.now()) + " days ago)*\n" +
                                    "**Members:** " + NumberFormat.getInstance().format(guild.getMemberCount()) + "\n" +
                                    "**Owner:** " + owner.getAsMention() + "\n" +
                                    "**Locale:** " + guild.getLocale().getLocale() + "\n" +
                                    "**Nitro Boosting:** " + Commands.GuildBoost(guild) + "\n" +
                                    "**Number of Roles:** " + guild.getRoles().size() + "\n" +
                                    "**Text Channels:** " + guild.getTextChannels().size() + "\n" +
                                    "**Voice Channels:** " + guild.getVoiceChannels().size(), false)
                    .setFooter(guild.getName(), guild.getIconUrl());
            hook.editOriginalEmbeds(embed.build()).queue();
        }, error -> hook.editOriginal("Could not load the server owner right now.").queue()));
    }

    private void createPoll(SlashCommandInteractionEvent event) {
        String question = option(event, "question");
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setFooter("Poll by: " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setDescription(question);
        event.getChannel().sendMessageEmbeds(embed.build()).queue(message -> {
            BotUtil.react(message, "👍");
            BotUtil.react(message, "👎");
            BotUtil.react(message, "🤷");
        });
        event.reply("Poll created.").setEphemeral(true).queue();
    }

    private void createAdvancedPoll(SlashCommandInteractionEvent event) {
        String[] options = option(event, "poll").split("\\s*,\\s*");
        if (options.length < 3) {
            replyError(event, "Use this format: `Question, Option1, Option2`.");
            return;
        }
        int optionCount = Math.min(options.length - 1, 7);
        String[] emojis = {"🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬"};
        StringBuilder optionText = new StringBuilder();
        for (int i = 0; i < optionCount; i++) {
            optionText.append(emojis[i]).append("**:** ").append(options[i + 1]).append("\n");
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("POLL:")
                .setDescription(options[0])
                .setFooter("Poll by: " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .addField("Options:", optionText.toString(), false);
        event.getChannel().sendMessageEmbeds(embed.build()).queue(message -> {
            for (int i = 0; i < optionCount; i++) {
                BotUtil.react(message, emojis[i]);
            }
        });
        event.reply("Poll created.").setEphemeral(true).queue();
    }

    private void replyUserInfo(SlashCommandInteractionEvent event) {
        User user = event.getOption("user", event.getUser(), OptionMapping::getAsUser);
        event.deferReply().queue(hook -> event.getGuild().retrieveMember(user).queue(member -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotStyle.PRIMARY)
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .setTitle("**-User Info-**")
                    .addField("Info for " + user.getName() + "#" + user.getDiscriminator(),
                            "**User ID:** ``" + user.getId() + "``\n" +
                                    "**Nickname:** " + member.getEffectiveName() + "\n" +
                                    "**Join Date:** <t:" + member.getTimeJoined().toEpochSecond() + ":R>, *" + NumberFormat.getInstance().format(DAYS.between(member.getTimeJoined(), OffsetDateTime.now())) + "* days\n" +
                                    "**Creation Date:** <t:" + user.getTimeCreated().toEpochSecond() + ":R>, *" + NumberFormat.getInstance().format(DAYS.between(user.getTimeCreated(), OffsetDateTime.now())) + "* days\n" +
                                    "**Tag:** " + member.getAsMention() + "\n" +
                                    "**Nitro Boosting:** " + Commands.boostCheck(member) + "\n" +
                                    "**Number of Roles:** " + member.getRoles().size(), false)
                    .addField("**Roles:**", Commands.getRoles(member).length() > 1000 ? "Reached Max Embed Length. *(Too many roles to display)*" : Commands.getRoles(member), false)
                    .setFooter(user.getName(), user.getEffectiveAvatarUrl());
            hook.editOriginalEmbeds(embed.build()).queue();
        }, error -> hook.editOriginal("That user is not in this server.").queue()));
    }

    private void replyFeatures(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("-AnchorBot-")
                .addField("Features:", "-Unlimited pinned messages.\n-Use Custom Embeds as pins.\n-Create slower posting pins.\n-Custom Prefix.\n-Removes \"Pinned Message:\" header.\n-All features are free to use.\n-More to come!", false);
        event.replyEmbeds(embed.build()).queue();
    }

    private void createEmbed(SlashCommandInteractionEvent event) {
        String message = option(event, "message");
        if (message.length() > 1000) {
            replyError(event, "Text is too long; it must be under 1000 characters.");
            return;
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setDescription(message)
                .setFooter("Embed By: " + event.getUser().getName());
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.reply("Embed sent.").setEphemeral(true).queue();
    }

    private void replyPermCheck(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        Member self = event.getGuild().getSelfMember();
        String result = "**AnchorBot has the following permissions in this CHANNEL:**\n\n" +
                permissionLine("Message History", self.hasPermission(channel, Permission.MESSAGE_HISTORY)) +
                permissionLine("Manage Messages", self.hasPermission(channel, Permission.MESSAGE_MANAGE)) +
                permissionLine("Embed Links", self.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) +
                permissionLine("Add Message Reactions", self.hasPermission(channel, Permission.MESSAGE_ADD_REACTION));
        event.reply(result).setEphemeral(true).queue();
    }

    private void replyStaffHelp(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("**-AnchorBot Staff Commands-**")
                .addField("Staff Commands:", "`/shardping`, `/getshard`, `/restartshard`, `/manualstop`, `/adminstats`, `/topservers`, `/shutdown`, `/restart`", false);
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void replyShardForServer(SlashCommandInteractionEvent event) {
        try {
            String serverId = option(event, "server_id");
            event.reply("`" + serverId + "` is on shard: **" + ((Long.parseLong(serverId) >>> 22) % Main.jda.getShardsTotal()) + "**").setEphemeral(true).queue();
        } catch (Exception e) {
            replyError(event, "Something went wrong. Use a numeric server ID.");
        }
    }

    private void restartShard(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        int shard = event.getOption("shard").getAsInt();
        Main.jda.restart(shard);
        event.reply("Shard restarted.").setEphemeral(true).queue();
    }

    private void manualStop(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        String channelId = option(event, "channel_id");
        Main.mapMessage.remove(channelId);
        Main.mapMessageSlow.remove(channelId);
        Main.mapMessageEmbed.remove(channelId);
        Main.webhookMessage.remove(channelId);
        ConvexDb.delete(DbKinds.STICKY, channelId);
        ConvexDb.delete(DbKinds.SLOW_STICKY, channelId);
        ConvexDb.delete(DbKinds.EMBED_STICKY, channelId);
        ConvexDb.delete(DbKinds.WEBHOOK_MESSAGE, channelId);
        event.reply("Stopped pins in channel `" + channelId + "`.").setEphemeral(true).queue();
    }

    private void replyAdminStats(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("AnchorBot Admin Stats")
                .setDescription("Runtime and shard status for AnchorBot.")
                .addField("Available processors:", Integer.toString(Runtime.getRuntime().availableProcessors()), false)
                .addField("Free memory:", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory()), true)
                .addField("Total memory available to JVM:", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()), true)
                .addField("AnchorBot session uptime:", "Uptime: ``" + uptime() + "``", false);
        File[] roots = File.listRoots();
        for (File root : roots) {
            embed.addField("File system root: " + root.getAbsolutePath(),
                    "Total space: " + FileUtils.byteCountToDisplaySize(root.getTotalSpace()) +
                            "\nFree space: " + FileUtils.byteCountToDisplaySize(root.getFreeSpace()) +
                            "\nUsable space: " + FileUtils.byteCountToDisplaySize(root.getUsableSpace()), true);
        }
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void replyTopServers(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        String info = Main.jda.getGuilds().stream()
                .sorted(Comparator.comparingInt(Guild::getMemberCount).reversed())
                .limit(10)
                .map(guild -> "**Name:** " + guild.getName() + "\n**Member Count:** " + NumberFormat.getInstance().format(guild.getMemberCount()) + "\n**ID:** ``" + guild.getId() + "``")
                .collect(Collectors.joining("\n\n"));
        event.reply(info + "\n\n*Out of* *" + Main.jda.getGuildCache().size() + "* *servers*").setEphemeral(true).queue();
    }

    private void shutdown(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        event.reply("```Shutting Down Bot```").setEphemeral(true).queue(success -> Main.jda.shutdown());
    }

    private void restart(SlashCommandInteractionEvent event) {
        if (!requireOwner(event)) {
            return;
        }
        event.reply("```Restarting Bot```").setEphemeral(true).queue(success -> Main.jda.restart());
    }

    private void replyShardInfo(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("-Shard Info-")
                .addField("Shards:", "Total Shards: " + Main.jda.getShardsTotal() + "\nThis Guilds Shard: " + ((event.getGuild().getIdLong() >>> 22) % Main.jda.getShardsTotal()), false);
        event.replyEmbeds(embed.build()).queue();
    }

    private void replyShardPing(SlashCommandInteractionEvent event) {
        StringBuilder pings = new StringBuilder("__**Shard Pings:**__\n`Average: " + Main.jda.getAverageGatewayPing() + "`\n");
        for (JDA shard : Main.jda.getShards()) {
            pings.append("**Shard:** ").append(shard.getShardInfo().getShardString()).append(" **Ping:** ").append(shard.getGatewayPing()).append("ms. **Status:** ").append(shard.getStatus()).append("\n");
        }
        event.reply(pings.toString()).queue();
    }

    private void replyHelp(SlashCommandInteractionEvent event) {
        String prefix = BotUtil.currentPrefix(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setTitle("**-AnchorBot Commands-**")
                .setDescription("Prefix commands still work with `" + prefix + "`.")
                .addField("Pin Commands:", "`" + prefix + "stick <message>` or `/stick`\n`" + prefix + "stickembed <message>` or `/stickembed`\n`" + prefix + "stickslow <message>` or `/stickslow`\n`" + prefix + "stickstop` or `/stickstop`\n`" + prefix + "getstickies` or `/getstickies`", false)
                .addField("Utility Commands:", "`/poll`, `/apoll`, `/userinfo`, `/serverinfo`, `/embed`, `/permcheck`, `/features`", false)
                .addField("Settings:", "`/prefix`, `/resetprefix`, `/setimage`, `/setbigimage`, `/setwebhook`", false);
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private static void addPinFields(EmbedBuilder embed, Guild guild, String title, Map<String, String> map) {
        for (String channelId : BotUtil.activeChannelIds(guild, map)) {
            embed.addField(title, "Channel: " + BotUtil.channelMention(guild, channelId) + "\n__Pinned Message:__```\n" + map.get(channelId) + "```", false);
        }
    }

    private static EmbedBuilder pinEmbed(String channelId, String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotStyle.PRIMARY)
                .setDescription(message);
        if (Main.mapImageLinkEmbed.containsKey(channelId)) {
            embed.setThumbnail(Main.mapImageLinkEmbed.get(channelId));
        }
        if (Main.mapBigImageLinkEmbed.containsKey(channelId)) {
            embed.setImage(Main.mapBigImageLinkEmbed.get(channelId));
        }
        return embed;
    }

    private static void sendWebhookPin(String channelId) {
        String webhookUrl = Main.webhookURL.get(channelId);
        if (!BotUtil.isDiscordWebhookUrl(webhookUrl) || !Main.webhookMessage.containsKey(channelId)) {
            return;
        }

        WebhookClientBuilder builder = new WebhookClientBuilder(webhookUrl);
        builder.setThreadFactory(job -> {
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

    private static void deleteIfPresent(GuildMessageChannel channel, Map<String, String> deleteIds, String channelId) {
        String messageId = deleteIds.remove(channelId);
        if (messageId != null) {
            channel.deleteMessageById(messageId).queue(null, error -> {});
        }
    }

    private static String option(SlashCommandInteractionEvent event, String name) {
        OptionMapping option = event.getOption(name);
        return option == null ? "" : BotUtil.stripCustomEmoji(option.getAsString());
    }

    private boolean requireManageMessages(SlashCommandInteractionEvent event) {
        if (BotUtil.hasManageMessages(event.getMember())) {
            return true;
        }
        replyError(event, "You need the `Manage Messages` permission to use this command.");
        return false;
    }

    private boolean requireManageServer(SlashCommandInteractionEvent event) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        }
        replyError(event, "You need the `Manage Server` permission to use this command.");
        return false;
    }

    private boolean requireOwner(SlashCommandInteractionEvent event) {
        if (Main.isOwner(event.getUser().getIdLong())) {
            return true;
        }
        replyError(event, "Only configured bot owners can use this command.");
        return false;
    }

    private static void replyError(SlashCommandInteractionEvent event, String message) {
        event.reply(message).setEphemeral(true).queue();
    }

    private static String permissionLine(String name, boolean value) {
        return "```" + (value ? "java" : "c") + "\n" + name + ": " + value + "```";
    }

    private static String uptime() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        long seconds = runtime.getUptime() / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds / 60) - (hours * 60);
        long remainingSeconds = seconds % 60;
        return hours + " Hours, " + minutes + " Min, " + remainingSeconds + " Seconds";
    }
}

