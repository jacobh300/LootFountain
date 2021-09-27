package prime.lootfountain.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import prime.lootfountain.LootFountain;

public class FountainHorizontalVelocity implements CommandExecutor {


    private LootFountain plugin;
    public FountainHorizontalVelocity(LootFountain plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player)commandSender;

        if(! commandSender.hasPermission("lootfountain.edit")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return true;
        }



        if(args.length < 2){
            player.sendMessage(ChatColor.RED + "Fountain Horizontal Velocity unsuccesful, proper usage: " + ChatColor.YELLOW + command.getUsage());
            return true;
        }

        String fountainName = args[0];

        //If fountain doesn't exist
        if( !(plugin.getConfig().isSet("ID." + fountainName))){
            player.sendMessage(ChatColor.RED + "Fountain doesn't exist");
            return true;
        }


        try{
            float value = Float.parseFloat( args[1] );

            //Add value to config. (Velocity changes will take effect on the next drop event)
            plugin.getConfig().set("ID." + fountainName + ".horizontalVelocity", value);
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Fountain Horizontal Velocity changed to " + ChatColor.BLUE + args[1]);

        }catch ( Exception e ) {
            //Invalid float value.
            player.sendMessage(ChatColor.RED + "Improper velocity value, proper usage: " + ChatColor.YELLOW + command.getUsage());
            return true;
        }

        return true;
    }



}
