package edu.utdallas.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;



/**
 * Cache Simulator 
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
 * @author sriee
 *
 */
public class Cache {

	private static ArrayList<ArrayList<CacheSpace>> cache = new ArrayList<ArrayList<CacheSpace>>();
	private static HashMap<String, Integer> memory = new HashMap<String, Integer>();
	private static int noOfInstructionMiss = 0;
	private static int noOfDataMiss = 0;
	private static int noOfInstructions = 0;
	private static int hit = 0;
	
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

			CacheUtility cU = new CacheUtility(args[1],args[2],args[3]);
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

			while(traceFileScanner.hasNextLine()){
				noOfInstructions++;
				String instructionLine = traceFileScanner.nextLine().trim();
				
				/**
				 * For Trace file of format : 0xhex L/S 
				 */
				/*instructionLine = instructionLine.replaceFirst("0x", "");
				String[] parts = instructionLine.split("\t");
				parts[0] = parts[0].substring(4, parts[0].length());*/

				/**
				 * For Trace file of format : L/S hex
				 */
				String[] parts = instructionLine.split(" ");
				parts[1] = parts[1].substring(0,6);
				 
				String binaryInstruction = cU.hexToBin(parts[0]);


				/*********************************************************************
				 * Cache Operation													 *
				 *********************************************************************/
				int tagInt = cU.binaryToTag(binaryInstruction);
				int indexInt = cU.binaryToIndex(binaryInstruction);
				int offset = cU.binaryToOffset(binaryInstruction) % cU.getBlockSize();
				int setCounter = 0; //set counter
				boolean flag = false;
				
				Iterator<ArrayList<CacheSpace>> setIterator = cache.iterator();
				while(setIterator.hasNext()){
					ArrayList<CacheSpace> set = setIterator.next();
					if(setCounter == indexInt){
						int count = 1;
						Iterator<CacheSpace> it = set.iterator(); 
						while(it.hasNext()){
							CacheSpace slot = new CacheSpace();
							slot = it.next();
							if(slot.tag == tagInt){ //cache hit
								hit++;
								if ( parts[1].equals("S") ){ //store hit

									//writing to cache									
									slot.word[offset] = cU.getData(); 
									slot.validBit = 1; 
									slot.tag = tagInt;
									slot.index = indexInt;

									//Write-through memory portion pulling the old value at that key if it exists
									if(memory.containsKey(binaryInstruction)){
										memory.put(binaryInstruction, slot.word[offset]);
										System.out.println("store hit " + parts[0]);
									} else {
										//key = (tag + set); Value = new random data 
										memory.put(binaryInstruction, slot.word[offset]);
										System.out.println("store hit " + parts[0]);
									}
								}

								if ( parts[1].equals("L")){ //load hit
									System.out.println("load hit " + parts[0]);
								}
								flag = true;
								break;
							}
							/*********************************************************************
							 * Cache Miss 														 *
							 *********************************************************************/
							if((count == cU.getSetAssociation()) && (slot.tag != tagInt)){ //cache miss  
								int toCache = 0;
								if ( parts[1].equals("S") ){ //store miss
									noOfDataMiss++;
									toCache = cU.getData();
									memory.put(binaryInstruction, toCache);
									System.out.println("store miss " + parts[0]);
								} else if ( parts[1].equals("L") ){ //load miss
									noOfInstructionMiss++;
									//pulling data from memory
									if(! memory.containsKey(binaryInstruction)){
										memory.put(binaryInstruction, 0);
										System.out.println("load miss " + parts[0]);
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
							
								//Random Replacement Policy
								set.set(cU.getRandomReplacementSlot(), cacheSlot);
							}
							
							count++;
						}
						
					}
					if (flag) break;
					setCounter+=1;
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
