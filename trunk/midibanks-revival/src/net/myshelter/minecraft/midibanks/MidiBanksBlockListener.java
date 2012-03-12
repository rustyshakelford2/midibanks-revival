package net.myshelter.minecraft.midibanks;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class MidiBanksBlockListener implements Listener {
	MidiBanks plugin;

	public MidiBanksBlockListener(MidiBanks plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (event.getBlock().getType() != Material.REDSTONE_WIRE)
			return;
		if (!this.plugin.redstone)
			return;
		boolean disable = false;
		if ((event.getOldCurrent() == 0) || (event.getNewCurrent() != 0))
			disable = false;
		else if ((event.getOldCurrent() != 0) || (event.getNewCurrent() == 0))
			disable = true;
		else
			return;
		ArrayList<org.bukkit.block.Block> checkSigns = new ArrayList<org.bukkit.block.Block>();
		if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN)
				&& (((org.bukkit.material.Sign) event.getBlock()
						.getRelative(1, 0, 0).getState().getData()).getFacing() == BlockFace.NORTH))
			checkSigns.add(event.getBlock().getRelative(1, 0, 0));
		if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN)
				&&
				/* 34 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(-1, 0, 0).getState().getData())
						.getFacing() == BlockFace.SOUTH))
			/* 35 */checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
		/* 36 */if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN)
				&&
				/* 37 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, 1).getState().getData()).getFacing() == BlockFace.EAST))
			/* 38 */checkSigns.add(event.getBlock().getRelative(0, 0, 1));
		/* 39 */if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN)
				&&
				/* 40 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, -1).getState().getData())
						.getFacing() == BlockFace.WEST))
			/* 41 */checkSigns.add(event.getBlock().getRelative(0, 0, -1));
		/* 42 */if (event.getBlock().getRelative(0, 1, 0).getType() == Material.WALL_SIGN)
			checkSigns.add(event.getBlock().getRelative(0, 1, 0));
		/* 43 */for (Block cb : checkSigns) {
			/* 44 */org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign) cb
					.getState();
			/* 45 */if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
				/* 46 */if (midiSign.getLine(3).contains("Y")) {
					/* 47 */if (!disable) {
						/* 48 */boolean playing = false;
						/* 49 */for (int i = 0; i < this.plugin.songs.size(); i++) {
							/* 50 */if (((SongInstance) this.plugin.songs
									.get(i)).midiSign.getBlock().getLocation()
									.equals(midiSign.getBlock().getLocation())) {
								/* 51 */playing = true;
								/* 52 */break;
							}
						}
						/* 55 */if (playing)
							this.plugin.stopMusic(midiSign);
						else
							/* 56 */this.plugin.learnMusic(midiSign, true);
					}
					/* 58 */} else if (disable)
					this.plugin.stopMusic(midiSign);
				else
					/* 59 */this.plugin.learnMusic(midiSign, true);
		}
		/* 61 */if (disable)
			return;
		/* 62 */checkSigns = new ArrayList();
		/* 63 */if ((event.getBlock().getRelative(1, 0, 0).getType() == Material.WALL_SIGN)
				&&
				/* 64 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(1, 0, 0).getState().getData()).getFacing() != BlockFace.NORTH))
			/* 65 */checkSigns.add(event.getBlock().getRelative(1, 0, 0));
		/* 66 */if ((event.getBlock().getRelative(-1, 0, 0).getType() == Material.WALL_SIGN)
				&&
				/* 67 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(-1, 0, 0).getState().getData())
						.getFacing() != BlockFace.SOUTH))
			/* 68 */checkSigns.add(event.getBlock().getRelative(-1, 0, 0));
		/* 69 */if ((event.getBlock().getRelative(0, 0, 1).getType() == Material.WALL_SIGN)
				&&
				/* 70 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, 1).getState().getData()).getFacing() != BlockFace.EAST))
			/* 71 */checkSigns.add(event.getBlock().getRelative(0, 0, 1));
		/* 72 */if ((event.getBlock().getRelative(0, 0, -1).getType() == Material.WALL_SIGN)
				&&
				/* 73 */(((org.bukkit.material.Sign) event.getBlock()
						.getRelative(0, 0, -1).getState().getData())
						.getFacing() != BlockFace.WEST))
			/* 74 */checkSigns.add(event.getBlock().getRelative(0, 0, -1));
		/* 75 */for (Block cb : checkSigns) {
			/* 76 */org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign) cb
					.getState();
			/* 77 */if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) {
				/* 78 */SongInstance rc = null;
				/* 79 */for (int i = 0; i < this.plugin.songs.size(); i++)
					/* 80 */if (((SongInstance) this.plugin.songs.get(i)).midiSign
							.getBlock().getLocation()
							.equals(midiSign.getBlock().getLocation())) {
						/* 81 */rc = (SongInstance) this.plugin.songs.get(i);
						/* 82 */rc.toggle();
					}
			}
		}
	}

	public void onSignChange(SignChangeEvent event) {
		/* 88 */if (!event.getLine(1).equalsIgnoreCase("[MIDI]"))
			return;
		/* 89 */if (this.plugin.varCanCreate(event.getPlayer()))
			return;
		/* 90 */event.getBlock().setType(Material.AIR);
		/* 91 */event
				.getPlayer()
				.getWorld()
				.dropItemNaturally(event.getBlock().getLocation(),
						new ItemStack(Material.SIGN));
	}
}

