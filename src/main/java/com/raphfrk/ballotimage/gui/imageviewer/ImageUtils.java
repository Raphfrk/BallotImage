package com.raphfrk.ballotimage.gui.imageviewer;

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

}
