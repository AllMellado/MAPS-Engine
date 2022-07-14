package mapsEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import cartago.*;
import java.util.Arrays;
import java.util.LinkedList;;

public class PriceForecast extends Artifact  {
	private static String filepathToData;
	private static String filepathToNames;
	private static List<String[]> data = new LinkedList<>();
	private static String[] classes;
	private static int numAttributes;
	private static int numClasses;
	private static List<Pair<String, String[]>> attributes = new LinkedList<>();
	private static TreeNode tree;
	private static int numRows;
	private static String best_attribute;
	private static float best_threshold;
	
	private static class Pair<K, V>{
		public final K key;
		public final V values;
		
		public Pair(K key, V value) {
			this.key = key;
			this.values = value;
		}
	}
	
	void init(String pathToData, String pathToNames) {
		setFilepathToData(pathToData);
		setFilepathToNames(pathToNames);
		setNumAttributes(-1);
		setNumClasses(-1);
		setTree(null);
	}

	@OPERATION
	public void readData() {
		String line;
		
		try {
			BufferedReader namesFile = new BufferedReader(new FileReader(getFilepathToNames()));	
			String[] classes = namesFile.readLine().split(",");
			setClasses(classes);
			setNumClasses(classes.length);
			
			while((line = namesFile.readLine()) != null) {
				String[] attrData = line.split(":");
				Pair<String, String[]> attribute = new Pair<String, String[]>(attrData[0], attrData[1].split(","));
				System.out.println("Attribute Name> "+attribute.key);
				System.out.println("Values> "+Arrays.toString(attribute.values));
				getAttributes().add(attribute);
			}
			setNumAttributes(getAttributes().size());
			namesFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader dataFile = new BufferedReader(new FileReader(getFilepathToData()));
			
			while((line = dataFile.readLine()) != null) {
				String[] row = line.split(",");
				if( row.length > 0 || !row[0].equals("") ) {
					getData().add(row);
				}
			}
			setNumRows(getData().size());
			dataFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Classes> "+ Arrays.toString(getClasses()));
		//getData().forEach(System.out::println);
		//System.out.println("Attributes> "+getAttributes());
	}
	
	@OPERATION
	public void printTree() {
		printNode(getTree(), "");
	}
	
	public void printNode( TreeNode node, String indent ) {
		if( !node.getIsLeaf() ) {
			if( node.getThreshold() == -1 ) {
				for(int i = 0; i < node.getChildren().size(); i++) {
					String attr = node.getLabel();
					TreeNode child = node.getChildren().get(i);
					String[] values = null;
					for(int index = 0; index < getNumAttributes(); index++) {
						if( getAttributes().get(index).key.equals(attr) ) {
							values = getAttributes().get(index).values;
							break;
						}
					}
					if( node.getChildren().get(i).getIsLeaf() ) {
						System.out.println(indent+attr+" "+values[i]+" : "+ child.getLabel());
					}else {
						System.out.println(indent+attr+" "+values[i]+" : ");
						printNode(child, indent.concat("   "));
					}
				}
			}else {
				String attr = node.getLabel();
				float threshold = node.getThreshold();
				TreeNode left_child = node.getChildren().get(0);
				TreeNode right_child = node.getChildren().get(1);
				
				if( left_child.getIsLeaf() ) {
					System.out.println(indent+attr+" <= "+threshold+" : "+left_child.getLabel());
				}else {
					System.out.println(indent+attr+" <= "+threshold+" : ");
					printNode(left_child, indent.concat("   "));
				}
				
				if( right_child.getIsLeaf() ) {
					System.out.println(indent+attr+" > "+threshold+" : "+right_child.getLabel());
				}else {
					System.out.println(indent+attr+" > "+threshold+" : ");
					printNode(right_child, indent.concat("   "));
				}
			}
		}
	}
	
	@OPERATION
	public void generateTree() {
		System.out.println("Generating Decision Tree");
		setTree( recursiveGeneration(getData(), getAttributes()) );
	}
	
	public TreeNode recursiveGeneration( List<String[]> currData, List<Pair<String, String[]>> attributes ) {
		System.out.println(currData.size());
		if( currData.size() < 0.02*getNumRows() ) {
			if( data.size() > 0 ) {
				return new TreeNode(true, getCommonClass(currData), -1);
			}else {
				return new TreeNode(true, getCommonClass(getData()), -1);
			}
		}else {
			if( isAllSameClass(currData) ) {
				return new TreeNode(true, currData.get(0)[currData.get(0).length-1], -1);
			} else {
				List<List<String[]>> best_split = getBestSplit(currData, attributes);
				TreeNode node = new TreeNode(false, getBest_attribute(), getBest_threshold());
				List<TreeNode> children = new LinkedList<TreeNode>();
				for(int i = 0; i < best_split.size(); i++) {
					children.add(recursiveGeneration(best_split.get(i), attributes));
				}
				node.setChildren(children);
				return node;
			}
		}
	}
	
	public String getCommonClass(List<String[]> data) {
		String[] classes = getClasses();
		int[] numEachClass = new int[classes.length];
		for(int i = 0; i < data.size(); i++) {
			for( int c_i = 0; c_i < classes.length; c_i++ ) {
				if(data.get(i)[data.get(i).length-1].equals(classes[c_i])) {
					numEachClass[c_i] += 1;
				}
			}
		}
		int max = 0;
		int max_i = -1;
		for(int i = 0; i < numEachClass.length; i++) {
			if(numEachClass[i] > max) {
				max = numEachClass[i];
				max_i = i;
			}
		}
		return classes[max_i];
	}
	
	public boolean isAllSameClass(List<String[]> data) {
		for(int i = 0; i < data.size(); i++) {
			if( data.get(i)[data.get(i).length-1] != data.get(0)[data.get(0).length-1] ) {
				return false;
			}
		}
		return true;
	}
	
	public List<List<String[]>> getBestSplit(List<String[]> data, List<Pair<String, String[]>> attributes) {
		List<List<String[]>> best_split = null;
		float max_gain = -1;
		
		for(int attr_index = 0; attr_index < attributes.size(); attr_index++) {
			if( !attributes.get(attr_index).values[0].equals("continuous") ) {
				System.out.println("Still working | nominal | "+attr_index);
				String[] attr_values = attributes.get(attr_index).values;
				
				List<List<String[]>> subsets = new LinkedList<>();
				List<String[]> subset = null;
				
				for(int i = 0; i < attr_values.length; i++ ) {
					subset = new LinkedList<>();
					for(int row_index = 0; row_index < data.size(); row_index++) {
						if( data.get(row_index)[attr_index].equals(attr_values[i])){
							subset.add(data.get(row_index));
						}
					}
					subsets.add(subset);
				}
				
				float attr_gain = gain(data, subsets);
				if( attr_gain > max_gain) {
					max_gain = attr_gain;
					best_split = subsets;
					setBest_attribute(attributes.get(attr_index).key);
					setBest_threshold(-1);
				}
			}else {
				System.out.println("Still working | numeric | "+attr_index);
				int index = attr_index;
				data.sort((a,b)->a[index].compareTo(b[index]));
				for(int row_index = 0; row_index < data.size()-1; row_index++) {
					if( !data.get(row_index)[attr_index].equals(data.get(row_index+1)[attr_index])) {
						
						List<String[]> lesser = data.subList(0, row_index+1);
						List<String[]> greater = data.subList(row_index+1, data.size()-1);
						
						List<List<String[]>> subsets = new LinkedList<>();
						subsets.add(lesser);
						subsets.add(greater);
						
						float attr_gain = gain(data, subsets);
						
						if( attr_gain > max_gain) {
							max_gain = attr_gain;
							best_split = subsets;
							setBest_attribute(attributes.get(attr_index).key);
							setBest_threshold(( Float.parseFloat(data.get(row_index)[attr_index]) + Float.parseFloat(data.get(row_index+1)[attr_index]) ) / 2); 
						}
					}
				}
			}
		}
		return best_split;
	}
	
	public float gain(List<String[]> data, List<List<String[]>> subsets){
		int data_len = data.size();
		
		float data_impurity = calc_entropy(data);
		
		float[] weights = new float[subsets.size()];
		List<String[]> subset;
		for(int i = 0; i < subsets.size(); i++) {
			subset = subsets.get(i);
			weights[i] = subset.size() / data_len;
		}
		
		float subsets_impurity = 0;
		for(int i = 0; i < subsets.size(); i++) {
			subsets_impurity += weights[i] * calc_entropy(subsets.get(i));
		}
		
		return data_impurity - subsets_impurity;
	}
	
	public float calc_entropy( List<String[]> data){
		int data_len = data.size();
		
		if(data_len == 0) return 0;
		
		int[] numEachClass = new int[getNumClasses()];
		int class_index;
		List<String> classes = Arrays.asList(getClasses());
		for(int i = 0; i < data_len; i++) {
			class_index = classes.indexOf(data.get(i)[getNumAttributes()]);
			numEachClass[class_index] += 1;
		}
		
		float entropy = 0;
		for(int i = 0; i < numEachClass.length; i++) {
			numEachClass[i] /= data_len;
			entropy += numEachClass[i] * log(numEachClass[i]);
		}
		
		return entropy;
		
	}
	
	public float log( int x) {
		if(x == 0) {
			return 0;
		}else {
			return (float) Math.log(x);
		}
	}
	
	public static String getFilepathToData() {
		return filepathToData;
	}

	public static void setFilepathToData(String filepathToData) {
		PriceForecast.filepathToData = filepathToData;
	}

	public static String getFilepathToNames() {
		return filepathToNames;
	}

	public static void setFilepathToNames(String filepathToNames) {
		PriceForecast.filepathToNames = filepathToNames;
	}

	public static List<String[]> getData() {
		return data;
	}

	public static void setData(List<String[]> data) {
		PriceForecast.data = data;
	}

	public static String[] getClasses() {
		return classes;
	}

	public static void setClasses(String[] classes) {
		PriceForecast.classes = classes;
	}

	public static int getNumAttributes() {
		return numAttributes;
	}

	public static void setNumAttributes(int numAttributes) {
		PriceForecast.numAttributes = numAttributes;
	}

	public static int getNumClasses() {
		return numClasses;
	}

	public static void setNumClasses(int numClasses) {
		PriceForecast.numClasses = numClasses;
	}

	public static List<Pair<String, String[]>> getAttributes() {
		return attributes;
	}

	public static void setAttributes(List<Pair<String, String[]>> attributes) {
		PriceForecast.attributes = attributes;
	}
	
	public static TreeNode getTree() {
		return tree;
	}

	public static void setTree(TreeNode tree) {
		PriceForecast.tree = tree;
	}

	public static int getNumRows() {
		return numRows;
	}

	public static void setNumRows(int numRows) {
		PriceForecast.numRows = numRows;
	}

	public static String getBest_attribute() {
		return best_attribute;
	}

	public static void setBest_attribute(String best_attribute) {
		PriceForecast.best_attribute = best_attribute;
	}

	public static float getBest_threshold() {
		return best_threshold;
	}

	public static void setBest_threshold(float best_threshold) {
		PriceForecast.best_threshold = best_threshold;
	}
	
	
}
