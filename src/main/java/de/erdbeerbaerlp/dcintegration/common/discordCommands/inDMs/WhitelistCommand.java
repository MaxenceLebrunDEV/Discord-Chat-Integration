package de.erdbeerbaerlp.dcintegration.common.discordCommands.inDMs;

import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.PlayerLinkController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.erdbeerbaerlp.dcintegration.common.util.Variables.discord_instance;


public class WhitelistCommand extends DMCommand {
    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Whitelists you on the server by linking with Discord";
    }

    @Override
    public void execute(String[] args, MessageReceivedEvent ev) {
        if (discord_instance.getChannel().getGuild().isMember(ev.getAuthor())) {
            Member m = discord_instance.getChannel().getGuild().getMember(ev.getAuthor());
            if (Configuration.instance().linking.requiredRoles.length != 0) {
                AtomicBoolean ok = new AtomicBoolean(false);
                m.getRoles().forEach((role) -> {
                    for (String s : Configuration.instance().linking.requiredRoles) {
                        if (s.equals(role.getId())) ok.set(true);
                    }
                });
                if (!ok.get()) {
                    ev.getChannel().sendMessage(Configuration.instance().localization.link_requiredRole).queue();
                    return;
                }
            }
        } else {
            ev.getChannel().sendMessage(Configuration.instance().localization.link_notMember).queue();
            return;
        }
        if (PlayerLinkController.isDiscordLinked(ev.getAuthor().getId())) {
            ev.getChannel().sendMessage(Configuration.instance().localization.alreadyLinked.replace("%player%", PlayerLinkController.getNameFromUUID(PlayerLinkController.getPlayerFromDiscord(ev.getAuthor().getId())))).queue();
            return;
        }
        if (args.length > 1) {
            ev.getChannel().sendMessage(Configuration.instance().localization.tooManyArguments).queue();
            return;
        }
        if (args.length < 1) {
            ev.getChannel().sendMessage(Configuration.instance().localization.notEnoughArguments).queue();
            return;
        }
        UUID u;
        String s = args[0];
        try {
            if (!s.contains("-"))
                s = s.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                );
            u = UUID.fromString(s);
            final boolean linked = PlayerLinkController.linkPlayer(ev.getAuthor().getId(), u);
            if (linked)
                ev.getChannel().sendMessage(Configuration.instance().localization.linkSuccessful.replace("%name%", PlayerLinkController.getNameFromUUID(u))).queue();
            else
                ev.getChannel().sendMessage(Configuration.instance().localization.linkFailed).queue();
        } catch (IllegalArgumentException e) {
            ev.getChannel().sendMessage(Configuration.instance().localization.link_argumentNotUUID.replace("%prefix%", Configuration.instance().commands.prefix).replace("%arg%", s)).queue();
        }
    }

    @Override
    public boolean requiresLink() {
        return false;
    }
}
