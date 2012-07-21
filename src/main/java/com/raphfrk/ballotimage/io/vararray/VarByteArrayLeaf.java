package com.raphfrk.ballotimage.io.vararray;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class VarByteArrayLeaf extends VarByteArray {
	
	private final byte[] array;
	
	public VarByteArrayLeaf(int size) {
		array = new byte[size];
	}

	@Override
	public VarByteArray getSubArray(int index) {
		throw new UnsupportedOperationException("Method getSubArray() cannot be called on leaf arrays");
	}

	@Override
	public byte getOffset(int firstIndex, int... indexes) {
		return array[indexes[firstIndex]];
	}

	@Override
	public void setOffset(byte value, int firstIndex, int... indexes) {
		array[indexes[firstIndex]] = value;
	}

	@Override
	public void read(InputStream in) throws IOException {
		for (int i = 0; i < array.length; i++) {
			int r = in.read();
			if (r == -1) {
				throw new EOFException("End of file reached");
			} 
			array[i] = (byte) r;
		}
	}

	@Override
	public int size() {
		return array.length;
	}
	
}
