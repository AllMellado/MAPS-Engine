// CArtAgO artifact code for project mapsEngine

package mapsEngine;

import java.util.Random;

import cartago.*;

public class CheckWeather extends Artifact {
	
	void init(int initialValue) {
	}
	
	@OPERATION
	public void check(OpFeedbackParam<Integer> flag){
		Random generator = new Random();
		int intValue = generator.nextInt(100);
		if(intValue < 30) {
			flag.set(1); 
		}else if(intValue >= 30 && intValue < 60){
			flag.set(2);
		}else if(intValue >= 60){
			flag.set(3);
		}
			
	}

}

