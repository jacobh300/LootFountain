package prime.lootfountain.listeners;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import prime.lootfountain.Fountain;
import prime.lootfountain.ItemSerializer;
import prime.lootfountain.LootFountain;
import prime.lootfountain.utils.Dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class InventoryClickListener implements Listener {
    private LootFountain plugin;

    public InventoryClickListener(LootFountain plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    //Click Inventory Handler
    @EventHandler
    public void onClick(InventoryClickEvent event){
        String inventoryTitle = event.getView().getTitle();

        if(inventoryTitle.equalsIgnoreCase(Dictionary.VIEW_FOUNTAINLIST_TITLE)){
            ViewFountainHandler(event);
        }else if(inventoryTitle.contains(Dictionary.VIEW_FOUNTAININVENTORY_TITLE)){
            FountainLootTableHandler(event);
        }

    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(event.getPlayer().hasPermission("fountain.delete")) {
            String inventoryTitle = event.getView().getTitle();
            if (inventoryTitle.contains(Dictionary.VIEW_FOUNTAININVENTORY_TITLE)) {
                String updatedBase64Inventory = ItemSerializer.toBase64(event.getInventory());
                String fountainID = inventoryTitle.substring(inventoryTitle.indexOf(":") + 1);
                plugin.getConfig().set("ID." + fountainID + ".loottable", updatedBase64Inventory);
                plugin.saveConfig();
            }
        }
    }


    private void FountainLootTableHandler(InventoryClickEvent event){
        if(event.isRightClick() && event.getWhoClicked().hasPermission("fountain.deleteitem")) {
            ItemStack item = event.getInventory().getItem(event.getSlot());
            if (item != null) {
                event.getInventory().clear(event.getSlot());
                event.getWhoClicked().sendMessage(ChatColor.YELLOW + "Fountain Item Removed: " + ChatColor.LIGHT_PURPLE
                        + item.getType().name() );
            }
        }
        event.setCancelled(true);
    }


    //Handling On Click Of Fountain
    private void ViewFountainHandler(InventoryClickEvent event){

        //Left Click Event
        if(event.isLeftClick() && event.getWhoClicked().hasPermission("fountain.view")){
            ItemStack item = event.getInventory().getItem(event.getSlot());

            if(item != null) {
                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                Inventory inventory = Bukkit.createInventory(null, 9 * 6, Dictionary.VIEW_FOUNTAININVENTORY_TITLE + name);
                event.getWhoClicked().openInventory(inventory);

                String base64LootTable =  plugin.getConfig().getString("ID." + name +".loottable");

                try {
                    inventory.setContents(  ItemSerializer.itemStackArrayFromBase64(base64LootTable) );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        //Right Click Event
        }else if(event.isRightClick() && event.getWhoClicked().hasPermission("fountain.delete")){
                ItemStack item = event.getInventory().getItem(event.getSlot());
                if (item != null) {
                    if(item.getItemMeta().hasDisplayName()) {
                        String itemName = item.getItemMeta().getDisplayName();
                        String name = ChatColor.stripColor(itemName);
                        event.getInventory().clear(event.getSlot());

                        plugin.fountainManager.removeFountain(name);
                    }
                }
        }

        event.setCancelled(true);
    }


}
