package mapsEngine;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.function.Function;

import com.google.common.io.Files;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import jason.util.Pair;

public class ArffMethods extends Artifact {
	private static final DecimalFormat df = new DecimalFormat("0");
	private static final DecimalFormat df2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
	
	private Node<String, Double> decision_tree;
	private Node<String, Double> prediction_tree;
	private Pair<String, Double> failedAttribute;
	
	private int counter;
	
	private String textTree;
	
	private String folderPath;
	private String treePath;
	private String filePath;
	private String textTreePath;
	private String priceTreePath;
	private String priceTextPath;
	private String pricePath;
	private String agentName;
	private String logFolder;
	private int agentType;
	
	private double satisfactionPref;
	
	private String[] spotTypes = {"Standard","Elderly","Disabled"};
	private String[] parkTypes = {"Coberto","Descoberto","Meio-Fio"};
	private String[] vehicleTypes = {"Motocicleta", "Carro","Caminhonete"};
	private String[] daysWeek = {"Segunda","Terca","Quarta","Quinta","Sexta","Sabado","Domingo"};
	
	private Double prefPrice;
	private String parkPref;
	private String vehiclePref;
	private double[] parkPrefWeights = new double[4];
	private double[] vehiclePrefWeights = new double[4];
	private double[] priceWeights = new double[3];
	private double[] durationWeights = new double[3];
	private double[] distDestWeights = new double[3];
	private double[] distSpotWeigths = new double[3];
	
	void init(String path, int agentType) {
		this.counter = 1;
		this.satisfactionPref = 0.7;
		this.folderPath = path;
		this.agentType = agentType;
	}
	
	@OPERATION
	public void setPathName(String name, String logFolder) {
		this.agentName = name;
		this.logFolder = logFolder;
		this.textTreePath = this.folderPath+"/arrf_files/"+name+"_text.txt";
		this.treePath = this.folderPath+"/arrf_files/"+name+".txt";
		this.filePath = this.folderPath+"/arrf_files/"+name+".arff";
		this.priceTreePath = this.folderPath+"/arrf_files/"+name+"_price.txt";
		this.pricePath = this.folderPath+"/arrf_files/"+name+"_price.arff";
		this.priceTextPath = this.folderPath+"/arrf_files/"+name+"_priceText.txt";
	}
	
	@OPERATION
	public void setWeights(double prefPrice, Object[] mainWeigths, 
							Object[] priceW, Object[] typeW ) {
		this.prefPrice = prefPrice;
		
		this.priceWeights[0] = Double.parseDouble(mainWeigths[0].toString());
        this.vehiclePrefWeights[0] = Double.parseDouble(mainWeigths[1].toString());
        	
        this.priceWeights[2] = 1 + prefPrice*Double.parseDouble(priceW[0].toString());
        this.priceWeights[1] = prefPrice + (10-prefPrice)*Double.parseDouble(priceW[1].toString());

	    this.vehiclePrefWeights[1] = Double.parseDouble(typeW[0].toString());
	    this.vehiclePrefWeights[2] = Double.parseDouble(typeW[1].toString());
	    this.vehiclePrefWeights[3] = Double.parseDouble(typeW[2].toString());
	}
	
//	@OPERATION
//	public void setWeights(double prefPrice, Object[] mainWeigths, 
//							Object[] priceWeights, Object[] typeWeights, Object[] disdDestWeights ) {
//		this.prefPrice = prefPrice;
//		
//		this.priceWeights[0] = Double.parseDouble(mainWeigths[0].toString());
//        this.distDestWeights[0] = Double.parseDouble(mainWeigths[2].toString());
//        
//        if(this.agentType == 1){
//        	this.parkPrefWeights[0] = Double.parseDouble(mainWeigths[1].toString());
//        	
//        	this.priceWeights[2] = 1 + prefPrice*Double.parseDouble(priceWeights[0].toString());
//            this.priceWeights[1] = prefPrice + (10-prefPrice)*Double.parseDouble(priceWeights[1].toString());
//        	
//        	this.parkPrefWeights[1] = Double.parseDouble(typeWeights[0].toString());
//	        this.parkPrefWeights[2] = Double.parseDouble(typeWeights[1].toString());
//	        this.parkPrefWeights[3] = Double.parseDouble(typeWeights[2].toString());
//	        
//	        this.distDestWeights[1] = 50 + Double.parseDouble(disdDestWeights[0].toString());
//	        this.distDestWeights[2] = this.distDestWeights[1] + 100 + Double.parseDouble(disdDestWeights[1].toString());
//        }else {
//        	this.vehiclePrefWeights[0] = Double.parseDouble(mainWeigths[1].toString());
//        	
//        	this.priceWeights[1] = 1 + prefPrice*Double.parseDouble(priceWeights[0].toString());
//            this.priceWeights[2] = prefPrice + (10-prefPrice)*Double.parseDouble(priceWeights[1].toString());
//        	
//	        this.vehiclePrefWeights[1] = Double.parseDouble(typeWeights[0].toString());
//	        this.vehiclePrefWeights[2] = Double.parseDouble(typeWeights[1].toString());
//	        this.vehiclePrefWeights[3] = Double.parseDouble(typeWeights[2].toString());
//	        this.vehiclePrefWeights[4] = Double.parseDouble(typeWeights[3].toString());
//	        this.vehiclePrefWeights[5] = Double.parseDouble(typeWeights[4].toString());
//	        this.vehiclePrefWeights[6] = Double.parseDouble(typeWeights[5].toString());
//	        
//	        this.distDestWeights[1] = 200 + Double.parseDouble(disdDestWeights[0].toString());
//	        this.distDestWeights[2] = this.distDestWeights[1] + 200 + Double.parseDouble(disdDestWeights[1].toString());
//	        
//	        String str = this.agentName+": \n"+this.priceWeights[0]+" "+this.vehiclePrefWeights[0]+" "+this.distDestWeights[0]+"\n";
//	        str += "Price: "+this.priceWeights[1]+" "+this.priceWeights[2]+"\n";
//	        str += "Vehicle: "+this.vehiclePrefWeights[1]+" "+this.vehiclePrefWeights[2]+" "+this.vehiclePrefWeights[3]+" "+this.vehiclePrefWeights[4]+" "+this.vehiclePrefWeights[5]+" "+this.vehiclePrefWeights[6]+"\n";
//	        str += "DistDest: "+this.distDestWeights[1]+" "+this.distDestWeights[2]+"\n";
//	        //System.out.println(str);
//        }
//	}
	
	@OPERATION
	public void createArff_old(){
		Random rand = new Random();

		int parkID;
		int vehicleID;
    	double price;
    	int distDest; 
		
    	double satisfationSum = 0;
    	
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(this.filePath));
			
			String arrfText = "";
	        if(this.agentType == 1){
	            arrfText = "@relation comprador\n\n"+
	            		"@attribute preco NUMERIC\n"+
	            		"@attribute tipo {Coberto, Descoberto, Meio-Fio}\n"+
	            		"@attribute distanciadestino INTEGER\n"+
	            		"@attribute compra {Sim, Nao}\n\n"+
	            		"@data\n";
	            
	            this.satisfactionPref = 0.7;
	            
		        for(int i = 0; i < 2000; i++) {
		            parkID = rand.nextInt(3);
		            price = 2 + 98*rand.nextDouble();
		           	distDest = 20 + rand.nextInt(581);
		            	
		            satisfationSum = calcSatisfactionDriver(parkID, price, distDest);
		            	
		            arrfText += df2.format(price)+", "+
		            			this.parkTypes[parkID]+", "+
		            			df.format(distDest)+", ";
		            	
		            if( satisfationSum >= this.satisfactionPref ) {
		            	arrfText += "Sim\n";
		            }else {
		            	arrfText += "Nao\n";
		            }
		            satisfationSum = 0;
		        }
	        }else {
	        	arrfText = "@relation vendedor\n\n"+
	            		"@attribute preco NUMERIC\n"+
	        			"@attribute tipo {Motocicleta, Carro, Caminhonete}\n"+
	            		"@attribute venda {Sim, Nao}\n\n"+
	            		"@data\n";
	      
	            for(int i = 0; i < 500; i++) {
	            	price = 6 + 6*rand.nextDouble();
	            	vehicleID = rand.nextInt(3);
	            			
	            	satisfationSum = calcSatisfactionManager(vehicleID, price);
 
	            	arrfText += df2.format(price)+", "+
	            			this.vehicleTypes[vehicleID]+", ";
	            	
	            	if( satisfationSum >= this.satisfactionPref ) {
	            		arrfText += "Sim\n";
	            	}else {
	            		arrfText += "Nao\n";
	            	}
	            	satisfationSum = 0;
	            }
	            
	        }
	 
			writer.write(arrfText);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	public void createArff(){
		BufferedWriter writer = null;
		BufferedReader reader = null;
		String[] name = this.agentName.split("_");
		try {
			writer = new BufferedWriter(new FileWriter(this.filePath));
			reader = new BufferedReader(new FileReader(this.logFolder+"/"+name[0]+"_"+name[1]+".csv"));
			
			String arrfText = "";
	        arrfText = "@relation vendedor\n\n"+
	            		"@attribute preco NUMERIC\n"+
	        			"@attribute tipo {Motocicleta, Carro, Caminhonete}\n"+
	        			"@attribute hora NUMERIC\n"+
	            		"@attribute venda {Sim, Nao}\n\n"+
	            		"@data\n";
	      
	        String line = reader.readLine();
			String[] words = null;
			line = reader.readLine();
            while( line != null ) {
            	words = line.split(";");
            	if(words[7].equals("CounterOffer")) {	
            		arrfText += words[17]+", "+words[6]+", "+", "+words[12].split(":")[0]+", Sim\n";
            	}
            	if(words[7].equals("PriceTooLow")) {	
            		arrfText += words[17]+", "+words[6]+", "+", "+words[12].split(":")[0]+", Nao\n";
            	}
            	line = reader.readLine();
            }
	        
			writer.write(arrfText);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	public void updateArff(){
		BufferedWriter writer = null;
		BufferedReader reader = null;
		String[] name = this.agentName.split("_");
		try {
			writer = new BufferedWriter(new FileWriter(this.filePath, true));
			reader = new BufferedReader(new FileReader(this.logFolder+"/"+name[0]+"_"+name[1]+".csv"));
			
			String arrfText = "";
	      
	        String line = reader.readLine();
			String[] words = null;
			line = reader.readLine();
            while( line != null ) {
            	words = line.split(";");
            	if(words[7].equals("CounterOffer")) {	
            		arrfText += words[17]+", "+words[6]+", "+", "+words[12].split(":")[0]+", Sim\n";
            	}
            	if(words[7].equals("PriceTooLow")) {	
            		arrfText += words[17]+", "+words[6]+", "+", "+words[12].split(":")[0]+", Nao\n";
            	}
            	line = reader.readLine();
            }
	        
			writer.write(arrfText);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OPERATION
	public void createForecastArff() {
		BufferedWriter writer = null;
		BufferedReader reader = null;
		String[] name = this.agentName.split("_");
		try {
			writer = new BufferedWriter(new FileWriter(this.pricePath));
			reader = new BufferedReader(new FileReader(this.logFolder+"/"+name[0]+"_"+name[1]+".csv"));
			
			String arrfText = "@relation previsao\n\n"+
        			"@attribute tipoveiculo {Motoneta, Motocicleta, Carro, Camioneta, Caminhonete, Caminhao}\n"+
        			"@attribute tipovaga {Standard, Elderly, Disabled}\n"+
        			"@attribute diasemana {Segunda, Terca, Quarta, Quinta, Sexta, Sabado, Domingo}\n"+
        			"@attribute dia INTEGER\n"+
        			"@attribute hora INTEGER\n";
        	
			String line = reader.readLine();
			String[] words = null;
			String arrfData = "";
			List<String> prices = new LinkedList<String>();
			String price;
			line = reader.readLine();
            while( line != null ) {
            	words = line.split(";");
            	if(words[7].equals("OfferAccepted")) {
            		if( words[17].split("\\.").length > 1 ) {
	            		price = words[17].split("\\.")[0]+"."+words[17].split("\\.")[1].charAt(0)+"0";
            		}else {
            			price = words[17]+".00";
            		}
            		
            		arrfData += words[6]+", "+words[4]+", "+words[9]+", "+
                			words[11].split("|")[1]+", "+words[12].split(":")[0]+", "+
                			price+"\n";
            		
            		if(!prices.contains(price)) {
            			prices.add(price);
            		}
            	}
            	line = reader.readLine();
            }
            String priceStr = "@attribute price {"+prices.get(0);
			for(int i = 1; i < prices.size(); i++) {
				priceStr += ", "+prices.get(i);
			}
			priceStr += "}\n\n";
			
			arrfText += priceStr + "@data\n" + arrfData;
			
			writer.write(arrfText);
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double calcSatisfactionDriver(int parkID, double price, int distDest){
		double satisfationSum = this.parkPrefWeights[0]*this.parkPrefWeights[parkID+1];
		
    	if( price <= this.priceWeights[1] ) {
    		satisfationSum += this.priceWeights[0];
    	}else if(price < this.priceWeights[2]) {
    		satisfationSum += this.priceWeights[0]*(price - this.priceWeights[2])/(this.priceWeights[1] - this.priceWeights[2]);
    	}
    	
    	if( distDest <= this.distDestWeights[1] ) {
    		satisfationSum += this.distDestWeights[0];
    	}else if(distDest < this.distDestWeights[2]) {
    		satisfationSum += this.distDestWeights[0]*(distDest - this.distDestWeights[2])/(this.distDestWeights[1] - this.distDestWeights[2]);
    	}
    	
    	return satisfationSum;
	}
	
	private double calcSatisfactionManager(int vehicleID, double price){
		double satisfationSum = this.vehiclePrefWeights[0]*this.vehiclePrefWeights[vehicleID+1];
		this.priceWeights[1] = 8.8;
		this.priceWeights[2] = 4.2;
    	if( price >= this.priceWeights[1] ) {
    		//System.out.println(this.agentName+" "+price+" "+this.priceWeights[1]+" "+this.priceWeights[2]+" 1: "+this.vehiclePrefWeights[0]*this.vehiclePrefWeights[vehicleID+1]+" "+this.priceWeights[0]);
    		satisfationSum += this.priceWeights[0];
    	}else if(price > this.priceWeights[2]) {
    		//System.out.println(this.agentName+" "+price+" "+this.priceWeights[2]+" 2: "+this.vehiclePrefWeights[0]*this.vehiclePrefWeights[vehicleID+1]+" "+this.priceWeights[0]*(price - this.priceWeights[2])/(this.priceWeights[1] - this.priceWeights[2]));
    		satisfationSum += this.priceWeights[0]*(price - this.priceWeights[2])/(this.priceWeights[1] - this.priceWeights[2]);
    	}
    	
    	return satisfationSum;
	}
	
	@OPERATION
	public void getDecisionTree() {
		try {
			File base = new File("M:\\Downloads\\IC_DATA\\arrf_files\\base.arff");
			File copy = new File(this.filePath);
			Files.copy(base, copy);
			base = new File("M:\\Downloads\\IC_DATA\\arrf_files\\base.txt");
			copy = new File(this.treePath);
			Files.copy(base, copy);
			BufferedReader reader = new BufferedReader(new FileReader(this.folderPath+"/arrf_files/base_text.txt"));
			String line = null;
			this.textTree = "";
			while( (line = reader.readLine()) != null ){
				this.textTree += line+"\n";
			}
			reader.close();
			this.decision_tree = makeTree(this.textTree.split("\n"), 0, this.textTree.split("\n").length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@OPERATION
	public void getPredictionTree() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.priceTextPath));
			String line = null;
			this.textTree = "";
			while( (line = reader.readLine()) != null ){
				this.textTree += line+"\n";
			}
			reader.close();
			this.prediction_tree = makeTree(this.textTree.split("\n"), 0, this.textTree.split("\n").length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@OPERATION
	public void classifyArff(){
		try {
			Runtime rt = Runtime.getRuntime();
			
			boolean emptyTreeFlag = true;
			while(emptyTreeFlag) {
				Process pr = rt.exec("java weka.classifiers.trees.J48 -t "+this.filePath);
				
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	            String line=null;
	            for(int i = 0; i < 6; i++) {
	            	line = input.readLine();
	            }
	            if( line != null && line.equals("") ) {
		            this.textTree = "";
		            while( !(line = input.readLine()).equals("") ) {
		            	this.textTree += line+"\n";
		            }
		            //if( this.agentType == 0 ) System.out.println(this.agentName+ " tree:\n"+textTree+"\n");
		            this.decision_tree = makeTree(this.textTree.split("\n"), 0, this.textTree.split("\n").length);
		            BufferedWriter writer = new BufferedWriter(new FileWriter(this.textTreePath));
		            writer.write(this.textTree);
		            writer.close();
	            }else {
	            	this.decision_tree = null;
	            }
	            
	            if(this.decision_tree != null) {
	            	emptyTreeFlag = false;
	            }else {
	            	createArff();
	            }  
	            rt.freeMemory();
            	pr.destroy();
            	input.close();
			}
            
			//printTree(decision_tree);
			
			//System.out.println(this.agentName+"'s tree completed");
			
			printTreeRec(decision_tree, false, 1);
			//System.out.println();
		}catch (Exception e){
			//System.err.println("\nTHIS HORRIBLE ERROR:"+e.getMessage());
			System.err.println("\n"+this.agentName+" > ERROR | Arvore:\n"+this.textTree);
			e.printStackTrace();
		}
	}
	
	@OPERATION
	public void classifyForecastArff(){
		try {
			Runtime rt = Runtime.getRuntime();

			boolean emptyTreeFlag = true;
			while(emptyTreeFlag) {
				Process pr = rt.exec("java weka.classifiers.trees.J48 -t "+this.pricePath);
				
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				
	            String line=null;
	            for(int i = 0; i < 6; i++) {
	            	input.readLine();
	            }
	            this.textTree = "";
	            //System.out.println(this.agentName+"'s textTree:");
	            while(!(line = input.readLine()).equals("")) {
	            	//System.out.println(line);
	            	this.textTree += line+"\n";
	            }
	            //System.out.println();
	            
	            //System.out.println(this.agentName+ " tree:\n"+textTree+"\n");
	            this.prediction_tree = makeTree(this.textTree.split("\n"), 0, this.textTree.split("\n").length);
	            BufferedWriter writer = new BufferedWriter(new FileWriter(this.priceTextPath));
	            writer.write(this.textTree);
	            writer.close();
	            if(this.prediction_tree != null) {
	            	emptyTreeFlag = false;
	            }
	            rt.freeMemory();
            	pr.destroy();
            	input.close();
			}
            
			//printTree(decision_tree);
			
			//System.out.println(this.agentName+"'s price tree completed");

			printTreeRec(prediction_tree, false, 0);
			//System.out.println();
		}catch (Exception e){
			System.err.println("\n"+this.agentName+" > ERROR");
			//e.printStackTrace();
			//System.err.println("\n"+e.getMessage());
		}
	}
	
	@OPERATION
	public void checkOffer(String type, double price, int hour, OpFeedbackParam<String> answer, OpFeedbackParam<Double> value) {
		List<Pair<String, Double>> offer = new LinkedList<Pair<String, Double>>();
		offer.add( new Pair<String, Double>("preco", price) );
		offer.add( new Pair<String, Double>("tipo", (double) Arrays.asList(this.vehicleTypes).indexOf(type)));
		offer.add( new Pair<String, Double>("hora", (double) hour) );
		
//		double satisfactionSum = calcSatisfactionManager(Arrays.asList(this.vehicleTypes).indexOf(type), price);
		
		String result = decisionSearchRec(this.decision_tree, offer);
//		try{if(write == 1) {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true));
//			String arrfText = df2.format(price)+", "+type+", ";
//        	
//        	if( satisfactionSum >= this.satisfactionPref + this.counter/10 ) {
//        		//System.out.println(this.agentName+" "+arrfText+" - "+satisfactionSum+" "+(this.satisfactionPref+this.counter/10));
//        		arrfText += "Sim\n";
//        	}else {
//        		arrfText += "Nao\n";
//        	}
//        	writer.write(arrfText);
//			writer.close();
//		}}catch(Exception e) { e.printStackTrace(); }
		
		if( result.equals("Sim") ) {
			answer.set( "Sim" );
			value.set( 0.0 );
		}else {
			answer.set( this.failedAttribute.getFirst() );
			value.set( this.failedAttribute.getSecond() );
		}
		
	}
	
//	@OPERATION
//	public void checkOffer(String type, double price, double distdest, OpFeedbackParam<String> answer, OpFeedbackParam<Double> value) {
//		double satisfactionSum = 0;
//		//System.out.println("Checking: "+type+" | "+price+" | "+distdest);
//		List<Pair<String, Double>> offer = new LinkedList<Pair<String, Double>>();
//		
//		offer.add( new Pair<String, Double>("preco", price) );
//		
//		offer.add( new Pair<String, Double>("distanciadestino", distdest) );
//		
//		if( Arrays.asList(this.parkTypes).indexOf(type) == -1) {
//			offer.add( new Pair<String, Double>("tipo", (double) Arrays.asList(this.vehicleTypes).indexOf(type)));
//			satisfactionSum = calcSatisfactionManager(Arrays.asList(this.vehicleTypes).indexOf(type), price, (int) distdest);
//		}else {
//			offer.add( new Pair<String, Double>("tipo", (double) Arrays.asList(this.parkTypes).indexOf(type)));
//			satisfactionSum = calcSatisfactionDriver(Arrays.asList(this.parkTypes).indexOf(type), price, (int) distdest);
//		}
//		String result = decisionSearchRec(this.decision_tree, offer);
//		//System.out.println(this.agentName+": "+result);
//		if( result.equals("Sim") ) {
//			answer.set( "Sim" );
//			value.set( satisfactionSum );
//		}else {
//			answer.set( this.failedAttribute.getFirst() );
//			value.set( this.failedAttribute.getSecond() );
//		}
//		
//	}
	
	@OPERATION
	public void priceForecast(String spotType, String vehicleType, int day, String dayWeek, int hour, OpFeedbackParam<Double> predictedPrice) {
		List<Pair<String, Double>> prediction = new LinkedList<Pair<String, Double>>();

		prediction.add( new Pair<String, Double>("tipoveiculo", (double) Arrays.asList(this.vehicleTypes).indexOf(vehicleType)));
		prediction.add( new Pair<String, Double>("tipovaga", (double) Arrays.asList(this.spotTypes).indexOf(spotType)));
		prediction.add( new Pair<String, Double>("diasemana", (double) Arrays.asList(this.daysWeek).indexOf(dayWeek)));
		prediction.add( new Pair<String, Double>("dia", (double) day) );
		prediction.add( new Pair<String, Double>("hora", (double) hour) );
		
		predictedPrice.set( Double.parseDouble( decisionSearchRec(this.prediction_tree, prediction) ) );
	}
	
	private String decisionSearchRec(Node<String, Double> tree, List<Pair<String, Double>> offer) {
		Node<String, Double> node = tree;
		int index = -1;
		try {
		while( !node.isLeaf() ) {
			for( int i = 0; i < offer.size(); i++) {
				if( offer.get(i).getFirst().equals(node.key) ) { 
					index = i;
					break;
				}
			}
			if( node.value == -2 ) {
				//System.out.println(this.agentName+": "+offer.get(index).getSecond() +" "+node.value);
				node = node.children.get((int)(double)offer.get(index).getSecond());
			}else if( offer.get(index).getSecond() <= node.value ) {
				//System.out.println(this.agentName+" : "+offer.get(index).getSecond() +" "+node.value);
				node = node.children.get(0);
			}else {
				//System.out.println(this.agentName+": "+offer.get(index).getSecond() +" "+node.value);
				node = node.children.get(1);
			}
			//System.out.println(this.agentName+": "+node.isLeaf());
		}
		}catch (Exception e){
			System.err.println("\n"+"Last Node: "+node.key+" "+node.value+" "+node.children.get(0));
			e.printStackTrace();
		}
		this.failedAttribute = new Pair<String, Double>(node.parent.key, node.parent.value);
		return node.key;
	}
	
	private Node<String, Double> makeTree(String[] textTree, int start, int end){
		if( start == end ) return null;
		
		Node<String, Double> node = null;
		List<String[]> lines = new LinkedList<String[]>();
		List<Integer> positions = new LinkedList<Integer>();;
		
		lines.add(textTree[start].split(" "));
		positions.add(start);
		
		int depth = 0;
		for(int j = 0; j < lines.get(0).length; j++) {
    		if(lines.get(0)[j].equals("|") || lines.get(0)[j].equals("")) {
    			depth += 1;
    		}
    	}

		String[] auxLine;
		for(int i = start+1; i < end; i++) {
			auxLine = textTree[i].split(" ");
			if( auxLine[depth].equals(lines.get(0)[depth]) ) {
				lines.add(auxLine);
				positions.add(i);
			}
		} 

		for(int i = 0; i < lines.size(); i++) {
			if( lines.get(i).length == 1 ) {
				return null;
			}
			int idx = lines.get(i)[depth+2].indexOf(':');
			if( idx != -1 ) {
				lines.get(i)[depth+2] = lines.get(i)[depth+2].replace(":", "");
			}
			if(i == 0) {
				if( lines.get(i)[depth+1].equals("=")) {
					node = new Node<String, Double>(lines.get(i)[depth], -2.);
				}else {
					node = new Node<String, Double>(lines.get(i)[depth], Double.parseDouble(lines.get(i)[depth+2]));
				}
			}
			if(idx != -1) {
				node.addChild(new Node<String, Double>(lines.get(i)[depth+3], -1.));
			}else {
				if( i >= lines.size()-1) {
					node.addChild(makeTree(textTree, positions.get(i)+1, end));
				}else {
					node.addChild(makeTree(textTree, positions.get(i)+1, positions.get(i+1)));
				}
			}
		}
		return node;
	}
	
	private void printTreeRec(Node<String, Double> node, boolean printFlag, int f) {
		int[] indentFlags = new int[30];
		
		BufferedWriter writer = null;
		try {
			if( f == 0 ) {
				writer = new BufferedWriter(new FileWriter(this.priceTreePath));
			}else {
				writer = new BufferedWriter(new FileWriter(this.folderPath+"/arrf_files/"+this.agentName+"_"+counter+".txt"));
				counter++;
			}
			
			if(printFlag) {
				Arrays.fill(indentFlags, -1);
				printTreeRec(node, indentFlags);
			}
			
			Arrays.fill(indentFlags, -1);
			printTreeRec(node, indentFlags, writer);	
			
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void printTreeRec(Node<String, Double> node, int[] indentFlags) {
		if(node == null) return;
		
		if(indentFlags[0] == -1) {
			System.out.println(decision_tree.key + " : "+ decision_tree.value);
		}
		int depth = node.getLevel();
		for(int i = 0; i < node.children.size(); i++) {
			String indent = "";
			Arrays.fill(indentFlags, depth, indentFlags.length, -1);
			if(i < node.children.size()-1) {
				indent += makeIndent(indentFlags) + "   ├── ";
				indentFlags[depth] = 1;
			}else {
				indent += makeIndent(indentFlags) + "   └── ";
				indentFlags[depth] = 0;
			}
			System.out.println(indent + node.children.get(i).key + " : "+ node.children.get(i).value);
			printTreeRec(node.children.get(i), indentFlags);
		}
	}
	
	private void printTreeRec(Node<String, Double> node, int[] indentFlags, BufferedWriter writer) throws IOException {
		if(node == null) return;
		
		if(indentFlags[0] == -1) {
			writer.write(node.key + " : "+ node.value+"\n");
		}
		int depth = node.getLevel();
		for(int i = 0; i < node.children.size(); i++) {
			String indent = "";
			Arrays.fill(indentFlags, depth, indentFlags.length, -1);
			if(i < node.children.size()-1) {
				indent += fileIndent(indentFlags) + "   |--- ";
				indentFlags[depth] = 1;
			}else {
				indent += fileIndent(indentFlags) + "   \\--- ";
				indentFlags[depth] = 0;
			}
			writer.write(indent + node.children.get(i).key + " : "+ node.children.get(i).value+"\n");
			printTreeRec(node.children.get(i), indentFlags, writer);
		}
	}
	
	private static String makeIndent(int[] indentFlags) {
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < indentFlags.length; i++) {
			if(indentFlags[i] == 1) {
				sb.append("   │   ");
			}
			if(indentFlags[i] == 0) {
				sb.append("       ");
			}
		}
		return sb.toString();
	}
	
	private static String fileIndent(int[] indentFlags) {
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < indentFlags.length; i++) {
			if(indentFlags[i] == 1) {
				sb.append("   |   ");
			}
			if(indentFlags[i] == 0) {
				sb.append("       ");
			}
		}
		return sb.toString();
	}
	
	private void printTree(Node<String, Double> tree) {
		for ( Node<String, Double> node : tree) {
			String indent = createIndent(node.getLevel());
			System.out.println(indent + node.key + " : "+ node.value);
		}
	}
	
	private static String createIndent(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append("|   ");
		}
		return sb.toString();
	}
}
