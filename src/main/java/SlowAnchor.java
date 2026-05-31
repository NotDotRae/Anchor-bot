import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlowAnchor extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if (BotUtil.shouldIgnore(event)) {
            return;
        }


        String[] args = event.getMessage().getContentRaw().split("\\s+");
        //Member anchorBot = event.getGuild().getMemberById(Main.botId);
        String channelId = event.getChannel().getId();

        String prefix = "?";
        if (Main.mapPrefix.containsKey(event.getGuild().getId())) {
            prefix = Main.mapPrefix.get(event.getGuild().getId());
        }

        if (event.getMessage().getContentRaw().startsWith(prefix + "stickslow ") && event.getMessage().getContentRaw().matches("[\\S]+\\s{2,}.*") && (permCheck(event.getMember())) && !event.getAuthor().isBot()) {
            event.getMessage().reply(event.getMember().getAsMention() + " please only use one space after the `?stickslow` command!").queue();
            return;
        }

        if (args[0].equalsIgnoreCase(prefix + "stickslow") && (permCheck(event.getMember())) && !event.getAuthor().isBot()) {
            if (false) {
                EmbedBuilder em = new EmbedBuilder();
                em.setTitle("**Whoops! This is an AnchorBot Command!** ")
                        .addField("__AnchorBot__ allows for slow pins plus other features.", "This command is available to all servers.", false);
                event.getMessage().replyEmbeds(em.setColor(BotStyle.PRIMARY).build()).queue();
            } else {
                try {
                    //remove last pin message if there is one (user used pin command while already having one)

                if(Main.mapMessage.containsKey(event.getChannel().getId())) {
                        Main.mapMessage.remove(channelId);

                        if(Main.mapDeleteId.get(channelId) != null) {
                            event.getChannel().deleteMessageById(Main.mapDeleteId.get(channelId)).queue(null, (error) -> {});
                        }
                    }


                    if(Main.mapDeleteId2.get(channelId) != null) {
                        event.getChannel().deleteMessageById(Main.mapDeleteId2.get(channelId)).queue(null, (error) -> {});
                    }


                        String input = event.getMessage().getContentRaw();

                        if (event.getMessage().getContentRaw().contains(prefix + "stickslow \n")) {
                            event.getMessage().reply(event.getMember().getAsMention() + " Error: Please provide text after `" + prefix + "stickslow` before using a new line.").queue();
                            return;
                        }


                        String [] arr = input.split(" ", 2);
                        Main.mapMessageSlow.put(event.getChannel().getId(), BotUtil.stripCustomEmoji(arr[1].trim()));
                        removeDB(channelId);
                        addDB(channelId, Main.mapMessageSlow.get(channelId));

                    SilentMessages.send(event.getChannel(), Main.mapMessageSlow.get(channelId)).queue(m -> Main.mapDeleteId2.put(event.getChannel().getId(), m.getId()));
                    BotUtil.react(event.getMessage(), "✅");
                } catch (Exception e) {
                    event.getMessage().reply(event.getMember().getAsMention() + " please use this format: `" + prefix + "stickslow <message>`.").queue();
                }
            }
            

        } else if (args[0].equalsIgnoreCase(prefix + "stickslow") && (!permCheck(event.getMember() ))) {
            //Adds X emote
            BotUtil.react(event.getMessage(), "❌");
            //event.getChannel().sendMessage(event.getMember().getAsMention() + " you need the global `Manage Messages` permission to use this command!").queue();
        }

        else if ( (args[0].equalsIgnoreCase(prefix + "stickstop") || args[0].equalsIgnoreCase(prefix + "unstick")) && (permCheck(event.getMember() ))) {
            Main.mapMessageSlow.remove(channelId);

            if(Main.mapDeleteId2.get(channelId) != null) {
                event.getChannel().deleteMessageById(Main.mapDeleteId2.get(channelId)).queue(null, (error) -> {});
            }

            removeDB(channelId);
            BotUtil.react(event.getMessage(), "✅");
        } else if ( (args[0].equalsIgnoreCase(prefix + "stickstop") || args[0].equalsIgnoreCase(prefix + "unstick")) && (!permCheck(event.getMember() ))) {
            //Adds X mark
            BotUtil.react(event.getMessage(), "❌");
            //event.getChannel().sendMessage(event.getMember().getAsMention() + " you need the global `Manage Messages` permission to use this command!").queue();
        }

        if (Main.mapMessageSlow.get(channelId) != null) {

            event.getChannel().getHistory().retrievePast(18).queue(history -> {
                try {
                    for(Message m : history.subList(0, 13)) {
                        //if message is pin message
                        if(m.getContentRaw().equals(Main.mapMessageSlow.get(channelId))) {
                            //if message is older then 35 sec
                            if(m.getTimeCreated().compareTo(OffsetDateTime.now().minusSeconds(35)) < 0) {
                                m.delete().queue(null, (error) -> {});
                                SilentMessages.send(event.getChannel(), Main.mapMessageSlow.get(channelId)).queue(mes -> Main.mapDeleteId2.put(channelId, mes.getId()));
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    //do nothing
                }

                //gets set to true if one of last five messages contains pin message.
                Boolean check = false;

                try {
                    for(Message m : history.subList(0, 13)) {
                        if(m.getContentRaw().equals(Main.mapMessageSlow.get(channelId))) {
                            check = true;
                        }
                    }
                } catch (Exception e) {
                    //do nothing
                }

                if(!check) {
                    if(Main.mapDeleteId2.get(channelId) != null) {
                        event.getChannel().deleteMessageById(Main.mapDeleteId2.get(channelId)).queue(null, (error) -> {});
                    }

                    //If message send fails, stickstop
                    if (event.getChannel().canTalk()) {
                        SilentMessages.send(event.getChannel(), Main.mapMessageSlow.get(channelId)).queue(mes -> Main.mapDeleteId2.put(channelId, mes.getId()));
                    } else {
                        Main.mapMessageSlow.remove(channelId);
                        removeDB(channelId);
                        System.out.println("StickStop Override due to missing write permission");
                    }
                }

                //Added to make sure it does not bug and send two pins (next 5 lines)
                List<Message> indexes = new ArrayList<>();

                for (Message mes : history) {
                    if (mes.getContentRaw().equals(Main.mapMessageSlow.get(channelId))) {
                        indexes.add(mes);
                    }
                }

                if (!indexes.isEmpty() && indexes.size() > 1) {
                    indexes.remove(0);
                    for (Message mess : indexes) {
                        mess.delete().queue(null, (error) -> {});
                    }
                }
            });
        }
    }

    public boolean permCheck(Member member) {
        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            return true;
        } else {
            return false;
        }
    }

    public void addDB(String channelId, String message) {
        ConvexDb.upsert(DbKinds.SLOW_STICKY, channelId, message);
    }

    public void removeDB(String channelId) {
        ConvexDb.delete(DbKinds.SLOW_STICKY, channelId);
    }


}

