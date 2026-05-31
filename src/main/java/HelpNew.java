import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class HelpNew extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (event.getAuthor().isBot()) {
            return;
        }

        String prefix = currentPrefix(event.getGuild().getId());
        if (args[0].equalsIgnoreCase(prefix + "help") || args[0].equalsIgnoreCase(prefix + "commands") || args[0].equalsIgnoreCase(Main.prefix + "help")) {
            try {
                event.getMessage().replyEmbeds(pinHelp(prefix).build())
                        .setComponents(navButtons("pin"))
                        .queue(null, error -> event.getChannel().sendMessage("I need the `Embed Links` permission.").queue());
            } catch (Exception e) {
                event.getMessage().reply("I need the `Embed Links` permission.").queue();
            }
        }
    }

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) {
            return;
        }

        String prefix = event.getGuild() == null ? "?" : currentPrefix(event.getGuild().getId());
        try {
            event.replyEmbeds(pinHelp(prefix).build())
                    .addComponents(navButtons("pin"))
                    .queue(null, error -> event.reply("I need the `Embed Links` permission.").setEphemeral(true).queue());
        } catch (Exception e) {
            event.reply("I need the `Embed Links` permission.").setEphemeral(true).queue();
        }
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getGuild() == null || event.getButton() == null) {
            return;
        }
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null) {
            return;
        }
        String prefix = currentPrefix(event.getGuild().getId());

        if (buttonId.equals("pin") || buttonId.equals("sticky")) {
            event.editMessageEmbeds(pinHelp(prefix).build()).setComponents(navButtons("pin")).queue();
        } else if (buttonId.equals("utility")) {
            event.editMessageEmbeds(utilityHelp(prefix).build()).setComponents(navButtons("utility")).queue();
        } else if (buttonId.equals("other")) {
            event.editMessageEmbeds(otherHelp(prefix).build()).setComponents(navButtons("other")).queue();
        } else if (buttonId.equals("features")) {
            event.editMessageEmbeds(featuresHelp(prefix).build()).setComponents(navButtons("features")).queue();
        }
    }

    private static String currentPrefix(String guildId) {
        return Main.mapPrefix.getOrDefault(guildId, "?");
    }

    private static EmbedBuilder baseEmbed(String title, String prefix) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setColor(BotStyle.PRIMARY);
        embed.setDescription("Do not include `<>` when using commands.");
        embed.addField("Prefix:", "This guild's prefix: `" + prefix + "`", true);
        embed.addField("Feature Access:", AdvancedStatus(), true);
        embed.setFooter("Click a button to see other commands.", BotUtil.botAvatarUrl());
        return embed;
    }

    private static EmbedBuilder pinHelp(String prefix) {
        return baseEmbed("**-AnchorBot Commands-**", prefix)
                .addField("Pin Commands:", "``" + prefix + "stick <message>`` - Pins message to the channel.\n" +
                        "``" + prefix + "stickstop`` - Cancels the pinned message.\n" +
                        "``" + prefix + "getstickies`` - Lists all active pins in the server.\n" +
                        "Members need Manage Messages permission to use pin commands.", false);
    }

    private static EmbedBuilder utilityHelp(String prefix) {
        return baseEmbed("**-AnchorBot Utility Commands-**", prefix)
                .addField("Utility Commands:", "``" + prefix + "poll <question>`` - Create a yes/no poll.\n" +
                        "``" + prefix + "apoll <question, option1, option2>`` - Create a multiple choice poll.\n" +
                        "``" + prefix + "userinfo <@user>`` - Get info on a member.\n" +
                        "``" + prefix + "serverinfo`` - Get info on the server.\n" +
                        "``" + prefix + "embed <message>`` - Turns your message into an embed.", false);
    }

    private static EmbedBuilder otherHelp(String prefix) {
        return baseEmbed("**-AnchorBot Other Commands-**", prefix)
                .addField("Other Commands:", "``" + prefix + "about`` - Information about AnchorBot.\n" +
                        "``" + prefix + "uptime`` - Shows the bot uptime.\n" +
                        "``" + prefix + "permcheck`` - Checks the bot's channel permissions.\n" +
                        "``" + prefix + "features`` - Shows included features.\n" +
                        "``" + prefix + "disablecommands`` - Disable all non-pin commands.\n" +
                        "``" + prefix + "enablecommands`` - Enable all non-pin commands.", false);
    }

    private static EmbedBuilder featuresHelp(String prefix) {
        return baseEmbed("**-AnchorBot Feature Commands-**", prefix)
                .addField("Feature Commands:", "``" + prefix + "stickembed <message>`` - Creates a pinned embed.\n" +
                        "``" + prefix + "stickslow <message>`` - Creates a slower pin.\n" +
                        "``" + prefix + "stickwebhook <message>`` - Creates a pinned webhook message.\n" +
                        "``" + prefix + "setwebhook <webhook url>`` - Sets the channel webhook.\n" +
                        "``" + prefix + "webhookstop`` - Stops the webhook pin.\n" +
                        "``" + prefix + "setimage <image url>`` - Sets an embed image.\n" +
                        "``" + prefix + "removeimage`` - Removes an embed image.\n" +
                        "``" + prefix + "setbigimage <image url>`` - Sets a large embed image.\n" +
                        "``" + prefix + "removebigimage`` - Removes a large embed image.\n" +
                        "``" + prefix + "getimage`` - Shows the current channel image setting.\n" +
                        "``" + prefix + "prefix <prefix>`` - Sets the server prefix.\n" +
                        "``?resetprefix`` - Resets the prefix to `?`.", false);
    }

    private static ActionRow navButtons(String selected) {
        return ActionRow.of(
                button("pin", "Pin", "\uD83D\uDCCC", selected),
                button("utility", "Utility", "\uD83C\uDF9B", selected),
                button("other", "Other", "\uD83D\uDCC3", selected),
                button("features", "Features", "\uD83E\uDDE1", selected));
    }

    private static Button button(String id, String label, String emoji, String selected) {
        Button button = selected.equals(id) ? Button.success(id, label) : Button.primary(id, label);
        return button.withEmoji(Emoji.fromUnicode(emoji));
    }

    public static String AdvancedStatus() {
        return "`All Features Active`";
    }
}

