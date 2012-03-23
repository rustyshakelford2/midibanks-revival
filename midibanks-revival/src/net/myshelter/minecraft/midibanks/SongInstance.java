/*     */ package net.myshelter.minecraft.midibanks;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import javax.sound.midi.MidiEvent;
/*     */ import javax.sound.midi.MidiMessage;
/*     */ import javax.sound.midi.Track;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.block.Block;
/*     */ import org.bukkit.block.BlockFace;
/*     */ import org.bukkit.block.NoteBlock;
/*     */ 
/*     */ public class SongInstance
/*     */ {
/*     */   private MidiBanks plugin;
/*     */   public org.bukkit.block.Sign midiSign;
/*     */   private Track music;
/*  19 */   protected int track = 0;
/*  20 */   protected double resolution = 1.0D;
/*  21 */   protected double tempoCoef = 1.0D;
/*  22 */   protected boolean chanCollapse = false;
/*  23 */   protected boolean shift = false;
/*  24 */   protected boolean loop = false;
/*  25 */   protected boolean display = false;
/*  26 */   protected boolean remRepeated = false;
/*  27 */   protected String chans = "l";
/*  28 */   protected int window = -1;
/*  29 */   protected boolean repOctave = false;
/*  30 */   protected Integer instrument = Integer.valueOf(-1);
/*     */ 
/*  32 */   private double tick = 0.0D;
/*  33 */   private int event = 0;
/*  34 */   private int count = 0;
/*     */   private int[] latestNote;
/*  36 */   private boolean paused = false;
/*  37 */   int sx = 0; int sz = 0;
/*     */   Block firstBlock;
/*     */ 
/*     */   protected SongInstance(MidiBanks plugin, org.bukkit.block.Sign midiSign, Track music, String ochans)
/*     */   {
/*  41 */     this.plugin = plugin;
/*  42 */     this.midiSign = midiSign;
/*  43 */     this.music = music;
/*     */ 
/*  45 */     BlockFace direction = ((org.bukkit.material.Sign)midiSign.getData()).getFacing();
/*  46 */     if (direction == BlockFace.NORTH) this.sx = 1;
/*  47 */     if (direction == BlockFace.SOUTH) this.sx = -1;
/*  48 */     if (direction == BlockFace.EAST) this.sz = 1;
/*  49 */     if (direction == BlockFace.WEST) this.sz = -1;
/*  50 */     this.firstBlock = midiSign.getBlock().getRelative(this.sx * 2, 0, this.sz * 2);
/*     */ 
/*  52 */     this.latestNote = new int[16];
/*  53 */     for (int i = 0; i < this.latestNote.length; i++) this.latestNote[i] = -1;
/*     */ 
/*  55 */     if (ochans != null) {
/*  56 */       this.chans = "";
/*  57 */       for (int i = 0; i < ochans.length(); i++) {
/*  58 */         if (ochans.charAt(i) == 'l') this.chans += "0123456789abcdef"; else
/*  59 */           this.chans += ochans.charAt(i);
/*  60 */         if ((ochans.charAt(i) == 's') || (ochans.charAt(i) == 'm')) {
/*  61 */           plugin.pinHandler.outputPin(midiSign.getBlock().getRelative(this.sx * (this.chans.length() + 1), 0, this.sz * (this.chans.length() + 1)), SongEvent.START);
/*     */         }
/*     */       }
/*  64 */       this.chans = this.chans.substring(0, 32 < this.chans.length() ? 32 : this.chans.length());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void nextTick() {
/*  69 */     if (this.paused) return;
/*  70 */     this.tick += this.resolution * this.tempoCoef;
/*  71 */     if (this.tick >= this.music.ticks()) { over(); return; }
/*  72 */     int lcount = 0;
/*  73 */     int keyNote = 0;
/*  74 */     if (this.window > -1) keyNote = -6 + 12 * this.window;
/*     */ 
/*  77 */     for (; (this.event < this.music.size() - 1) && (this.music.get(this.event).getTick() <= this.tick); this.event += 1) {
/*  78 */       if (this.music.get(this.event).getMessage().getStatus() >> 4 != 9)
/*     */         continue;
/*  80 */       ArrayList<Block> relBlocks = new ArrayList();
/*  81 */       int channel = 0;
/*  82 */       if (this.chanCollapse) { relBlocks.add(this.firstBlock);
/*     */       } else {
/*  84 */         channel = this.music.get(this.event).getMessage().getStatus() & 0xF;
/*     */ 
/*  86 */         for (int i = 0; i < this.chans.length(); i++) {
/*  87 */           String si = String.valueOf(this.chans.charAt(i));
/*  88 */           if ((si.equals(Integer.toHexString(channel))) || (si.equals("o")))
/*  89 */             relBlocks.add(this.midiSign.getBlock().getRelative(this.sx * (i + 2), 0, this.sz * (i + 2)));
/*  90 */           if (si.equals("n"))
/*  91 */             this.plugin.pinHandler.outputPin(this.midiSign.getBlock().getRelative(this.sx * (i + 2), 0, this.sz * (i + 2)), SongEvent.NOTE);
/*     */         }
/*     */       }
/*  94 */       if (relBlocks.size() <= 0)
/*     */         continue;
/*  96 */       this.count += 1; lcount++;
/*  97 */       if (this.display) {
/*  98 */         this.midiSign.setLine(0, String.valueOf(this.count));
/*  99 */         this.midiSign.update();
/*     */       }
/* 101 */       int midiNote = this.music.get(this.event).getMessage().getMessage()[1];
/*     */ 
/* 103 */       if (this.remRepeated) {
/* 104 */         if (this.latestNote[channel] != midiNote)
/* 105 */           this.latestNote[channel] = midiNote;
/*     */       }
/*     */       else
/*     */       {
/*     */         Integer iaux;
/*     */         Integer iaux1 = 0;
/* 109 */         if (this.window < 0) {
/* 110 */           iaux1 = Integer.valueOf((midiNote + (this.shift ? 6 : -6)) % 24);
/*     */         }
/*     */         else
/*     */         {
/* 112 */           Integer iaux2;
/* 112 */           if ((midiNote >= keyNote) && (midiNote < keyNote + 24)) { iaux2 = Integer.valueOf(midiNote - keyNote);
/*     */           }
/*     */           else
/*     */           {
/* 113 */             Integer iaux3;
/* 113 */             if ((this.repOctave) && (midiNote < keyNote)) { iaux3 = Integer.valueOf((midiNote - 6) % 12);
/*     */             }
/*     */             else
/*     */             {
/* 114 */               Integer iaux4;
/* 114 */               if ((this.repOctave) && (midiNote >= keyNote + 24)) iaux4 = Integer.valueOf(12 + (midiNote + 6) % 12); else
/* 115 */                 iaux4 = Integer.valueOf(-1); 
/*     */             }
/*     */           }
/*     */         }
/* 117 */         if (iaux1.intValue() < 0)
/*     */           continue;
/* 119 */         for (Block relBlock : relBlocks) {
/* 120 */           if ((relBlock == null) || (relBlock.getType() != Material.NOTE_BLOCK)) continue;
/*     */           try {
/* 122 */             NoteBlock nb = (NoteBlock)relBlock.getState();
/* 123 */             if (this.instrument.intValue() > -1) { nb.play(this.instrument.byteValue(), iaux1.byteValue());
/*     */             } else {
/* 125 */               nb.setRawNote(iaux1.byteValue());
/* 126 */               nb.update();
/* 127 */               nb.play();
/*     */             }
/*     */           } catch (NullPointerException localNullPointerException) {
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void over() {
/* 136 */     boolean loopnow = false; boolean theend = true;
/* 137 */     if ((this.loop) && (this.count > 0))
/* 138 */       loopnow = true;
/* 139 */     for (SongInstance si : this.plugin.songs)
/* 140 */       if ((si != this) && (si.midiSign.getBlock().getLocation() == this.midiSign.getBlock().getLocation())) {
/* 141 */         loopnow = false;
/* 142 */         theend = false;
/*     */       }
/* 144 */     if (loopnow)
/* 145 */       this.plugin.learnMusic(this.midiSign);
/* 146 */     if (theend) {
/* 147 */       for (int i = 0; i < this.chans.length(); i++)
/* 148 */         if ((this.chans.charAt(i) == 'z') || (this.chans.charAt(i) == 'm'))
/* 149 */           this.plugin.pinHandler.outputPin(this.midiSign.getBlock().getRelative(this.sx * (i + 2), 0, this.sz * (i + 2)), SongEvent.END);
/*     */     }
/* 151 */     this.plugin.songs.remove(this);
/*     */   }
/*     */ 
/*     */   public boolean toggle() {
/* 155 */     if (this.paused) {
/* 156 */       this.midiSign.setLine(0, "PLAYING");
/* 157 */       this.midiSign.update();
/* 158 */       return this.paused = false;
/*     */     }
/* 160 */     this.midiSign.setLine(0, "PAUSED");
/* 161 */     this.midiSign.update();
/* 162 */     return this.paused = true;
/*     */   }
/*     */ }

/* Location:           C:\Users\jfmh\Downloads\midibankstest2.jar
 * Qualified Name:     net.myshelter.minecraft.midibanks.SongInstance
 * JD-Core Version:    0.6.0
 */