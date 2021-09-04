package prime.lootfountain;


import org.bukkit.*;
import org.bukkit.entity.*;


import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import prime.lootfountain.utils.WeightedRandomBag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Fountain {

    LootFountain plugin;
    Location fountainLocation;

    Boolean isDropping = false;
    ArrayList<ArmorStand> tags;
    String fountainID;

    int dropEventIntervalInSeconds;

    double dropEventDuration;
    double itemFrequency;

    BukkitRunnable dropEvent;
    BukkitRunnable dropperTask;
    BukkitRunnable StopTask;


    public Fountain(LootFountain plugin, String fountainID, Location location, int dropEventIntervalInSeconds, double dropEventDuration, double itemFrequency){
        this.plugin = plugin;

        this.dropEventIntervalInSeconds = dropEventIntervalInSeconds;
        this.dropEventDuration = dropEventDuration;
        this.itemFrequency = itemFrequency;

        tags = new ArrayList<>();
        fountainLocation = location;
        this.fountainID = fountainID;
    }


    public void displayFountainNameTag(String fountainID){
        if(tags != null) {
            for (ArmorStand tag : tags) {
                tag.remove();
            }
        }

        //Replace Underscores with spaces
        addNewTag(ChatColor.GOLD + fountainID.replace("_", " "));
        addNewTag(ChatColor.YELLOW + "Drop event every " + (this.dropEventIntervalInSeconds) + " seconds");
        addNewTag(ChatColor.YELLOW + "Event Duration " + this.dropEventDuration + " seconds" );
        addNewTag(ChatColor.YELLOW + "Item drop every " + this.itemFrequency + " seconds" );
    }

    public void startFountain(){
        displayFountainNameTag(fountainID);

        dropEvent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isDropping) startDropTask(fountainID, (long)itemFrequency, (long)dropEventDuration);
            }
        };

        dropEvent.runTaskTimer(plugin, 20, (20L *  this.dropEventIntervalInSeconds) + (20L * (long)dropEventDuration));
    }


    //Function for adding new name tags with armor stands.
    public void addNewTag(String tagText){
        Location location = fountainLocation.clone();
        location.setY( (location.getY() - tags.size() * 0.5) +2 );

        ArmorStand subTag =  (ArmorStand) fountainLocation.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        subTag.setGravity(false);
        subTag.setVelocity(new Vector(0,0,0));
        subTag.setCustomName(ChatColor.GOLD + tagText);
        subTag.setCustomNameVisible(true);
        subTag.setPersistent(true);
        subTag.setInvisible(true);
        subTag.setInvulnerable(true);

        subTag.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        subTag.setMarker(true);

        tags.add(subTag);

    }

    public void removeFountainNameTag(){
        if(tags.size() > 0) {
            for (Entity tag : tags) {
                tag.remove();
            }
        }
    }

    public void RemoveFountain(){
        removeFountainNameTag();
        dropEvent.cancel();
        dropperTask.cancel();
        StopTask.cancel();
    }


    //Dropping item.
    public void fountainDropItem(ItemStack item){


        float maxVelocity = 0.5f;
        float minVelocity = -0.5f;
        int despawnTimer = 6;
        boolean gravity = false;

        if (fountainLocation != null && item.getType() != Material.AIR) {

            List<String> lore = item.getItemMeta().getLore();

            String percentLine = null;
            String removeLine = null;
            for (String string : lore) {
                if (string.contains("%:")) percentLine = string;
                else if (string.contains("RIGHT-CLICK to remove from loot table")) removeLine = string;
            }

            if (percentLine != null) lore.remove(percentLine);
            if (removeLine != null ) lore.remove(removeLine);

            ItemMeta newMetaData = item.getItemMeta();
            newMetaData.setLore( lore );

            item.setItemMeta( newMetaData );

            //Despawn item after 3 seconds (1 sec = 20 ticks)
            Entity droppedItem = fountainLocation.getWorld().dropItem(fountainLocation, item);
            droppedItem.setTicksLived( 6000 - (despawnTimer * 20)) ;

            droppedItem.setGlowing(true);
            Random rand = new Random();


            float x = rand.nextFloat() * (maxVelocity - (minVelocity)) + (minVelocity);
            float z = rand.nextFloat() * (maxVelocity - (minVelocity)) + (minVelocity);
            droppedItem.setVelocity( new Vector(x, 0.25, z));
            droppedItem.setGravity(gravity);

            droppedItem.setCustomName( item.getItemMeta().getDisplayName() );

            droppedItem.setCustomNameVisible(true);
        }

    }


    public void startDropTask(String fountainID, long dropFrequencyInSeconds, long dropLengthInSeconds){
        if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Starting drop task for fountain: " + ChatColor.BLUE + fountainID);

        //Cancel any previous tasks of the fountain.
        if(dropperTask != null) dropperTask.cancel();
        if(StopTask != null) StopTask.cancel();


        //Runnable for rolling for a item every second
         dropperTask =  new BukkitRunnable() {
            @Override
            public void run() {
                isDropping = true;
                ItemStack[] lootTable = GetLootItemStackFromConfig(fountainID);
                ItemStack item = null;

                if (lootTable != null) {
                    item = GetWeightedRandomItem(lootTable);
                }

                if (item != null) {
                    fountainDropItem(item);
                }
            }
        };

        StopTask = new BukkitRunnable() {
            @Override
            public void run() {
                isDropping = false;
                if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Stopping drop task for fountain: " + ChatColor.BLUE + fountainID);
                if(!dropperTask.isCancelled()) dropperTask.cancel();
                this.cancel();
            }
        };


        dropperTask.runTaskTimer(plugin, 0,  20 * dropFrequencyInSeconds); //Triggers every second
        StopTask.runTaskTimer(plugin, 20 * dropLengthInSeconds, 0); //Triggers after 5 seconds and stops itself as well as the fountain dropping.
    }

    public ItemStack GetWeightedRandomItem(ItemStack[] items){
        WeightedRandomBag<ItemStack> itemDrops = new WeightedRandomBag<>();
        itemDrops.addEntry( new ItemStack(Material.AIR) , 50);

        for(ItemStack item : items) {
            if(item != null) itemDrops.addEntry(item, GetLootItemDropChance(item));
        }
        return itemDrops.getRandom();
    }

    //Get drop chance of item
    //Returns 0 if no lore or if no % was found.
    private float GetLootItemDropChance(ItemStack item){
        ArrayList<String> lore;
        if(!item.getItemMeta().hasLore()) return 0;
        else{
            lore = (ArrayList<String>) item.getItemMeta().getLore();
            for(String pieceOfLore : lore){
                if(pieceOfLore.contains("%:")){
                    try {
                        return Float.parseFloat(pieceOfLore.substring(pieceOfLore.indexOf(":") + 1)) ;
                    }catch(NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }

    //Returns ItemStack Array of a given FountainID.
    private ItemStack[] GetLootItemStackFromConfig(String fountainID){
        String base64Inventory = plugin.getConfig().getString("ID." + fountainID + ".loottable");

        try{
            return ItemSerializer.itemStackArrayFromBase64(base64Inventory);
        }catch (IOException e){
            System.out.println("Exception in loading inventory");
            e.printStackTrace();
        }

        return null;
    }







}
