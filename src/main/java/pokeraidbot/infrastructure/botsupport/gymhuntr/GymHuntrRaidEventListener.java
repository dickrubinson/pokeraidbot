package pokeraidbot.infrastructure.botsupport.gymhuntr;

import main.BotServerMain;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pokeraidbot.Utils;
import pokeraidbot.domain.config.ClockService;
import pokeraidbot.domain.config.LocaleService;
import pokeraidbot.domain.gym.Gym;
import pokeraidbot.domain.gym.GymRepository;
import pokeraidbot.domain.pokemon.Pokemon;
import pokeraidbot.domain.pokemon.PokemonRepository;
import pokeraidbot.domain.raid.Raid;
import pokeraidbot.domain.raid.RaidRepository;
import pokeraidbot.infrastructure.jpa.config.Config;
import pokeraidbot.infrastructure.jpa.config.ServerConfigRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class GymHuntrRaidEventListener implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GymHuntrRaidEventListener.class);

    private ServerConfigRepository serverConfigRepository;
    private RaidRepository raidRepository;
    private GymRepository gymRepository;
    private PokemonRepository pokemonRepository;
    private LocaleService localeService;
    private ExecutorService executorService;
    private final ClockService clockService;

    public GymHuntrRaidEventListener(ServerConfigRepository serverConfigRepository, RaidRepository raidRepository,
                                     GymRepository gymRepository, PokemonRepository pokemonRepository,
                                     LocaleService localeService, ExecutorService executorService,
                                     ClockService clockService) {
        this.serverConfigRepository = serverConfigRepository;
        this.raidRepository = raidRepository;
        this.gymRepository = gymRepository;
        this.pokemonRepository = pokemonRepository;
        this.localeService = localeService;
        this.executorService = executorService;
        this.clockService = clockService;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent guildEvent = (GuildMessageReceivedEvent) event;
            if (guildEvent.getAuthor().isBot() && StringUtils.containsIgnoreCase(
                    guildEvent.getAuthor().getName(), "gymhuntrbot")) {
                final String serverName = guildEvent.getGuild().getName().toLowerCase();
                Config config = serverConfigRepository.getConfigForServer(serverName);
                final List<MessageEmbed> embeds = guildEvent.getMessage().getEmbeds();
                if (embeds != null && embeds.size() > 0) {
                    for (MessageEmbed embed : embeds) {
                        final LocalDateTime currentDateTime = clockService.getCurrentDateTime();
                        final String description = embed.getDescription();
                        final String title = embed.getTitle();
                        // todo: detect "starting soon" for tier 5 bosses, and have mapping of what legendary boss
                        // is active for the current zone
                        if (StringUtils.containsIgnoreCase(title, "has started!")) {
                            List<String> newRaidArguments = argumentsToCreateRaid(description, clockService);
//                            guildEvent.getMessage().getChannel().sendMessage().queue();
                            // todo: arguments checking
                            final Iterator<String> iterator = newRaidArguments.iterator();
                            final String gym = iterator.next();
                            final String pokemon = iterator.next();
                            final String time = iterator.next();
                            final Pokemon raidBoss = pokemonRepository.getByName(pokemon);
                            config = serverConfigRepository.getConfigForServer(serverName);
                            final Gym raidGym = gymRepository.findByName(gym, config.getRegion());
                            final LocalDate currentDate = currentDateTime.toLocalDate();
                            final LocalDateTime endOfRaid = LocalDateTime.of(currentDate,
                                    LocalTime.parse(time, Utils.timeParseFormatter));
                            final boolean moreThan10MinutesLeftOnRaid = endOfRaid.isAfter(currentDateTime.plusMinutes(10));
                            if (moreThan10MinutesLeftOnRaid) {
                                // todo: check if raid already exists
                                final Raid raidToCreate = new Raid(raidBoss,
                                        endOfRaid,
                                        raidGym,
                                        localeService, config.getRegion());
                                final Raid createdRaid = raidRepository.newRaid(guildEvent.getAuthor(), raidToCreate);
                                final Locale locale = config.getLocale();
                                final MessageEmbed messageEmbed = new EmbedBuilder().setTitle(null, null)
                                        .setDescription(localeService.getMessageFor(LocaleService.NEW_RAID_CREATED,
                                                locale, createdRaid.toString(locale))).build();
                                guildEvent.getMessage().getChannel().sendMessage(messageEmbed).queue(m -> {
                                    LOGGER.info("Raid created via Gymhuntr integration: " + createdRaid);
                                });
                            } else {
                                LOGGER.debug("Skipped creating raid at " + gym +
                                        ", less than 10 minutes remaining on it.");
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<String> argumentsToCreateRaid(String description, ClockService clockService) {
        final String[] firstPass = description.replaceAll("[*]", "").replaceAll("[.]", "")
                .replaceAll("Raid Ending: ", "").split("\n");
        final String[] timeArguments = firstPass[3].replaceAll("hours ", "")
                .replaceAll("min ", "").replaceAll("sec", "").split(" ");
        final String timeString = Utils.printTime(clockService.getCurrentTime()
                .plusHours(Long.parseLong(timeArguments[0]))
                .plusMinutes(Long.parseLong(timeArguments[1]))
                .plusSeconds(Long.parseLong(timeArguments[2])));
        final String[] secondPass = new String[]{firstPass[0], firstPass[1], timeString};
        return Arrays.asList(secondPass);
    }
}
