// CArtAgO artifact code for project mapsEngine

package mapsEngine;

import cartago.*;

public class CheckTime extends Artifact {
	
	boolean counting;
	final static long TICK_TIME = 100;
	TimeSimulation TS;
	
	void init() {
		//System.out.println("Artifact created");
		//this.counting = false;
		//this.TS = TimeSimulation.getInstance();
		//defineObsProperty("time",0,0,0);
	}
	
	@OPERATION 
	void start() {
		if(!this.counting) {
			this.counting = true;
			execInternalOp("count");
		}else {
			failed("already counting");
		}
	}
	
	@OPERATION
	void stop() {
		this.counting = false;
	}
	
	@INTERNAL_OPERATION
	void count() {
		while(this.counting) {
			signal("tick");
			await_time(TICK_TIME);
		}
	}
	
	@OPERATION
	void checkTime(OpFeedbackParam<Integer[]> Time) {
		String dateTime = null;//this.TS.getDateTime();
		//ObsProperty prop = getObsProperty("time");

		String[] temp = dateTime.split("-");
		String[] date = temp[0].split("/");
		
		Integer[] dates = {Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2])};
		
		//prop.updateValues(dates[0], dates[1], dates[2]);
		
		Time.set(dates);
	}
}

