 package net.myshelter.minecraft.midibanks;
 import java.util.TimerTask;
 
 public class MidiPlayerStep extends TimerTask
 {
   private MidiBanks plugin;
 
   protected MidiPlayerStep(MidiBanks plugin)
   {
     this.plugin = plugin;
   }
 
   @Override
public void run() {
     for (int i = 0; i < this.plugin.songs.size(); i++)
       (this.plugin.songs.get(i)).nextTick();
   }
 }