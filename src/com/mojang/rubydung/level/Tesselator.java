package com.mojang.rubydung.level;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Vertex tesselator for efficient geometry submission.
 * 
 * Buffers vertices, texture coordinates, and colors for submission to OpenGL
 * using vertex arrays. Automatically flushes when capacity is reached.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Tesselator {
   /** Maximum vertices per flush */
   private static final int MAX_VERTICES = 100000;
   /** Vertex position buffer (x, y, z per vertex) */
   private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(300000);
   /** Texture coordinate buffer (u, v per vertex) */
   private FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(200000);
   /** Vertex color buffer (r, g, b per vertex) */
   private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(300000);
   /** Number of vertices currently buffered */
   private int vertices = 0;
   /** Current texture U coordinate */
   private float u;
   /** Current texture V coordinate */
   private float v;
   /** Current color red component */
   private float r;
   /** Current color green component */
   private float g;
   /** Current color blue component */
   private float b;
   /** Whether color data is being used */
   private boolean hasColor = false;
   /** Whether texture coordinate data is being used */
   private boolean hasTexture = false;

   /**
    * Submits all buffered geometry to OpenGL and clears the buffer.
    * Sets up vertex arrays and draws all buffered vertices as quads.
    */
   public void flush() {
      // Prepare buffers for submission
      ((Buffer)this.vertexBuffer).flip();
      ((Buffer)this.texCoordBuffer).flip();
      ((Buffer)this.colorBuffer).flip();
      
      // Set up vertex arrays
      GL11.glVertexPointer(3, 0, this.vertexBuffer);
      if (this.hasTexture) {
         GL11.glTexCoordPointer(2, 0, this.texCoordBuffer);
      }

      if (this.hasColor) {
         GL11.glColorPointer(3, 0, this.colorBuffer);
      }

      // Enable appropriate client states
      GL11.glEnableClientState(32884);
      if (this.hasTexture) {
         GL11.glEnableClientState(32888);
      }

      if (this.hasColor) {
         GL11.glEnableClientState(32886);
      }

      // Draw all vertices as quads
      GL11.glDrawArrays(7, 0, this.vertices);
      
      // Disable client states
      GL11.glDisableClientState(32884);
      if (this.hasTexture) {
         GL11.glDisableClientState(32888);
      }

      if (this.hasColor) {
         GL11.glDisableClientState(32886);
      }

      this.clear();
   }

   private void clear() {
      this.vertices = 0;
      ((Buffer)this.vertexBuffer).clear();
      ((Buffer)this.texCoordBuffer).clear();
      ((Buffer)this.colorBuffer).clear();
   }

   /**
    * Resets the tesselator for a new batch of geometry.
    * Clears all buffers and flags.
    */
   public void init() {
      this.clear();
      this.hasColor = false;
      this.hasTexture = false;
   }

   /**
    * Sets the texture coordinate for the next vertex.
    * 
    * @param u texture U coordinate
    * @param v texture V coordinate
    */
   public void tex(float u, float v) {
      this.hasTexture = true;
      this.u = u;
      this.v = v;
   }

   /**
    * Sets the color for the next vertex.
    * 
    * @param r red component (0.0 to 1.0)
    * @param g green component (0.0 to 1.0)
    * @param b blue component (0.0 to 1.0)
    */
   public void color(float r, float g, float b) {
      this.hasColor = true;
      this.r = r;
      this.g = g;
      this.b = b;
   }

   /**
    * Adds a vertex with the current position, texture, and color settings.
    * Automatically flushes if MAX_VERTICES is reached.
    * 
    * @param x world X coordinate
    * @param y world Y coordinate
    * @param z world Z coordinate
    */
   public void vertex(float x, float y, float z) {
      this.vertexBuffer.put(this.vertices * 3 + 0, x).put(this.vertices * 3 + 1, y).put(this.vertices * 3 + 2, z);
      if (this.hasTexture) {
         this.texCoordBuffer.put(this.vertices * 2 + 0, this.u).put(this.vertices * 2 + 1, this.v);
      }

      if (this.hasColor) {
         this.colorBuffer.put(this.vertices * 3 + 0, this.r).put(this.vertices * 3 + 1, this.g).put(this.vertices * 3 + 2, this.b);
      }

      this.vertices++;
      if (this.vertices == 100000) {
         this.flush();
      }
   }
}
