package prime.lootfountain.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import prime.lootfountain.Fountain;
import prime.lootfountain.ItemSerializer;
import prime.lootfountain.LootFountain;
import prime.lootfountain.utils.Dictionary;

public class CreateFountain implements CommandExecutor {

    private LootFountain plugin;

    public CreateFountain(LootFountain plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("lootfountain.create")) CreateFountainFunction(sender, command, args);
        else sender.sendMessage(ChatColor.RED + "Insufficient Permissions");

        return true;
    }

    //Returns true if fountain creation is succesful.
    Boolean CreateFountainFunction(CommandSender sender,Command command ,String[] args){
        if(sender instanceof  Player){
            Player player = (Player)sender;

                //Checking for args
                if(args.length != 4){
                    player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "Use proper usage: " + command.getUsage() );
                    return false;
                }

                String fountainName = args[0];
                String frequency = args[1];
                String dropEventDurationString = args[2];
                String itemFrequencyString = args[3];



                //Check if fountain already created
                if(plugin.getConfig().isSet("ID." + fountainName)){
                    player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "ID already used." );
                    return false;
                }

                //Parse for floats in args.
                float dropEventFrequency = 0;
                try {
                    dropEventFrequency = Float.parseFloat(frequency);
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "Please use valid number for event frequency" );
                    return false;
                }

                //Parse for double in args.
                double dropEventDuration = 10;
                try {
                    dropEventDuration = Double.parseDouble(dropEventDurationString);
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "Please use valid number for event duration" );
                    return false;
                }

                //Parse for double in args.
                double itemFrequency = 10;
                try {
                    itemFrequency = Double.parseDouble(itemFrequencyString);
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "Please use valid number for item frequency" );
                    return false;
                }


            //Creating loot inventory
            Inventory inventory = Bukkit.createInventory(null, 9*6, Dictionary.INVENTORY_CREATOR_TITLE + " | " + ChatColor.GOLD + fountainName);
            String content = ItemSerializer.toBase64((inventory));


            //Adding fountain
            plugin.fountainManager.addFountain(fountainName, content, player.getLocation(),dropEventFrequency, dropEventDuration, itemFrequency);

            //Debug
            sender.sendMessage(ChatColor.YELLOW  + "Created fountain: " + ChatColor.BLUE + fountainName);
            if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW  + "Created fountain: " + ChatColor.BLUE + fountainName);
            return true;

        }else{
            return false;
        }
    }

}
