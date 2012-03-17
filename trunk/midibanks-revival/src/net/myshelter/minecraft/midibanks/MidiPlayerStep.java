package net.myshelter.minecraft.midibanks;

import java.util.TimerTask;

public class MidiPlayerStep extends TimerTask {
	private MidiBanks plugin;

	protected MidiPlayerStep(MidiBanks plugin) {
		/* 10 */this.plugin = plugin;
	}

	@Override
	public void run() {
		/* 14 */for (int i = 0; i < this.plugin.songs.size(); i++)
			/* 15 */(this.plugin.songs.get(i)).nextTick();
	}
}
