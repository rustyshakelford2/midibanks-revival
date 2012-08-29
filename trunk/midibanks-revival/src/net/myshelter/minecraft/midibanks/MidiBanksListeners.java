package net.myshelter.minecraft.midibanks;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class MidiBanksListeners implements Listener {
	MidiBanks plugin;
	protected static final Logger log = Logger.getLogger("Minecraft");
	public static Permission perms = null;

	public MidiBanksListeners(MidiBanks plugin) {
		this.plugin = plugin;
	}

	protected static void dolog(String msg) {
		log.info("[MidiBanks] " + msg);
	}

	//Sign A parameter
	@EventHandler
	public void onChunkLoaded(ChunkLoadEvent event) {
		if (plugin.disallowAutostart) {
			return;
		}
		for (BlockState cbs : event.getChunk().getTileEntities()) {
			if (cbs.getBlock().getType() == Material.WALL_SIGN) {
				Sign midiSign = (Sign) cbs;
				if ((!midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
						|| (!midiSign.getLine(3).contains("A"))) {
					continue;
				}
				plugin.learnMusic(midiSign);
			}
		}
	}

	//Sign A parameter
	@EventHandler
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (BlockState cbs : event.getChunk().getTileEntities()) {
			if (cbs.getBlock().getType() == Material.WALL_SIGN) {
				Sign midiSign = (Sign) cbs;
				if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
					plugin.stopMusic(midiSign);
				}
			}
		}
	}

	//Sign interactions
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.WALL_SIGN) {
				return;
			}
			Sign midiSign = (Sign) event.getClickedBlock().getState();
			if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
				return;
			}
			try {

				if (!plugin.Allowed("midibanks.can-use", event.getPlayer())) {
					return;
				}
			} catch (NoClassDefFoundError e) {
			}
			SongInstance rc = null;
			for (int i = 0; i < plugin.songs.size(); i++) {
				if ((plugin.songs.get(i)).midiSign.getBlock().getLocation()
						.equals(midiSign.getBlock().getLocation())) {
					rc = plugin.songs.get(i);
					rc.toggle();
				}
			}
			if (rc == null) {
				plugin.learnMusic(midiSign);
			}
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.WALL_SIGN) {
				return;
			}
			Sign midiSign = (Sign) event.getClickedBlock().getState();
			if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
				return;
			}
			try {
				if (!plugin.Allowed("midibanks.can-use", event.getPlayer())) {
					return;
				}
			} catch (NoClassDefFoundError e) {
			}
			plugin.stopMusic(midiSign);
		}

	}

	// Redstone shit
	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (event.getBlock().getType() != Material.REDSTONE_WIRE) {
			return;
		}
		if (!plugin.redstone) {
			return;
		}
		boolean disableredstone = false;
		if ((event.getOldCurrent() == 0) || (event.getNewCurrent() != 0)) {
			disableredstone = false;
		} else if ((event.getOldCurrent() != 0) || (event.getNewCurrent() == 0)) {
			disableredstone = true;
		} else {
			return;
		}
		ArrayList<Block> checkSigns = new ArrayList<Block>();
		if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(1, 0, 0).getState().getData()).getFacing() == BlockFace.NORTH)) {
			checkSigns.add(event.getBlock().getRelative(1, 0, 0));
		}
		if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(-1, 0, 0).getState().getData())
						.getFacing() == BlockFace.SOUTH)) {
			checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
		}
		if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, 1).getState().getData()).getFacing() == BlockFace.EAST)) {
			checkSigns.add(event.getBlock().getRelative(0, 0, 1));
		}
		if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, -1).getState().getData())
						.getFacing() == BlockFace.WEST)) {
			checkSigns.add(event.getBlock().getRelative(0, 0, -1));
		}
		if (event.getBlock().getRelative(0, 1, 0).getType() == Material.WALL_SIGN) {
			checkSigns.add(event.getBlock().getRelative(0, 1, 0));
		}
		for (Block cb : checkSigns) {
			org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign) cb
					.getState();
			if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
				if (midiSign.getLine(3).contains("Y")) {
					if (!disableredstone) {
						boolean playing = false;
						for (int i = 0; i < plugin.songs.size(); i++) {
							if ((plugin.songs.get(i)).midiSign.getBlock()
									.getLocation()
									.equals(midiSign.getBlock().getLocation())) {
								playing = true;
								break;
							}
						}
						if (playing) {
							plugin.stopMusic(midiSign);
						} else {
							plugin.learnMusic(midiSign, true);
						}
					}
				} else if (disableredstone) {
					plugin.stopMusic(midiSign);
				} else {
					plugin.learnMusic(midiSign, true);
				}
			}
		}
		if (disableredstone) {
			return;
		}
		checkSigns = new ArrayList<Block>();
		if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(1, 0, 0).getState().getData()).getFacing() != BlockFace.NORTH)) {
			checkSigns.add(event.getBlock().getRelative(1, 0, 0));
		}
		if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(-1, 0, 0).getState().getData())
						.getFacing() != BlockFace.SOUTH)) {
			checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
		}
		if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, 1).getState().getData()).getFacing() != BlockFace.EAST)) {
			checkSigns.add(event.getBlock().getRelative(0, 0, 1));
		}
		if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, -1).getState().getData())
						.getFacing() != BlockFace.WEST)) {
			checkSigns.add(event.getBlock().getRelative(0, 0, -1));
		}
		for (Block cb : checkSigns) {
			org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign) cb
					.getState();
			if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
				SongInstance rc = null;
				for (int i = 0; i < plugin.songs.size(); i++) {
					if ((plugin.songs.get(i)).midiSign.getBlock().getLocation()
							.equals(midiSign.getBlock().getLocation())) {
						rc = plugin.songs.get(i);
						rc.toggle();
					}
				}
			}
		}
	}

	// Sign thing
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!event.getLine(1).equalsIgnoreCase("[MIDI]")) {
			return;
		}
		try {
			if (plugin.Allowed("midibanks.can-create", event.getPlayer())) {
				return;
			}
		} catch (NoClassDefFoundError e) {
		}
		event.getBlock().setType(Material.AIR);
		event.getPlayer()
				.getWorld()
				.dropItemNaturally(event.getBlock().getLocation(),
						new ItemStack(Material.SIGN));
	}
}
