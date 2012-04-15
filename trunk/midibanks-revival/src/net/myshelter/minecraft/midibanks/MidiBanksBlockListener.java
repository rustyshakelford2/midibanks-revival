 package net.myshelter.minecraft.midibanks;
 
 import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginBase;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.myshelter.minecraft.midibanks.MidiBanks;
 public class MidiBanksBlockListener implements Listener
 {
   MidiBanks plugin;
   private Permission perms = null;
 
   public MidiBanksBlockListener(MidiBanks plugin)
   {
     this.plugin = plugin;
   }
   	@EventHandler
   public void onBlockRedstoneChange(BlockRedstoneEvent event)
   	{
     if (event.getBlock().getType() != Material.REDSTONE_WIRE) return;
     if (!this.plugin.redstone) return;
     boolean disable = false;
     if ((event.getOldCurrent() == 0) || (event.getNewCurrent() != 0)) disable = false;
     else if ((event.getOldCurrent() != 0) || (event.getNewCurrent() == 0)) disable = true; else
       return;
     ArrayList<Block> checkSigns = new ArrayList<Block>();
     if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(1, 0, 0).getState().getData()).getFacing() == BlockFace.NORTH))
       checkSigns.add(event.getBlock().getRelative(1, 0, 0));
     if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(-1, 0, 0).getState().getData()).getFacing() == BlockFace.SOUTH))
       checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
     if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(0, 0, 1).getState().getData()).getFacing() == BlockFace.EAST))
       checkSigns.add(event.getBlock().getRelative(0, 0, 1));
     if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(0, 0, -1).getState().getData()).getFacing() == BlockFace.WEST))
       checkSigns.add(event.getBlock().getRelative(0, 0, -1));
     if (event.getBlock().getRelative(0, 1, 0).getType() == Material.WALL_SIGN) checkSigns.add(event.getBlock().getRelative(0, 1, 0));
     for (Block cb : checkSigns) {
       org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign)cb.getState();
       if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
         if (midiSign.getLine(3).contains("Y")) {
           if (!disable) {
             boolean playing = false;
             for (int i = 0; i < this.plugin.songs.size(); i++) {
               if ((this.plugin.songs.get(i)).midiSign.getBlock().getLocation().equals(midiSign.getBlock().getLocation())) {
                 playing = true;
                 break;
               }
             }
             if (playing) this.plugin.stopMusic(midiSign); else
               this.plugin.learnMusic(midiSign, true);
           }
         } else if (disable) this.plugin.stopMusic(midiSign); else
           this.plugin.learnMusic(midiSign, true);
     }
     if (disable) return;
     	 checkSigns = new ArrayList<Block>();
     if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(1, 0, 0).getState().getData()).getFacing() != BlockFace.NORTH))
       checkSigns.add(event.getBlock().getRelative(1, 0, 0));
     if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(-1, 0, 0).getState().getData()).getFacing() != BlockFace.SOUTH))
       checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
     if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(0, 0, 1).getState().getData()).getFacing() != BlockFace.EAST))
       checkSigns.add(event.getBlock().getRelative(0, 0, 1));
     if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN) && 
       (((org.bukkit.material.Sign)event.getBlock().getRelative(0, 0, -1).getState().getData()).getFacing() != BlockFace.WEST))
       checkSigns.add(event.getBlock().getRelative(0, 0, -1));
     for (Block cb : checkSigns) {
       org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign)cb.getState();
       if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
         SongInstance rc = null;
         for (int i = 0; i < this.plugin.songs.size(); i++)
           if ((this.plugin.songs.get(i)).midiSign.getBlock().getLocation().equals(midiSign.getBlock().getLocation())) {
             rc = this.plugin.songs.get(i);
             rc.toggle();
           }
       }
     }
   }
   @EventHandler
   public void onSignChange(SignChangeEvent event) 
   {
     if (!event.getLine(1).equalsIgnoreCase("[MIDI]")) return;
     //if (this.plugin.varCanCreate(event.getPlayer())) return;
			try{
		if(plugin.Allowed("midibanks.can-create",event.getPlayer())) return;
			//if(!event.getPlayer().isOp()) return;
			}
				catch (NoClassDefFoundError e)
			{
			}
     event.getBlock().setType(Material.AIR);
     event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
   }
 }