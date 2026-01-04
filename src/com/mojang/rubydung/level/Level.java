package com.mojang.rubydung.level;

import com.mojang.rubydung.phys.AABB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents the 3D game world/level.
 * 
 * Stores and manages a 3D voxel grid of blocks. Each block is either solid (type 1) or air (type 0).
 * Handles collision detection, lighting calculations, and persistence (save/load).
 * Uses a byte array for efficient memory usage (256x64x256 = ~4MB).
 * 
 * The coordinate system is:
 * - X: horizontal (0 to width)
 * - Y: vertical/depth (0 to depth, where Y increases upward)
 * - Z: horizontal (0 to height)
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Level {
   /** World width in blocks (X dimension) */
   public final int width;
   /** World height in blocks (Z dimension) */
   public final int height;
   /** World depth in blocks (Y dimension, vertical) */
   public final int depth;
   /** Block data: 0 = air, 1 = solid. Layout: (y * height + z) * width + x */
   private byte[] blocks;
   /** Light depth for each XZ column (height of light surface) */
   private int[] lightDepths;
   /** Listeners notified when level changes occur */
   private ArrayList<LevelListener> levelListeners = new ArrayList<>();

   /**
    * Creates a new level with the specified dimensions.
    * Initializes blocks with terrain (solid below 2/3 height, air above).
    * Automatically loads persisted data if available.
    * 
    * @param w width in blocks (X)
    * @param h height in blocks (Z)
    * @param d depth in blocks (Y)
    */
   public Level(int w, int h, int d) {
      this.width = w;
      this.height = h;
      this.depth = d;
      this.blocks = new byte[w * h * d];
      this.lightDepths = new int[w * h];

      for (int x = 0; x < w; x++) {
         for (int y = 0; y < d; y++) {
            for (int z = 0; z < h; z++) {
               int i = (y * this.height + z) * this.width + x;
               this.blocks[i] = (byte)(y <= d * 2 / 3 ? 1 : 0);
            }
         }
      }

      this.calcLightDepths(0, 0, w, h);
      this.load();
   }

   /**
    * Loads the level from disk (level.dat).
    * Uses GZIP compression to reduce file size.
    * Notifies listeners when data is loaded.
    */
   public void load() {
      try {
         DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(new File("level.dat"))));
         dis.readFully(this.blocks);
         this.calcLightDepths(0, 0, this.width, this.height);

         for (int i = 0; i < this.levelListeners.size(); i++) {
            this.levelListeners.get(i).allChanged();
         }

         dis.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   /**
    * Saves the level to disk (level.dat).
    * Uses GZIP compression for efficient storage.
    */
   public void save() {
      try {
         DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("level.dat"))));
         dos.write(this.blocks);
         dos.close();
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   /**
    * Recalculates light depth (lighting surface) for a rectangular region.
    * Used for determining brightness of blocks based on exposure to sky.
    * Notifies listeners of light changes.
    * 
    * @param x0 minimum X coordinate
    * @param y0 minimum Z coordinate
    * @param x1 width of region to update
    * @param y1 height of region to update
    */
   public void calcLightDepths(int x0, int y0, int x1, int y1) {
      // For each XZ column
      for (int x = x0; x < x0 + x1; x++) {
         for (int z = y0; z < y0 + y1; z++) {
            int oldDepth = this.lightDepths[x + z * this.width];
            
            // Find topmost solid block
            int y = this.depth - 1;
            while (y > 0 && !this.isLightBlocker(x, y, z)) {
               y--;
            }

            this.lightDepths[x + z * this.width] = y;
            
            // Notify listeners of light changes
            if (oldDepth != y) {
               int yl0 = oldDepth < y ? oldDepth : y;
               int yl1 = oldDepth > y ? oldDepth : y;

               for (int i = 0; i < this.levelListeners.size(); i++) {
                  this.levelListeners.get(i).lightColumnChanged(x, z, yl0, yl1);
               }
            }
         }
      }
   }

   /**
    * Registers a listener to be notified of level changes.
    * 
    * @param levelListener the listener to add
    */
   public void addListener(LevelListener levelListener) {
      this.levelListeners.add(levelListener);
   }

   /**
    * Unregisters a level change listener.
    * 
    * @param levelListener the listener to remove
    */
   public void removeListener(LevelListener levelListener) {
      this.levelListeners.remove(levelListener);
   }

   /**
    * Checks if the block at the given coordinates is solid.
    * 
    * @param x X coordinate
    * @param y Y coordinate (vertical)
    * @param z Z coordinate
    * @return true if block is solid, false if air or out of bounds
    */
   public boolean isTile(int x, int y, int z) {
      return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height
         ? this.blocks[(y * this.height + z) * this.width + x] == 1
         : false;
   }

   /**
    * Checks if the block at the given coordinates blocks collision/light.
    * 
    * @param x X coordinate
    * @param y Y coordinate (vertical)
    * @param z Z coordinate
    * @return true if block is solid/blocking
    */
   public boolean isSolidTile(int x, int y, int z) {
      return this.isTile(x, y, z);
   }

   /**
    * Checks if the block at the given coordinates blocks light.
    * 
    * @param x X coordinate
    * @param y Y coordinate (vertical)
    * @param z Z coordinate
    * @return true if block blocks light
    */
   public boolean isLightBlocker(int x, int y, int z) {
      return this.isSolidTile(x, y, z);
   }

   /**
    * Gets all solid block collision boxes within the given region.
    * Used for physics calculations and collision detection.
    * 
    * @param aABB bounding box to search within
    * @return list of collision boxes for solid blocks
    */
   public ArrayList<AABB> getCubes(AABB aABB) {
      ArrayList<AABB> aABBs = new ArrayList<>();
      
      // Convert float coordinates to block coordinates
      int x0 = (int)aABB.x0;
      int x1 = (int)(aABB.x1 + 1.0F);
      int y0 = (int)aABB.y0;
      int y1 = (int)(aABB.y1 + 1.0F);
      int z0 = (int)aABB.z0;
      int z1 = (int)(aABB.z1 + 1.0F);
      
      // Clamp to world bounds
      if (x0 < 0) {
         x0 = 0;
      }

      if (y0 < 0) {
         y0 = 0;
      }

      if (z0 < 0) {
         z0 = 0;
      }

      if (x1 > this.width) {
         x1 = this.width;
      }

      if (y1 > this.depth) {
         y1 = this.depth;
      }

      if (z1 > this.height) {
         z1 = this.height;
      }

      // Collect all solid block AABBs in the region
      for (int x = x0; x < x1; x++) {
         for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
               if (this.isSolidTile(x, y, z)) {
                  aABBs.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
               }
            }
         }
      }

      return aABBs;
   }

   /**
    * Gets the brightness value for a block based on lighting.
    * Blocks above the light surface are bright; blocks below are dark.
    * Out-of-bounds blocks return bright value.
    * 
    * @param x X coordinate
    * @param y Y coordinate (vertical)
    * @param z Z coordinate
    * @return brightness factor (0.8 for dark, 1.0 for bright)
    */
   public float getBrightness(int x, int y, int z) {
      float dark = 0.8F;
      float light = 1.0F;
      if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.depth || z >= this.height) {
         return light;
      } else {
         return y < this.lightDepths[x + z * this.width] ? dark : light;
      }
   }

   /**
    * Sets the block type at the given coordinates.
    * Updates lighting and notifies listeners of the change.
    * 
    * @param x X coordinate
    * @param y Y coordinate (vertical)
    * @param z Z coordinate
    * @param type block type (0 = air, 1 = solid)
    */
   public void setTile(int x, int y, int z, int type) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         this.blocks[(y * this.height + z) * this.width + x] = (byte)type;
         this.calcLightDepths(x, z, 1, 1);

         for (int i = 0; i < this.levelListeners.size(); i++) {
            this.levelListeners.get(i).tileChanged(x, y, z);
         }
      }
   }
}
