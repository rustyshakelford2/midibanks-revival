/*    */ package net.myshelter.minecraft.midibanks;
/*    */ 
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ import org.bukkit.block.BlockFace;
/*    */ 
/*    */ public class MidiBanksOutputPinHandler
/*    */   implements OutputPinHandler
/*    */ {
/*  9 */   private boolean redstone = true;
/*    */ 
   public MidiBanksOutputPinHandler(boolean redstone) { this.redstone = redstone; }
/*    */ 
/*    */   private boolean buttonDepress(Block main, BlockFace side)
/*    */   {
     Block aux = main.getRelative(side);
     if (aux == null) return false;
     if (aux.getType() != Material.STONE_BUTTON) return false;
     byte facing = (byte)(aux.getData() & 0x7);
     if ((facing == 1) && (side != BlockFace.SOUTH)) return false;
     if ((facing == 2) && (side != BlockFace.NORTH)) return false;
     if ((facing == 3) && (side != BlockFace.WEST)) return false;
     if ((facing == 4) && (side != BlockFace.EAST)) return false;
     aux.setData((byte)(aux.getData() | 0x8));
     return true;
/*    */   }
/*    */ 
/*    */   @Override
public void outputPin(Block main, SongEvent event) {
     if (!this.redstone) return;
     if ((main.getType() == Material.STONE_PLATE) || (main.getType() == Material.WOOD_PLATE)) {
       main.setData((byte) 1);
       return;
/*    */     }
     Block aux = main.getRelative(0, 1, 0);
     if ((aux.getType() == Material.STONE_PLATE) || (aux.getType() == Material.WOOD_PLATE)) {
       aux.setData((byte) 1);
       return;
/*    */     }
     if (buttonDepress(main, BlockFace.NORTH)) return;
     if (buttonDepress(main, BlockFace.SOUTH)) return;
     if (buttonDepress(main, BlockFace.EAST)) return;
     if (buttonDepress(main, BlockFace.WEST)) return;
/*    */   }
/*    */ }

/* Location:           C:\Users\jfmh\Downloads\midibankstest2.jar
 * Qualified Name:     net.myshelter.minecraft.midibanks.MidiBanksOutputPinHandler
 * JD-Core Version:    0.6.0
 */