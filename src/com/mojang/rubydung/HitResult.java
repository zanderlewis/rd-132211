package com.mojang.rubydung;

/**
 * Represents the result of a raycast hit against a block.
 * 
 * Stores information about which block was hit and which face was hit.
 * Used for determining which block the player is looking at for block placement/destruction.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class HitResult {
   /** X coordinate of the hit block */
   public int x;
   /** Y coordinate of the hit block (vertical) */
   public int y;
   /** Z coordinate of the hit block */
   public int z;
   /** Unknown field (possibly distance or collision order) */
   public int o;
   /** Which face was hit: 0=bottom, 1=top, 2=front, 3=back, 4=left, 5=right */
   public int f;

   /**
    * Creates a new hit result.
    * 
    * @param x block X coordinate
    * @param y block Y coordinate (vertical)
    * @param z block Z coordinate
    * @param o unknown field
    * @param f hit face (0-5)
    */
   public HitResult(int x, int y, int z, int o, int f) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.o = o;
      this.f = f;
   }
}
