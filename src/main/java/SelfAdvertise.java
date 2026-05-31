import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.discordbots.api.client.DiscordBotListAPI;

public class SelfAdvertise extends ListenerAdapter
{
    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }


        if(event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannel().getId().equals("771095262126669854")) {
            if (Main.topggAPIToken.isBlank()) {
                return;
            }

            DiscordBotListAPI api = new DiscordBotListAPI.Builder()
                    .token(Main.topggAPIToken)
                    .botId(Main.botId)
                    .build();

            String userId = event.getAuthor().getId();
            api.hasVoted(userId).whenComplete((hasVoted, e) -> {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                if(Boolean.TRUE.equals(hasVoted)) {
                   BotUtil.react(event.getMessage(), "⚓");
                } else {
                    event.getMessage().delete().queue();
                }
            });
        }
    }
}

