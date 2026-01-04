package com.mojang.rubydung;

import com.mojang.rubydung.level.Chunk;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelRenderer;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.swing.JOptionPane;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 * Main entry point and game engine for RubyDung.
 * 
 * Handles the game loop, rendering, input processing, and world interactions.
 * Uses LWJGL (Lightweight Java Game Library) for OpenGL rendering and window management.
 * The game world consists of a 3D voxel-based level that can be edited by placing and breaking blocks.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class RubyDung implements Runnable {
   /** Fullscreen mode toggle */
   private static final boolean FULLSCREEN_MODE = false;
   /** Display width in pixels */
   private int width;
   /** Display height in pixels */
   private int height;
   /** Fog color RGBA components (used for atmosphere effect) */
   private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
   /** Game timer maintaining 60 TPS (ticks per second) */
   private Timer timer = new Timer(60.0F);
   /** The game world/level containing all blocks and environment data */
   private Level level;
   /** Handles rendering of chunks and visible level geometry */
   private LevelRenderer levelRenderer;
   /** The player entity */
   private Player player;
   /** OpenGL viewport buffer for pick rendering */
   private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
   /** OpenGL selection buffer for block picking via raycasting */
   private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
   /** Result of the last block raycasting operation */
   private HitResult hitResult = null;

   /**
    * Initializes the OpenGL display, input devices, and game world.
    * Sets up the rendering context, creates the level, and spawns the player.
    * 
    * @throws LWJGLException if display creation fails
    * @throws IOException if level loading fails
    */
   public void init() throws LWJGLException, IOException {
      // Setup fog color from RGB integer
      int col = 920330;
      float fr = 0.5F;
      float fg = 0.8F;
      float fb = 1.0F;
      this.fogColor.put(new float[]{(col >> 16 & 0xFF) / 255.0F, (col >> 8 & 0xFF) / 255.0F, (col & 0xFF) / 255.0F, 1.0F});
      ((Buffer)this.fogColor).flip();
      
      // Create display window and input devices
      Display.setDisplayMode(new DisplayMode(1024, 768));
      Display.create();
      Keyboard.create();
      Mouse.create();
      this.width = Display.getDisplayMode().getWidth();
      this.height = Display.getDisplayMode().getHeight();
      
      // Configure OpenGL settings
      GL11.glEnable(3553);
      GL11.glShadeModel(7425);
      GL11.glClearColor(fr, fg, fb, 0.0F);
      GL11.glClearDepth(1.0);
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glMatrixMode(5888);
      
      // Initialize game world and player
      this.level = new Level(256, 256, 64);
      this.levelRenderer = new LevelRenderer(this.level);
      this.player = new Player(this.level);
      Mouse.setGrabbed(true);
   }

   /**
    * Cleans up resources when the game closes.
    * Saves the level and destroys input devices and display.
    */
   public void destroy() {
      this.level.save();
      Mouse.destroy();
      Keyboard.destroy();
      Display.destroy();
   }

   @Override
   public void run() {
      try {
         this.init();
      } catch (Exception var9) {
         JOptionPane.showMessageDialog(null, var9.toString(), "Failed to start RubyDung", 0);
         System.exit(0);
      }

      long lastTime = System.currentTimeMillis();
      int frames = 0;

      try {
         while (!Keyboard.isKeyDown(1) && !Display.isCloseRequested()) {
            this.timer.advanceTime();

            for (int i = 0; i < this.timer.ticks; i++) {
               this.tick();
            }

            this.render(this.timer.a);
            frames++;

            while (System.currentTimeMillis() >= lastTime + 1000L) {
               System.out.println(frames + " fps, " + Chunk.updates);
               Chunk.updates = 0;
               lastTime += 1000L;
               frames = 0;
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         this.destroy();
      }
   }

   /**
    * Updates game logic for one tick (1/60th of a second).
    * Called multiple times per frame depending on frame rate.
    */
   public void tick() {
      this.player.tick();
   }

   /**
    * Positions the camera at the player's location with proper rotation.
    * 
    * @param a interpolation factor between 0 and 1 for smooth animation
    */
   private void moveCameraToPlayer(float a) {
      GL11.glTranslatef(0.0F, 0.0F, -0.3F);
      GL11.glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
      float x = this.player.xo + (this.player.x - this.player.xo) * a;
      float y = this.player.yo + (this.player.y - this.player.yo) * a;
      float z = this.player.zo + (this.player.z - this.player.zo) * a;
      GL11.glTranslatef(-x, -y, -z);
   }

   /**
    * Configures the projection and view matrices for the main render pass.
    * Sets up perspective projection and positions the camera.
    * 
    * @param a interpolation factor for smooth camera movement
    */
   private void setupCamera(float a) {
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GLU.gluPerspective(70.0F, (float)this.width / this.height, 0.05F, 1000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      this.moveCameraToPlayer(a);
   }

   /**
    * Configures the camera for selection/picking mode (block raycasting).
    * Uses a small pick matrix for per-pixel hit detection.
    * 
    * @param a interpolation factor
    * @param x screen X coordinate of pick location
    * @param y screen Y coordinate of pick location
    */
   private void setupPickCamera(float a, int x, int y) {
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      ((Buffer)this.viewportBuffer).clear();
      GL11.glGetInteger(2978, this.viewportBuffer);
      ((Buffer)this.viewportBuffer).flip();
      ((Buffer)this.viewportBuffer).limit(16);
      GLU.gluPickMatrix(x, y, 5.0F, 5.0F, this.viewportBuffer);
      GLU.gluPerspective(70.0F, (float)this.width / this.height, 0.05F, 1000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      this.moveCameraToPlayer(a);
   }

   /**
    * Performs raycasting to determine which block the player is looking at.
    * Uses OpenGL selection rendering to find the closest block along the camera's ray.
    * 
    * @param a interpolation factor
    */
   private void pick(float a) {
      ((Buffer)this.selectBuffer).clear();
      GL11.glSelectBuffer(this.selectBuffer);
      GL11.glRenderMode(7170);
      this.setupPickCamera(a, this.width / 2, this.height / 2);
      this.levelRenderer.pick(this.player);
      int hits = GL11.glRenderMode(7168);
      ((Buffer)this.selectBuffer).flip();
      ((Buffer)this.selectBuffer).limit(this.selectBuffer.capacity());
      long closest = 0L;
      int[] names = new int[10];
      int hitNameCount = 0;

      for (int i = 0; i < hits; i++) {
         int nameCount = this.selectBuffer.get();
         long minZ = this.selectBuffer.get();
         this.selectBuffer.get();
         if (minZ >= closest && i != 0) {
            for (int j = 0; j < nameCount; j++) {
               this.selectBuffer.get();
            }
         } else {
            closest = minZ;
            hitNameCount = nameCount;

            for (int j = 0; j < nameCount; j++) {
               names[j] = this.selectBuffer.get();
            }
         }
      }

      if (hitNameCount > 0) {
         this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
      } else {
         this.hitResult = null;
      }
   }

   /**
    * Main render function called once per frame.
    * Handles input processing, block picking, and rendering the entire scene.
    * Supports placing/destroying blocks based on mouse clicks.
    * 
    * @param a interpolation factor for smooth entity animation
    */
   public void render(float a) {
      // Handle mouse look
      float xo = Mouse.getDX();
      float yo = Mouse.getDY();
      this.player.turn(xo, yo);
      this.pick(a);

      // Process mouse input (block placement/destruction)
      while (Mouse.next()) {
         if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && this.hitResult != null) {
            // Right click: destroy block
            this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
         }

         if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.hitResult != null) {
            // Left click: place block adjacent to hit face
            int x = this.hitResult.x;
            int y = this.hitResult.y;
            int z = this.hitResult.z;
            if (this.hitResult.f == 0) {
               y--;
            }

            if (this.hitResult.f == 1) {
               y++;
            }

            if (this.hitResult.f == 2) {
               z--;
            }

            if (this.hitResult.f == 3) {
               z++;
            }

            if (this.hitResult.f == 4) {
               x--;
            }

            if (this.hitResult.f == 5) {
               x++;
            }

            this.level.setTile(x, y, z, 1);
         }
      }

      // Process keyboard input (save level)
      while (Keyboard.next()) {
         if (Keyboard.getEventKey() == 28 && Keyboard.getEventKeyState()) {
            this.level.save();
         }
      }

      // Clear and setup rendering
      GL11.glClear(16640);
      this.setupCamera(a);
      GL11.glEnable(2884);
      GL11.glEnable(2912);
      GL11.glFogi(2917, 2048);
      GL11.glFogf(2914, 0.2F);
      GL11.glFog(2918, this.fogColor);
      
      // Render opaque geometry
      GL11.glDisable(2912);
      this.levelRenderer.render(this.player, 0);
      
      // Render transparent/depth-sorted geometry
      GL11.glEnable(2912);
      this.levelRenderer.render(this.player, 1);
      
      // Render block selection highlight
      GL11.glDisable(3553);
      if (this.hitResult != null) {
         this.levelRenderer.renderHit(this.hitResult);
      }

      GL11.glDisable(2912);
      Display.update();
   }

   /**
    * Checks for OpenGL errors and throws an exception if one occurred.
    * Useful for debugging rendering issues.
    * 
    * @throws IllegalStateException if an OpenGL error is detected
    */
   public static void checkError() {
      int e = GL11.glGetError();
      if (e != 0) {
         throw new IllegalStateException(GLU.gluErrorString(e));
      }
   }

   /**
    * Entry point for the RubyDung application.
    * Starts the game in a new thread.
    * 
    * @param args command line arguments (unused)
    * @throws LWJGLException if LWJGL initialization fails
    */
   public static void main(String[] args) throws LWJGLException {
      new Thread(new RubyDung()).start();
   }
}
