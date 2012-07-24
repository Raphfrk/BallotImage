package com.raphfrk.ballotimage.gui.imageviewer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ImageUtilsTest {
	
	/**
	 * Maximum tolerance between approx spline and calculated spline
	 */
	private static float TOLERANCE = 0.003F;
	
	@Test
	public void splineTest() {
		
		int o = 2;
		
		float[] results = new float[(o + 1) * 10];
		
		for (int x = 0; x < 10; x++) {
			float dx = (x + 1.5F) * 0.1F;
			
			float[] coeffs = ImageUtils.getSplineCoefficients(o, dx);
			
			int j = 0;
			for (int i = x; i < results.length; i += 10) {
				results[i] = coeffs[j++];
			}
		}
		
		// Generate approx spline using convolution
		
		float[] target = new float[(o + 1) * 10];
		
		target[0] = 10;
		
		for (int i = 0; i < o + 1; i++) {
			conv(target, 10);
		}
		
		float maxError = 0;
		
		for (int i = 0; i < target.length; i++) {
			float err = Math.abs(target[i] - results[i]);
			if (err > maxError) {
				maxError = err;
			}
		}
		
		assertTrue("Error " + maxError + " exceeded tolerances", maxError < TOLERANCE);
	}
	
	private void conv(float[] target, int l) {
		
		for (int i = target.length - 1; i >= 0; i--) {
			float total = 0;
			for (int j = i; j >= 0 && j > i - l; j--) {
				total += target[j];
			}
			target[i] = total / l;
			
		}
		
	}
	
	@Test
	public void splineInterpTest() {
		
		int o = 2;
		
		float h = 255.0F;
		float l = 0.0F;
		
		float[] data = {l, l, l, l, h, l, l, l, l, l};
		
		float peak = 0;
		float maxValue = -Float.MAX_VALUE;
		float prev = -Float.MAX_VALUE;
		
		for (float x = -5; x <= 15; x += 0.1) {
			float result = ImageUtils.splineInterpolate(o, data, x);
			if (result > maxValue) {
				peak = x;
				maxValue = result;
			}
			if (x < 4.49) {
				assertTrue("Result is not monotonic increasing before peak", result >= prev);
			} else if (x > 4.51) {
				assertTrue("Result is not monotonic decreasing after peak", result <= prev);
			}
			prev = result;
		}
		
		assertTrue("Peak is in the wrong position " + peak, Math.abs(peak - 4.5F) < 0.01);
		
	}

}
