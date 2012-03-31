 package net.myshelter.minecraft.midibanks;
import java.util.logging.Logger;

 import org.bukkit.Material;
 import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class MidiBanksPlayerListener implements Listener
 {
   MidiBanks plugin;
   protected static final Logger log = Logger.getLogger("Minecraft");
   protected static void dolog(String msg)
   {
     log.info("[MidiBanks] " + msg);
   }

   public MidiBanksPlayerListener(MidiBanks plugin)
   {
     this.plugin = plugin;
   }
 	@EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
     if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
       if (event.getClickedBlock().getType() != Material.WALL_SIGN) return;
       Sign midiSign = (Sign)event.getClickedBlock().getState();
       if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) return;
       //if (!this.plugin.varCanUse(event.getPlayer())) return;
				try{
				PermissionUser user = PermissionsEx.getUser(event.getPlayer());
				if(!user.has("midibanks.can-use")|!event.getPlayer().isOp()|!event.getPlayer().hasPermission("midibanks.can-use")) return;
				}
				catch (NoClassDefFoundError e)
				{
				}
       SongInstance rc = null;
       for (int i = 0; i < this.plugin.songs.size(); i++)
         if ((this.plugin.songs.get(i)).midiSign.getBlock().getLocation().equals(midiSign.getBlock().getLocation())) {
           rc = this.plugin.songs.get(i);
           rc.toggle();
         }
       if (rc == null)
         this.plugin.learnMusic(midiSign);
     }
     if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
       if (event.getClickedBlock().getType() != Material.WALL_SIGN) return;
       Sign midiSign = (Sign)event.getClickedBlock().getState();
       if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) return;
				try
				{
				PermissionUser user = PermissionsEx.getUser(event.getPlayer());
				if(!user.has("midibanks.can-use")|!event.getPlayer().isOp()|!event.getPlayer().hasPermission("midibanks.can-use")) return;
				}
				catch (NoClassDefFoundError e)
				{
					//dolog("reverting to op permissions");
				}
       this.plugin.stopMusic(midiSign);
     }
   }
 }