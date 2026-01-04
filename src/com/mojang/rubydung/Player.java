package com.mojang.rubydung;

import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.phys.AABB;
import java.util.List;
import org.lwjgl.input.Keyboard;

/**
 * Represents the player entity in the game world.
 * 
 * Handles player position, rotation, velocity, collision detection, and input processing.
 * The player is controlled with WASD/arrow keys for movement, space/left-ctrl for jumping,
 * and mouse movement for looking around.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Player {
   /** Reference to the game world for collision detection */
   private Level level;
   /** Previous frame X position (for interpolation) */
   public float xo;
   /** Previous frame Y position (for interpolation) */
   public float yo;
   /** Previous frame Z position (for interpolation) */
   public float zo;
   /** Current X position */
   public float x;
   /** Current Y position */
   public float y;
   /** Current Z position */
   public float z;
   /** X velocity */
   public float xd;
   /** Y velocity (vertical) */
   public float yd;
   /** Z velocity */
   public float zd;
   /** Yaw rotation (horizontal) in degrees */
   public float yRot;
   /** Pitch rotation (vertical) in degrees, clamped to [-90, 90] */
   public float xRot;
   /** Axis-aligned bounding box for collision detection */
   public AABB bb;
   /** Whether the player is standing on solid ground */
   public boolean onGround = false;

   /**
    * Creates a new player in the specified level.
    * Spawns at a random X/Z position above the world.
    * 
    * @param level the game world to spawn in
    */
   public Player(Level level) {
      this.level = level;
      this.resetPos();
   }

   /**
    * Resets the player to a random spawn position above the world.
    */
   private void resetPos() {
      float x = (float)Math.random() * this.level.width;
      float y = this.level.depth + 10;
      float z = (float)Math.random() * this.level.height;
      this.setPos(x, y, z);
   }

   /**
    * Sets the player position and updates the collision bounding box.
    * 
    * @param x world X coordinate
    * @param y world Y coordinate
    * @param z world Z coordinate
    */
   private void setPos(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
      float w = 0.3F;
      float h = 0.9F;
      this.bb = new AABB(x - w, y - h, z - w, x + w, y + h, z + w);
   }

   /**
    * Adjusts player rotation based on mouse movement.
    * Pitch (xRot) is clamped to prevent over-rotation.
    * 
    * @param xo mouse X delta
    * @param yo mouse Y delta
    */
   public void turn(float xo, float yo) {
      this.yRot = (float)(this.yRot + xo * 0.15);
      this.xRot = (float)(this.xRot - yo * 0.15);
      if (this.xRot < -90.0F) {
         this.xRot = -90.0F;
      }

      if (this.xRot > 90.0F) {
         this.xRot = 90.0F;
      }
   }

   /**
    * Updates player physics and input for one game tick.
    * 
    * Handles:
    * - Keyboard input processing
    * - Velocity and gravity calculations
    * - Collision detection and response
    * - Ground contact detection
    */
   public void tick() {
      // Store previous position for interpolation
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      float xa = 0.0F;
      float ya = 0.0F;
      
      // Reset position on R key
      if (Keyboard.isKeyDown(19)) {
         this.resetPos();
      }

      // Directional input (forward/backward)
      if (Keyboard.isKeyDown(200) || Keyboard.isKeyDown(17)) {
         ya--;
      }

      if (Keyboard.isKeyDown(208) || Keyboard.isKeyDown(31)) {
         ya++;
      }

      // Directional input (left/right)
      if (Keyboard.isKeyDown(203) || Keyboard.isKeyDown(30)) {
         xa--;
      }

      if (Keyboard.isKeyDown(205) || Keyboard.isKeyDown(32)) {
         xa++;
      }

      // Jump input
      if ((Keyboard.isKeyDown(57) || Keyboard.isKeyDown(219)) && this.onGround) {
         this.yd = 0.12F;
      }

      // Apply acceleration based on ground state
      this.moveRelative(xa, ya, this.onGround ? 0.02F : 0.005F);
      
      // Apply gravity
      this.yd = (float)(this.yd - 0.005);
      
      // Physics update with collision
      this.move(this.xd, this.yd, this.zd);
      
      // Friction and drag
      this.xd *= 0.91F;
      this.yd *= 0.98F;
      this.zd *= 0.91F;
      if (this.onGround) {
         this.xd *= 0.8F;
         this.zd *= 0.8F;
      }
   }

   /**
    * Moves the player with full collision detection.
    * Uses axis-aligned collision to prevent moving through blocks.
    * Updates velocity to zero for blocked directions.
    * 
    * @param xa X displacement
    * @param ya Y displacement (vertical)
    * @param za Z displacement
    */
   public void move(float xa, float ya, float za) {
      float xaOrg = xa;
      float yaOrg = ya;
      float zaOrg = za;
      
      // Get all solid blocks in the movement region
      List<AABB> aABBs = this.level.getCubes(this.bb.expand(xa, ya, za));

      // Collide Y (vertical) first
      for (int i = 0; i < aABBs.size(); i++) {
         ya = aABBs.get(i).clipYCollide(this.bb, ya);
      }
      this.bb.move(0.0F, ya, 0.0F);

      // Then collide X
      for (int i = 0; i < aABBs.size(); i++) {
         xa = aABBs.get(i).clipXCollide(this.bb, xa);
      }
      this.bb.move(xa, 0.0F, 0.0F);

      // Finally collide Z
      for (int i = 0; i < aABBs.size(); i++) {
         za = aABBs.get(i).clipZCollide(this.bb, za);
      }
      this.bb.move(0.0F, 0.0F, za);
      
      // Check if player is on ground
      this.onGround = yaOrg != ya && yaOrg < 0.0F;
      
      // Zero out velocity for blocked directions
      if (xaOrg != xa) {
         this.xd = 0.0F;
      }

      if (yaOrg != ya) {
         this.yd = 0.0F;
      }

      if (zaOrg != za) {
         this.zd = 0.0F;
      }

      // Update player position from bounding box
      this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
      this.y = this.bb.y0 + 1.62F;
      this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
   }

   /**
    * Applies movement relative to where the player is looking.
    * Rotates input based on yaw and applies speed normalization.
    * 
    * @param xa forward/backward input
    * @param za left/right input
    * @param speed movement speed multiplier
    */
   public void moveRelative(float xa, float za, float speed) {
      float dist = xa * xa + za * za;
      if (!(dist < 0.01F)) {
         dist = speed / (float)Math.sqrt(dist);
         xa *= dist;
         za *= dist;
         float sin = (float)Math.sin(this.yRot * Math.PI / 180.0);
         float cos = (float)Math.cos(this.yRot * Math.PI / 180.0);
         this.xd += xa * cos - za * sin;
         this.zd += za * cos + xa * sin;
      }
   }
}
