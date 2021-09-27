package prime.lootfountain.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import prime.lootfountain.LootFountain;

public class FountainToggleGravity implements CommandExecutor {


    private LootFountain plugin;
    public FountainToggleGravity(LootFountain plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {


        Player player = (Player)commandSender;

        if(! commandSender.hasPermission("lootfountain.edit")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return true;
        }

        if(args.length < 1){
            player.sendMessage(ChatColor.RED + "Fountain Gravity Toggle Unsuccessful, proper usage: " + ChatColor.YELLOW + command.getUsage());
            return true;
        }

        String fountainName = args[0];
        //If fountain doesn't exist
        if( !(plugin.getConfig().isSet("ID." + fountainName))){
            player.sendMessage(ChatColor.RED + "Fountain doesn't exist");
            return true;
        }

        //If gravity hasn't been set yet
        if( !(plugin.getConfig().isSet("ID." + fountainName + ".gravity"))){
            plugin.getConfig().set("ID." + fountainName + ".gravity", false);
            player.sendMessage(ChatColor.GREEN + "Fountain gravity set to " + ChatColor.BLUE + "false");
            return true;
        }


        Boolean gravityValue = plugin.getConfig().getBoolean("ID." + fountainName + ".gravity");

        if(gravityValue) {
            plugin.getConfig().set( "ID." + fountainName + ".gravity", false);
            player.sendMessage(ChatColor.GREEN + "Fountain gravity set to " + ChatColor.BLUE + "false");
        }
        else {
            plugin.getConfig().set( "ID." + fountainName + ".gravity", true);
            player.sendMessage(ChatColor.GREEN + "Fountain gravity set to " + ChatColor.BLUE + "true");
        }


        plugin.saveConfig();

        return true;
    }
}
