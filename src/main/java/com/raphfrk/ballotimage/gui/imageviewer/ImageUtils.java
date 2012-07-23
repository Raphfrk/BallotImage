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
