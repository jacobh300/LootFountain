package prime.lootfountain.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import prime.lootfountain.LootFountain;
import prime.lootfountain.utils.Dictionary;

import java.util.ArrayList;

public class ViewFountains  implements CommandExecutor {



    LootFountain lootFountain;

    public ViewFountains(LootFountain lootFountain){
        this.lootFountain = lootFountain;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("lootfountain.view")) viewFountainsCommand(sender,command, args);
        else sender.sendMessage(ChatColor.RED + "Insufficient Permissions");
        return  true;
    }


    //Displays current fountains in a chest UI
    public void viewFountainsCommand(CommandSender sender, Command command, String[] args){
        if(sender instanceof  Player){
            Player player = (Player) sender;

            Inventory inventory = Bukkit.createInventory(null, 9*6, Dictionary.VIEW_FOUNTAINLIST_TITLE);
            ConfigurationSection configurationSection = lootFountain.getConfig().getConfigurationSection("ID");

            for(String fountainName : configurationSection.getKeys(false)){

                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.GREEN + fountainName);


                ArrayList<String> stringList = new ArrayList<String>();
                Location location;
                location = lootFountain.getConfig().getLocation("ID." + fountainName + ".location");
                if(location == null) return;

                stringList.add(ChatColor.GRAY + "X/Y/Z: " + location.getBlockX() + ", " + location.getBlockY() +  ", " + location.getBlockZ());

                String frequency = lootFountain.getConfig().getString("ID." + fountainName + ".dropEventFrequency");
                double dropEventDuration = lootFountain.getConfig().getDouble("ID." + fountainName + ".dropEventDuration");
                double itemFrequency = lootFountain.getConfig().getDouble("ID." + fountainName + ".itemFrequency");

                stringList.add(ChatColor.YELLOW + "Drop event every " + frequency + " seconds");
                stringList.add(ChatColor.YELLOW + "Event Duration " + dropEventDuration + " seconds" );
                stringList.add(ChatColor.YELLOW + "Item drop every " + itemFrequency + " seconds" );
                stringList.add("");
                stringList.add(ChatColor.GREEN + "LEFT-CLICK to view loot table");
                stringList.add(ChatColor.RED + "RIGHT-CLICK to delete fountain");

                itemMeta.setLore(stringList);
                item.setItemMeta(itemMeta);

                inventory.addItem(item);
                player.openInventory(inventory);
            }

        }
    }



}
