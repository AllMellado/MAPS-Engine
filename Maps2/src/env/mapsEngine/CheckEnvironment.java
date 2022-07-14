// CArtAgO artifact code for project mapsEngine

package mapsEngine;

import java.util.Random; 

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class CheckEnvironment extends Artifact {
	
	private String[][] legendData;
	private String[][] matrixData;
	
	TimeSimulation TS;
	
	double past_occupancy;
	int past_weather;
	
	int dm_line = -1;
	double[] dm_intervals;
	
	int wt_line = -1;
	int[] wt_intervals;
	
	int tm_line = -1;
	int[] tm_intervals;
	
	void init() { 
		this.past_occupancy = 0;
		this.past_weather = 0;
	}
	
	
	@OPERATION
	public void getLegendData(String filePath){
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
	
	@OPERATION
	public void check( double occupancy, int nr_requests, int nSpotsMax, Object[] dateTime, OpFeedbackParam<String> rule) {		
		
		String Dm_str = null;
		String Wt_str = null;
		String Tm_str = null;
		
		if(this.dm_line != -1) {
			Dm_str = checkDemand(occupancy, nr_requests, nSpotsMax);
		}
		if(this.wt_line != -1) {
			Wt_str = checkWeather();
		}
		if(this.tm_line != -1) {
			Tm_str = checkTime( Integer.parseInt(dateTime[3].toString()), Integer.parseInt(dateTime[4].toString()));
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
		
		rule.set(ruleAux);
	}
	
	public String checkDemand(double occupancy,  int nr_requests, int nSpotsMax) {
		double P = (this.past_occupancy+nr_requests)/nSpotsMax;
		this.past_occupancy = occupancy; 
		
		if(P > 1) {
			P = 1;
		}
		
		if(dm_line == -1) {
			return null;
		}
		
		for(int i = 1; i < this.dm_intervals.length; i++) {
			if( P >= this.dm_intervals[i-1] && P <= this.dm_intervals[i]) {
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

	public String checkTime( int hour, int minute) {
		
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

