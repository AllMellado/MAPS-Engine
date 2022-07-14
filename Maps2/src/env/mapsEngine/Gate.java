// CArtAgO artifact code for project mAPS

package mapsEngine;

import cartago.Artifact;
import cartago.OPERATION;

public class Gate extends Artifact {
	
	void init() {		
	}


	@OPERATION
	public void openGate(){
		//System.out.println("Opening gate!");
		
	}
	
	@OPERATION
	public void closeGate(){
		//System.out.println("Closing gate!");
	}
	

}