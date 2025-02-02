# CaptureTheBlock

``` Note - These docs haven't been updated since v3.4. Most things are the same, but new things have been added. ```

Capture The Block (CTB) is a Minecraft minigame implemented through a Spigot plugin.

In this game, players are required to find and stand on a given block within the time limit. If everyone on your team stands on the block before the time limit, your team gets the point. You do not have to be standing on the block when the timer runs out. Once all teams have found their blocks, all teams are given new blocks. Admins can select different lists of blocks to select from so that early game teams can be assigned easy blocks, and later teams can be assigned harder blocks. Games can be stopped and restarted across multiple playsessions.

## Running a game
To run a game, you must have the `capturetheblock.control` permission.
1. Run `\ctb set add <setname>` for any sets you wish to enable
2. Do either of the following:
   * Run `\ctb team add <teamname>` to add any teams you wish to add, then run `\ctb team join <teamname> <playername>` to put players on teams or have the players run `\ctb team join <teamname>` to put themselves on teams.
   * Run `\ctb team addall` to add everyone to their own teams.
3. Run `\ctb start` to start the game.
   * If you believe that a team's block is too hard, run `\ctb team skip <teamname>` to give them a new block.
   * You can add more sets midgame by running `\ctb set add <setname>`. The new set will take effect next time a block is randomly selected.
4. When you are ready to end the game, do one of the following: 
   * Run `\ctb end` to end the game immediately
   * Run `\ctb endat <hh:mm>` to end the game at hh:mm (hh is in 24 hour format)
   * Run `\ctb final <number>` to end after number more rounds
1. To restart the game, simply run `\ctb start`. Note that enabled sets will be reset if the plugin is reloaded.

## Commands
All commands start with `/ctb <subcommand> [args ...]`

If you are not a game admin, you have access to the following commands:
* `help` - Shows the link to the help page on GitHub
* `score` - Shows current team scores
* `team`
  * `join <team>` - Joins the team *team*
  * `leave` - Leave the team you are on
  * `list` - Lists all players on your team

If you are an admin, you have access to the following commands:
* `help` - Shows the link to the help page on GitHub
* `score` - Shows current team scores
* `start` - Starts the CTB game
* `end` - Ends the CTB game
* `endat <hh:mm>` - Ends the CTB game after the round occurring at `hh:mm` in 24h format, or use time `never` to disable.  
* `final <count>` - Ends the CTB game after `count` more rounds. Use `-1` to disable. 
* `reset` - Ends the game and resets team scores
* `reloadconifg` - Reloads the CTB config and block sets
* `blocks` - Shows the block each team is going for
* `listallblocksbutitisbig` - Lists all block sets. **WARNING:** this produces **a lot** of output
* `moveon` - Starts the next round immediately
* `team`:
  * `list` - Lists all members of all teams
  * `join <team> [player]` - Puts `player` on team `team`. `player` defaults to you 
  * `leave [player]` - Kicks `player` off of their team. `player` defaults to you
  * `add <name>` - Adds a new team `name` 
  * `remove <name>` - Removes team `name`
  * `clear <team>` - Removes all players team `name`
  * `skip [name]` - Regenerates team `name`'s block. Defaults to you. Can not be done once all team members have found that block
  * `score <team> <add|remove|set> <amount>`:
    * `add` - Adds `amount` points to team `team`
    * `remove` - Removes `amount` points from team `team`
    * `set` - Sets team `team`'s score to `amount`
  * `addall` - Adds each players not already on a team to a team named after them. Creates the teams if needed, but does not remove other members from the team.
* `set`:
  * `add <name>` - Enables set `name` for block selection
  * `remove <name>` - Disable set `name` for block selection
  * `list` - Lists all enabled sets
  * `list <name>` - Lists all blocks and subsets in `name`
  * `listall` - Lists all available sets
  * `clear` - Disables all sets
* `mark`:
  * `player <name> [found|not_found]` - Mark player `name` as having found their block, or not found their block if `not_found` is specified. If the team had already earned their point, it will be revoked.
  * `team` - Mark team `name` as having found their block, or not found their block if `not_found` is specified. If the team had already earned their point, it will be revoked.
* `toggledebugmsg` - Toggles extra debug messages

## Permissions
All permissions use the base node `capturetheblock`
* `control` - Bearers of this permission are allowed to control the game, doing things like adding and removing block lists, starting and stopping the game, and modifying team scores.
* `spectate` - Players with this permission are game spectators. This does not necessarily mean they are in gamemode spectator.

## Configuration
There are only two options in the configuration file.
* `roundtime` is the length of a round in seconds
* `warntime` is the number of seconds before the end of the round that players will receive a warning

## Block files
Block files are stored in YAML files `<server root>/plugins/CaptureTheBlock/<setname>.blocks`. Blocks and subsets are stored in an array with the key `blocks`. Include other files by including a block `include_<othersetname>`. Comments can be included preceded by a `#`. A basic example file:

    blocks:
    # Include these blocks directly
    - DIRT
    - STONE
    # Include all blocks in diamonds.blocks
    - include_diamonds

## Team files
Team information such as members and score are stored in YAML files. The files are stored in `<server root>/plugins/CaptureTheBlock/<worldname>/<teamname>.team`. These files are not intended to be modified by the user, although careful modifications should not cause issues.

## Strings file
Strings used by the game are stored in `<server root>/plugins/CaptureTheBlock/strings.txt`.
* Each line is either blank, a comment (denoted by the first character being `#`), or a string definition.
* String definitions start with the string key.
  * String keys can contain `[a-zA-Z0-9_.]`.
  * Any spaces in the key will be removed.
  * A colon (`:`) separates the key from the value.
  * A single space immediately after the colon will be removed if it is present.
* The remaining line is the value.
  * Values can contain any character
  * Some values are used as format strings, you can use [`printf` style format specifiers](https://cplusplus.com/reference/cstdio/printf/) to include dynamic information in the string
  * Colors can be added to a string with `&{NAME}` where `NAME` is the [minecraft name of the color or effect to use](https://www.digminecraft.com/lists/color_list_pc.php), or a hex encoded color string in the format `#RRGGBB`.
  * Other strings can be added with `{key}` where `key` is the key of the other string to add.

TODO:
* reloadconfig added extra timer, broke getting blocks, and reset streaks
* Top clock doesn't seem to update when player joins / leaves