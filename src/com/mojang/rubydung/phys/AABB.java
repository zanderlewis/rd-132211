package com.mojang.rubydung.phys;

/**
 * Axis-Aligned Bounding Box for collision detection.
 * 
 * Represents a 3D rectangular volume used for physics, collision,
 * and spatial queries. Provides collision clipping and intersection tests.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class AABB {
   /** Collision epsilon for floating-point precision */
   private float epsilon = 0.0F;
   /** Minimum X coordinate */
   public float x0;
   /** Minimum Y coordinate */
   public float y0;
   /** Minimum Z coordinate */
   public float z0;
   /** Maximum X coordinate */
   public float x1;
   /** Maximum Y coordinate */
   public float y1;
   /** Maximum Z coordinate */
   public float z1;

   /**
    * Creates a bounding box with the specified extents.
    * 
    * @param x0 minimum X coordinate
    * @param y0 minimum Y coordinate
    * @param z0 minimum Z coordinate
    * @param x1 maximum X coordinate
    * @param y1 maximum Y coordinate
    * @param z1 maximum Z coordinate
    */
   public AABB(float x0, float y0, float z0, float x1, float y1, float z1) {
      this.x0 = x0;
      this.y0 = y0;
      this.z0 = z0;
      this.x1 = x1;
      this.y1 = y1;
      this.z1 = z1;
   }

   /**
    * Creates a new AABB expanded in the direction of movement.
    * Used for finding collisions along a movement vector.
    * 
    * @param xa X expansion amount (can be negative)
    * @param ya Y expansion amount (can be negative)
    * @param za Z expansion amount (can be negative)
    * @return new AABB encompassing the expanded region
    */
   public AABB expand(float xa, float ya, float za) {
      float _x0 = this.x0;
      float _y0 = this.y0;
      float _z0 = this.z0;
      float _x1 = this.x1;
      float _y1 = this.y1;
      float _z1 = this.z1;
      
      // Expand in direction of movement
      if (xa < 0.0F) {
         _x0 += xa;
      }

      if (xa > 0.0F) {
         _x1 += xa;
      }

      if (ya < 0.0F) {
         _y0 += ya;
      }

      if (ya > 0.0F) {
         _y1 += ya;
      }

      if (za < 0.0F) {
         _z0 += za;
      }

      if (za > 0.0F) {
         _z1 += za;
      }

      return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
   }

   /**
    * Creates a new AABB grown uniformly in all directions.
    * 
    * @param xa expansion in X direction
    * @param ya expansion in Y direction
    * @param za expansion in Z direction
    * @return new AABB that is larger on all sides
    */
   public AABB grow(float xa, float ya, float za) {
      float _x0 = this.x0 - xa;
      float _y0 = this.y0 - ya;
      float _z0 = this.z0 - za;
      float _x1 = this.x1 + xa;
      float _y1 = this.y1 + ya;
      float _z1 = this.z1 + za;
      return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
   }

   /**
    * Clips X movement to prevent collision with this AABB.
    * Calculates the maximum safe X displacement without intersecting.
    * 
    * @param c the moving AABB
    * @param xa proposed X movement
    * @return clipped X movement (may be less than proposed if collision detected)
    */
   public float clipXCollide(AABB c, float xa) {
      // Not intersecting in Y or Z, can move freely
      if (c.y1 <= this.y0 || c.y0 >= this.y1) {
         return xa;
      } else if (!(c.z1 <= this.z0) && !(c.z0 >= this.z1)) {
         // Moving right, colliding with left side of this AABB
         if (xa > 0.0F && c.x1 <= this.x0) {
            float max = this.x0 - c.x1 - this.epsilon;
            if (max < xa) {
               xa = max;
            }
         }

         // Moving left, colliding with right side of this AABB
         if (xa < 0.0F && c.x0 >= this.x1) {
            float max = this.x1 - c.x0 + this.epsilon;
            if (max > xa) {
               xa = max;
            }
         }

         return xa;
      } else {
         return xa;
      }
   }

   /**
    * Clips Y movement to prevent collision with this AABB.
    * Calculates the maximum safe Y displacement without intersecting.
    * 
    * @param c the moving AABB
    * @param ya proposed Y movement
    * @return clipped Y movement (may be less than proposed if collision detected)
    */
   public float clipYCollide(AABB c, float ya) {
      // Not intersecting in X or Z, can move freely
      if (c.x1 <= this.x0 || c.x0 >= this.x1) {
         return ya;
      } else if (!(c.z1 <= this.z0) && !(c.z0 >= this.z1)) {
         // Moving up, colliding with bottom of this AABB
         if (ya > 0.0F && c.y1 <= this.y0) {
            float max = this.y0 - c.y1 - this.epsilon;
            if (max < ya) {
               ya = max;
            }
         }

         // Moving down, colliding with top of this AABB
         if (ya < 0.0F && c.y0 >= this.y1) {
            float max = this.y1 - c.y0 + this.epsilon;
            if (max > ya) {
               ya = max;
            }
         }

         return ya;
      } else {
         return ya;
      }
   }

   /**
    * Clips Z movement to prevent collision with this AABB.
    * Calculates the maximum safe Z displacement without intersecting.
    * 
    * @param c the moving AABB
    * @param za proposed Z movement
    * @return clipped Z movement (may be less than proposed if collision detected)
    */
   public float clipZCollide(AABB c, float za) {
      // Not intersecting in X or Y, can move freely
      if (c.x1 <= this.x0 || c.x0 >= this.x1) {
         return za;
      } else if (!(c.y1 <= this.y0) && !(c.y0 >= this.y1)) {
         // Moving forward, colliding with back of this AABB
         if (za > 0.0F && c.z1 <= this.z0) {
            float max = this.z0 - c.z1 - this.epsilon;
            if (max < za) {
               za = max;
            }
         }

         // Moving backward, colliding with front of this AABB
         if (za < 0.0F && c.z0 >= this.z1) {
            float max = this.z1 - c.z0 + this.epsilon;
            if (max > za) {
               za = max;
            }
         }

         return za;
      } else {
         return za;
      }
   }

   /**
    * Tests if this AABB intersects with another AABB.
    * 
    * @param c the other AABB to test
    * @return true if the AABBs overlap
    */
   public boolean intersects(AABB c) {
      if (c.x1 <= this.x0 || c.x0 >= this.x1) {
         return false;
      } else {
         return c.y1 <= this.y0 || c.y0 >= this.y1 ? false : !(c.z1 <= this.z0) && !(c.z0 >= this.z1);
      }
   }

   /**
    * Translates this AABB by the specified amount.
    * 
    * @param xa X translation
    * @param ya Y translation
    * @param za Z translation
    */
   public void move(float xa, float ya, float za) {
      this.x0 += xa;
      this.y0 += ya;
      this.z0 += za;
      this.x1 += xa;
      this.y1 += ya;
      this.z1 += za;
   }
}
