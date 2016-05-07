# Hybrid Cache


## About
The Hybrid cache can switch between smaller and larger cache configurations and the associativity can be switched between 1, 2 and 4-way 

The Hybrid cache works as follows:
1.	Check for number of misses at periodic intervals.
2.	Check for factors viz. Active set count and Most Recently Used (MRU) count with respect to the threshold to reconfigure the cache.
3.	Reconfigure based on the decision tree 

### Design Specification
 
| Parameters         	  | Cache            			| 
| ----------------------- |:----------------------------| 
| Associativity    	 	  | 1,2 & 4 					| 
| Block size (Bytes) 	  | 16       					| 
| Cache size (Kilo Bytes) | 32,64,128,256,512,1024,2048 | 
| Addressing Type		  | 24 byte addressable			|
| Replacement Policy	  | Random 						|

 * The cache simulator has been designed for a uniprocessor system employing write through policy. The cache is a unified cache.
 * The data that will be fetched will be random data. Not any specific SPEC Benchmarks.
 * The trace files that have been used consist of load and store instructions.  

## Decision Tree 

![alt Title](https://cloud.githubusercontent.com/assets/8402606/15094312/03d074d8-1465-11e6-8d09-5e21526f6599.png)


## Results 

The results shown in the figure shows the number of misses per 1 million instructions. This is the biggest trace file that the Hybrid cache was tested. The figure gives a fair idea of how the Hybrid Cache performs for a worst case scenario. The number of misses are little better compared to normal state of the art cache. It is a decent improvement considering the additional functionality added for reconfiguration. 

![alt Title](https://cloud.githubusercontent.com/assets/8402606/15094315/25e67e28-1465-11e6-870f-a2e9cad96e5d.png) 

## Licence 

This project is licensed under the MIT License - see the [LICENCE](../master/LICENSE) file for details
 
