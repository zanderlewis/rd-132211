package com.mojang.rubydung.level;

import com.mojang.rubydung.Textures;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.opengl.GL11;

/**
 * Represents a 16x16x16 chunk of the level for efficient rendering.
 * 
 * Chunks are the basic unit of geometry optimization. Each chunk pre-compiles
 * its geometry into OpenGL display lists for fast rendering.
 * Chunks are rebuilt when their contained blocks change.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Chunk {
   /** Bounding box of this chunk */
   public AABB aabb;
   /** Reference to the parent level */
   public final Level level;
   /** Minimum X coordinate (inclusive) */
   public final int x0;
   /** Minimum Y coordinate (inclusive) */
   public final int y0;
   /** Minimum Z coordinate (inclusive) */
   public final int z0;
   /** Maximum X coordinate (exclusive) */
   public final int x1;
   /** Maximum Y coordinate (exclusive) */
   public final int y1;
   /** Maximum Z coordinate (exclusive) */
   public final int z1;
   /** Whether this chunk needs geometry rebuild */
   private boolean dirty = true;
   /** OpenGL display list IDs (2 for opaque and transparent layers) */
   private int lists = -1;
   /** Terrain texture for all blocks */
   private static int texture = Textures.loadTexture("/terrain.png", 9728);
   /** Tesselator for building geometry */
   private static Tesselator t = new Tesselator();
   /** Number of chunks rebuilt this frame */
   public static int rebuiltThisFrame = 0;
   /** Total number of chunks updated (for statistics) */
   public static int updates = 0;

   /**
    * Creates a new chunk with the specified bounds.
    * 
    * @param level the parent level
    * @param x0 minimum X coordinate
    * @param y0 minimum Y coordinate
    * @param z0 minimum Z coordinate
    * @param x1 maximum X coordinate
    * @param y1 maximum Y coordinate
    * @param z1 maximum Z coordinate
    */
   public Chunk(Level level, int x0, int y0, int z0, int x1, int y1, int z1) {
      this.level = level;
      this.x0 = x0;
      this.y0 = y0;
      this.z0 = z0;
      this.x1 = x1;
      this.y1 = y1;
      this.z1 = z1;
      this.aabb = new AABB(x0, y0, z0, x1, y1, z1);
      this.lists = GL11.glGenLists(2);
   }

   /**
    * Rebuilds the display list for a specific layer.
    * Limits to 2 chunks rebuilt per frame for smooth performance.
    * 
    * @param layer 0 for solid faces, 1 for transparent faces
    */
   private void rebuild(int layer) {
      // Limit chunk rebuilds to 2 per frame for performance
      if (rebuiltThisFrame != 2) {
         this.dirty = false;
         updates++;
         rebuiltThisFrame++;
         
         // Start recording OpenGL display list
         GL11.glNewList(this.lists + layer, 4864);
         GL11.glEnable(3553);
         GL11.glBindTexture(3553, texture);
         t.init();
         int tiles = 0;

         // Build geometry for all blocks in chunk
         for (int x = this.x0; x < this.x1; x++) {
            for (int y = this.y0; y < this.y1; y++) {
               for (int z = this.z0; z < this.z1; z++) {
                  if (this.level.isTile(x, y, z)) {
                     // Use texture 0 (rock) for bottom layer, 1 (grass) for top
                     int tex = y == this.level.depth * 2 / 3 ? 0 : 1;
                     tiles++;
                     if (tex == 0) {
                        Tile.rock.render(t, this.level, layer, x, y, z);
                     } else {
                        Tile.grass.render(t, this.level, layer, x, y, z);
                     }
                  }
               }
            }
         }

         t.flush();
         GL11.glDisable(3553);
         GL11.glEndList();
      }
   }

   /**
    * Renders this chunk's display list.
    * Rebuilds geometry if marked dirty.
    * 
    * @param layer 0 for opaque faces, 1 for transparent faces
    */
   public void render(int layer) {
      if (this.dirty) {
         this.rebuild(0);
         this.rebuild(1);
      }

      GL11.glCallList(this.lists + layer);
   }

   /**
    * Marks this chunk as needing geometry rebuild.
    * Called when blocks change within the chunk.
    */
   public void setDirty() {
      this.dirty = true;
   }
}
