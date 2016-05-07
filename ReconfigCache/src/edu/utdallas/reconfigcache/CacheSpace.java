package edu.utdallas.reconfigcache;

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
	public int lruCount = 0; 

	public void incrementLruCount(){
		if(lruCount < 4){
			lruCount++;
		}
	}
}
