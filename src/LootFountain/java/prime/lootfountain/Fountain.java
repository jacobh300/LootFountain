package prime.lootfountain;


import org.bukkit.*;
import org.bukkit.entity.*;


import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.FireworkMeta;
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

    //Default Fountain Values
    float horizontalVelocity = 0.5f;
    float verticalVelocity = 1.25f;
    int despawnTimer = 6;
    boolean gravity = true;



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
        addNewTag(ChatColor.AQUA + "" + ChatColor.BOLD + fountainID.replace("_", " "));
        addNewTag(ChatColor.GRAY + "Drop event every " + ChatColor.AQUA + (this.dropEventIntervalInSeconds) + ChatColor.GRAY + " seconds");
        addNewTag(ChatColor.GRAY + "Event Duration " + ChatColor.AQUA + this.dropEventDuration + ChatColor.GRAY + " seconds" );
        addNewTag(ChatColor.GRAY + "Item drop every " + ChatColor.AQUA + this.itemFrequency + ChatColor.GRAY + " seconds" );
    }

    public void startFountain(){
        displayFountainNameTag(fountainID);

        dropEvent = new BukkitRunnable() {
            @Override
            public void run() {

                try{
                   verticalVelocity = Float.parseFloat(  plugin.getConfig().getString("ID." + fountainID + ".verticalVelocity") );
                }catch (Exception e){
                    //Ignore if no vertical Velocity value in config.
                }

                try{
                    horizontalVelocity = Float.parseFloat(  plugin.getConfig().getString("ID." + fountainID + ".horizontalVelocity") );
                }catch (Exception e){
                    //Ignore if no vertical Velocity value in config.
                }

                try{
                    gravity = plugin.getConfig().getBoolean("ID." + fountainID + ".gravity"  );
                }catch (Exception e){
                    //Ignore if no vertical Velocity value in config.
                }





                if(!isDropping) startDropTask(fountainID, (long)itemFrequency, (long)dropEventDuration, horizontalVelocity, verticalVelocity, gravity);
            }
        };

        dropEvent.runTaskTimer(plugin, 20, (20L *  this.dropEventIntervalInSeconds) + (20L * (long)dropEventDuration));
    }


    //For reloading fountains.
    public void stopFountain(){
        if(dropEvent != null) dropEvent.cancel();
        if(dropperTask != null) dropperTask.cancel();
        if(StopTask != null) StopTask.cancel();
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

            Item drop = (Item)droppedItem;
            drop.setPickupDelay(20 * 1);
            drop.setGlowing(true);

            Random rand = new Random();



            float x = rand.nextFloat() * (horizontalVelocity - (horizontalVelocity * -1)) + (horizontalVelocity * -1);
            float z = rand.nextFloat() * (horizontalVelocity - (horizontalVelocity * -1)) + (horizontalVelocity * -1);
            droppedItem.setVelocity( new Vector(x, verticalVelocity, z));
            droppedItem.setGravity(gravity);

            droppedItem.setCustomName( item.getItemMeta().getDisplayName() );

            droppedItem.setCustomNameVisible(true);
        }

    }


    public void startDropTask(String fountainID, long dropFrequencyInSeconds, long dropLengthInSeconds, float horizontalVelocity, float verticalVelocity, boolean gravity){
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

                if (lootTable != null) item = GetWeightedRandomItem(lootTable);


                if (item != null && item.getType() != Material.AIR) {

                    float itemDropChance = GetLootItemDropChance(item);
                    if( itemDropChance < 10){
                        playSpecialItemEffect();
                    }

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


    public void playSpecialItemEffect(){

        Location loc = fountainLocation.clone();
        Firework firework = fountainLocation.getWorld().spawn(  loc.add(0,2,0),Firework.class);
        FireworkMeta data = firework.getFireworkMeta();
        FireworkEffect effect =  FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.BURST).trail(false).build();

        data.addEffect(effect);
        data.setPower(1);


        firework.setFireworkMeta(data);
        firework.detonate();

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
