package org.prithvidiamond1.Commands;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.prithvidiamond1.BotConstants;
import org.prithvidiamond1.DB.Models.DiscordServer;
import org.prithvidiamond1.DB.Repositories.ServerRepository.ServerRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the actions of the prefix command
 * <br>
 * Allows the user to change the guild prefix of the bot within the server
 */
@Component
public class PrefixCommand extends BaseCommand {
    private final String name = "prefix";

    private final String description = "A command to change the guild prefix of the bot. *_This command takes arguments_*";

    private final List<SlashCommandOption> slashCommandOptions = new ArrayList<>();

    private final ServerRepository serverRepository;

    public PrefixCommand(Logger logger, ServerRepository serverRepository){
        super(logger);
        this.serverRepository = serverRepository;

        slashCommandOptions.add(SlashCommandOption.create(
                SlashCommandOptionType.STRING,
                "prefix-string",
                "A command to change the guild prefix of the bot",
                true
        ));
    }

    /**
     * Method that does the core function of the prefix command
     * @param user the user calling the play command
     * @param server the server in which the play command was invoked
     * @param validatedPrefix a valid prefix that can be used to replace the current prefix for the server
     * @return an embed ({@link EmbedBuilder}) that can be used to notify the status of the command's actions after being called
     */
    private EmbedBuilder commandFunction(User user, Server server, String validatedPrefix){
        EmbedBuilder response;
        if (!user.isBot() && server.isAdmin(user)) {
            DiscordServer serverModel = this.serverRepository.resolveServerModelById(server);
            serverModel.setGuildPrefix(validatedPrefix);
            this.serverRepository.save(serverModel);
            response = new EmbedBuilder()
                    .setTitle("Guild Command Prefix Changed!")
                    .setDescription(String.format("Guild prefix has been set to **%s**", validatedPrefix))
                    .setColor(BotConstants.botAccentColor);
        } else {
            response = new EmbedBuilder()
                    .setTitle("You cannot change guild command prefixes for this server!")
                    .setDescription("You do not have the required permissions for this action! Contact a server admin and request for a change")
                    .setColor(BotConstants.botAccentColor);
        }

        return response;
    }

    /**
     * the guild version of the prefix command
     * @param event the guild command trigger event
     */
    @Override
    public void runCommand(MessageCreateEvent event) {
        User author;
        Server server;
        MessageAuthor messageAuthor = event.getMessageAuthor();
        String message = event.getMessageContent();
        if (messageAuthor.asUser().isPresent()) {
            author = messageAuthor.asUser().get();
        } else {
            // throw an error and log it.
            throw new NullPointerException("Message author was null");
        }
        if (event.getServer().isPresent()){
            server = event.getServer().get();
        } else {
            // throw an error and log it.
            throw new NullPointerException("Server was null");
        }

        DiscordServer serverModel = this.serverRepository.resolveServerModelById(server);
        String currentPrefix = serverModel.getGuildPrefix();

        String unvalidatedNewPrefix = message.substring((currentPrefix.length())+("prefix".length())).strip();

        boolean prefixIsValid = false;
        if (unvalidatedNewPrefix.length() != 0){
            prefixIsValid = ((unvalidatedNewPrefix.charAt(0)=='\"') && (unvalidatedNewPrefix.charAt(unvalidatedNewPrefix.length()-1)=='\"'));
        }

        if (!prefixIsValid) {
            new MessageBuilder().setEmbed(
                            new EmbedBuilder()
                                    .setTitle("Prefix Command")
                                    .setDescription(String.format("To use the prefix command, type **%sprefix \"<NEW_PREFIX>\"**\nNote that the new prefix cannot be a blank string or one with only white spaces", currentPrefix))
                                    .setColor(BotConstants.botAccentColor))
                    .send(event.getChannel())
                    .exceptionally(exception -> {   // Error message for failing to respond to the guild command
                        getLogger().error("Unable to respond to the guild command!");
                        getLogger().error(exception.getMessage());
                        return null;
                    });
        } else{
            String newPrefix = unvalidatedNewPrefix.replaceAll("\"", "");
            if (newPrefix.isBlank()) {
                new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                        .setTitle("Blank text cannot be set as a prefix!")
                                        .setDescription("Make sure to set a prefix that is not blank by following the correct syntax")
                                        .setColor(BotConstants.botAccentColor))
                        .send(event.getChannel())
                        .exceptionally(exception -> {   // Error message for failing to respond to the guild command
                            getLogger().error("Unable to respond to the guild command!");
                            getLogger().error(exception.getMessage());
                            return null;
                        });
            } else {
                EmbedBuilder response = commandFunction(author, server, newPrefix);
                new MessageBuilder().setEmbed(response)
                        .send(event.getChannel())
                        .exceptionally(exception -> { // Error message for failing to respond to the guild command
                            getLogger().error("Unable to respond to the guild command!");
                            getLogger().error(exception.getMessage());
                            return null;
                        });
            }
        }
    }

    /**
     * the slash version of the prefix command
     * @param event the slash command trigger event
     */
    @Override
    public void runCommand(SlashCommandCreateEvent event) {
        SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
        User author = slashCommandInteraction.getUser();
        slashCommandInteraction.getServer()
                .ifPresent(server -> slashCommandInteraction.getOptionByName("prefix-string").flatMap(SlashCommandInteractionOption::getStringValue)
                        .ifPresent(newPrefix -> {
                            EmbedBuilder response = commandFunction(author, server, newPrefix);
                            slashCommandInteraction.createImmediateResponder().addEmbed(response)
                                    .respond()
                                    .exceptionally(exception -> {   // Error message for failing to respond to the slash command interaction
                                        getLogger().error("Unable to respond to the slash command interaction");
                                        getLogger().error(exception.getMessage());
                                        return null;
                                    });
                        }));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public List<SlashCommandOption> getSlashCommandOptions() {
        return null;
    }
}
