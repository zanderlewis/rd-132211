package com.mojang.rubydung.level;

/**
 * Represents a block type and handles its rendering.
 * 
 * Currently supports two tile types: rock (tex 0) and grass (tex 1).
 * Renders individual tile faces with proper lighting and texture coordinates.
 * Uses a layered approach for depth sorting (opaque vs transparent faces).
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Tile {
   /** Rock tile type (texture index 0) */
   public static Tile rock = new Tile(0);
   /** Grass tile type (texture index 1) */
   public static Tile grass = new Tile(1);
   /** Texture coordinate index for this tile type */
   private int tex = 0;

   /**
    * Creates a tile with the specified texture index.
    * 
    * @param tex texture coordinate index (0 or 1)
    */
   private Tile(int tex) {
      this.tex = tex;
   }

   /**
    * Renders all visible faces of this tile at the given position.
    * Skips faces adjacent to other solid tiles.
    * Handles lighting and depth sorting through layers.
    * 
    * @param t tesselator for geometry output
    * @param level the level containing this tile
    * @param layer 0 for opaque, 1 for transparent faces
    * @param x world X coordinate
    * @param y world Y coordinate (vertical)
    * @param z world Z coordinate
    */
   public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
      // Texture coordinates for this tile type
      float u0 = this.tex / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.0F;
      float v1 = v0 + 0.0624375F;
      
      // Brightness multipliers for each side
      float c1 = 1.0F;      // Top/bottom
      float c2 = 0.8F;      // Front/back
      float c3 = 0.6F;      // Left/right
      
      // Block vertex positions
      float x0 = x + 0.0F;
      float x1 = x + 1.0F;
      float y0 = y + 0.0F;
      float y1 = y + 1.0F;
      float z0 = z + 0.0F;
      float z1 = z + 1.0F;
      
      // Bottom face
      if (!level.isSolidTile(x, y - 1, z)) {
         float br = level.getBrightness(x, y - 1, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1);
            t.vertex(x0, y0, z1);
            t.tex(u0, v0);
            t.vertex(x0, y0, z0);
            t.tex(u1, v0);
            t.vertex(x1, y0, z0);
            t.tex(u1, v1);
            t.vertex(x1, y0, z1);
         }
      }

      // Top face
      if (!level.isSolidTile(x, y + 1, z)) {
         float br = level.getBrightness(x, y, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v1);
            t.vertex(x1, y1, z1);
            t.tex(u1, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v1);
            t.vertex(x0, y1, z1);
         }
      }

      // Front face (Z-)
      if (!level.isSolidTile(x, y, z - 1)) {
         float br = level.getBrightness(x, y, z - 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v1);
            t.vertex(x1, y0, z0);
            t.tex(u1, v1);
            t.vertex(x0, y0, z0);
         }
      }

      // Back face (Z+)
      if (!level.isSolidTile(x, y, z + 1)) {
         float br = level.getBrightness(x, y, z + 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v0);
            t.vertex(x0, y1, z1);
            t.tex(u0, v1);
            t.vertex(x0, y0, z1);
            t.tex(u1, v1);
            t.vertex(x1, y0, z1);
            t.tex(u1, v0);
            t.vertex(x1, y1, z1);
         }
      }

      // Left face (X-)
      if (!level.isSolidTile(x - 1, y, z)) {
         float br = level.getBrightness(x - 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0);
            t.vertex(x0, y1, z1);
            t.tex(u0, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v1);
            t.vertex(x0, y0, z0);
            t.tex(u1, v1);
            t.vertex(x0, y0, z1);
         }
      }

      // Right face (X+)
      if (!level.isSolidTile(x + 1, y, z)) {
         float br = level.getBrightness(x + 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1);
            t.vertex(x1, y0, z1);
            t.tex(u1, v1);
            t.vertex(x1, y0, z0);
            t.tex(u1, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v0);
            t.vertex(x1, y1, z1);
         }
      }
   }

   /**
    * Renders a single face of the tile.
    * Used for highlighting and selection rendering.
    * 
    * @param t tesselator for geometry output
    * @param x world X coordinate
    * @param y world Y coordinate (vertical)
    * @param z world Z coordinate
    * @param face which face to render (0-5: bottom, top, front, back, left, right)
    */
   public void renderFace(Tesselator t, int x, int y, int z, int face) {
      float x0 = x + 0.0F;
      float x1 = x + 1.0F;
      float y0 = y + 0.0F;
      float y1 = y + 1.0F;
      float z0 = z + 0.0F;
      float z1 = z + 1.0F;
      if (face == 0) {
         t.vertex(x0, y0, z1);
         t.vertex(x0, y0, z0);
         t.vertex(x1, y0, z0);
         t.vertex(x1, y0, z1);
      }

      if (face == 1) {
         t.vertex(x1, y1, z1);
         t.vertex(x1, y1, z0);
         t.vertex(x0, y1, z0);
         t.vertex(x0, y1, z1);
      }

      if (face == 2) {
         t.vertex(x0, y1, z0);
         t.vertex(x1, y1, z0);
         t.vertex(x1, y0, z0);
         t.vertex(x0, y0, z0);
      }

      if (face == 3) {
         t.vertex(x0, y1, z1);
         t.vertex(x0, y0, z1);
         t.vertex(x1, y0, z1);
         t.vertex(x1, y1, z1);
      }

      if (face == 4) {
         t.vertex(x0, y1, z1);
         t.vertex(x0, y1, z0);
         t.vertex(x0, y0, z0);
         t.vertex(x0, y0, z1);
      }

      if (face == 5) {
         t.vertex(x1, y0, z1);
         t.vertex(x1, y0, z0);
         t.vertex(x1, y1, z0);
         t.vertex(x1, y1, z1);
      }
   }
}
