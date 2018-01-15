Done:

See [Changelog](CHANGELOG.md).

Needs testing/keeping track of:

-

Being developed:

* Ensure the overview can handle discord API timeouts and weirdness
* Area categorization for gyms

Discussion:

-

Experiment with:

* Geofence possibility to define areas
* Be able to subscribe via !raid track to any raid in a certain area
* Read Pokemon go screenshot to create raid automatically (Swepocks)
* Can we create a map with a certain pokestop as centre and plot all raids in that area around it?
* Ability to create channels on the fly to put certain raids in

Fix issue:
* SocketTimeoutException during Discord issues - group seems to be cleaned up when it occurs, instead just let it skip the current edit
* "Raid group will now be cleaned up" - should print something more identifiable
* Can signup at raid group end time via raid group emote pressing. Seems bad.
* Better error message if bot doesn't have correct rights on server, and give info to admin on what rights
to set

Fix, misc:

* Uniqueconstraints to prevent in database that the same user can signup more than once for a certain raid.

Do, features:

* Save overview channel in server config, so we won't have to check every channel during start up 
(takes time if server has many channels)
* PokemonGoConfig entity for keeping track of current legendary bosses etc - things that may change on an
instant, should be able to reconfigure these in runtime via admin tool.
* !raid install needs to be made easier
* Only use a single Emoticon listener for signups to reduce memory and complexity?
* Should only be able to create raids for raidbosses (atleast via fuzzy search)
* !raid track handling should be smarter for people who are on several servers
* !raid track for gym, if a user wants to see when there is a raid at their "home gym"
so they can get their daily raid done quickly (maybe subscribe to an "area" instead?)
* Separate so different servers can't share raids and reporting (in case we get malicious users)
* Set Bot to private so not anyone can add it (atleast production/live bot)
* Switch to Java 9, try to reduce runtime (https://devcenter.heroku.com/changelog-items/1263)

-- next

* Admin to send messages to when something happened they need to know about
* Runtime signup statistics for each server, reset on server restart or start of next day, whichever comes first.
* Move gyms to the database, use CSV files just to initialize and keep in synch?
* Make it possible to change gyms in database via admin command (Zhorhn only)
* REST API with open operations (read-only)
* Web UI for administration, using the REST API
* Web page with raid list for a certain region (using REST API)
* Be able to auto create groups via botintegration for several tiers and to different channels
* Raid group messages could be sent to a specific channel, if so configured. Separate EX raid channel.
* Choose your own Nickname, if you don't want the discord username - !raid nick {nickname}. Display said nick in group
signups, raid status etc. Store both user name or user id on signups as well as the nickname. Use nick for presentation and
user name/user id for checking.
* !raid change remove {gym} -> !raid remove raid {gym}
* !r nest command for reporting rare pokemon nests near gym
* Admins/moderators should only be able to moderate stuff on their own server - means that change remove etc need
to check server, and that raid entity, group etc also need to include server
* Performance improvements. Reduce number of queries, optimize, add caching.
* !raid change group (time) (gym) - if more groups possible, reply with list of id:s and info to decide what group
* !raid change groupbyid (id) (time)
* !raid remove group (time) (gym) - if more groups possible, reply with list of id:s and info to decide what group
* !raid remove groupbyid (id)
* Handle changing raid group when user has many raid groups for a raid
* Handle changing raid group as mod when there are many raid groups for a raid
* Max number of chars for a gym name in !raid list and !raid overview?
* Umeå request: If people do similar map commands after one other, skip following commands
* Create an in-bot FAQ, f.ex. "Why does my group not update? What to do?"
* In !raid list, if the server has an overview, give a hint that there is an overview the user can use instead.
* Can we listen for +(number) (time) (gym) and fix possible user weirdness like forgetting 
time (equalling no time to "now", if raid is active) to signup using that?
* Snooze button for raid group
* REST API with passtoken and acqquiring tickets

Maybe, features:

* Automatically create eggs via bot integration also for tier 4, and hatch when boss spawns
* Emote to mark group as having finished (removes all signups) - only group creator?
* Config for permission setting of various groups of commands:
    * Raid functions like create, list can be set to be admin only
    * Raid group creation can be set to admin only
* Config for time of automatic message deletion
    * Number of seconds, or -1 = never delete
* Config for turning on and off some feature groups (map, info, raid)
* Enable bot owner to trigger import of a regional dataset "on the fly" and save gym region data in database 
(needs non-free Heroku version)
* !raid me - Answer in PM with my current signups over all raids in the region and my current trackings 
* Admin commands where bot owner can purge configuration, get statistics, help a user (send man help via DM?)
* !raid install needs to be "protected" so admins can only affect the configuration of their own server
(this may mean the install command has to be run in the context of a server chat so we can fetch server name)
* Timezone handling via config, used in all commands related to time
* Credit Iconninja for icons
* Check permissions during config - need to have MANAGE_MESSAGE and access to send messages, among other things
* Embeddedlänkar vid sökning som möjliggör att köra kommandot igen för bara det resultatet via klick
* Config should have a note if a server is TEST or PROD and a env property which type of server,
so we can't get test config on a prod server running by mistake
* Add counters/counter moves to all pokemon, based on "good dps pokemons" for each type?
* Checking error codes?
...


Geofence coordinates for Uppsala:
(Using https://codepen.io/jennerpalacios/full/mWWVeJ)

59.915873050901915,17.60284423828125
59.84920437021957,17.522850036621094
59.78896721916371,17.589111328125
59.77704497190381,17.66155242919922
59.85110119255739,17.806777954101562
59.90606202929295,17.716140747070312

Use this Java API to experiment with map areas for 
raid subscription
https://github.com/davidmoten/rtree