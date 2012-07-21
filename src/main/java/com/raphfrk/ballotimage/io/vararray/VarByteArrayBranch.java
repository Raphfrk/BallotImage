package com.raphfrk.ballotimage.io.vararray;

import java.io.IOException;
import java.io.InputStream;

public class VarByteArrayBranch extends VarByteArray {

	private final int dimension;
	private VarByteArray[] subArrays;
	
	public VarByteArrayBranch(int... sizes) {
		if (sizes.length < 2) {
			throw new IllegalArgumentException("Branch arrays must be of dimension 2 or larger");
		}
		this.dimension = sizes.length;
		int length = sizes[0];
		subArrays = new VarByteArray[length];
		int[] newSizes = new int[sizes.length - 1];
		for (int i = 1; i < sizes.length; i++) {
			newSizes[i - 1] = sizes[i];
		}
		for (int i = 0; i < length; i++) {
			subArrays[i] = VarByteArray.newInstance(newSizes);
		}
	}

	public VarByteArray getSubArray(int index) {
		return subArrays[index];
	}

	@Override
	public byte getOffset(int firstIndex, int... indexes) {
		if (indexes.length - firstIndex != dimension) {
			throw new IllegalArgumentException("Dimension length mismatch");
		}
		return subArrays[indexes[firstIndex]].getOffset(firstIndex + 1, indexes);
	}

	@Override
	public void setOffset(byte value, int firstIndex, int... indexes) {
		if (indexes.length - firstIndex != dimension) {
			throw new IllegalArgumentException("Dimension length mismatch, firstIndex " + firstIndex + ", length " + indexes.length + ", dimension " + dimension);
		}
		subArrays[indexes[firstIndex]].setOffset(value, firstIndex + 1, indexes);
	}
	
	public void read(InputStream in) throws IOException {
		for (int i = 0; i < subArrays.length; i++) {
			subArrays[i].read(in);
		}
	}

	@Override
	public int size() {
		return subArrays.length;
	}


}
