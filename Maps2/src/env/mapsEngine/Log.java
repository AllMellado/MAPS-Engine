// CArtAgO artifact code for project mapsEngine

package mapsEngine;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cartago.*;
import io.netty.util.Signal;

public class Log extends Artifact {
	
	private static final DecimalFormat df2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
	
	final static long TICK_TIME = 1;
	
	private List<String> mainLog;
	private List<LinkedList<String>> managersLog;
	private String[] week = {"Segunda","Terca","Quarta","Quinta","Sexta","Sabado","Domingo"};
	private int dayWeek;
	private int daysCounter;
	private int dayFlag;
	private String filePath;
	private String[][] data;
	private int nrAgents;
	private int logSize;
	private int year;
	private int month;
	private int day;
	private int hour;
	private int min;
	private int sec;
	private int pastDay;
	private long past;
	private boolean startFlag;
	
	void init(String filePath, int n_managers) {
		this.dayWeek = 0;
		this.daysCounter = 0;
		this.nrAgents = 0;
		this.logSize = 0;
		this.dayFlag = 1;
		this.mainLog = new LinkedList<String>();
		this.managersLog = new LinkedList<LinkedList<String>>();
		for(int i = 0; i < n_managers; i++) {
			this.managersLog.add( i, new LinkedList<String>() );
		}
		this.data = new String[100000][4];
		this.filePath = filePath;
		
		this.year = 2021;
		this.month = 12;
		this.day = 1;
		this.pastDay = 1;
		this.hour = 0;
		this.min = 0;
		this.sec = 0;
		this.startFlag = true;
		System.out.println("Starting date "+this.year+"/"+this.month+"/"+this.day+" "+this.hour+":"+this.min+":"+this.sec);
		try {
			File dir = new File(this.filePath);
			if( !dir.exists() ) {
				dir.mkdirs();
				String header = "Manager;ParkType;Sector;SpotId;SpotType;Driver;VehicleType;"
						+ "Description;Rule;DiaSemanda;Year;Date(M/D);"
						+ "EventTime(H:M:S);Delay(S);ArriveTime(H:M:S);LeaveTime(H:M:S);"
						+ "BasePrice;NegotiatedPrice;PriceChange;PriceVariation(%);"
						+ "TotalSpots;SpotsUsed;OcupationRate(%)\n";
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath+"/main.csv"));
				writer.write(header);
				writer.close();
				for(int i = 0; i < n_managers; i++) {
					writer = new BufferedWriter(new FileWriter(this.filePath+"/manager_"+(i+1)+".csv"));
					writer.write(header);
					writer.close();
				}
			}
			
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@OPERATION
	public void poisson(double mean, OpFeedbackParam<Integer> result) {
	    Random r = new Random();
	    double L = Math.exp(-mean);
	    int k = 0;
	    double p = 1.0;
	    do {
	        p = p * r.nextDouble();
	        k++;
	    } while (p > L);
	    result.set(k - 1);
	}
	
	@OPERATION
	public void forceUpdate() {
		updateDateTime();
	}
	
	private void updateDateTime() {
		if(this.startFlag) {
			this.past = System.nanoTime()/100000;
			this.startFlag = false;
		}
		long present = System.nanoTime()/100000;
		int hourchange = 0;
		int timelapsed = Math.abs( (int)(present - this.past) );
		
		this.past = present;
		
		this.sec += timelapsed;
		
		if( this.sec/60 >= 1) {
			this.min += this.sec/60;
			this.sec = this.sec%60;
		}
		if( this.min/60 >= 1) {
			this.hour += this.min/60;
			this.min = this.min%60;
			hourchange = 1;
		}
		if( this.hour/24 >= 1 ) {
			this.day += this.hour/24;
			this.hour = this.hour%24;
		}
		if( (double)this.day/30 > 1 ) {
			this.month += this.day/30;
			this.day = this.day%30;
		}
		if( (double)this.month/12 > 1 ) {
			this.year += this.month/12;
			this.month = this.month%12;
		}
		
		if( this.dayFlag != day) {
			this.dayFlag = day;
			this.dayWeek++;
			if(this.dayWeek > 6) {
				this.dayWeek = 0;
			}
		}
		
		if( this.day != this.pastDay ) {
			this.dayWeek++;
			if(this.dayWeek > 6) {
				this.dayWeek = 0;
			}
			this.pastDay = this.day;
			this.daysCounter++;
			System.out.println("## SYSTEM DATE>> "+this.year +"/"+ this.month +"/"+ this.day +" "+ this.hour +":"+ this.min);
			signal("updateDateTimeDay",this.year,this.month,this.day,this.hour,this.min);
		}
		
		if(hourchange == 1) {
			System.out.println("DATE TIME: "+this.year +"/"+ this.month +"/"+ this.day +" "+ this.hour +":"+ this.min);
			signal("updateDateTime",this.year,this.month,this.day,this.hour,this.min);
		}
	}
	
	public int getDay() {
		System.out.println("DAY "+this.day);
		return this.day;
	}
	
	@OPERATION
	public void getHourWeek(OpFeedbackParam<Integer> hour, OpFeedbackParam<String> dayWeek) {
		updateDateTime();
		hour.set(this.hour);
		dayWeek.set(this.week[this.dayWeek]);
	}
	
	@OPERATION
	public void getDayWeek(int days, int hours, OpFeedbackParam<Integer> day, OpFeedbackParam<String> dayWeek, OpFeedbackParam<Integer> hour, OpFeedbackParam<Integer> dateTime) {
		updateDateTime();
		
		int futureHour = this.hour + hours;  
		int futureDay = this.day + days;
		int futureDayWeek = this.dayWeek + days;
		int futureMonth = this.month;
		int futureYear = this.year;
		
		if( futureHour/24 >= 1 ) {
			futureDay += futureHour/24;
			futureDayWeek += futureHour/24;
			futureHour = futureHour%24;
		}
		
		if( (double)futureDay/30 > 1 ) {
			futureMonth += futureDay/30;
			futureDay = futureDay%30;
		}
		
		if( (double)futureMonth/12 > 1 ) {
			futureYear += futureMonth/12;
			futureMonth = futureMonth%12;
		}
			
		if( (double)futureDayWeek/6 > 1 ) {
			futureDayWeek = futureDayWeek%6; 
		}
		
		day.set(futureDay);
		dayWeek.set(this.week[futureDayWeek]);
		hour.set(futureHour);
		dateTime.set(futureYear*12*30*24+futureMonth*30*24+futureDay*24+futureHour);
	}
	
	@OPERATION 
	public void fileWrite(	Object manager, Object driver, Object sector, Object spot, 
							Object parkType, Object spotType, Object vehicleType,
						 	Object desc, Object rule, Object basePrice, Object negotiatedPrice,
						 	Object occupation, Object spotMax, Object spotUsed, Object[] nrFreeSpots ) {
		
		updateDateTime();
		
		occupation = df2.format(Double.parseDouble(occupation.toString()));
		
		long delayTime = 0;
		int flag = 0;
		try{
			for(int i = 0; i < this.nrAgents; i++) {
				if(this.data[i][0].equals(driver.toString()) && this.data[i][3].equals(manager.toString()) ){
	
					flag = 1;
					String pastDateTime[] = this.data[i][1].split("#");
					String pastDates[] = pastDateTime[0].split("/");
					String pastTimes[] = pastDateTime[1].split(":");
						
					this.data[i][1] = this.year +"/"+ this.month +"/"+ this.day +"#"+ this.hour +":"+ this.min +":"+ this.sec;
						
					long yearDelay   = 				    this.year  - Integer.parseInt(pastDates[0]);
					long monthDelay  = yearDelay*12   + this.month - Integer.parseInt(pastDates[1]);
					long dayDelay    = monthDelay*30  + this.day   - Integer.parseInt(pastDates[2]);
					long hourDelay   = dayDelay*24    + this.hour  - Integer.parseInt(pastTimes[0]);
					long minuteDelay = hourDelay*60   + this.min   - Integer.parseInt(pastTimes[1]);
					delayTime        = minuteDelay*60 + this.sec   - Integer.parseInt(pastTimes[2]);
					
					//oldPrice = this.data[i][2];
					//this.data[i][2] = currentPrice.toString();
					
					break;
				}
			}
			
			if(flag == 0 & !driver.toString().equals("?")) {
				this.data[this.nrAgents][0] = driver.toString();
				this.data[this.nrAgents][1] = this.year +"/"+ this.month +"/"+ this.day +"#"+ this.hour +":"+ this.min +":"+ this.sec;
				//this.data[this.nrAgents][2] = currentPrice.toString();
				this.data[this.nrAgents][3] = manager.toString();
				this.nrAgents++;
			}
			
			double variation = 0;
			double change = 0;
			String ngPrice = negotiatedPrice.toString();
			if( !negotiatedPrice.toString().equals("?") ) {
				ngPrice = df2.format(Double.parseDouble(negotiatedPrice.toString()));
				change = Double.parseDouble(negotiatedPrice.toString()) - Double.parseDouble(basePrice.toString());
				variation = Double.parseDouble(negotiatedPrice.toString())/Double.parseDouble(basePrice.toString());
				variation = -1*(100 - 100*variation);
				if( variation == 0 ) variation *= -1;
			}
			
			String timeArrive = "?";
			String timeLeave = "?";
			if(desc.toString().equals("SpotAllocated") || desc.toString().equals("ReservedSpotAllocated")){
				timeArrive = this.hour+":"+this.min+":"+this.sec; 
			}
			if(desc.toString().equals("LeavingSpot")) {
				timeLeave = this.hour+":"+this.min+":"+this.sec;
			}
			
			String strMonth;
			if(this.month < 10) {
				strMonth = "0"+this.month;
			}else {
				strMonth = ""+this.month;
			}
			String strDay;
			if(this.day < 10) {
				strDay = "0"+this.day;
			}else {
				strDay = ""+this.day;
			}
			String strHour;
			if(this.hour < 10) {
				strHour = "0"+this.hour;
			}else {
				strHour = ""+this.hour;
			}
			String strMin;
			if(this.min < 10) {
				strMin = "0"+this.min;
			}else {
				strMin = ""+this.min;
			}
			String strSec;
			if(this.sec < 10) {
				strSec = "0"+this.sec;
			}else {
				strSec = ""+this.sec;
			}
			//	Manager ParkType Sector SpotId 
			//	SpotType Driver VehicleType
			//	Description Rule DiaSemanda Year Date(M/D)
			//	EventTime(H:M:S) Delay(S) ArriveTime(H:M:S) LeaveTime(H:M:S)
			//	CurrentPrice OldPrice PriceChange PriceVariation(%)"
			//	TotalSpots SpotsUsed OcupationRate(%)
			
			String str = manager.toString()+";"+parkType.toString()+";"+sector.toString()+";"+spot.toString()+";"
						+spotType.toString()+";"+driver.toString()+";"+vehicleType.toString()+";"
						+desc.toString()+";"+rule.toString()+";"+this.week[this.dayWeek]+";"
						+this.year+";"+strMonth+"|"+strDay+";"+strHour+":"+strMin+":"+strSec+";"
						+delayTime+";"+timeArrive+";"+timeLeave+";"
						+basePrice.toString()+";"+ngPrice+";"+df2.format(change)+";"+df2.format(variation)+";"
						+spotMax.toString()+";"+spotUsed.toString()+" ("+nrFreeSpots[0]+"|"+nrFreeSpots[1]+");"+occupation.toString()+"\n";
			
//			if( this.month > 0 ) {
//				BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath+"/main.csv", true));
//				writer.write(str);
//				writer.close();
//				
//				BufferedWriter writerMngr = new BufferedWriter(new FileWriter(this.filePath+"/"+manager+".csv", true));
//				writerMngr.write(str);
//				writerMngr.close();
//			}
			int index = Integer.parseInt(manager.toString().split("_")[1])-1;
			if(this.daysCounter >= 1 ) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath+"/main.csv", true));
				for ( String line : this.mainLog ) {
					writer.write(line);
				}
				writer.close();
				this.mainLog.clear();
				BufferedWriter writerMngr = null;
				for(int i = 0; i < this.managersLog.size(); i++) {
					writerMngr = new BufferedWriter(new FileWriter(this.filePath+"/manager_"+(i+1)+".csv", true));
					for( String line : this.managersLog.get(i) ) {
						writerMngr.write(line);
					}
					writerMngr.close();
					this.managersLog.get(i).clear();
				}
				this.daysCounter = 0;
			}
			if( this.month < 20) {
				this.mainLog.add(str);
				this.managersLog.get(index).add(str);
			}else {
				this.daysCounter = -1;
			}
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
}

