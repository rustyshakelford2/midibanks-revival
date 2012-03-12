package net.myshelter.minecraft.midibanks;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class MidiBanksWorldListener implements Listener {
	MidiBanks plugin;

	public MidiBanksWorldListener(MidiBanks plugin) {
		/* 15 */this.plugin = plugin;
	}
	@EventHandler
	public void onChunkLoaded(ChunkLoadEvent event) {
		/* 19 */if (this.plugin.disallowAutostart)
			return;
		/* 20 */for (BlockState cbs : event.getChunk().getTileEntities())
			/* 21 */if (cbs.getBlock().getType() == Material.WALL_SIGN) {
				/* 22 */Sign midiSign = (Sign) cbs;
				/* 23 */if ((!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) ||
				/* 24 */(!midiSign.getLine(3).contains("A")))
					continue;
				/* 25 */this.plugin.learnMusic(midiSign);
			}
	}
	@EventHandler
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		/* 30 */for (BlockState cbs : event.getChunk().getTileEntities())
			/* 31 */if (cbs.getBlock().getType() == Material.WALL_SIGN) {
				/* 32 */Sign midiSign = (Sign) cbs;
				/* 33 */if (midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
					/* 34 */this.plugin.stopMusic(midiSign);
			}
	}
}
