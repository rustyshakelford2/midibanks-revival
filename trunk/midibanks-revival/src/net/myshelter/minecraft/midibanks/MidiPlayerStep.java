package net.myshelter.minecraft.midibanks;

import java.util.TimerTask;

public class MidiPlayerStep extends TimerTask {
	private MidiBanks plugin;

	protected MidiPlayerStep(MidiBanks plugin) {
		/* 10 */this.plugin = plugin;
	}

	public void run() {
		/* 14 */for (int i = 0; i < this.plugin.songs.size(); i++)
			/* 15 */((SongInstance) this.plugin.songs.get(i)).nextTick();
	}
}
