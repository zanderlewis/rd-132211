package com.mojang.rubydung.level;

/**
 * Listener interface for level changes.
 * 
 * Implemented by LevelRenderer to respond to block and lighting changes
 * and update the appropriate chunks.
 * 
 * @author Mojang
 * @version RD-132211
 */
public interface LevelListener {
   /**
    * Called when a block is placed or destroyed.
    * 
    * @param x block X coordinate
    * @param y block Y coordinate (vertical)
    * @param z block Z coordinate
    */
   void tileChanged(int var1, int var2, int var3);

   /**
    * Called when lighting changes for a vertical column.
    * 
    * @param x column X coordinate
    * @param z column Z coordinate
    * @param y0 minimum Y affected
    * @param y1 maximum Y affected
    */
   void lightColumnChanged(int var1, int var2, int var3, int var4);

   /**
    * Called when the entire level changes (e.g., load/reset).
    */
   void allChanged();
}
