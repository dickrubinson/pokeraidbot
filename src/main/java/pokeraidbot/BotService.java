package pokeraidbot;

import com.jagrosh.jdautilities.commandclient.CommandClient;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import com.jagrosh.jdautilities.commandclient.CommandListener;
import com.jagrosh.jdautilities.commandclient.examples.AboutCommand;
import com.jagrosh.jdautilities.commandclient.examples.PingCommand;
import com.jagrosh.jdautilities.commandclient.examples.ShutdownCommand;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import pokeraidbot.commands.*;
import pokeraidbot.domain.*;
import pokeraidbot.domain.tracking.TrackingCommandListener;
import pokeraidbot.jda.AggregateCommandListener;
import pokeraidbot.jda.EmoticonMessageListener;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Arrays;

public class BotService {
    private String ownerId;
    private String token;
    private JDA botInstance;
    private CommandClient commandClient;
    private AggregateCommandListener aggregateCommandListener;
    private TrackingCommandListener trackingCommandListener;

    public BotService(LocaleService localeService, GymRepository gymRepository, RaidRepository raidRepository,
                      PokemonRepository pokemonRepository, PokemonRaidStrategyService raidInfoService,
                      ConfigRepository configRepository, String ownerId, String token) {
        this.ownerId = ownerId;
        this.token = token;
        if (!System.getProperty("file.encoding").equals("UTF-8")) {
            System.err.println("ERROR: Not using UTF-8 encoding");
            System.exit(-1);
        }

        EventWaiter waiter = new EventWaiter();
        trackingCommandListener = new TrackingCommandListener(configRepository, localeService);
        aggregateCommandListener = new AggregateCommandListener(Arrays.asList(trackingCommandListener,
                new EmoticonMessageListener(this, localeService, configRepository, raidRepository,
                        pokemonRepository, gymRepository)));

        CommandClientBuilder client = new CommandClientBuilder();
        client.setOwnerId(this.ownerId);
        client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");
        client.setPrefix("!raid ");
        client.setGame(Game.of("Type !raid usage"));
        client.addCommands(
                new AboutCommand(
                        Color.BLUE, localeService.getMessageFor(LocaleService.AT_YOUR_SERVICE, LocaleService.DEFAULT),
                        new String[]{LocaleService.featuresString_SV}, Permission.ADMINISTRATOR
                ),
                new PingCommand(),
                new HelpCommand(localeService, configRepository),
                new ShutdownCommand(),
                new NewRaidCommand(gymRepository, raidRepository, pokemonRepository, localeService,
                        configRepository),
                new RaidStatusCommand(gymRepository, raidRepository, localeService,
                        configRepository),
                new RaidListCommand(raidRepository, localeService, configRepository, pokemonRepository),
                new SignUpCommand(gymRepository, raidRepository, localeService,
                        configRepository),
                new WhereIsGymCommand(gymRepository, localeService,
                        configRepository),
                new RemoveSignUpCommand(gymRepository, raidRepository, localeService,
                        configRepository),
                new PokemonVsCommand(pokemonRepository, raidInfoService, localeService, configRepository),
                new ServerInfoCommand(configRepository, localeService),
                new DonateCommand(localeService, configRepository),
                new TrackPokemonCommand(this, configRepository, localeService, pokemonRepository)
        );

        try {
            commandClient = client.build();
            commandClient.setListener(aggregateCommandListener);
            botInstance = new JDABuilder(AccountType.BOT)
                    // set the token
                    .setToken(this.token)

                    // set the game for when the bot is loading
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setGame(Game.of("loading..."))

                    // add the listeners
                    .addEventListener(waiter)
                    .addEventListener(commandClient)

                    // start it up!
                    .buildBlocking();
        } catch (LoginException | RateLimitedException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public JDA getBot() {
        return botInstance;
    }

    public CommandClient getCommandClient() {
        return commandClient;
    }

    public TrackingCommandListener getTrackingCommandListener() {
        return trackingCommandListener;
    }
    public CommandListener getAggregateCommandListener() {
        return aggregateCommandListener;
    }
}
