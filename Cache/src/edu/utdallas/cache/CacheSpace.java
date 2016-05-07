package edu.utdallas.cache;

/**
 * Cache line which contains attributes. Used to find out whether there are 
 * cache hit or miss 
 * 
 * @author sriee
 */
public class CacheSpace {
	public int validBit = 0;
	public int index = 0;
	public int tag = 0;
	public int[] word;
}
