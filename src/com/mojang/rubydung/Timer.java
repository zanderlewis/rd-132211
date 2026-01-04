package com.mojang.rubydung;

/**
 * Manages game timing and frame-rate independent updates.
 * 
 * Maintains a fixed tick rate (typically 60 TPS) while allowing variable frame rates.
 * Calculates interpolation factor for smooth animation between physics frames.
 * Clamps updates to prevent spiraling in case of frame drops.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Timer {
   /** Nanoseconds per second */
   private static final long NS_PER_SECOND = 1000000000L;
   /** Maximum nanoseconds per update (1 second) to prevent spiraling */
   private static final long MAX_NS_PER_UPDATE = 1000000000L;
   /** Maximum ticks per frame to prevent catch-up spiraling */
   private static final int MAX_TICKS_PER_UPDATE = 100;
   /** Target ticks per second (physics simulation frequency) */
   private float ticksPerSecond;
   /** Time of last update call */
   private long lastTime;
   /** Number of ticks that should be processed this frame */
   public int ticks;
   /** Interpolation factor (0 to 1) for smooth rendering between ticks */
   public float a;
   /** Time scale multiplier (affects game speed) */
   public float timeScale = 1.0F;
   /** Current frames per second */
   public float fps = 0.0F;
   /** Accumulated time not yet processed as a tick */
   public float passedTime = 0.0F;

   /**
    * Creates a new timer with the specified target tick rate.
    * 
    * @param ticksPerSecond target simulation frequency (typically 60)
    */
   public Timer(float ticksPerSecond) {
      this.ticksPerSecond = ticksPerSecond;
      this.lastTime = System.nanoTime();
   }

   /**
    * Advances the timer and calculates ticks and interpolation.
    * Should be called once per frame.
    * 
    * Sets the number of ticks to process and interpolation factor 'a'.
    */
   public void advanceTime() {
      // Calculate elapsed time in nanoseconds
      long now = System.nanoTime();
      long passedNs = now - this.lastTime;
      this.lastTime = now;
      
      // Handle time backwards (system clock adjustment)
      if (passedNs < 0L) {
         passedNs = 0L;
      }

      // Cap maximum time per frame to prevent spiraling
      if (passedNs > 1000000000L) {
         passedNs = 1000000000L;
      }

      // Calculate current FPS
      this.fps = (float)(1000000000L / passedNs);
      
      // Accumulate time as fractional ticks
      this.passedTime = this.passedTime + (float)passedNs * this.timeScale * this.ticksPerSecond / 1.0E9F;
      
      // Extract whole ticks
      this.ticks = (int)this.passedTime;
      
      // Cap ticks per frame to prevent catch-up spiraling
      if (this.ticks > 100) {
         this.ticks = 100;
      }

      // Keep fractional part for next frame
      this.passedTime = this.passedTime - this.ticks;
      
      // Interpolation factor (0 to 1) for smooth rendering
      this.a = this.passedTime;
   }
}
