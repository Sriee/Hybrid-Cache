package edu.utdallas.reconfigcache;

import java.io.File;
import java.io.FilenameFilter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;

 /**
 * Reconfigurable Cache Simulator 
 * 
 * Unified Cache, Takes trace file as inputs 
 * 
 * Format of Trace file should be in the following format
 * [L/S] Hex Address 
 * 
 * Ex : S 40bc74
 *
 * Replacement Policy Used - Random Replacement Policy 
 * Addressing type - (24 bit) byte addressable
 * 
 * @param args[1] - Name of the trace file 
 * @param args[2] - Cache Size (in KB) - [32, 64, 128, 256, 512, 1024, 2048]
 * @param args[3] - Set Associativity - [1, 2, 4]
 * @param args[4] - Block Size (in bytes) - [16] Should be pow(2)
 *	
 * @description Hybrid Cache, a cache architecture that allows reconfiguration in both its size and associativity
 *				resulting in greater number of hits and decreased cache latency in comparison to a state-of-the-art cache
 * @author sriee
 *
 */
 
public class ReconfigCache {
	static String associativity, blockSize, cacheSize; 
	private static ArrayList<ArrayList<CacheSpace>> cache = new ArrayList<ArrayList<CacheSpace>>();
	private static HashMap<String, Integer> memory = new HashMap<String, Integer>();
	private static int noOfInstructionMiss = 0;
	private static int noOfDataMiss = 0;
	private static int noOfInstructions = 0;
	private static int checkPoint = 0;
	private static int hit = 0;
	private static CacheUtility cU = null;
	private static double activeSetCountAvg = 0;
	private static double lruCountAvg = 0;
	static TreeMap<Integer,Integer> allSet = new TreeMap<Integer,Integer>();
	static TreeMap<String,Integer> lruChain = new TreeMap<String,Integer>();
	static TreeMap<Integer,Integer> activeSet = new TreeMap<Integer,Integer>();


	public static CacheUtility reconfigCache(int activeSetSize, int lruChainSize){
		//calculate and round off activesetcount n lrucount
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
		activeSetCountAvg = Double.parseDouble( df.format(activeSetSize/(double)cU.getNumSets()));
		lruCountAvg = Double.parseDouble(df.format(lruChainSize/(double)cU.getNumBlocks()));
				
		Integer blockSize = cU.getBlockSize(); 
		String bSize = blockSize.toString();
		if(activeSetCountAvg>0.6){
			if(lruCountAvg>0.6){
				if(activeSetCountAvg>0.9){
					System.out.println("Changed to config 1");
					return cU = new CacheUtility("2048", "4", bSize);
					
				}	
				else{
					System.out.println("Changed to config 2");
					return cU = new CacheUtility("1024", "2", bSize);
				}	
			}
			else{
				if(activeSetCountAvg>0.7){
					System.out.println("Changed to config 3");
					return cU = new CacheUtility("1024", "1", bSize);
				}
				else{
					System.out.println("Changed to config 4");
					return cU = new CacheUtility("512", "1", bSize);
				}
			}
		}
		else{
			if(lruCountAvg>0.2){
				if(activeSetCountAvg>0.4){
					System.out.println("Changed to config 5");
					return cU = new CacheUtility("256", "2", bSize);
				}
				else{
					System.out.println("Changed to config 6");
					return cU = new CacheUtility("128", "2", bSize);
				}	
			}
			else{
				if(activeSetCountAvg>0.2){
					System.out.println("Changed to config 7");
					return cU = new CacheUtility("128", "1", bSize);
				}
				else{
					System.out.println("Changed to config 8");
					return cU = new CacheUtility("64", "1", bSize);
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		try{	
			String currentDirectory = System.getProperty("user.dir");
			File workingDirectory = new File(currentDirectory);

			FilenameFilter traceFileFilter = new FilenameFilter(){
				public boolean accept(File workingDirectory, String fileName){
					return fileName.equals(args[0]);
				}
			};
			String[] traceFileNames = workingDirectory.list(traceFileFilter);

			if(traceFileNames.length == 0){
				System.out.println("Could not find " + args[0] + " in the present directory...");
				System.exit(0);
			}

			cU = new CacheUtility(args[1],args[2],args[3]);
			File traceFile = new File (traceFileNames[0]);
			Scanner traceFileScanner = new Scanner(traceFile);

			/*********************************************************************
			 * Initialize Cache													 *
			 *********************************************************************/

			for(int i = 0; i < cU.getNumSets(); i++){
				ArrayList<CacheSpace> initSetBlock = new ArrayList<CacheSpace>();
				for(int k = 0; k < cU.getSetAssociation(); k++){
					CacheSpace initSlot = new CacheSpace();
					initSlot.word = new int[Integer.parseInt(args[3])];
					initSetBlock.add(initSlot);
				}
				cache.add(initSetBlock);
			}
			
			//store counters
	
			while(traceFileScanner.hasNextLine()){
				noOfInstructions++;
				checkPoint++;
				String instructionLine = traceFileScanner.nextLine().trim();
				instructionLine = instructionLine.replaceFirst("0x", "");

				String[] parts = instructionLine.split(" ");
				parts[1] = parts[1].substring(0,6);

				String binaryInstruction = cU.hexToBin(parts[1]);
				/*********************************************************************
				 * Cache Operation													 *
				 *********************************************************************/
				int tagInt = cU.binaryToTag(binaryInstruction);
				int indexInt = cU.binaryToIndex(binaryInstruction);
				int offset = cU.binaryToOffset(binaryInstruction) % cU.getBlockSize();
				
				int setCounter = 0; //set counter
				boolean flag = false;
				int activeSetCount = 0;
				Iterator<ArrayList<CacheSpace>> setIterator = cache.iterator();
				while(setIterator.hasNext()){
					ArrayList<CacheSpace> set = setIterator.next();
					
					if (allSet.get(indexInt) != null) {
						activeSetCount = allSet.get(indexInt);
						}
					
					if(setCounter == indexInt){
						int count = 1;
						Iterator<CacheSpace> it = set.iterator(); 
						while(it.hasNext()){
							CacheSpace slot = new CacheSpace();
							slot = it.next();
							if(slot.tag == tagInt){ //cache hit
								hit++;
								if ( parts[0].equals("S") ){ //store hit

									//writing to cache									
									slot.word[offset] = cU.getData(); 
									slot.validBit = 1; 
									slot.tag = tagInt;
									slot.index = indexInt;
									slot.incrementLruCount();		//Incrementing LRU count

									//Write-through memory portion pulling the old value at that key if it exists
									if(memory.containsKey(binaryInstruction)){
										memory.put(binaryInstruction, slot.word[offset]);
									} else {
										//key = (tag + set); Value = new random data 
										memory.put(binaryInstruction, slot.word[offset]);
									}
								}

								if ( parts[1].equals("L")){ //load hit
								}
								flag = true;
								break;
							}
							/*********************************************************************
							 * Cache Miss - Stores instruction only when its frequency = 3		 *
							 *********************************************************************/
							if((count == cU.getSetAssociation()) && (slot.tag != tagInt)){ //cache miss  
								int toCache = 0;
								if ( parts[0].equals("S") ){ //store miss
									noOfDataMiss++;
									toCache = cU.getData();
									memory.put(binaryInstruction, toCache);

								} else if ( parts[0].equals("L") ){ //load miss
									noOfInstructionMiss++;
									//pulling data from memory
									if(! memory.containsKey(binaryInstruction)){
										memory.put(binaryInstruction, 0);
									} else {
										toCache = memory.get(binaryInstruction);
									}
								}

								//make new CacheSpace object to put in cache
								CacheSpace cacheSlot = new CacheSpace();

								//putting memory in cache
								cacheSlot.word = new int[Integer.parseInt(args[3])];
								cacheSlot.word[offset] = toCache; 
								cacheSlot.validBit = 1; 
								cacheSlot.tag = tagInt;
								cacheSlot.index = indexInt; 
								cacheSlot.incrementLruCount();		//Incrementing LRU count

								//Random Replacement Policy
								set.set(cU.getRandomReplacementSlot(), cacheSlot);
							}
							//cacheslot of misses are not being counted?
							if(slot.lruCount == 4){
								String slotKey = Integer.toString(indexInt)+Integer.toString(count);
								lruChain.put(slotKey, slot.lruCount);
							}
							count++;
						}
						
					}
					if (flag) break;
					setCounter+=1;
				}
				
				if(activeSetCount < 4) {
					activeSetCount++ ;
					allSet.put(indexInt, activeSetCount);
				}
				if(activeSetCount == 4){
					activeSet.put(indexInt,activeSetCount);	
				}
				
				if(checkPoint == 10000){
					reconfigCache(activeSet.size(),lruChain.size());
					allSet.clear();
					activeSet.clear();
					lruChain.clear();
					checkPoint = 0;
					cache.clear();
					for(int i = 0; i < cU.getNumSets(); i++){
						ArrayList<CacheSpace> initSetBlock = new ArrayList<CacheSpace>();
						for(int k = 0; k < cU.getSetAssociation(); k++){
							CacheSpace initSlot = new CacheSpace();
							initSlot.word = new int[Integer.parseInt(args[3])];
							initSetBlock.add(initSlot);
						}
						cache.add(initSetBlock);
					}
				}
				/*********************************************************************
				 * End of cache Operation											 *
				 *********************************************************************/	
			} // End of while

			//Printing the results 
			printResult();

			//Closing trace file 
			traceFileScanner.close();

		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid number of arguments...");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	} //End of main 

	/**
	 * Display's the results after cache operations 
	 */
	public static void printResult(){
		System.out.println();
		System.out.println(line("*",100));
		System.out.println();
		System.out.println("\t\t\t\tSmart Cache");
		System.out.println();
		System.out.println("\t\tTotal number of misses\t: " + (noOfInstructionMiss + noOfDataMiss) + "/" + noOfInstructions);
		System.out.println("\t\tInstruction misses\t: " + noOfInstructionMiss);
		System.out.println("\t\tData misses\t\t: " + noOfDataMiss);
		System.out.println("\t\tHit\t\t\t: " + hit);
		System.out.println();
		System.out.println(line("*",100));
	}

	/**
	 * Prints a line of *'s
	 * 
	 * @param s		
	 * @param num
	 * @return
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}

}
