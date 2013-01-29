package net.myshelter.minecraft.midibanks;

import org.bukkit.block.Block;

public abstract interface OutputPinHandler {
	public abstract void outputPin(Block paramBlock, SongEvent paramSongEvent);
}