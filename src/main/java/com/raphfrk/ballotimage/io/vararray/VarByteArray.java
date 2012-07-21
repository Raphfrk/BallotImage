package com.raphfrk.ballotimage.io.vararray;

import java.io.IOException;
import java.io.InputStream;

public abstract class VarByteArray {

	/**
	 * Gets a sub array of this byte array
	 * 
	 * @param index
	 * @return
	 */
	public abstract VarByteArray getSubArray(int index);
	
	/**
	 * Gets the value for the given index, the last index given is the leaf index
	 * 
	 * @param indexes
	 * @return
	 */
	public byte get(int... indexes) {
		return getOffset(0, indexes);
	}
	
	/**
	 * Gets the value for the given indexes, with the first index being specified
	 * 
	 * @param firstIndex
	 * @param indexes
	 * @return
	 */
	public abstract byte getOffset(int firstIndex, int... indexes);
	
	/**
	 * Sets the value for the given index, the last index given is the leaf index
	 * 
	 * @param value
	 * @param indexes
	 * @return
	 */
	public void set(byte value, int... indexes) {
		setOffset(value, 0, indexes);
	}
	
	/**
	 * Sets the value for the given indexes, with the first index being specified
	 * 
	 * @param value
	 * @param firstIndex
	 * @param indexes
	 * @return
	 */
	public abstract void setOffset(byte value, int firstIndex, int... indexes);
	
	/**
	 * Reads the VarByteArray from the given input stream
	 * 
	 * @param in
	 * @throws IOException
	 */
	public abstract void read(InputStream in) throws IOException;
	
	/**
	 * Gets the size of the outer array
	 * 
	 * @return
	 */
	public abstract int size();
	
	/**
	 * Creates an new VarByteArray instance
	 * @param sizes
	 * @return
	 */
	public static VarByteArray newInstance(int... sizes) {
		if (sizes.length == 1) {
			return new VarByteArrayLeaf(sizes[0]);
		} else {
			return new VarByteArrayBranch(sizes);
		}
	}
	
}
