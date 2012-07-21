package com.raphfrk.ballotimage.io.vararray;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class VarByteArrayTest {

	@Test
	public void test3dArray() {
		
		int[] size = new int[] {10, 15, 20};
		
		byte[][][] referenceArray = new byte[size[0]][size[1]][size[2]];
		
		VarByteArray varArray = VarByteArray.newInstance(size);
		
		int updates = 3000;
		
		Random r = new Random();
		
		int[] indexes = new int[3];
		
		for (int i = 0; i < updates; i++) {
			int i0 = r.nextInt(size[0]);
			int i1 = r.nextInt(size[1]);
			int i2 = r.nextInt(size[2]);

			byte value = (byte) r.nextInt();
			
			indexes[0] = i0;
			indexes[1] = i1;
			indexes[2] = i2;
			
			referenceArray[i0][i1][i2] = value;
			varArray.set(value, indexes);
		}
		
		for (int i0 = 0; i0 < size[0]; i0++) {
			for (int i1 = 0; i1 < size[1]; i1++) {
				for (int i2 = 0; i2 < size[2]; i2++) {
					indexes[0] = i0;
					indexes[1] = i1;
					indexes[2] = i2;
					assertTrue("Mismatch between reference and var array", referenceArray[i0][i1][i2] == varArray.get(indexes));
				}
			}
		}
	}
	
	@Test
	public void test1dArray() {

		int size = 100;
		
		byte[] referenceArray = new byte[size];
		
		VarByteArray varArray = VarByteArray.newInstance(size);
		
		int updates = 3000;
		
		Random r = new Random();
		
		for (int i = 0; i < updates; i++) {
			int i0 = r.nextInt(size);

			byte value = (byte) r.nextInt();
			
			referenceArray[i0] = value;
			varArray.set(value, i0);
		}

		for (int i0 = 0; i0 < size; i0++) {
			assertTrue("Mismatch between reference and var array", referenceArray[i0] == varArray.get(i0));
		}
	}
	
}
