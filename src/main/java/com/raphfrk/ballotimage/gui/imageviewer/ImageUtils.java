package com.raphfrk.ballotimage.gui.imageviewer;

import java.util.concurrent.atomic.AtomicReference;

public class ImageUtils {
	
	public static byte[][] zoomNearest(byte[][] in, int zoom) {
		int sy = in.length;
		int sx = in[0].length;
		
		int zy = sy * zoom;
		int zx = sx * zoom;
		
		byte[][] ret = new byte[zy][zx];
		
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
	
	public static byte[][] filter(byte[][] in, byte[][] filter) {
		
		int sy = in.length;
		int sx = in[0].length;
		
		byte[][] ret = new byte[sy][sx];
		
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
				int sum = 0;
				for (int fx = 0; fx < fsx; fx++) {
					for (int fy = 0; fy < fsy; fy++) {
						int xo = fx + x - fcx;
						int yo = fy + y - fcy;
						if (xo < 0 || xo >= sx || yo < 0 || yo >= sy) {
							continue;
						}
						sum += (in[yo][xo] & 0xFF) * filter[fx][fy];
					}
				}
				sum /= fsum;
				byte result = 0;
				if (sum > 255) {
					result = (byte) 255;
				} else if (sum < 0) {
					result = (byte) 0;
				} else {
					result = (byte) sum;
				}
				ret[y][x] = result;
			}
		}
		
		return ret;
	}
	
	public static byte[][] genCircleFilter(int radius) {
		int size = radius * 2 + 1;
		int radiusSquared = radius * radius;
		byte[][] ret = new byte[size][size];
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
	public static float splineInterpolate(int n, byte[] data, float x) {
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
	public static float splineInterpolate(int n, byte[] data, int x, float dx) {
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
			temp += coeffs[i] * (data[satJ] & 0xFF);
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
