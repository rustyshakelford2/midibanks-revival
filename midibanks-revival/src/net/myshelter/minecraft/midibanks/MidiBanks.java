package net.myshelter.minecraft.midibanks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Stack;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MidiBanks extends JavaPlugin {
	protected MidiBanksBlockListener listener;
	protected MidiBanksPlayerListener plistener;
	protected MidiBanksWorldListener wlistener;
	protected Timer player;
	protected ArrayList<SongInstance> songs;
	/* 72 */protected static final Logger log = Logger.getLogger("Minecraft");
	public static final int tempo = 20;
	/* 79 */boolean disallowAutostart = false;
	/* 80 */boolean disallowLoop = false;
	/* 81 */boolean redstone = true;
	public OutputPinHandler pinHandler;

	protected static void dolog(String msg) {
		log.info("[MidiBanks] " + msg);
	}
	public void onEnable() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		this.listener = new MidiBanksBlockListener(this);
		this.plistener = new MidiBanksPlayerListener(this);
		this.wlistener = new MidiBanksWorldListener(this);
		this.songs = new ArrayList();

		/* 106 */this.disallowAutostart = getConfig().getBoolean(
				"disallow-autostart", false);
		this.disallowLoop = getConfig().getBoolean("disallow-loop", false);
		this.redstone = getConfig().getBoolean("redstone", true);

		this.pinHandler = new MidiBanksOutputPinHandler(this.redstone);

		resetPlayer();
		getServer().getPluginManager().registerEvents(plistener, this);
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(wlistener, this);
		getServer().getPluginManager().registerEvents(wlistener, this);
		dolog("Enabled! Version is " + getDescription().getVersion());

		/* 120 */if (this.disallowAutostart)
			return;

		/* 123 */dolog("Auto-starting A banks in currently loaded chunks...");
		/* 124 */int count = 0;
		/* 125 */for (World w : getServer().getWorlds()) {
			/* 126 */for (Chunk c : w.getLoadedChunks()) {
				/* 127 */for (BlockState cbs : c.getTileEntities())
					/* 128 */if (cbs.getBlock().getType() == Material.WALL_SIGN) {
						/* 129 */org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign) cbs;
						/* 130 */if ((!midiSign.getLine(1).equalsIgnoreCase(
						"[MIDI]"))
						||
						/* 131 */(!midiSign.getLine(3).contains("A")))
							continue;
						/* 132 */learnMusic(midiSign);
						/* 133 */count++;
					}
			}
		}
		/* 137 */dolog("Done; found " + count + " A banks.");
	}

	public void onDisable() {
		/* 141 */this.player.cancel();
		/* 142 */dolog("Disabled.");
	}

	public void resetPlayer() {
		/* 146 */this.songs.clear();
		/* 147 */this.player = new Timer();
		/* 148 */MidiPlayerStep np = new MidiPlayerStep(this);
		/* 149 */this.player.schedule(np, 20L, 20L);
	}

	public File getMidiFile(String name) {
		/* 153 */File midiFile = new File(getDataFolder() + "/" + name + ".mid");
		/* 154 */if (!midiFile.exists()) {
			/* 155 */Stack dirs = new Stack();
			/* 156 */dirs.push(getDataFolder());
			try {
				while (true) {
					/* 159 */File thisdir = (File) dirs.pop();
					/* 160 */midiFile = new File(thisdir + "/" + name + ".mid");
					/* 161 */if (midiFile.exists())
						break;
					/* 162 */if (thisdir.listFiles() != null)
						/* 163 */for (File dircontent : thisdir.listFiles())
							/* 164 */if (dircontent.isDirectory())
								/* 165 */dirs.push(dircontent);
				}
			} catch (EmptyStackException localEmptyStackException) {
			} catch (NullPointerException localNullPointerException) {
			}
		}
		/* 171 */if (midiFile.exists())
			return midiFile;
		/* 172 */return null;
	}

	public void learnMusic(org.bukkit.block.Sign midiSign) {
		/* 186 */learnMusic(midiSign, false);
	}

	protected void learnMusic(org.bukkit.block.Sign midiSign, boolean fromRS) {
		/* 189 */if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]"))
			return;
		/* 190 */stopMusic(midiSign);
		/* 191 */int track = -1;
		int window = -1;
		int instrument = -1;
		/* 192 */double tempoCoef = 1.0D;
		/* 193 */String chans = "l";
		/* 194 */boolean chanCollapse = false;
		boolean shift = false;
		boolean loop = false;
		boolean display = false;
		boolean remrep = false;
		boolean repOctave = false;

		/* 196 */ArrayList checkRedstone = new ArrayList();
		/* 197 */if (((org.bukkit.material.Sign) midiSign.getData())
				.getFacing() == BlockFace.NORTH)
			/* 198 */checkRedstone.add(midiSign.getBlock()
					.getRelative(-1, 0, 0));
		/* 199 */if (((org.bukkit.material.Sign) midiSign.getData())
				.getFacing() == BlockFace.SOUTH)
			/* 200 */checkRedstone
			.add(midiSign.getBlock().getRelative(1, 0, 0));
		/* 201 */if (((org.bukkit.material.Sign) midiSign.getData())
				.getFacing() == BlockFace.EAST)
			/* 202 */checkRedstone.add(midiSign.getBlock().getRelative(0, 0, -1));
		/* 203 */if (((org.bukkit.material.Sign) midiSign.getData()).getFacing() == BlockFace.WEST)
			/* 204 */checkRedstone.add(midiSign.getBlock().getRelative(0, 0, 1));
		/* 205 */checkRedstone.add(midiSign.getBlock().getRelative(0, -1, 0));
		/* 206 */boolean hasRedstone = false;
		boolean powered = false;
		/* 215 */Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
		/* 216 */Matcher mFileName = pFileName.matcher(midiSign.getLine(2));
		/* 217 */if (mFileName.find())
			try {
				/* 218 */File midiFile = getMidiFile(midiSign.getLine(2));
				/* 219 */if (midiFile == null)
					return;

				/* 221 */String settings = midiSign.getLine(3);
				/* 222 */Pattern pNextSign = Pattern.compile("N([lrud])");
				/* 223 */Matcher mNextSign = pNextSign.matcher(midiSign
						.getLine(3));
				/* 224 */if (mNextSign.find()) {
					/* 225 */BlockFace direction = ((org.bukkit.material.Sign) midiSign
							.getData()).getFacing();
					/* 226 */Block sb = null;
					/* 227 */int sx = 0;
					int sz = 0;
					/* 228 */if (direction == BlockFace.NORTH)
						sz = -1;
					/* 229 */if (direction == BlockFace.SOUTH)
						sz = 1;
					/* 230 */if (direction == BlockFace.EAST)
						sx = 1;
					/* 231 */if (direction == BlockFace.WEST)
						sx = -1;
					/* 232 */if (mNextSign.group(1).equals("l"))
						sb = midiSign.getBlock().getRelative(sx, 0, sz);
					/* 233 */if (mNextSign.group(1).equals("r"))
						sb = midiSign.getBlock().getRelative(-1 * sx, 0,
								-1 * sz);
					/* 234 */if (mNextSign.group(1).equals("u"))
						sb = midiSign.getBlock().getRelative(0, 1, 0);
					/* 235 */if (mNextSign.group(1).equals("d"))
						sb = midiSign.getBlock().getRelative(0, -1, 0);
					/* 236 */if ((sb.getType() == Material.WALL_SIGN)
							|| (sb.getType() == Material.SIGN_POST)) {
						/* 237 */org.bukkit.block.Sign setSign = (org.bukkit.block.Sign) sb
						.getState();
						/* 238 */settings = "";
						/* 239 */for (int i = 0; i < 4; i++)
							settings = settings + setSign.getLine(i);
					}
				}

				/* 243 */Pattern pTrack = Pattern.compile("T([0-9a-f]+)");
				/* 244 */Matcher mTrack = pTrack.matcher(settings);
				/* 245 */if (mTrack.find())
					track = Integer.parseInt(mTrack.group(1), 16);

				/* 247 */Pattern pChans = Pattern
				.compile("=([0123456789abcdeflmnosz ]+)");
				/* 248 */Matcher mChans = pChans.matcher(settings);
				/* 249 */if (mChans.find())
					chans = mChans.group(1);

				/* 251 */Pattern pTempo = Pattern.compile("(<|>)([2-9])");
				/* 252 */Matcher mTempo = pTempo.matcher(settings);
				/* 253 */if (mTempo.find()) {
					/* 254 */if (mTempo.group(1).equals("<"))
						tempoCoef = 1.0D / Integer.parseInt(mTempo.group(2));
					/* 255 */if (mTempo.group(1).equals(">"))
						tempoCoef = Integer.parseInt(mTempo.group(2));
				}
				/* 257 */Pattern pFineTempo = Pattern
				.compile("(\\{|\\})([1-9])");
				/* 258 */Matcher mFineTempo = pFineTempo.matcher(settings);
				/* 259 */if (mFineTempo.find()) {
					/* 260 */if (mFineTempo.group(1).equals("{"))
						tempoCoef -= Integer.parseInt(mFineTempo.group(2)) / 10.0D;
					/* 261 */if (mFineTempo.group(1).equals("}"))
						tempoCoef += Integer.parseInt(mFineTempo.group(2)) / 10.0D;
				}

				/* 264 */Pattern pInstrument = Pattern.compile("I([0-9])");
				/* 265 */Matcher mInstrument = pInstrument.matcher(settings);
				/* 266 */if (mInstrument.find())
					instrument = Integer.parseInt(mInstrument.group(1));

				/* 268 */Pattern pWindow = Pattern.compile("W([0-9])");
				/* 269 */Matcher mWindow = pWindow.matcher(settings);
				/* 270 */if (mWindow.find())
					window = Integer.parseInt(mWindow.group(1));

				/* 272 */if (settings.contains("C"))
					chanCollapse = true;
				/* 273 */if (settings.contains("S"))
					shift = true;
				/* 274 */if (settings.contains("L"))
					loop = !this.disallowLoop;
				/* 275 */if (settings.contains("D"))
					display = true;
				/* 276 */if (settings.contains("X"))
					remrep = true;
				/* 277 */if (settings.contains("R"))
					repOctave = true;

				/* 279 */Sequence midi = MidiSystem.getSequence(midiFile);
				/* 280 */if (midi.getTracks().length <= track)
					return;

				/* 282 */if (!settings.contains("O")) {
					/* 283 */int realTempo = 0;
					/* 284 */Track first = midi.getTracks()[0];
					/* 285 */for (int i = 0; i < first.size(); i++)
						/* 286 */if ((first.get(i).getMessage().getStatus() == 255)
								&& (first.get(i).getMessage().getMessage()[1] == 81)) {
							/* 287 */byte[] bf = first.get(i).getMessage()
							.getMessage();
							/* 288 */for (int j = 3; j < 6; j++) {
								/* 289 */realTempo <<= 8;
								/* 290 */realTempo += bf[j];
							}
							/* 292 */break;
						}
					/* 294 */if (realTempo > 0)
						tempoCoef *= 500000.0D / realTempo * 0.8D;

				}

				/* 298 */if (track < 0) {
					/* 299 */for (int i = 0; i < midi.getTracks().length; i++) {
						/* 300 */SongInstance si = new SongInstance(this,
								midiSign, midi.getTracks()[i], chans);
						/* 301 */si.track = i;
						/* 302 */si.resolution = Math.floor(midi
								.getResolution() / 24);
						/* 303 */si.chanCollapse = chanCollapse;
						/* 304 */si.shift = shift;
						/* 305 */si.loop = loop;
						/* 306 */si.display = display;
						/* 307 */si.tempoCoef = tempoCoef;
						/* 308 */si.remRepeated = remrep;
						/* 309 */si.window = window;
						/* 310 */si.repOctave = repOctave;
						/* 311 */si.instrument = Integer.valueOf(instrument);
						/* 312 */this.songs.add(si);
					}
				} else {
					/* 315 */SongInstance si = new SongInstance(this, midiSign,
							midi.getTracks()[track], chans);
					/* 316 */si.track = track;
					/* 317 */si.resolution = Math
					.floor(midi.getResolution() / 24);
					/* 318 */si.chanCollapse = chanCollapse;
					/* 319 */si.shift = shift;
					/* 320 */si.loop = loop;
					/* 321 */si.display = display;
					/* 322 */si.tempoCoef = tempoCoef;
					/* 323 */si.remRepeated = remrep;
					/* 324 */si.window = window;
					/* 325 */si.repOctave = repOctave;
					/* 326 */si.instrument = Integer.valueOf(instrument);
					/* 327 */this.songs.add(si);
				}
				/* 329 */midiSign.setLine(0, "PLAYING");
			} catch (InvalidMidiDataException imde) {
				/* 331 */midiSign.setLine(0, "NOT A MIDI");
			} catch (IOException ioe) {
				/* 333 */midiSign.setLine(0, "CAN'T READ FILE");
			}
			else {
				/* 335 */midiSign.setLine(0, "BAD FILENAME");
			}
		/* 337 */getServer().getScheduler().scheduleSyncDelayedTask(this,
				new UpdateSign(midiSign));
	}

	public void stopMusic(org.bukkit.block.Sign midiSign) {
		try {
			/* 342 */for (int i = 0; i < this.songs.size(); i++) {
				/* 343 */if (midiSign
						.getBlock()
						.getLocation()
						.equals(((SongInstance) this.songs.get(i)).midiSign
								.getBlock().getLocation())) {
					/* 344 */this.songs.remove(i);
					/* 345 */i--;
				}
			}
			/* 348 */midiSign.setLine(0, "");
			/* 349 */getServer().getScheduler().scheduleSyncDelayedTask(this,
					new UpdateSign(midiSign));
		} catch (NullPointerException localNullPointerException) {
		}
	}
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		/* 354 */if (!command.getName().equalsIgnoreCase("midibanks"))
			return false;
		/* 355 */if (args.length < 1)
			return true;
		/* 356 */boolean admin = false;
		/* 357 */if ((!(sender instanceof Player))
				|| (((Player) sender).isOp()))
			admin = true;
		/* 358 */boolean cannormalcmd = (admin);
		/* 359 */if ((args[0].equalsIgnoreCase("halt")) && (admin)) {
			/* 360 */this.player.cancel();
			/* 361 */resetPlayer();
		}
		int[] chans = null;
		int b;
		File[] t = null;
		/* 363 */if ((args[0].equalsIgnoreCase("check")) && (args.length >= 2)
				&& (cannormalcmd)) {
			/* 364 */Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
			/* 365 */Matcher mFileName = pFileName.matcher(args[1]);
			/* 366 */if (mFileName.find())
				try {
					/* 367 */File midiFile = getMidiFile(args[1]);
					/* 368 */if (midiFile == null)
						return true;
					/* 369 */Sequence midi = MidiSystem.getSequence(midiFile);
					int i = 0;
					/* 370 */if (args.length >= 3) {
						/* 372 */int track = Integer.parseInt(args[2], 16);
						/* 373 */if ((track >= 0)
								&& (track < midi.getTracks().length)) {
							/* 374 */sender.sendMessage("== MIDI Sequence "
									+ args[1] + ".mid - Track "
									+ Integer.toHexString(track) + " ==");
							/* 375 */Track t1 = midi.getTracks()[track];
							/* 376 */sender.sendMessage("Length: " + t1.ticks()
									+ " ticks, " + t1.size() + " events");
							/* 377 */int count = 0;
							int chanc = 0;
							/* 378 */chans = new int[16];
							/* 379 */for (int i1 = 0; i1 < chans.length; i1++)
								chans[i1] = 0;
							/* 380 */for (int i1 = 0; i1 < t1.size(); i1++) {
								/* 381 */if (t1.get(i1).getMessage()
										.getStatus() >> 4 == 9) {
									/* 382 */count++;
									/* 383 */chans[(t1.get(i1).getMessage()
											.getStatus() & 0xF)] += 1;
								}
							}
							/* 386 */label = "";
							/* 387 */for (i = 0; i < chans.length; i++) {
								/* 388 */if (chans[i] > 0) {
									/* 389 */chanc++;
									/* 390 */label = label
									+ Integer.toHexString(i) + ":"
									+ chans[i] + " ";
								}
							}
							/* 393 */sender.sendMessage("Note ONs: " + count
									+ " (" + chanc + " channel(s) used)");
							/* 394 */sender.sendMessage(label);

						}
						/* 396 */sender
						.sendMessage("No such track in this sequence.");

					}

					/* 400 */sender.sendMessage("== MIDI Sequence " + args[1]
					                                                       + ".mid ==");
					/* 401 */Double secs = Double.valueOf(midi
							.getMicrosecondLength() / 1000000.0D);
					/* 402 */sender.sendMessage("Length: "
							+ String.format("%.2f", new Object[] { secs })
							+ "s, " + midi.getTickLength() + " ticks");
					/* 403 */int a = 0;
					b = 0;
					String midistring = i + "";
					String midilegth = midi.getTracks().length + "";
					/* 404 */String bychan = (midistring = midilegth);
					for (chans = null; chans.length < bychan.length(); chans = null) {

						/* 405 */if (t.length > 20)
							a++;
						/* 406 */if (t.length >= 0.8D * midi.getTickLength())
							continue;
						b++;
					}
					/* 408 */sender.sendMessage("Tracks: "
							+ midi.getTracks().length + " total, " + a
							+ " significant, " + b + " long");
					/* 409 */if (midi.getDivisionType() == 0.0F)
						/* 410 */sender.sendMessage("Tempo: PPQ "
								+ midi.getResolution());
					/* 411 */if (midi.getDivisionType() == 24.0F)
						/* 412 */sender.sendMessage("Tempo: "
								+ midi.getResolution()
								* 24
								+ " tick/s (est. tick length "
								+ String.format("%.2f", new Object[] { Double
										.valueOf(1000.0D / (midi
												.getResolution() * 24)) })
												+ "ms)");
					/* 413 */if (midi.getDivisionType() == 25.0F)
						/* 414 */sender.sendMessage("Tempo: "
								+ midi.getResolution()
								* 25
								+ " tick/s (est. tick length "
								+ String.format("%.2f", new Object[] { Double
										.valueOf(1000.0D / (midi
												.getResolution() * 25)) })
												+ "ms)");
					/* 415 */if (midi.getDivisionType() == 30.0F)
						/* 416 */sender.sendMessage("Tempo: "
								+ midi.getResolution()
								* 30
								+ " tick/s (est. tick length "
								+ String.format("%.2f", new Object[] { Double
										.valueOf(1000.0D / (midi
												.getResolution() * 30)) })
												+ "ms)");
					/* 417 */if (midi.getDivisionType() != 29.969999F)

						sender.sendMessage("Tempo: "
								+ midi.getResolution()
								* 29.969999999999999D
								+ " tick/s (est. tick length "
								+ String.format(
										"%.2f",
										new Object[] { Double.valueOf(1000.0D / (midi
												.getResolution() * 29.969999999999999D)) })
												+ "ms)");
				} catch (InvalidMidiDataException imde) {
					/* 421 */sender
					.sendMessage("Error reading MIDI data. Is this a MIDI file?");
				} catch (IOException ioe) {
					/* 423 */sender.sendMessage("No such file!");
				}
				else
					/* 425 */sender
					.sendMessage("Invalid filename. Filenames can only have letters, numbers, underscores and dashes.");
		}

		int i;
		/* 428 */if ((args[0].equalsIgnoreCase("channels"))
				&& (args.length >= 2) && (cannormalcmd)) {
			/* 429 */Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
			/* 430 */Matcher mFileName = pFileName.matcher(args[1]);
			/* 431 */if (mFileName.find())
				try {
					/* 432 */File midiFile = getMidiFile(args[1]);
					/* 433 */if (midiFile == null)
						return true;
					/* 434 */Sequence midi = MidiSystem.getSequence(midiFile);
					/* 435 */sender.sendMessage("== MIDI Sequence " + args[1]
					                                                       + ".mid - Channels ==");
					/* 436 */boolean[] chans1 = new boolean[16];
					/* 437 */for (int i1 = 0; i1 < chans1.length; i1++)
						chans1[i1] = false;
					/* 438 */for (Track t1 : midi.getTracks()) {
						/* 439 */for (int i1 = 0; i1 < t1.size(); i1++) {
							/* 440 */if (t1.get(i1).getMessage().getStatus() >> 4 == 9)
								/* 441 */chans1[(t1.get(i1).getMessage()
										.getStatus() & 0xF)] = true;
						}
					}
					/* 444 */for (i = 0; i < chans1.length; i++) {
						/* 446 */if (chans1[i] != false) {
						}
					}
					/* 449 */sender.sendMessage("Used: " + label);
				} catch (InvalidMidiDataException imde) {
					/* 451 */sender
					.sendMessage("Error reading MIDI data. Is this a MIDI file?");
				} catch (IOException ioe) {
					/* 453 */sender.sendMessage("No such file!");
				}
				else {
					/* 455 */sender
					.sendMessage("Invalid filename. Filenames can only have letters, numbers, underscores and dashes.");
				}
		}
		/* 458 */if ((args[0].equalsIgnoreCase("list")) && (cannormalcmd)) {
			/* 459 */String result = "";
			/* 460 */HashSet names = new HashSet();
			/* 461 */Stack dirs = new Stack();
			/* 462 */dirs.push(getDataFolder());
			try {
				while (true) {
					/* 465 */File thisdir = (File) dirs.pop();
					/* 466 */i = (t = thisdir.listFiles()).length;
					for (label = 0 + ""; label.length() < i; label.charAt(i++)) {
						File dircontent = t[i];
						/* 467 */if (dircontent.isDirectory())
							/* 468 */dirs.push(dircontent);
						/* 469 */else if (dircontent.getName().endsWith(".mid"))
							/* 470 */names.add(dircontent.getName().substring(
									0, dircontent.getName().length() - 4));
					}
				}
			} catch (EmptyStackException localEmptyStackException) {
			} catch (NullPointerException localNullPointerException) {
			}
			/* 476 */ArrayList sortNames = new ArrayList();
			/* 477 */sortNames.addAll(names);
			/* 478 */Collections.sort(sortNames);
			/* 479 */int page = 0;
			int maxpage = (int) Math.floor(sortNames.size() / 40);
			try {
				/* 481 */if (args.length > 1)
					page = Integer.parseInt(args[1]) - 1;
			} catch (NumberFormatException localNumberFormatException) {
			}
			/* 483 */if (page > maxpage)
				page = maxpage;
			/* 484 */if (page < 0)
				page = 0;
			/* 485 */sender
			.sendMessage("== List of available MIDI files == (page "
					+ (page + 1) + " of " + (maxpage + 1) + ")");
			/* 486 */for (int i1 = page * 40; (i1 < (page + 1) * 40)
			&& (i1 < sortNames.size());) {
				/* 487 */result = "";
				/* 488 */for (int j = 0; (j < 10) && (i1 < sortNames.size()); i1++) {
					/* 489 */result = result + (String) sortNames.get(i1) + " ";

					/* 488 */j++;
				}
				/* 490 */if (result == "")
					continue;
				sender.sendMessage(result);
			}
		}
		/* 493 */return true;
	}

	class UpdateSign implements Runnable {
		private org.bukkit.block.Sign midiSign;

		UpdateSign(org.bukkit.block.Sign midiSign) {
			/* 178 */this.midiSign = midiSign;
		}

		public void run() {
			/* 181 */this.midiSign.update();
		}

	}

}
