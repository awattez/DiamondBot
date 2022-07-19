package org.prithvidiamond1.Commands;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.prithvidiamond1.Listeners.CommandHandler;
import org.prithvidiamond1.Main;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.prithvidiamond1.ServerHelperFunctions.resolveServerModelById;

/**
 * This class contains the actions of the help command
 * <br>
 * Displays a description of all the available bot commands
 */
public class HelpCommand implements Command{
    private final String name = "help";

    private final String description = "A command that shows all the commands of the bot and their descriptions";

    private final List<SlashCommandOption> slashCommandOptions = null;

    /**
     * the guild version of the help command
     * @param event the guild command trigger event
     */
    @Override
    public void runCommand(MessageCreateEvent event) {
        event.getServer().ifPresent(server -> {
            String commandPrefix = resolveServerModelById(server).getGuildPrefix();
            new MessageBuilder().setEmbed(new EmbedBuilder()
                            .setTitle("List of guild commands and their descriptions")
                            .setDescription(CommandHandler.generateHelpDescription(commandPrefix))
                            .setThumbnail(Main.botIconURL)
                            .setColor(Main.botAccentColor))
                    .send(event.getChannel())
                    .exceptionally(exception -> {   // Error message for failing to respond to the guild command
                        Main.logger.error("Unable to respond to the guild command!");
                        Main.logger.error(exception.getMessage());
                        return null;
                    });
        });
    }

    /**
     * the slash version of the help command
     * @param event the slash command trigger event
     */
    @Override
    public void runCommand(SlashCommandCreateEvent event) {
        SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
        slashCommandInteraction.getServer().ifPresent(server -> slashCommandInteraction.createImmediateResponder()
                        .addEmbed(new EmbedBuilder()
                                .setTitle("List of slash commands and their descriptions")
                                .setDescription(CommandHandler.generateHelpDescription("/")) // Slash commands don't actually have command prefixes
                                .setThumbnail(Main.botIconURL)
                                .setColor(Main.botAccentColor)
                        ).respond()
                        .exceptionally(exception -> {   // Error message for failing to respond to the slash command interaction
                            Main.logger.error("Unable to respond to the slash command interaction");
                            Main.logger.error(exception.getMessage());
                            return null;
                        })
        );
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
        return this.slashCommandOptions;
    }
}
