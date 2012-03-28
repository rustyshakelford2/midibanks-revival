 package net.myshelter.minecraft.midibanks;
 import org.bukkit.Material;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 public class MidiBanksWorldListener implements Listener
 {
   MidiBanks plugin;
 
   public MidiBanksWorldListener(MidiBanks plugin)
   {
     this.plugin = plugin;
   }
 	@EventHandler
   public void onChunkLoaded(ChunkLoadEvent event) {
     if (this.plugin.disallowAutostart) return;
     for (BlockState cbs : event.getChunk().getTileEntities())
       if (cbs.getBlock().getType() == Material.WALL_SIGN) {
         Sign midiSign = (Sign)cbs;
         if ((!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) || 
           (!midiSign.getLine(3).contains("A"))) continue;
         this.plugin.learnMusic(midiSign);
       }
   }
 	@EventHandler
   public void onChunkUnLoaded(ChunkUnloadEvent event) {
     for (BlockState cbs : event.getChunk().getTileEntities())
       if (cbs.getBlock().getType() == Material.WALL_SIGN) {
         Sign midiSign = (Sign)cbs;
         if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
           this.plugin.stopMusic(midiSign);
       }
   }
 }