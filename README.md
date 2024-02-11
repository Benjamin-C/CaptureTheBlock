# CaptureTheBlock

Capture The Block (CTB) is a Minecraft minigame implemented through a Spigot plugin.

In this game, players are required to find and stand on a given block within the time limit. If everyone on your team stands on the block before the time limit, your team gets the point. Once all teams have found their blocks, all teams are given new blocks. Admins can select different lists of blocks to select from so that early game teams can be assigned easy blocks, and later teams can be assigned harder blocks. Games can be stoppped and restarted across multiple playsessions.

## Commannds
All commands start with `/ctb <subcommand> [args ...]`

If you are not a game admin, you have access to the following commands:
* `score` - Shows current team scores
* `team`
  * `join <team>` - Joins the team *team*
  * `leave` - Leave the team you are on
  * `list` - Lists all players on your team

If you are an admin, you have access to the following commands:
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