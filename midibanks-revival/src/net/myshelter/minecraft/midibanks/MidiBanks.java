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
import org.bukkit.plugin.java.JavaPlugin;
 
 public class MidiBanks extends JavaPlugin
 {
   protected MidiBanksBlockListener listener;
   protected MidiBanksPlayerListener plistener;
   protected MidiBanksWorldListener wlistener;
   protected Timer player;
   protected ArrayList<SongInstance> songs;
   protected static final Logger log = Logger.getLogger("Minecraft");
   public static final int tempo = 20;
   boolean disallowAutostart = false;
   boolean disallowLoop = false;
   boolean redstone = true;
   public OutputPinHandler pinHandler;
 
   protected static void dolog(String msg)
   {
     log.info("[MidiBanks] " + msg);
   }
   public void onEnable() {
     if (!getDataFolder().exists()) getDataFolder().mkdir();
 
     this.listener = new MidiBanksBlockListener(this);
     this.plistener = new MidiBanksPlayerListener(this);
     this.wlistener = new MidiBanksWorldListener(this);
     this.songs = new ArrayList();
 
     this.disallowAutostart = getConfig().getBoolean("disallow-autostart", false);
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
 
     if (this.disallowAutostart) return;
 
     dolog("Auto-starting A banks in currently loaded chunks...");
     int count = 0;
     for (World w : getServer().getWorlds()) {
       for (Chunk c : w.getLoadedChunks()) {
         for (BlockState cbs : c.getTileEntities())
           if (cbs.getBlock().getType() == Material.WALL_SIGN) {
             org.bukkit.block.Sign midiSign = (org.bukkit.block.Sign)cbs;
             if ((!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) || 
               (!midiSign.getLine(3).contains("A"))) continue;
             learnMusic(midiSign);
             count++;
           }
       }
     }
     dolog("Done; found " + count + " A banks.");
   }
 
   public void onDisable() {
     this.player.cancel();
     dolog("Disabled.");
   }
 
   public void resetPlayer() {
     this.songs.clear();
     this.player = new Timer();
     MidiPlayerStep np = new MidiPlayerStep(this);
     this.player.schedule(np, 20L, 20L);
   }
 
   public File getMidiFile(String name) {
     File midiFile = new File(getDataFolder() + "/" + name + ".mid");
     if (!midiFile.exists()) {
       Stack dirs = new Stack();
       dirs.push(getDataFolder());
       try {
         while (true) {
           File thisdir = (File)dirs.pop();
           midiFile = new File(thisdir + "/" + name + ".mid");
           if (midiFile.exists()) break;
           if (thisdir.listFiles() != null)
             for (File dircontent : thisdir.listFiles())
               if (dircontent.isDirectory())
                 dirs.push(dircontent);
         }
       } catch (EmptyStackException localEmptyStackException) {
       } catch (NullPointerException localNullPointerException) {
       }
     }
     if (midiFile.exists()) return midiFile;
     return null;
   }
 
   public void learnMusic(org.bukkit.block.Sign midiSign)
   {
     learnMusic(midiSign, false);
   }
   protected void learnMusic(org.bukkit.block.Sign midiSign, boolean fromRS) {
     if (!midiSign.getLine(1).equalsIgnoreCase("[MIDI]")) return;
     stopMusic(midiSign);
     int track = -1; int window = -1; int instrument = -1;
     double tempoCoef = 1.0D;
     String chans = "l";
     boolean chanCollapse = false; boolean shift = false; boolean loop = false; boolean display = false; boolean remrep = false; boolean repOctave = false;
 
     ArrayList<Block> checkRedstone = new ArrayList<Block>();
     if (((org.bukkit.material.Sign)midiSign.getData()).getFacing() == BlockFace.NORTH)
       checkRedstone.add(midiSign.getBlock().getRelative(-1, 0, 0));
     if (((org.bukkit.material.Sign)midiSign.getData()).getFacing() == BlockFace.SOUTH)
       checkRedstone.add(midiSign.getBlock().getRelative(1, 0, 0));
     if (((org.bukkit.material.Sign)midiSign.getData()).getFacing() == BlockFace.EAST)
       checkRedstone.add(midiSign.getBlock().getRelative(0, 0, -1));
     if (((org.bukkit.material.Sign)midiSign.getData()).getFacing() == BlockFace.WEST)
       checkRedstone.add(midiSign.getBlock().getRelative(0, 0, 1));
     checkRedstone.add(midiSign.getBlock().getRelative(0, -1, 0));
     boolean hasRedstone = false; boolean powered = false;
     for (Block prb : checkRedstone) {
       if (prb.getType() == Material.REDSTONE_WIRE) {
         hasRedstone = true;
         if (prb.getData() <= 0) continue; powered = true;
       }
     }
     if ((this.redstone) && (!fromRS) && (hasRedstone) && (!powered) && (!midiSign.getLine(3).contains("Y"))) return;
 
     Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
     Matcher mFileName = pFileName.matcher(midiSign.getLine(2));
     if (mFileName.find()) try {
         File midiFile = getMidiFile(midiSign.getLine(2));
         if (midiFile == null) return;
 
         String settings = midiSign.getLine(3);
         Pattern pNextSign = Pattern.compile("N([lrud])");
         Matcher mNextSign = pNextSign.matcher(midiSign.getLine(3));
         if (mNextSign.find()) {
           BlockFace direction = ((org.bukkit.material.Sign)midiSign.getData()).getFacing();
           Block sb = null;
           int sx = 0; int sz = 0;
           if (direction == BlockFace.NORTH) sz = -1;
           if (direction == BlockFace.SOUTH) sz = 1;
           if (direction == BlockFace.EAST) sx = 1;
           if (direction == BlockFace.WEST) sx = -1;
           if (mNextSign.group(1).equals("l")) sb = midiSign.getBlock().getRelative(sx, 0, sz);
           if (mNextSign.group(1).equals("r")) sb = midiSign.getBlock().getRelative(-1 * sx, 0, -1 * sz);
           if (mNextSign.group(1).equals("u")) sb = midiSign.getBlock().getRelative(0, 1, 0);
           if (mNextSign.group(1).equals("d")) sb = midiSign.getBlock().getRelative(0, -1, 0);
           if ((sb.getType() == Material.WALL_SIGN) || (sb.getType() == Material.SIGN_POST)) {
             org.bukkit.block.Sign setSign = (org.bukkit.block.Sign)sb.getState();
             settings = "";
             for (int i = 0; i < 4; i++) settings = settings + setSign.getLine(i);
           }
         }
 
         Pattern pTrack = Pattern.compile("T([0-9a-f]+)");
         Matcher mTrack = pTrack.matcher(settings);
         if (mTrack.find()) track = Integer.parseInt(mTrack.group(1), 16);
 
         Pattern pChans = Pattern.compile("=([0123456789abcdeflmnosz ]+)");
         Matcher mChans = pChans.matcher(settings);
         if (mChans.find()) chans = mChans.group(1);
 
         Pattern pTempo = Pattern.compile("(<|>)([2-9])");
         Matcher mTempo = pTempo.matcher(settings);
         if (mTempo.find()) {
           if (mTempo.group(1).equals("<")) tempoCoef = 1.0D / Integer.parseInt(mTempo.group(2));
           if (mTempo.group(1).equals(">")) tempoCoef = Integer.parseInt(mTempo.group(2));
         }
         Pattern pFineTempo = Pattern.compile("(\\{|\\})([1-9])");
         Matcher mFineTempo = pFineTempo.matcher(settings);
         if (mFineTempo.find()) {
           if (mFineTempo.group(1).equals("{")) tempoCoef -= Integer.parseInt(mFineTempo.group(2)) / 10.0D;
           if (mFineTempo.group(1).equals("}")) tempoCoef += Integer.parseInt(mFineTempo.group(2)) / 10.0D;
         }
 
         Pattern pInstrument = Pattern.compile("I([0-9])");
         Matcher mInstrument = pInstrument.matcher(settings);
         if (mInstrument.find()) instrument = Integer.parseInt(mInstrument.group(1));
 
         Pattern pWindow = Pattern.compile("W([0-9])");
         Matcher mWindow = pWindow.matcher(settings);
         if (mWindow.find()) window = Integer.parseInt(mWindow.group(1));
 
         if (settings.contains("C")) chanCollapse = true;
         if (settings.contains("S")) shift = true;
         if (settings.contains("L")) loop = !this.disallowLoop;
         if (settings.contains("D")) display = true;
         if (settings.contains("X")) remrep = true;
         if (settings.contains("R")) repOctave = true;
 
         Sequence midi = MidiSystem.getSequence(midiFile);
         if (midi.getTracks().length <= track) return;
 
         if (!settings.contains("O")) {
           int realTempo = 0;
           Track first = midi.getTracks()[0];
           for (int i = 0; i < first.size(); i++)
             if ((first.get(i).getMessage().getStatus() == 255) && (first.get(i).getMessage().getMessage()[1] == 81)) {
               byte[] bf = first.get(i).getMessage().getMessage();
               for (int j = 3; j < 6; j++) {
                 realTempo <<= 8;
                 realTempo += bf[j];
               }
               break;
             }
           if (realTempo > 0) tempoCoef *= 500000.0D / realTempo * 0.8D;
 
         }
 
         if (track < 0) {
           for (int i = 0; i < midi.getTracks().length; i++) {
             SongInstance si = new SongInstance(this, midiSign, midi.getTracks()[i], chans);
             si.track = i;
             si.resolution = Math.floor(midi.getResolution() / 24);
             si.chanCollapse = chanCollapse;
             si.shift = shift;
             si.loop = loop;
             si.display = display;
             si.tempoCoef = tempoCoef;
             si.remRepeated = remrep;
             si.window = window;
             si.repOctave = repOctave;
             si.instrument = Integer.valueOf(instrument);
             this.songs.add(si);
           }
         } else {
           SongInstance si = new SongInstance(this, midiSign, midi.getTracks()[track], chans);
           si.track = track;
           si.resolution = Math.floor(midi.getResolution() / 24);
           si.chanCollapse = chanCollapse;
           si.shift = shift;
           si.loop = loop;
           si.display = display;
           si.tempoCoef = tempoCoef;
           si.remRepeated = remrep;
           si.window = window;
           si.repOctave = repOctave;
           si.instrument = Integer.valueOf(instrument);
           this.songs.add(si);
         }
         midiSign.setLine(0, "PLAYING");
       } catch (InvalidMidiDataException imde) {
         midiSign.setLine(0, "NOT A MIDI");
       } catch (IOException ioe) {
         midiSign.setLine(0, "CAN'T READ FILE");
       } else {
       midiSign.setLine(0, "BAD FILENAME");
     }
     getServer().getScheduler().scheduleSyncDelayedTask(this, new UpdateSign(midiSign));
   }
 
   public void stopMusic(org.bukkit.block.Sign midiSign) {
     try {
       for (int i = 0; i < this.songs.size(); i++) {
         if (midiSign.getBlock().getLocation().equals(((SongInstance)this.songs.get(i)).midiSign.getBlock().getLocation())) {
           this.songs.remove(i);
           i--;
         }
       }
       midiSign.setLine(0, "");
       getServer().getScheduler().scheduleSyncDelayedTask(this, new UpdateSign(midiSign)); } catch (NullPointerException localNullPointerException) {
     }
   }
  //Command area
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     if (!command.getName().equalsIgnoreCase("midi")) return false;
     if (args.length < 1) return true;
     boolean admin = false;
     if ((!(sender instanceof Player)) || (((Player)sender).isOp())) admin = true;
     boolean cannormalcmd = (admin);
     if ((args[0].equalsIgnoreCase("halt")) && (admin)) {
       this.player.cancel();
       resetPlayer();
     }
     int[] chans = null;
     int b;

     if ((args[0].equalsIgnoreCase("check")) && (args.length >= 2) && (cannormalcmd)) {
       Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
       Matcher mFileName = pFileName.matcher(args[1]);
       if (mFileName.find()) try {
           File midiFile = getMidiFile(args[1]);
           if (midiFile == null) return true;
           Sequence midi = MidiSystem.getSequence(midiFile);
           int i;
           if (args.length >= 3)
           {
             int track = Integer.parseInt(args[2], 16);
             if ((track >= 0) && (track < midi.getTracks().length)) {
               sender.sendMessage("== MIDI Sequence " + args[1] + ".mid - Track " + Integer.toHexString(track) + " ==");
               Track track11 = midi.getTracks()[track];
               sender.sendMessage("Length: " + track11.ticks() + " ticks, " + track11.size() + " events");
               int count = 0; int chanc = 0;
               chans = new int[16];
               for (int i1 = 0; i1 < chans.length; i1++) chans[i1] = 0;
               for (int i1 = 0; i1 < track11.size(); i1++) {
                 if (track11.get(i1).getMessage().getStatus() >> 4 == 9) {
                   count++;
                   chans[(track11.get(i1).getMessage().getStatus() & 0xF)] += 1;
                 }
               }
               String bychan = "";
               for (i = 0; i < chans.length; i++) {
                 if (chans[i] > 0) {
                   chanc++;
                   bychan = bychan + Integer.toHexString(i) + ":" + chans[i] + " ";
                 }
               }
               sender.sendMessage("Note ONs: " + count + " (" + chanc + " channel(s) used)");
               sender.sendMessage(bychan); 
             }
             sender.sendMessage("No such track in this sequence."); 
           }
 
           sender.sendMessage("== MIDI Sequence " + args[1] + ".mid ==");
           Double secs = Double.valueOf(midi.getMicrosecondLength() / 1000000.0D);
           sender.sendMessage("Length: " + String.format("%.2f", new Object[] { secs }) + "s, " + midi.getTickLength() + " ticks");
           int a = 0; b = 0;
     		Track track1 = null;
           String bychan = Integer.toString((i = (int) midi.getTracks().length)); for ( i = 0; chans[i] < bychan.length(); i++) 
					{ 
					/* 405 */             //if (track1.size() > 20) a++;
             //if (track1.ticks() >= 0.8D * midi.getTickLength()) continue; b++;
           }
           sender.sendMessage("Tracks: " + midi.getTracks().length + " total, " + a + " significant, " + b + " long");
           if (midi.getDivisionType() == 0.0F)
             sender.sendMessage("Tempo: PPQ " + midi.getResolution());
           if (midi.getDivisionType() == 24.0F)
             sender.sendMessage("Tempo: " + midi.getResolution() * 24 + " tick/s (est. tick length " + String.format("%.2f", new Object[] { Double.valueOf(1000.0D / (midi.getResolution() * 24)) }) + "ms)");
           if (midi.getDivisionType() == 25.0F)
             sender.sendMessage("Tempo: " + midi.getResolution() * 25 + " tick/s (est. tick length " + String.format("%.2f", new Object[] { Double.valueOf(1000.0D / (midi.getResolution() * 25)) }) + "ms)");
           if (midi.getDivisionType() == 30.0F)
             sender.sendMessage("Tempo: " + midi.getResolution() * 30 + " tick/s (est. tick length " + String.format("%.2f", new Object[] { Double.valueOf(1000.0D / (midi.getResolution() * 30)) }) + "ms)");
           if (midi.getDivisionType() != 29.969999F)  sender.sendMessage("Tempo: " + midi.getResolution() * 29.969999999999999D + " tick/s (est. tick length " + String.format("%.2f", new Object[] { Double.valueOf(1000.0D / (midi.getResolution() * 29.969999999999999D)) }) + "ms)");
         }
         catch (InvalidMidiDataException imde) {
           sender.sendMessage("Error reading MIDI data. Is this a MIDI file?");
         } catch (IOException ioe) {
           sender.sendMessage("No such file!");
         } else
         sender.sendMessage("Invalid filename. Filenames can only have letters, numbers, underscores and dashes.");
     }
     String bychan;
     int i;
     if ((args[0].equalsIgnoreCase("channels")) && (args.length >= 2) && (cannormalcmd)) {
       Pattern pFileName = Pattern.compile("^[A-Za-z0-9_-]+$");
       Matcher mFileName = pFileName.matcher(args[1]);
       if (mFileName.find()) try {
           File midiFile = getMidiFile(args[1]);
           if (midiFile == null) return true;
           Sequence midi = MidiSystem.getSequence(midiFile);
           sender.sendMessage("== MIDI Sequence " + args[1] + ".mid - Channels ==");
           boolean[] chans1 = new boolean[16];
           for (int interger1 = 0; interger1 < chans1.length; interger1++) chans1[interger1] = false;
           for (Track track12 : midi.getTracks()) {
             for (int i1 = 0; i1 < track12.size(); i1++) {
               if (track12.get(i1).getMessage().getStatus() >> 4 == 9)
                 chans1[(track12.get(i1).getMessage().getStatus() & 0xF)] = true;
             }
           }
           bychan = "";
           for (i = 0; i < chans1.length; i++) {
             if (chans1[i] != false)
               bychan = bychan + Integer.toHexString(i) + " ";
           }
           sender.sendMessage("Used: " + bychan);
         } catch (InvalidMidiDataException imde) {
           sender.sendMessage("Error reading MIDI data. Is this a MIDI file?");
         } catch (IOException ioe) {
           sender.sendMessage("No such file!");
         } else {
         sender.sendMessage("Invalid filename. Filenames can only have letters, numbers, underscores and dashes.");
       }
     }
     if ((args[0].equalsIgnoreCase("list")) && (cannormalcmd)) {
       String result = "";
				File[] Files;
       HashSet names = new HashSet();
       Stack dirs = new Stack();
       dirs.push(getDataFolder());
       try {
         while (true) {
           File thisdir = (File)dirs.pop();
           i = (Files = thisdir.listFiles()).length; for (File as : Files) { File dircontent = as;
             if (dircontent.isDirectory())
               dirs.push(dircontent);
             else if (dircontent.getName().endsWith(".mid"))
               names.add(dircontent.getName().substring(0, dircontent.getName().length() - 4)); }
         }
       } catch (EmptyStackException localEmptyStackException) {
       }
       catch (NullPointerException localNullPointerException) {
       }
       ArrayList sortNames = new ArrayList();
       sortNames.addAll(names);
       Collections.sort(sortNames);
       int page = 0; int maxpage = (int)Math.floor(sortNames.size() / 40);
       try {
         if (args.length > 1) page = Integer.parseInt(args[1]) - 1; 
       } catch (NumberFormatException localNumberFormatException) {
       }
       if (page > maxpage) page = maxpage;
       if (page < 0) page = 0;
       sender.sendMessage("== List of available MIDI files == (page " + (page + 1) + " of " + (maxpage + 1) + ")");
       for (int i1 = page * 40; (i1 < (page + 1) * 40) && (i1 < sortNames.size()); ) {
         result = "";
         for (int j = 0; (j < 10) && (i1 < sortNames.size()); i1++) {
           result = result + (String)sortNames.get(i1) + " ";
 
           j++;
         }
         if (result == "") continue; sender.sendMessage(result);
       }
     }
     return true;
   }
 
   //boolean varCanCreate(Player p)
   //{
     //return this.varProvider.getFlag(p, "can-create", true);
   //}
   //boolean varCanUse(Player p) {
    // return this.varProvider.getFlag(p, "can-use", true);
   //}
 
   class UpdateSign
     implements Runnable
   {
     private org.bukkit.block.Sign midiSign;
 
     UpdateSign(org.bukkit.block.Sign midiSign)
     {
       this.midiSign = midiSign;
     }
     public void run() {
       this.midiSign.update();
     }
   }
 }