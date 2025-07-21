package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import edu.cmu.tetrad.graph.Node;

public class PowerSet implements Enumeration<HashSet<Node>> {
	List<Node> nodes;
	private List<HashSet<Node>> subSets;
	private int index;
	private int[] lista;
	private HashMap<Integer,HashSet<Node>> hashMap;
	
	
	PowerSet(List<Node> nodes,int k) {
		if(nodes.size()<=k)
			k=nodes.size();
		this.nodes=nodes;
		subSets = new ArrayList<HashSet<Node>>();
		index=0;
		hashMap=new HashMap<Integer, HashSet<Node>>();
		lista=ListFabric.getList(nodes.size());
		for (int i : lista) {
			HashSet<Node> newSubSet = new HashSet<Node>();
			String selection = Integer.toBinaryString(i);
			for (int j = selection.length() - 1; j >= 0; j--) {
				if (selection.charAt(j) == '1') {
					newSubSet.add(nodes.get(selection.length() - j - 1));
				}
			}
			if(newSubSet.size()<=k){
				subSets.add(newSubSet);
				hashMap.put(i, newSubSet);
			}
		}
	}

	
	PowerSet(List<Node> nodes) {
		if(nodes.size()>maxPow)
			maxPow=nodes.size();
		this.nodes=nodes;
		subSets = new ArrayList<HashSet<Node>>();
		index=0;
		hashMap=new HashMap<Integer, HashSet<Node>>();
		lista=ListFabric.getList(nodes.size());
		for (int i : lista) {
			HashSet<Node> newSubSet = new HashSet<Node>();
			String selection = Integer.toBinaryString(i);
			for (int j = selection.length() - 1; j >= 0; j--) {
				if (selection.charAt(j) == '1') {
					newSubSet.add(nodes.get(selection.length() - j - 1));
				}
			}
			subSets.add(newSubSet);
			hashMap.put(i, newSubSet);
		}
	}
    
	public boolean hasMoreElements() {
		return index<subSets.size();
	}

	public HashSet<Node> nextElement() {
		return subSets.get(index++);
	}
	
	public void resetIndex(){
		this.index = 0;
	}
	
	private static int maxPow = 0;
	public static long maxPowerSetSize() {
		return (long) Math.pow(2,maxPow);
	}
	
//	public void firstTest(boolean result) {
//		int numInicial=lista[index-1];
//		for(int i=0;i<lista.length;i++) {
//			if((lista[i] & numInicial)==numInicial) {
//				if(result)
//					hashMap.get(lista[i]).firstTest=HashSet<Node>.TEST_TRUE;
//				else
//					hashMap.get(lista[i]).firstTest=HashSet<Node>.TEST_FALSE;
//			}
//		}
//	}
//	
//	public void secondTest(boolean result) {
//		int numInicial=lista[index-1];
//		for(int i=0;i<lista.length;i++) {
//			if((lista[i] & numInicial)==numInicial) {
//				if(result)
//					hashMap.get(lista[i]).secondTest=HashSet<Node>.TEST_TRUE;
//				else
//					hashMap.get(lista[i]).secondTest=HashSet<Node>.TEST_FALSE;
//			}
//		}
//	}
//	
//	public void reset(boolean isFordwardSearch) {
//		index=0;
//		for(int i=0;i<subSets.size();i++) {
//			HashSet<Node> aux=subSets.get(i);
//			if(isFordwardSearch)
//				aux.secondTest=HashSet<Node>.TEST_NOT_EVALUATED;
//			else
//				aux.firstTest=HashSet<Node>.TEST_NOT_EVALUATED;
//		}
//	}
}
