package net.myshelter.minecraft.midibanks;

import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MidiBanksPlayerListener implements Listener {
	MidiBanks plugin;
	protected static final Logger log = Logger.getLogger("Minecraft");
	public static Permission perms = null;

	protected static void dolog(String msg) {
		log.info("[MidiBanks] " + msg);
	}

	public MidiBanksPlayerListener(MidiBanks plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.WALL_SIGN)
				return;
			Sign midiSign = (Sign) event.getClickedBlock().getState();
			if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
				return;
			// if (!this.plugin.varCanUse(event.getPlayer())) return;
			try {

				if (!plugin.Allowed("midibanks.can-use", event.getPlayer()))
					return;
			} catch (NoClassDefFoundError e) {
			}
			SongInstance rc = null;
			for (int i = 0; i < this.plugin.songs.size(); i++)
				if ((this.plugin.songs.get(i)).midiSign.getBlock()
						.getLocation()
						.equals(midiSign.getBlock().getLocation())) {
					rc = this.plugin.songs.get(i);
					rc.toggle();
				}
			if (rc == null)
				this.plugin.learnMusic(midiSign);
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.WALL_SIGN)
				return;
			Sign midiSign = (Sign) event.getClickedBlock().getState();
			if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
				return;
			try {
				if (!plugin.Allowed("midibanks.can-use", event.getPlayer()))
					return;
				// if(!event.getPlayer().isOp()) return;
			} catch (NoClassDefFoundError e) {
				// dolog("reverting to op permissions");
			}
			this.plugin.stopMusic(midiSign);
		}
	}
}