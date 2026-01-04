package com.mojang.rubydung.level;

import com.mojang.rubydung.phys.AABB;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * View frustum for frustum culling optimization.
 * 
 * Calculates the 6 planes of the camera's view frustum from the current
 * projection and model-view matrices. Used to quickly determine if chunks
 * are visible before rendering.
 * 
 * @author Mojang
 * @version RD-132211
 */
public class Frustum {
   /** Frustum plane equations (6 planes, 4 coefficients each) */
   public float[][] m_Frustum = new float[6][4];
   /** Right plane constant */
   public static final int RIGHT = 0;
   /** Left plane constant */
   public static final int LEFT = 1;
   /** Bottom plane constant */
   public static final int BOTTOM = 2;
   /** Top plane constant */
   public static final int TOP = 3;
   /** Back plane constant */
   public static final int BACK = 4;
   /** Front plane constant */
   public static final int FRONT = 5;
   /** Plane equation coefficient A (x component of normal) */
   public static final int A = 0;
   /** Plane equation coefficient B (y component of normal) */
   public static final int B = 1;
   /** Plane equation coefficient C (z component of normal) */
   public static final int C = 2;
   /** Plane equation coefficient D (distance from origin) */
   public static final int D = 3;
   /** Singleton frustum instance */
   private static Frustum frustum = new Frustum();
   /** OpenGL projection matrix buffer */
   private FloatBuffer _proj = BufferUtils.createFloatBuffer(16);
   /** OpenGL model-view matrix buffer */
   private FloatBuffer _modl = BufferUtils.createFloatBuffer(16);
   /** Clipping matrix (projection * model-view) */
   private FloatBuffer _clip = BufferUtils.createFloatBuffer(16);
   /** Projection matrix array */
   float[] proj = new float[16];
   /** Model-view matrix array */
   float[] modl = new float[16];
   /** Clipping matrix array */
   float[] clip = new float[16];

   private Frustum() {
   }

   /**
    * Gets the singleton frustum instance and recalculates it.
    * Should be called once per frame after the camera is set up.
    * 
    * @return the frustum calculated from current OpenGL matrices
    */
   public static Frustum getFrustum() {
      frustum.calculateFrustum();
      return frustum;
   }

   private void normalizePlane(float[][] frustum, int side) {
      float magnitude = (float)Math.sqrt(frustum[side][0] * frustum[side][0] + frustum[side][1] * frustum[side][1] + frustum[side][2] * frustum[side][2]);
      frustum[side][0] = frustum[side][0] / magnitude;
      frustum[side][1] = frustum[side][1] / magnitude;
      frustum[side][2] = frustum[side][2] / magnitude;
      frustum[side][3] = frustum[side][3] / magnitude;
   }

   private void calculateFrustum() {
      // Clear buffers
      ((Buffer)this._proj).clear();
      ((Buffer)this._modl).clear();
      ((Buffer)this._clip).clear();
      
      // Get current OpenGL matrices
      GL11.glGetFloat(2983, this._proj);
      GL11.glGetFloat(2982, this._modl);
      ((Buffer)this._proj).flip().limit(16);
      this._proj.get(this.proj);
      ((Buffer)this._modl).flip().limit(16);
      this._modl.get(this.modl);
      
      // Compute clip matrix = projection * modelview (matrix multiplication)
      this.clip[0] = this.modl[0] * this.proj[0] + this.modl[1] * this.proj[4] + this.modl[2] * this.proj[8] + this.modl[3] * this.proj[12];
      this.clip[1] = this.modl[0] * this.proj[1] + this.modl[1] * this.proj[5] + this.modl[2] * this.proj[9] + this.modl[3] * this.proj[13];
      this.clip[2] = this.modl[0] * this.proj[2] + this.modl[1] * this.proj[6] + this.modl[2] * this.proj[10] + this.modl[3] * this.proj[14];
      this.clip[3] = this.modl[0] * this.proj[3] + this.modl[1] * this.proj[7] + this.modl[2] * this.proj[11] + this.modl[3] * this.proj[15];
      this.clip[4] = this.modl[4] * this.proj[0] + this.modl[5] * this.proj[4] + this.modl[6] * this.proj[8] + this.modl[7] * this.proj[12];
      this.clip[5] = this.modl[4] * this.proj[1] + this.modl[5] * this.proj[5] + this.modl[6] * this.proj[9] + this.modl[7] * this.proj[13];
      this.clip[6] = this.modl[4] * this.proj[2] + this.modl[5] * this.proj[6] + this.modl[6] * this.proj[10] + this.modl[7] * this.proj[14];
      this.clip[7] = this.modl[4] * this.proj[3] + this.modl[5] * this.proj[7] + this.modl[6] * this.proj[11] + this.modl[7] * this.proj[15];
      this.clip[8] = this.modl[8] * this.proj[0] + this.modl[9] * this.proj[4] + this.modl[10] * this.proj[8] + this.modl[11] * this.proj[12];
      this.clip[9] = this.modl[8] * this.proj[1] + this.modl[9] * this.proj[5] + this.modl[10] * this.proj[9] + this.modl[11] * this.proj[13];
      this.clip[10] = this.modl[8] * this.proj[2] + this.modl[9] * this.proj[6] + this.modl[10] * this.proj[10] + this.modl[11] * this.proj[14];
      this.clip[11] = this.modl[8] * this.proj[3] + this.modl[9] * this.proj[7] + this.modl[10] * this.proj[11] + this.modl[11] * this.proj[15];
      this.clip[12] = this.modl[12] * this.proj[0] + this.modl[13] * this.proj[4] + this.modl[14] * this.proj[8] + this.modl[15] * this.proj[12];
      this.clip[13] = this.modl[12] * this.proj[1] + this.modl[13] * this.proj[5] + this.modl[14] * this.proj[9] + this.modl[15] * this.proj[13];
      this.clip[14] = this.modl[12] * this.proj[2] + this.modl[13] * this.proj[6] + this.modl[14] * this.proj[10] + this.modl[15] * this.proj[14];
      this.clip[15] = this.modl[12] * this.proj[3] + this.modl[13] * this.proj[7] + this.modl[14] * this.proj[11] + this.modl[15] * this.proj[15];
      
      // Extract frustum plane equations from clip matrix
      // Right plane (x > 0)
      this.m_Frustum[0][0] = this.clip[3] - this.clip[0];
      this.m_Frustum[0][1] = this.clip[7] - this.clip[4];
      this.m_Frustum[0][2] = this.clip[11] - this.clip[8];
      this.m_Frustum[0][3] = this.clip[15] - this.clip[12];
      this.normalizePlane(this.m_Frustum, 0);
      
      // Left plane (x < 0)
      this.m_Frustum[1][0] = this.clip[3] + this.clip[0];
      this.m_Frustum[1][1] = this.clip[7] + this.clip[4];
      this.m_Frustum[1][2] = this.clip[11] + this.clip[8];
      this.m_Frustum[1][3] = this.clip[15] + this.clip[12];
      this.normalizePlane(this.m_Frustum, 1);
      
      // Bottom plane (y < 0)
      this.m_Frustum[2][0] = this.clip[3] + this.clip[1];
      this.m_Frustum[2][1] = this.clip[7] + this.clip[5];
      this.m_Frustum[2][2] = this.clip[11] + this.clip[9];
      this.m_Frustum[2][3] = this.clip[15] + this.clip[13];
      this.normalizePlane(this.m_Frustum, 2);
      
      // Top plane (y > 0)
      this.m_Frustum[3][0] = this.clip[3] - this.clip[1];
      this.m_Frustum[3][1] = this.clip[7] - this.clip[5];
      this.m_Frustum[3][2] = this.clip[11] - this.clip[9];
      this.m_Frustum[3][3] = this.clip[15] - this.clip[13];
      this.normalizePlane(this.m_Frustum, 3);
      
      // Back plane (z < 0)
      this.m_Frustum[4][0] = this.clip[3] - this.clip[2];
      this.m_Frustum[4][1] = this.clip[7] - this.clip[6];
      this.m_Frustum[4][2] = this.clip[11] - this.clip[10];
      this.m_Frustum[4][3] = this.clip[15] - this.clip[14];
      this.normalizePlane(this.m_Frustum, 4);
      
      // Front plane (z > 0)
      this.m_Frustum[5][0] = this.clip[3] + this.clip[2];
      this.m_Frustum[5][1] = this.clip[7] + this.clip[6];
      this.m_Frustum[5][2] = this.clip[11] + this.clip[10];
      this.m_Frustum[5][3] = this.clip[15] + this.clip[14];
      this.normalizePlane(this.m_Frustum, 5);
   }

   public boolean pointInFrustum(float x, float y, float z) {
      for (int i = 0; i < 6; i++) {
         if (this.m_Frustum[i][0] * x + this.m_Frustum[i][1] * y + this.m_Frustum[i][2] * z + this.m_Frustum[i][3] <= 0.0F) {
            return false;
         }
      }

      return true;
   }

   public boolean sphereInFrustum(float x, float y, float z, float radius) {
      for (int i = 0; i < 6; i++) {
         if (this.m_Frustum[i][0] * x + this.m_Frustum[i][1] * y + this.m_Frustum[i][2] * z + this.m_Frustum[i][3] <= -radius) {
            return false;
         }
      }

      return true;
   }

   public boolean cubeFullyInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
      for (int i = 0; i < 6; i++) {
         if (!(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }

         if (!(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }
      }

      return true;
   }

   public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
      for (int i = 0; i < 6; i++) {
         if (!(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z1 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y1 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x1 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)
            && !(this.m_Frustum[i][0] * x2 + this.m_Frustum[i][1] * y2 + this.m_Frustum[i][2] * z2 + this.m_Frustum[i][3] > 0.0F)) {
            return false;
         }
      }

      return true;
   }

   public boolean cubeInFrustum(AABB aabb) {
      return this.cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
   }
}
