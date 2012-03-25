/*    */ package net.myshelter.minecraft.midibanks;
/*    */ 
/*    */ import java.util.ArrayList;
import java.util.logging.Logger;

/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
/*    */ 
/*    */ public class MidiBanksPlayerListener implements Listener
/*    */ {
/*    */   MidiBanks plugin;
/*  72 */   protected static final Logger log = Logger.getLogger("Minecraft");
/*     */   protected static void dolog(String msg)
/*     */   {
/*  74 */     log.info("[MidiBanks] " + msg);
/*     */   }

/*    */   public MidiBanksPlayerListener(MidiBanks plugin)
/*    */   {
/* 14 */     this.plugin = plugin;
/*    */   }
/*    */ 	@EventHandler
/*    */   public void onPlayerInteract(PlayerInteractEvent event) {
/* 18 */     if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
/* 19 */       if (event.getClickedBlock().getType() != Material.WALL_SIGN) return;
/* 20 */       Sign midiSign = (Sign)event.getClickedBlock().getState();
/* 21 */       if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) return;
/* 22 */       //if (!this.plugin.varCanUse(event.getPlayer())) return;
				try{
				PermissionUser user = PermissionsEx.getUser(event.getPlayer());
				if(!user.has("midibanks.can-use")) return;
				}
				catch (NoClassDefFoundError e)
				{
					if(!event.getPlayer().isOp()) return;
				}
/* 23 */       SongInstance rc = null;
/* 24 */       for (int i = 0; i < this.plugin.songs.size(); i++)
/* 25 */         if (((SongInstance)this.plugin.songs.get(i)).midiSign.getBlock().getLocation().equals(midiSign.getBlock().getLocation())) {
/* 26 */           rc = (SongInstance)this.plugin.songs.get(i);
/* 27 */           rc.toggle();
/*    */         }
/* 29 */       if (rc == null)
/* 30 */         this.plugin.learnMusic(midiSign);
/*    */     }
/* 32 */     if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
/* 33 */       if (event.getClickedBlock().getType() != Material.WALL_SIGN) return;
/* 34 */       Sign midiSign = (Sign)event.getClickedBlock().getState();
/* 35 */       if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) return;
				try
				{
				PermissionUser user = PermissionsEx.getUser(event.getPlayer());
				if(!user.has("midibanks.can-use")) return;
				}
				catch (NoClassDefFoundError e)
				{
					dolog("reverting to op permissions");
					if(!event.getPlayer().isOp()) return;
				}
/* 37 */       this.plugin.stopMusic(midiSign);
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Users\jfmh\Downloads\midibankstest2.jar
 * Qualified Name:     net.myshelter.minecraft.midibanks.MidiBanksPlayerListener
 * JD-Core Version:    0.6.0
 */