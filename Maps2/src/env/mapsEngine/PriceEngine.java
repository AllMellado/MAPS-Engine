// CArtAgO artifact code for project mapsEngine

package mapsEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import cartago.*;

public class PriceEngine extends Artifact {
	
	private String agentName;
	private double basePrice;
	private String logFolder;
	private String[][] legendData;
	private String[][] matrixData;
	
	double past_occupancy;
	int past_weather;
	
	int dm_line = -1;
	double[] dm_intervals;
	
	int wt_line = -1;
	int[] wt_intervals;
	
	int tm_line = -1;
	int[] tm_intervals;
		
	void init(float Bp, String name, String logFolder) {
		this.agentName = name;
		this.basePrice = Bp;
		this.logFolder = logFolder;
		this.past_occupancy = 0;
		this.past_weather = 0;
	}

// ------ UPDATE PRICE -----------
	
	@OPERATION
	public void updatePrice(int day, OpFeedbackParam<Double> NewMod, OpFeedbackParam<String> RuleUsed) {
		String terms = check(day);
		String[] auxTerms = terms.split("/");
		String[] tempTerms;
		int rule = 0;
		double value;
		double newMod = 0;
		int columns = this.matrixData[0].length;
		
		if( auxTerms[0].equals("Start") ) {
			NewMod.set(0.0);
			RuleUsed.set("null");
			return;
		}
		String priceStr = "";
		for(int i = 0; i < auxTerms.length; i++) {
			rule = findTerm(auxTerms[i]);
			tempTerms = auxTerms[i].split(":");
			
			priceStr += "["+this.agentName+"] now using rule ";
			for(int j = 0; j < tempTerms.length; j++) {
				priceStr += tempTerms[j]+" ";
			}
			
			if(rule == -1) {
				priceStr += "default modification: 0.0% > Current Price = "+this.basePrice;
				System.out.println(priceStr);
				NewMod.set(0.0);
				RuleUsed.set("null");
				return;
			}
			
			value = Double.parseDouble(this.matrixData[rule][columns-1]);
			
			newMod += value;
			priceStr += " - updated modification: "+value+"%";
		}
		System.out.println(priceStr+" > Current Price = "+(this.basePrice+this.basePrice*newMod) );
		NewMod.set(newMod);
		RuleUsed.set(terms);
	}
	
	public int findTerm(String terms) {
		
		String[] auxTerms = terms.split(":");
		int[] idArray = new int[this.matrixData.length];
		int[] proxIdArray = new int[this.matrixData.length];
		
		for(int i = 0; i < idArray.length; i++ ) {
			idArray[i] = i;
		}
		
		int size = this.matrixData.length;
		int nextSize;
		int column = 0;
		int end  = 0;
		while (end != 1) {
			nextSize = 0;
			for (int i = 0; i < size; i++) {
				if (this.matrixData[idArray[i]][column].equals(auxTerms[column]) || this.matrixData[idArray[i]][column].equals("?")) {
					proxIdArray[nextSize++] = idArray[i];
				}
			}
			idArray = proxIdArray;
			column++;
			size = nextSize;
			if (size <= 1) {
				end = 1;
			}
		}

		if (size == 0)
			return -1;

		return idArray[0];
	}

// ------ CHECK ENVIRO -----------
	
	@OPERATION
	public void getConfigurationData(String filePath){
		Reader file = Reader.getInstance(filePath);
		this.legendData = file.getLegendData();
		this.matrixData = file.getMatrixData();
		
		String[] auxIntervals;
		for(int i = 0; i < this.legendData.length; i++) {
			auxIntervals = this.legendData[i][2].split("-");
			
			if( this.legendData[i][0].equals("1") ) {
				this.dm_line = i;
				this.dm_intervals = new double[auxIntervals.length];
				for(int j = 0; j < auxIntervals.length; j++) {
					this.dm_intervals[j] = Double.parseDouble(auxIntervals[j]);
				}
			}
			
			if( this.legendData[i][0].equals("2") ) {
				this.wt_line = i;
				this.wt_intervals = new int[auxIntervals.length];
				for(int j = 0; j < auxIntervals.length; j++) {
					this.wt_intervals[j] = Integer.parseInt(auxIntervals[j]);
				}
			}
			
			if( this.legendData[i][0].equals("3") ) {
				this.tm_line = i;
				this.tm_intervals = new int[auxIntervals.length];
				for(int j = 0; j < auxIntervals.length; j++) {
					this.tm_intervals[j] = Integer.parseInt(auxIntervals[j]);
				}
			}
		}
		
	}
	
	public String check(int day) {		
		String Dm_str = null;
		String Wt_str = null;
		String Tm_str = null;
		
		if(this.dm_line != -1) {
			Dm_str = checkDemand(day);
		}
		if(this.wt_line != -1) {
			Wt_str = checkWeather();
		}
		if(this.tm_line != -1) {
			Tm_str = checkTime();
		}
		String ruleAux = "";
		
		if(Dm_str != null) {
			ruleAux += Dm_str+"/";
		}
		if(Wt_str != null) {
			ruleAux += Wt_str+"/";
		}
		if(Tm_str != null) {
			ruleAux += Tm_str+"/";
		}
		
		return ruleAux;
	}
	
	public String checkDemand(int day) {
		double mean = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(this.logFolder+"/"+this.agentName+".csv"));
			String line = reader.readLine();
			String[] words = null;
			double sum = 0;
			int counter = 0;
			line = reader.readLine();
	        while( line != null ) {
	        	words = line.split(";");
	        	if( Integer.parseInt(words[11].split("\\|")[1]) == day ) {
	        		counter++;
	        		sum += Double.parseDouble(words[22]);
	        	}
	        	line = reader.readLine();
	        }
	        if(counter > 0) {
	        	mean = (sum/counter)/100;
	        }
	       
		}catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 1; i < this.dm_intervals.length; i++) {
			if( mean >= this.dm_intervals[i-1] && mean <= this.dm_intervals[i]) {
				return createTerm(this.legendData[this.dm_line][1],i-1);
			}
		}
		return null;
	}
	
	public String checkWeather(){
		Random generator = new Random();
		int intValue = generator.nextInt(3)+(this.past_weather-1); 
		
		if(intValue < 0) intValue = 0;
		if(intValue >= this.wt_intervals.length) intValue = this.wt_intervals.length-1;
		
		this.past_weather = intValue;
		
		if(wt_line == -1) {
			return null;
		}
		
		for(int i = 0; i < this.wt_intervals.length; i++) {
			if( intValue == this.wt_intervals[i] ) {
				return createTerm(this.legendData[this.wt_line][1],i);
			}
		}
		
		
		return null;
	}

	public String checkTime() {
		int hour = 0;
		if(tm_line == -1) {
			return null;
		}
		
		for(int i = 1; i < this.tm_intervals.length; i++) {
			if( hour >= this.tm_intervals[i-1] && hour < this.tm_intervals[i]) {
				return createTerm(this.legendData[this.tm_line][1],i-1);
			}
		}
		
		
		return null;

	}
	
	public String createTerm(String str, int key){
		for(int i = 0; i < this.matrixData.length; i++) {
			if( this.matrixData[i][0].equals(str) ) {
				return str+":"+this.matrixData[i+key][1];
			}
		}
		
		return null;
	}
}

