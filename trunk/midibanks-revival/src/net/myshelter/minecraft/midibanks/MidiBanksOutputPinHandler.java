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
/* 11 */   public MidiBanksOutputPinHandler(boolean redstone) { this.redstone = redstone; }
/*    */ 
/*    */   private boolean buttonDepress(Block main, BlockFace side)
/*    */   {
/* 15 */     Block aux = main.getRelative(side);
/* 16 */     if (aux == null) return false;
/* 17 */     if (aux.getType() != Material.STONE_BUTTON) return false;
/* 18 */     byte facing = (byte)(aux.getData() & 0x7);
/* 19 */     if ((facing == 1) && (side != BlockFace.SOUTH)) return false;
/* 20 */     if ((facing == 2) && (side != BlockFace.NORTH)) return false;
/* 21 */     if ((facing == 3) && (side != BlockFace.WEST)) return false;
/* 22 */     if ((facing == 4) && (side != BlockFace.EAST)) return false;
/* 23 */     aux.setData((byte)(aux.getData() | 0x8));
/* 24 */     return true;
/*    */   }
/*    */ 
/*    */   public void outputPin(Block main, SongEvent event) {
/* 28 */     if (!this.redstone) return;
/* 29 */     if ((main.getType() == Material.STONE_PLATE) || (main.getType() == Material.WOOD_PLATE)) {
/* 30 */       main.setData((byte) 1);
/* 31 */       return;
/*    */     }
/* 33 */     Block aux = main.getRelative(0, 1, 0);
/* 34 */     if ((aux.getType() == Material.STONE_PLATE) || (aux.getType() == Material.WOOD_PLATE)) {
/* 35 */       aux.setData((byte) 1);
/* 36 */       return;
/*    */     }
/* 38 */     if (buttonDepress(main, BlockFace.NORTH)) return;
/* 39 */     if (buttonDepress(main, BlockFace.SOUTH)) return;
/* 40 */     if (buttonDepress(main, BlockFace.EAST)) return;
/* 41 */     if (buttonDepress(main, BlockFace.WEST)) return;
/*    */   }
/*    */ }

/* Location:           C:\Users\jfmh\Downloads\midibankstest2.jar
 * Qualified Name:     net.myshelter.minecraft.midibanks.MidiBanksOutputPinHandler
 * JD-Core Version:    0.6.0
 */