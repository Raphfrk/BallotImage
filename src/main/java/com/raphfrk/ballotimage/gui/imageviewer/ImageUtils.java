package com.raphfrk.ballotimage.gui.imageviewer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ImageUtils {
	
	public static float[][] zoomNearest(float[][] in, int zoom) {
		int sy = in.length;
		int sx = in[0].length;
		
		int zy = sy * zoom;
		int zx = sx * zoom;
		
		float[][] ret = new float[zy][zx];
		
		int ix = -1;
		int iy = -1;
		
		for (int x = 0; x < zx; x++) {
			if (x % zoom == 0) {
				ix++;
			}
			iy = -1;
			for (int y = 0; y < zy; y++) {
				if (y % zoom == 0) {
					iy++;
				}
				ret[y][x] = in[iy][ix];
			}
		}
		return ret;
	}
	
	public static float[][] filter(float[][] in, float[][] filter) {
		
		int sy = in.length;
		int sx = in[0].length;
		
		float[][] ret = new float[sy][sx];
		
		int fsy = filter.length;
		int fsx = filter[0].length;
		
		if (fsy % 2 == 0 || fsx % 2 == 0) {
			return null;
		}
		
		int fsum = 0;
		for (int fx = 0; fx < fsx; fx++) {
			for (int fy = 0; fy < fsy; fy++) {
				fsum += filter[fy][fx];
			}
		}
		
		int fcx = (fsx >> 1);
		int fcy = (fsy >> 1);
		
		for (int x = 0; x < sx; x++) {
			for (int y = 0; y < sy; y++) {
				float sum = 0;
				for (int fx = 0; fx < fsx; fx++) {
					for (int fy = 0; fy < fsy; fy++) {
						int xo = fx + x - fcx;
						int yo = fy + y - fcy;
						if (xo < 0 || xo >= sx || yo < 0 || yo >= sy) {
							continue;
						}
						sum += in[yo][xo] * filter[fx][fy];
					}
				}
				sum /= fsum;
				float result = 0;
				if (sum > 255) {
					result = 255;
				} else if (sum < 0) {
					result = 0;
				} else {
					result = sum;
				}
				ret[y][x] = result;
			}
		}
		
		return ret;
	}
	
	public static float[][] genCircleFilter(int radius) {
		int size = radius * 2 + 1;
		int radiusSquared = radius * radius;
		float[][] ret = new float[size][size];
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				if (x * x + y * y <= radiusSquared) {
					ret[y + radius][x + radius] = 1;
				} else {
					ret[y + radius][x + radius] = 0;
				}
			}
		}
		return ret;
	}

	/**
	 * Expands/shrinks the given image data to fit in a tx * ty array, keeping the aspect ratio fixed
	 * 
	 * @param n the order
	 * @param data the input data
	 * @param tx the x dimension of the output array
	 * @param ty the y dimension of the output array
	 * @return
	 */
	public static float[][] splineFit(int n, float[][] data, int tx, int ty) {
		return splineFit(n, data, tx, ty, true);
	}
	
	/**
	 * Expands/shrinks the given image data to fit in a tx * ty array.
	 * 
	 * @param n the order
	 * @param data the input data
	 * @param tx the x dimension of the output array
	 * @param ty the y dimension of the output array
	 * @param lockAspectRatio true to keep the aspect ratio fixed
	 * @return
	 */
	public static float[][] splineFit(int n, float[][] data, int tx, int ty, boolean lockAspectRatio) {
		
		float zx = ((float) tx) / data[0].length;
		float zy = ((float) ty) / data.length;
		
		if (lockAspectRatio) {
			if (zx > zy) {
				zx = zy;
			} else {
				zy = zx;
			}
		}
		
		float[][] out = new float[ty][tx];
		
		float izy = 1.0F / zy;
		float izx = 1.0F / zx;

		for (int y = 0; y < ty; y++) {
			float py = y * izy;
			float[] row = out[y];
			for (int x = 0; x < tx; x++) {
				float px = x * izx;
				row[x] = spline2DInterpolate(n, data, px, py);
			}
		}
		
		return out;

	}
	
	/**
	 * Computes the interpolated value at coordinates (x, y) using nth order Spline interpolation
	 * 
	 * @param n the order
	 * @param data the input data
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return
	 */
	public static float spline2DInterpolate(int n, float[][] data, float x, float y) {
		
		float[] temp = new float[n + 1];
		
		int yy = (int) Math.floor(y);
		float dy = y - yy;
		
		int iy = getFirstSplineIndex(n, yy, dy);
		
		for (int i = 0; i <= n; i++) {
			int ySat = i + iy;
			if (ySat < 0) {
				ySat = 0;
			} else if (ySat >= data.length) {
				ySat = data.length - 1;
			}
			temp[i] = splineInterpolate(n, data[ySat], x);
		}
		
		yy -= iy;
		
		return splineInterpolate(n, temp, yy, dy);
		
	}
	
	/**
	 * Gets the index of the first array element required to interpolate to (x + dx).  
	 * If the method returns i, then all array elements from i to (i + n), inclusive, must be 
	 * present in the array, in order to perform spline interpolation.<br>
	 * The returned value may be out of range for the array
	 * 
	 * @param n the order of the spline
	 * @param x the integer portion of the coordinate
	 * @param dx the fractional portion of the coordinate (0 <= dx < 1)
	 * @return the index of the first array element
	 */
	public static int getFirstSplineIndex(int n, int x, float dx) {
		int width = n + 1;
		
		if (width % 2 == 1) {
			x = x - (width >> 1);
		} else if (dx > 0.5F) {
			x = x - (width >> 1) + 1;
		} else {
			x = x - (width >> 1);
		}
		
		return x;
	}
	
	/**
	 * Interpolates to the given location using an nth order spline
	 * 
	 * @param n the order of the spline
	 * @param data the data
	 * @param x the coordinate
	 * @return the interpolated value
	 */
	public static float splineInterpolate(int n, float[] data, float x) {
		int xx = (int) Math.floor(x);
		float dx = x - xx;
		return splineInterpolate(n, data, xx, dx);
	}
	
	/**
	 * Interpolates to the given location using an nth order spline
	 * 
	 * @param n the order of the spline
	 * @param data the data
	 * @param x the integer portion of the coordinate
	 * @param dx the fractional portion of the coordinate (0 <= dx < 1)
	 * @return the interpolated value
	 */
	public static float splineInterpolate(int n, float[] data, int x, float dx) {
		x = getFirstSplineIndex(n, x, dx);

		int width = n + 1;
		if (width % 2 == 0) {
			if (dx > 0.5F) {
				dx = dx - 0.5F;
			} else {
				dx = dx + 0.5F;
			}
		}
		
		float[] coeffs = getSplineCoefficients(n, 1.0F - dx);
		
		float temp = 0F;
		int j = x;
		
		for (int i = 0; i < coeffs.length; i++) {
			int satJ = j++;
			if (satJ < 0) {
				satJ = 0;
			} else if (satJ >= data.length){
				satJ = data.length - 1;
			}
			temp += coeffs[i] * data[satJ];
		}
		
		return temp;
		
	}

	/**
	 * The spline coefficient cache
	 */
	private final static AtomicReference<float[][][]> coeffRoot = new AtomicReference<float[][][]>();
	
	/**
	 * Gets the spline coefficients for a given order and offset
	 * 
	 * @param n the order of the spline
	 * @param x the sub-sample offset
	 * @return
	 */
	public static float[] getSplineCoefficients(int n, float x) {
		float[][][] root = coeffRoot.get();
		if (root == null || root.length <= n) {
			expandSplineRoots(n);
			root = coeffRoot.get();
		}
		float[][] orderCoeff = root[n];
		
		float[] coeffs = new float[n + 1];
		
		for (int i = 0; i <= n; i++) {
			float[] current = orderCoeff[i];

			float temp = 0;
			for (int j = 0; j <= n; j++) {
				temp = (temp * x) + current[n - j];
			}

			coeffs[i] = temp;
		}
		
		return coeffs;
		
	}
	
	private static synchronized void expandSplineRoots(int n) {
		float[][][] oldRoot = coeffRoot.get();
		if (oldRoot != null && oldRoot.length > n) {
			return;
		}
		
		float[][][] newRoot = new float[n + 1][][];
		
		// Coefficient for zeroth order spline
		newRoot[0] = new float[][] {{1.0F}};
		
		// Compute coefficients for each spline order based on the coefficients from the previous order
		for (int o = 1; o < n + 1; o++) {
			newRoot[o] = new float[o + 1][o + 1];
			for (int i = 0; i < o; i++) {
				for (int p = 0; p < o; p++) {
					newRoot[o][i][p + 1] += newRoot[o - 1][i][p] / (p + 1);
					newRoot[o][i + 1][p + 1] -= newRoot[o - 1][i][p] / (p + 1);
					newRoot[o][i + 1][0] += newRoot[o - 1][i][p] / (p + 1);
				}
			}
		}
		
		coeffRoot.set(newRoot);
	}

}
