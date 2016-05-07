package edu.utdallas.cache;

import java.math.BigInteger;
import java.util.Random;

/**
 * Utility function with helper methods to perform cache specific operations 
 * 
 * @author sriee
 */
public class CacheUtility {
	
	private int cacheSize;
	private int setAssociation;
	private int blockSize;
	private int numBlocks;
	private int numSets;
	private int offsetBits;
	private int indexBits;
	private int tagBits;
	
	/**
	 * Initializes Cache parameters, calls helper functions to setup initial vlaues  
	 * required for cache operation 
	 * 
	 * @param cacheSize
	 * @param setAssociation
	 * @param blockSize
	 */
	public CacheUtility(String cacheSize, String setAssociation, String blockSize) {
		this.cacheSize = Integer.parseInt(cacheSize)*1024;
		this.setAssociation = Integer.parseInt(setAssociation);
		this.blockSize = Integer.parseInt(blockSize);
		this.setNumBlocks();
		this.setNumSets();
		this.setOffsetBits();
		this.setIndexBits();
		this.setTagBits();
	}
	
	/**
	 * @return the cacheSize
	 */
	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * @return the setAssociation
	 */
	public int getSetAssociation() {
		return setAssociation;
	}

	/**
	 * @return the blockSize
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @return the numBlocks
	 */
	public int getNumBlocks() {
		return numBlocks;
	}

	/**
	 * Calculates Number of blocks for given cache size 
	 * 
	 */
	public void setNumBlocks() {	
		this.numBlocks = this.cacheSize/this.blockSize;
	}

	/**
	 * @return the numSets
	 */
	public int getNumSets() {
		return numSets;
	}

	/**
	 * Calculates the number of sets
	 */
	public void setNumSets() {
		this.numSets = this.numBlocks/this.setAssociation; 
	}

	/**
	 * @return the offsetBits
	 */
	public int getOffsetBits() {
		return offsetBits;
	}

	/**
	 * Caculates number of offset bits 
	 */
	public void setOffsetBits() {
		double bSize = (double) this.blockSize;
		this.offsetBits = (int) Math.round(Math.log10(bSize)/Math.log10(2));
	}

	/**
	 * @return the setBits
	 */
	public int getIndexBits() {
		return indexBits;
	}

	/**
	 * Calculates the set bits 
	 */
	public void setIndexBits() {
		double nSets = (double) this.numSets;
		this.indexBits = (int) Math.round(Math.log10(nSets)/Math.log10(2));
	}

	/**
	 * @return the tagBits
	 */
	public int getTagBits() {
		return tagBits;
	}

	/**
	 * Calculates the tag bits for the 32 bit address
	 */
	public void setTagBits() {
		this.tagBits = 32 - this.indexBits - this.offsetBits;
	}

	/**
	 * Generates random data to be stored in the cache 
	 * 
	 * @return cache data
	 */
	public int getData(){
		Random rand = new Random();
		int n = rand.nextInt(this.blockSize);	
		return n;
	}
	/**
	 * Calculates hex values to binary string. Padds binary value with '0's' if the 
	 * converted binary is less than 32 bit in length 
	 * 
	 * @param hex the hex value to be converted 
	 * @return padded 32 bit binary string
	 */
	public String hexToBin(String hex){
		int padLen = 0;
		String zero = "";
		String hexBin = new BigInteger(hex, 16).toString(2); 
		if(hexBin.length() != 32){
			 padLen = 32 - hexBin.length();
			 for(int j = 0; j < padLen ; j++)
				 zero += "0";
			 hexBin = zero + hexBin;
		}
		return hexBin;
	}
	
	/**
	 * Calculates the tag for the input binary string  
	 * 
	 * @param binary Binary string 
	 * @return tag for the binary string
	 */
	public int binaryToTag(String binary){
		String t = binary.substring(0, this.tagBits);
		int tag = Integer.parseInt(t, 2);
		return tag; 
	}
	
	/**
	 * Calculates set for the input binary string
	 * 
	 * @param binary Binary string
	 * @return set for the binary string
	 */
	public int binaryToIndex(String binary){
		String s = binary.substring(this.tagBits, this.tagBits + this.indexBits);
		int set = Integer.parseInt(s, 2);
		return set;
	}
	
	/**
	 * Calculates offset for the input binary string
	 * 
	 * @param binary Binary string
	 * @return offset for the binary string
	 */
	public int binaryToOffset(String binary){
		String o = binary.substring(this.tagBits + this.indexBits);
		int offSet = Integer.parseInt(o, 2);
		return offSet;
	}
	
	/**
	 * Calculates the key for memory map 
	 * 
	 * @param binary Binary string
	 * @return key for memory map
	 */
	public String getMemoryKey(String binary){
		return binary.substring(0, this.getTagBits() + this.getIndexBits());
	}
	
	/**
	 * Calculates which slot has to be replaced in a given set 
	 * Implements "Random Replacement Policy"
	 * 
	 * @return Replacement slot number
	 */
	public int getRandomReplacementSlot(){
		return new Random().nextInt(this.setAssociation);
	}
}
