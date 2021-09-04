package prime.lootfountain.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import prime.lootfountain.ItemSerializer;
import prime.lootfountain.LootFountain;
import prime.lootfountain.utils.Dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddFountainItem implements CommandExecutor {


    LootFountain plugin;

    public AddFountainItem(LootFountain plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("lootfountain.additem")) {
            if (args.length < 2) {
                Player player = (Player)sender;
                player.sendMessage(ChatColor.RED + "Fountain Creation Unsuccessful! " + ChatColor.YELLOW + "Use proper usage: " + command.getUsage() );
                return true;
            }
            AddItem(args[0], args[1], sender, command);
        }else{
            sender.sendMessage(ChatColor.RED + "Insufficient Permissions");
        }
        return true;
    }

    private boolean AddItem(String fountainID, String chanceForDropString, CommandSender sender, Command command){
        float chanceForDrop = 0;
        if(sender instanceof Player) {
            Player player = (Player)sender;

            try {
                chanceForDrop = Float.parseFloat(chanceForDropString);
            }catch (NumberFormatException e){
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Fountain Item Addition Unsuccessful! ");
                player.sendMessage(command.getUsage());
                return true;
            }

            if(plugin.getConfig().getString("ID." + fountainID + ".loottable") == null) {
                player.sendMessage(ChatColor.RED + "Fountain Item Addition Unsuccessful! " + ChatColor.YELLOW + " Cannot find specified fountain");
                return true;
            }

            String base64LootTable = plugin.getConfig().getString("ID." + fountainID + ".loottable");

                try {
                    ItemStack[] itemArray = ItemSerializer.itemStackArrayFromBase64(base64LootTable);
                    Inventory inventory = Bukkit.createInventory(null, 9 * 6, Dictionary.VIEW_FOUNTAININVENTORY_TITLE + fountainID);
                    inventory.setContents(itemArray);

                    if(inventory.firstEmpty() == -1){
                        player.sendMessage(ChatColor.RED + "Fountain Item Addition Unsuccessful! " + ChatColor.YELLOW + " Fountain loottable is full");
                        return false;
                    }


                    ItemStack heldItem = player.getInventory().getItemInMainHand().clone();
                    if(heldItem.getType() == Material.AIR){
                        player.sendMessage(ChatColor.RED + "Fountain Item Addition Unsuccessful! " + ChatColor.YELLOW + " Cannot add empty item to loottable");
                        return false;

                    }

                    ItemMeta itemMeta = heldItem.getItemMeta();
                    ArrayList<String> currentLore;

                    if(itemMeta.getLore() == null) {
                        currentLore = new ArrayList<String>();
                    }else{
                        currentLore = (ArrayList<String>) itemMeta.getLore();
                    }

                    currentLore.add("%:" + chanceForDrop);
                    currentLore.add(ChatColor.RED + "RIGHT-CLICK to remove from loot table");

                    itemMeta.setLore(currentLore);
                    heldItem.setItemMeta(itemMeta);
                    inventory.addItem(heldItem);

                    String updatedBase64Inventory = ItemSerializer.toBase64(inventory);
                    plugin.getConfig().set("ID." + fountainID + ".loottable", updatedBase64Inventory);
                    plugin.saveConfig();

                    player.sendMessage(ChatColor.YELLOW + "Fountain Item Addition successful! Added " + ChatColor.LIGHT_PURPLE
                                        + heldItem.getType().name() + ChatColor.YELLOW + " to "
                                        + ChatColor.BLUE + fountainID +ChatColor.YELLOW
                                        + " with a chance of " + ChatColor.LIGHT_PURPLE
                                        + chanceForDropString + "%"
                                      );
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }


        }

        return false;
    }


}
