package net.myshelter.minecraft.midibanks;

import java.util.ArrayList;

import javax.sound.midi.Track;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;

public class SongInstance {
	private MidiBanks plugin;
	public org.bukkit.block.Sign midiSign;
	private Track music;
	protected int track = 0;
	protected double resolution = 1.0D;
	protected double tempoCoef = 1.0D;
	protected boolean chanCollapse = false;
	protected boolean shift = false;
	protected boolean loop = false;
	protected boolean display = false;
	protected boolean remRepeated = false;
	protected String chans = "l";
	protected int window = -1;
	protected boolean repOctave = false;
	protected Integer instrument = Integer.valueOf(-1);

	private double tick = 0.0D;
	private int event = 0;
	private int count = 0;
	private int[] latestNote;
	private boolean paused = false;
	int sx = 0;
	int sz = 0;
	Block firstBlock;

	protected SongInstance(MidiBanks plugin, org.bukkit.block.Sign midiSign,
			Track music, String ochans) {
		this.plugin = plugin;
		this.midiSign = midiSign;
		this.music = music;

		BlockFace direction = ((org.bukkit.material.Sign) midiSign.getData())
				.getFacing();
		if (direction == BlockFace.NORTH)
			this.sx = 1;
		if (direction == BlockFace.SOUTH)
			this.sx = -1;
		if (direction == BlockFace.EAST)
			this.sz = 1;
		if (direction == BlockFace.WEST)
			this.sz = -1;
		this.firstBlock = midiSign.getBlock().getRelative(this.sx * 2, 0,
				this.sz * 2);

		this.latestNote = new int[16];
		for (int i = 0; i < this.latestNote.length; i++)
			this.latestNote[i] = -1;

		if (ochans != null) {
			this.chans = "";
			for (int i = 0; i < ochans.length(); i++) {
				if (ochans.charAt(i) == 'l')
					this.chans += "0123456789abcdef";
				else
					this.chans += ochans.charAt(i);
				if ((ochans.charAt(i) == 's') || (ochans.charAt(i) == 'm')) {
					plugin.pinHandler.outputPin(
							midiSign.getBlock().getRelative(
									this.sx * (this.chans.length() + 1), 0,
									this.sz * (this.chans.length() + 1)),
							SongEvent.START);
				}
			}
			this.chans = this.chans.substring(0, 32 < this.chans.length() ? 32
					: this.chans.length());
		}
	}

	protected void nextTick() {
		if (this.paused)
			return;
		this.tick += this.resolution * this.tempoCoef;
		if (this.tick >= this.music.ticks()) {
			over();
			return;
		}
		int lcount = 0;
		int keyNote = 0;
		if (this.window > -1)
			keyNote = -6 + 12 * this.window;

		for (; (this.event < this.music.size() - 1)
				&& (this.music.get(this.event).getTick() <= this.tick); this.event += 1) {
			if (this.music.get(this.event).getMessage().getStatus() >> 4 != 9)
				continue;
			ArrayList<Block> relBlocks = new ArrayList();
			int channel = 0;
			if (this.chanCollapse) {
				relBlocks.add(this.firstBlock);
			} else {
				channel = this.music.get(this.event).getMessage().getStatus() & 0xF;

				for (int i = 0; i < this.chans.length(); i++) {
					String si = String.valueOf(this.chans.charAt(i));
					if ((si.equals(Integer.toHexString(channel)))
							|| (si.equals("o")))
						relBlocks.add(this.midiSign.getBlock().getRelative(
								this.sx * (i + 2), 0, this.sz * (i + 2)));
					if (si.equals("n"))
						this.plugin.pinHandler.outputPin(
								this.midiSign.getBlock()
										.getRelative(this.sx * (i + 2), 0,
												this.sz * (i + 2)),
								SongEvent.NOTE);
				}
			}
			if (relBlocks.size() <= 0)
				continue;
			this.count += 1;
			lcount++;
			if (this.display) {
				this.midiSign.setLine(0, String.valueOf(this.count));
				this.midiSign.update();
			}
			int midiNote = this.music.get(this.event).getMessage().getMessage()[1];

			if (this.remRepeated) {
				if (this.latestNote[channel] != midiNote)
					this.latestNote[channel] = midiNote;
			} else {
				Integer iaux;
				Integer iaux1 = 0;
				if (this.window < 0) {
					iaux1 = Integer
							.valueOf((midiNote + (this.shift ? 6 : -6)) % 24);
				} else {
					Integer iaux2;
					if ((midiNote >= keyNote) && (midiNote < keyNote + 24)) {
						iaux2 = Integer.valueOf(midiNote - keyNote);
					} else {
						Integer iaux3;
						if ((this.repOctave) && (midiNote < keyNote)) {
							iaux3 = Integer.valueOf((midiNote - 6) % 12);
						} else {
							Integer iaux4;
							if ((this.repOctave) && (midiNote >= keyNote + 24))
								iaux4 = Integer
										.valueOf(12 + (midiNote + 6) % 12);
							else
								iaux4 = Integer.valueOf(-1);
						}
					}
				}
				if (iaux1.intValue() < 0)
					continue;
				for (Block relBlock : relBlocks) {
					if ((relBlock == null)
							|| (relBlock.getType() != Material.NOTE_BLOCK))
						continue;
					try {
						NoteBlock nb = (NoteBlock) relBlock.getState();
						if (this.instrument.intValue() > -1) {
							nb.play(this.instrument.byteValue(),
									iaux1.byteValue());
						} else {
							nb.setRawNote(iaux1.byteValue());
							nb.update();
							nb.play();
						}
					} catch (NullPointerException localNullPointerException) {
					}
				}
			}
		}
	}

	private void over() {
		boolean loopnow = false;
		boolean theend = true;
		if ((this.loop) && (this.count > 0))
			loopnow = true;
		for (SongInstance si : this.plugin.songs)
			if ((si != this)
					&& (si.midiSign.getBlock().getLocation() == this.midiSign
							.getBlock().getLocation())) {
				loopnow = false;
				theend = false;
			}
		if (loopnow)
			this.plugin.learnMusic(this.midiSign);
		if (theend) {
			for (int i = 0; i < this.chans.length(); i++)
				if ((this.chans.charAt(i) == 'z')
						|| (this.chans.charAt(i) == 'm'))
					this.plugin.pinHandler.outputPin(
							this.midiSign.getBlock().getRelative(
									this.sx * (i + 2), 0, this.sz * (i + 2)),
							SongEvent.END);
		}
		this.plugin.songs.remove(this);
	}

	public boolean toggle() {
		if (this.paused) {
			this.midiSign.setLine(0, "PLAYING");
			this.midiSign.update();
			return this.paused = false;
		}
		this.midiSign.setLine(0, "PAUSED");
		this.midiSign.update();
		return this.paused = true;
	}
}