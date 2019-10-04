// Agent manager in project testMaps

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }


/* Initial beliefs and rules */

nSpotsMAX(10).
nSpotsUsed(0).
isFull(false).
pFull(0).
basePrice(10).
currentPrice(10).
minute(00).
hour(00).
day(01).
month(01).

//-3.80356167695194, 43.46296641666926
spot(0,"ParkingSpot","free","offstreet","Relationship","Maps","geojson","24").
spot(1,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(2,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(3,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(4,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(5,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(6,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(7,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(8,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
spot(9,"ParkingSpot","free","offstreet","Relationship","Maps","geo:json","24").
//spot(10,0, "EMPTY").
//spot(11,0, "EMPTY").
//spot(12,0, "EMPTY").
//spot(13,0, "EMPTY").
//spot(14,0, "EMPTY").
//spot(15,0, "EMPTY").
//spot(16,0, "EMPTY").
//spot(17,0, "EMPTY").
//spot(18,0, "EMPTY").
//spot(19,0, "EMPTY").
//spot(20,0, "EMPTY").
//spot(21,0, "EMPTY").
//spot(22,0, "EMPTY").
//spot(23,0, "EMPTY").
//spot(24,0, "EMPTY").



/* Initial goals */

!setupParking.

//Initials goals and beliefs

+!setupParking <-
	     makeArtifact("a_Gate", "mapsEngine.Gate", ["Starting"], ArtId);
	     focus(ArtId);
	     .print("Parking has opened");
	     
	     makeArtifact("a_Control", "mapsEngine.QueueControl", ["20"], ArtId2);
	     focus(ArtId2);
	     
	     !updatePricing;
		 !timeFlow.

//"F:\\Downloads/data.csv"
+!updatePricing : basePrice(Bp) &
		 currentPrice(Cp) &
		 pFull (P) &
		 month (M) &
		 hour (H) &
		 minute(Min)	
		<-
		.println("Old Price: R$",Cp);
		cartago.new_obj("mapsEngine.Price",[Bp],Id);
		cartago.invoke_obj(Id,getMatrixData("F:\\Downloads/data.csv"));
		//cartago.invoke_obj(Id,getMatrixData("F:\\Downloads/data2.csv"));
		cartago.invoke_obj(Id,updatePrice("Horario;MeioDia/Evento;Festival;Musica/Demanda;Alta"),Res);
		.println("New Price: R$",Res,"\n").

+!timeFlow : minute(Min) & 
			 hour(H) &
			 day(D) &
			 month(M)   
		<- 
		//.print(H,":",Min," ",D,"/",M);
		-minute(Min);
		if( Min >= 60 ){
			+minute(0);
			-hour(H);
			if(H+1 >= 24){
				+hour(0);
				-day(D);
				if( D >= 3 ){
					+day(1);
					-month(M);
					if(M >= 12){
						+month(1);
					}else{
						+month(M+1);
					}
				}else{
					+day(D+1);
				}
			}else{
				+hour(H+1);
			}
			!updatePricing;
		}else{
			+minute(Min+1);
		};
		.wait(100);
		!timeFlow.
	     
+!requestSpot(TRUST)[source(AG)] <-
	.term2string(AG,AGENT);
	.print("Agent: ",AGENT," has requested a spot! - Background:(",TRUST,")");	
	!allocateSpot(AGENT,TRUST).
	
+!requestSpotQueue(AGENT,TRUST) <-
	.print("Agent: ",AGENT," has requested a spot from Queue! - Background:(",TRUST,")");	
	!allocateSpot(AGENT,TRUST).
	
	
+!allocateSpot(AGENT,TRUST) : 
					nSpotsUsed(N) & 
					nSpotsMAX(MAX) & 
					isFull(COND) &
					pFull(P) & 
					COND = false 
					<- 
					
					+~find;
					
					for(spot(Id,Ty,A,Ct,Ref,Loc,Tloc,Coord)){
						if(A = "free" & ~find & (COND = false)){
							-spot(Id,Ty,A,Ct,Ref,Loc,Tloc,Coord);
							+spot(Id,Ty,AGENT,Ct,Ref,Loc,Tloc,Coord);	
							
							.print("Spot (",Id,") has allocated for the agent: ",AGENT);
							
							openGate;
							
							.send(AGENT,tell,spotOk); 
							.send(AGENT,achieve,park(Id));
							
										
							-nSpotsUsed(N); 
							+nSpotsUsed(N+1);
							
							if((N+1) = MAX){
								-isFull(COND);
								+isFull(true);
								.print("Parking lot FULL!");
							};
							
							-pFull(P);					
							+pFull(((N+1) * 100) / MAX);
							.print("Parking usage: ",((N+1) * 100) / MAX,"%");
							
							closeGate;
							
							
							//!printSpots;			
							-~find;
						}
					};
					+~find.
	
+!allocateSpot(AGENT,TRUST) : isFull(COND) & COND = true <- 
		insertDriverQueue(AGENT,TRUST).
		
	
+!printSpots <-
	for(spot(Id,Ty,A,Ct,Ref,Loc,Tloc,Coord)){
	     	 	.print("Spot: ",Id," - Condition: ",A," - Ct: ",Ct," - Ref: ",Ref);	     
	     }.
	     
+!checkQueue : nSpotsUsed(N) & isFull(COND)<-
	
	isAnyone(C);
	if(C = false){
		freeDriver(AG,BG);
		!requestSpotQueue(AG,BG);
	}else{
		.print("Nobody at queue");
	}.
	     
+!leaveSpot(Id)[source(AG)] : nSpotsUsed(N) & nSpotsMAX(MAX) &  isFull(COND) &
					pFull(P) <-
	.term2string(AG,AGENT);
	.print(AGENT," leaving the spot: ",Id);
	
	-nSpotsUsed(N);
	+nSpotsUsed(N-1);
	
	-isFull(COND);
	+isFull(false);
	
	-spot(Id,Ty,AGENT,"offstreet","Relationship","Maps","geo:json","24");
	+spot(Id,Ty,"free","offstreet","Relationship","Maps","geo:json","24");	

	-pFull(P);					
	+pFull(((N-1) * 100) / MAX);
	.print("Parking usage: ",((N-1) * 100) / MAX,"%");
	.kill_agent(AGENT);
	
	!checkQueue.
	


