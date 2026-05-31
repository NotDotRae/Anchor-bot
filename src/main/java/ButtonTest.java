import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class ButtonTest extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }


        //split each word of text into array by spaces
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        //if bot sent the message return and do nothing
        if (event.getAuthor().isBot()) {
            return;
        }

        //sets prefix (? default, can be different for features members)
        String prefix = "?";
        if (Main.mapPrefix.containsKey(event.getGuild().getId())) {
            prefix = Main.mapPrefix.get(event.getGuild().getId());
        }

        if (args[0].equalsIgnoreCase(prefix + "b") && event.getAuthor().getId().equals("0")) {

            event.getChannel().sendMessage("Click a Button:")
                    .setComponents(ActionRow.of(Button.primary("blue", "Blue"),
                            Button.success("green", "Green"),
                            Button.danger("red", "Red"),
                            Button.secondary("gray", "Gray"))).queue();
        }
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {
        String customId = event.getButton() == null ? null : event.getButton().getCustomId();
        if (customId == null) {
            return;
        }
        String mention = event.getMember() == null ? event.getUser().getAsMention() : event.getMember().getAsMention();
        if (customId.equals("blue")) {
            event.reply(mention + " Clicked The **Blue** Button!").queue(m -> m.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else if (customId.equals("green")) {
            event.reply(mention + " Clicked The **Green** Button!").queue(m -> m.deleteOriginal().queueAfter(8, TimeUnit.SECONDS));
        } else if (customId.equals("red")) {
            event.reply(mention + " Clicked The **Red** Button!").queue(m -> m.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else if (customId.equals("gray")) {
            event.reply(mention + " Clicked The **Gray** Button!").queue(m -> m.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        }
    }
}

