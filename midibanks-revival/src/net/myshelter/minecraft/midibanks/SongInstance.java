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
	int SideX = 0;
	int SideZ = 0;
	Block firstBlock;

	protected SongInstance(MidiBanks plugin, org.bukkit.block.Sign midiSign,
			Track music, String ochans) {
		this.plugin = plugin;
		this.midiSign = midiSign;
		this.music = music;

		BlockFace direction = ((org.bukkit.material.Sign) midiSign.getData())
				.getFacing();
		if (direction == BlockFace.NORTH) {
			SideX = 1;
		}
		if (direction == BlockFace.SOUTH) {
			SideX = -1;
		}
		if (direction == BlockFace.EAST) {
			SideZ = 1;
		}
		if (direction == BlockFace.WEST) {
			SideZ = -1;
		}
		firstBlock = midiSign.getBlock().getRelative(SideX * 2, 0, SideZ * 2);

		latestNote = new int[16];
		for (int i = 0; i < latestNote.length; i++) {
			latestNote[i] = -1;
		}

		if (ochans != null) {
			chans = "";
			for (int i = 0; i < ochans.length(); i++) {
				if (ochans.charAt(i) == 'l') {
					chans += "0123456789abcdef";
				} else {
					chans += ochans.charAt(i);
				}
				if ((ochans.charAt(i) == 's') || (ochans.charAt(i) == 'm')) {
					plugin.pinHandler.outputPin(
							midiSign.getBlock().getRelative(
									SideX * (chans.length() + 1), 0,
									SideZ * (chans.length() + 1)),
							SongEvent.START);
				}
			}
			chans = chans.substring(0,
					32 < chans.length() ? 32 : chans.length());
		}
	}

	protected void nextTick() {
		if (paused) {
			return;
		}
		tick += resolution * tempoCoef;
		if (tick >= music.ticks()) {
			over();
			return;
		}
		int lcount = 0;
		int keyNote = 0;
		if (window > -1) {
			keyNote = -6 + 12 * window;
		}

		for (; (event < music.size() - 1)
				&& (music.get(event).getTick() <= tick); event += 1) {
			if (music.get(event).getMessage().getStatus() >> 4 != 9) {
				continue;
			}
			ArrayList<Block> realBlocks = new ArrayList();
			int channel = 0;
			if (chanCollapse) {
				realBlocks.add(firstBlock);
			} else {
				channel = music.get(event).getMessage().getStatus() & 0xF;

				for (int i = 0; i < chans.length(); i++) {
					String si = String.valueOf(chans.charAt(i));
					if ((si.equals(Integer.toHexString(channel)))
							|| (si.equals("o"))) {
						realBlocks.add(midiSign.getBlock().getRelative(
								SideX * (i + 2), 0, SideZ * (i + 2)));
					}
					if (si.equals("n")) {
						plugin.pinHandler.outputPin(
								midiSign.getBlock().getRelative(
										SideX * (i + 2), 0, SideZ * (i + 2)),
								SongEvent.NOTE);
					}
				}
			}
			if (realBlocks.size() <= 0) {
				continue;
			}
			count += 1;
			lcount++;
			if (display) {
				midiSign.setLine(0, String.valueOf(count));
				midiSign.update();
			}
			int midiNote = music.get(event).getMessage().getMessage()[1];

			if (remRepeated) {
				if (latestNote[channel] != midiNote) {
					latestNote[channel] = midiNote;
				}
			} else {
				Integer iaux1 = 0;
				if (window < 0) {
					iaux1 = Integer.valueOf((midiNote + (shift ? 6 : -6)) % 24);
				} else {
					if ((midiNote >= keyNote) && (midiNote < keyNote + 24)) {
						Integer.valueOf(midiNote - keyNote);
					} else {
						if ((repOctave) && (midiNote < keyNote)) {
							Integer.valueOf((midiNote - 6) % 12);
						} else {
							if ((repOctave) && (midiNote >= keyNote + 24)) {
								Integer.valueOf(12 + (midiNote + 6) % 12);
							} else {
								Integer.valueOf(-1);
							}
						}
					}
				}
				if (iaux1.intValue() < 0) {
					continue;
				}
				for (Block relBlock : realBlocks) {
					if ((relBlock == null)
							|| (relBlock.getType() != Material.NOTE_BLOCK)) {
						continue;
					}
					try {
						NoteBlock nb = (NoteBlock) relBlock.getState();
						if (instrument.intValue() > -1) {
							nb.play(instrument.byteValue(), iaux1.byteValue());
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
		if ((loop) && (count > 0)) {
			loopnow = true;
		}
		for (SongInstance si : plugin.songs) {
			if ((si != this)
					&& (si.midiSign.getBlock().getLocation() == midiSign
							.getBlock().getLocation())) {
				loopnow = false;
				theend = false;
			}
		}
		if (loopnow) {
			plugin.learnMusic(midiSign);
		}
		if (theend) {
			for (int i = 0; i < chans.length(); i++) {
				if ((chans.charAt(i) == 'z') || (chans.charAt(i) == 'm')) {
					plugin.pinHandler.outputPin(midiSign.getBlock()
							.getRelative(SideX * (i + 2), 0, SideZ * (i + 2)),
							SongEvent.END);
				}
			}
		}
		plugin.songs.remove(this);
	}

	public boolean toggle() {
		if (paused) {
			midiSign.setLine(0, "PLAYING");
			midiSign.update();
			return paused = false;
		}
		midiSign.setLine(0, "PAUSED");
		midiSign.update();
		return paused = true;
	}
}