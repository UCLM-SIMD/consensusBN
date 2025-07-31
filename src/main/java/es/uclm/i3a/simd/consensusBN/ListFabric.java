package es.uclm.i3a.simd.consensusBN;

/**
 * ListFabric is a utility class that generates lists of integers representing subsets
 * of a set of a given size, with a maximum size constraint on the number of elements
 * in each subset.
 */
public class ListFabric {
	
	/**
	 * MAX_SIZE is the maximum number of elements that can be included in any subset.
	 * It is set to Integer.MAX_VALUE by default, meaning no limit unless specified.
	 */
	public static int MAX_SIZE=Integer.MAX_VALUE; 

	/**
	 * Generates a list of integers representing all subsets of a set of a given size.
	 * Each integer is a bitmask where the i-th bit represents the inclusion of the i-th element.
	 * @param size the size of the set for which subsets are generated
	 * @return an array of integers representing the subsets
	 */
	public static int[] generateList(int size) {
		int[] lista;
		if(size==0) {
			return new int[1];
		}
		// Generation of powers of numbers that are powers of 2
		int[] pow2=new int[size];
		pow2[0]=1;
		for(int i=1;i<size;i++) {
			pow2[i]=2*pow2[i-1];
		}
		
		// Initialization of aux, where aux[0] stores the numbers and aux[1] the number of ones in aux[0]
		int tam=(int)Math.pow(2, size);
		int[][] aux=new int[2][tam];
		
		// Algorithm of generation of the list
		aux[0][0]=0; // aux[0] almacena los numeros
		aux[1][0]=0; // aux[1] el nº de unos de aux[0]
		int counter=1;
		int index=0;
		while(aux[1][index]<size && aux[1][index]<MAX_SIZE) {
			for(int i=0;i<pow2.length;i++) {
				if(pow2[i]>aux[0][index]) {
					aux[0][counter]=aux[0][index]+pow2[i];
					aux[1][counter]=aux[1][index]+1;
					counter++;
				}
			}
			index++;
		}
		lista=new int[counter];
		System.arraycopy(aux[0], 0, lista, 0, counter);
		return lista;
	}

}
