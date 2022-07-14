// Agent driver in project testMaps

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }


/* Initial beliefs and rules */

//showAllConsole.
//showNegotiation.
//showReservation.
					
minmaxBudget(200, 500).

tickTime(365).

velocity(50).

dateTime(2021,12,30,0,0).

timeCounter(-1).

start.

/* Initial goals */

!setupAgent.
 
+updateDateTime(Y,M,D,H,Mi) : start <-
	-+dateTime(Y,M,D,H,Mi);
	-start;
	poisson(1,Hours);
	-+wait(Hours*60);
	+first;
	+waiting.
	
+updateDateTimeDay(Y,M,D,H,Mi) : waitNextDay <-
	-waitNextDay.

+updateDateTime(Y,M,D,H,Mi) : waiting & wait(Minutes) & not waitNextDay<-
	-+dateTime(Y,M,D,H,Mi);
	?timeCounter(StartDateTime);
	if( StartDateTime == -1 ){
		-+timeCounter(Y*12*30*24*60+M*30*24*60+D*24*60+H*60+Mi);
		+start;
	}
	DateTime = Y*12*30*24*60+M*30*24*60+D*24*60+H*60+Mi;
	
	if( not start & DateTime-StartDateTime + 90 >= Minutes ){
		-+timeCounter(-1);
		-waiting;
		!continue;
	}
	-start.

+!continue : first <-
	-first;
	!setAttributes.

+!continue : negotiate <-
	-negotiate;
	!parkingNegotiation.

+!continue : arrive & chosenReservation(Manager, FutureDay, FutureHour, DateTime) <-
	-arrive;
	-booking;
	if(showReservation){.print("Confirming reservation with ",Manager);}
	.send(Manager, askOne, confirmReservation,Answer,500);
	if( not Answer == false & not Answer == timeout ){
		!moveToParking;
	}else{
		.print("Reservation not allocated by ",Manager);
		!parkingNegotiation;
	}.
	
+!continue : leave <-
	-leave;
	!leaveSpot.
	
+!continue : not leave & not arrive & not negotiate <-
	-a.

+!setupAgent <-	
	.wait(300);
	
  	lookupArtifact("a_Log",ArtLog);
  	focus(ArtLog);
  	
	.send(creator, askOne, managersList(MList),List);
	+List;
	
//	?arffPath(Arff_Path);
//	.my_name(N);
//	.concat("a_arff", N, ArtArffName);
//	makeArtifact(ArtArffName,"mapsEngine.ArffMethods", [Arff_Path, 1], ArtArff);
//	focus(ArtArff);
	
	?driverConf(VD,IA,PR);
	.nth(0,VD,VT);
	.nth(1,VD,PC);
	
	.nth(0,PR,MP);
	.nth(1,PR,WP);
	
	+vehicleType(VT);
	+mySpotType("Standard");
  	+priceRange(PC*WP,MP,IA/PC);
	
//  	.concat(N,"_",VT,Name);
//  	setPathName(Name, "");
//  	setWeights(PF, MainWeigths, PrWeigths, VhWeights, DistWeigths);
//	//createArff;
//  	//classifyArff;
//  	getDecisionTree;
  	
  	//.print("type ",VT,", looking for ",ST," spots and pref price of R$",PF," per 30 minutes.");
  	
  	.send("creator",askOne,driverReady).

+!setAttributes  <-
  	poisson(1,Days);
  	poisson(14,Hours);
  	poisson(34, SpMinutes);
	poisson(20, SpHours);
	-+timeToSpend(SpHours, SpMinutes);
	+waitNextDay;
  	-+wait(Days*24*60+Hours*60);
  	+negotiate;
  	+waiting.

+!fileWrite(Manager, Sec, ID, ST, VT, Desc) <-
	fileWrite(Manager, Driver, Sec, ID, PT, ST, VT, Desc, "?", "?", "?", "?", "?", "?").

+!moveToParking : chosenOffer(Manager, Prc) & spotSecured(Manager)<-
	Time = 50 + math.floor(math.random(450));
	-spotSecured(Manager);
	.concat("MovingToParking",Time,String);
	.send(Manager, achieve, driverWrite(String));
	.wait(Time);
	
	if(showAllConsole){.print("Arrived at parking");}
	!askSpot(Manager).
	
+!parkingNegotiation : vehicleType(VehicleType) & mySpotType(SpotType) & timeToSpend(Hours, Minutes) <-
	if(showAllConsole){
	if( not booking ){
		.print("Looking up available parkings");
	}else{
		.print("Looking up reservation prices");
	}}
	!lookupParkings;
	while(not noParkings & not spotSecured(Manager)){
		if( prechosen(Mangr,PrePrc) ){
			-+chosenOffer(Mangr,PrePrc);
		}else{
			!chooseBestOffer;
		}
		if(chosenOffer(Mngr, Prc) & not noParkings){
			if( not booking){
				if( prechosen(Mngr, PrP) ){
					.send(Mngr, achieve, driverWrite("PreChosenManager"));
				}else{
					.send(Mngr, achieve, driverWrite("ChosenManager"));
				}
				.send(Mngr, askOne, secureSpot(SpotType, VehicleType, Prc,Prc*(2*Hours + math.ceil(Minutes/30))), Answer, 500);
			}else{
				?reservation(Mngr, FutureDay, FutureHour, DateTime);
				-+chosenReservation(Mngr, FutureDay, FutureHour, DateTime);
				if(showReservation){.print("Reserving spot with ",Mngr," for day ",FutureDay," at hour ",FutureHour);}
				.send(Mngr, askOne, reserveSpot(SpotType, VehicleType, Prc, FutureDay, FutureHour, DateTime), Answer, 500);
			}
			if( not Answer == false & not Answer == timeout ){
				if(showNegotiation){.print("Spot transaction completed by ",Mngr);}
				-+spotSecured(Mngr);
			}else{
				.send(Mngr, achieve, giveUpSpot);
			}
		}
	}
	if(noParkings){
		-noParkings;
		!setAttributes;
	}else{
		if( not booking ){
			!moveToParking;
		}else{
			?bkWait(Days,Hours,Minutes);
			-+wait(Days*24*60+Hours*60+Minutes-30);
			+arrive;
			+waiting;
		}
	}.

+!lookupParkings : managersList(MList) & vehicleType(VehicleType) & mySpotType(SpotType) <- 	
	-+offersList([]);
	-chosenReservation(M, FD, FH, DT);
	-spotSecured(Mg);
	-prechosen(Mg,Pr);
	.shuffle(MList,RandMList);
	for(.member(Manager, RandMList)){
		if(not booking){
			.send(Manager, askOne, currentPrice(P), CrPrice);
			+CrPrice;
			?currentPrice(Price);
			-currentPrice(Price);

			!offer(Manager, Price);
		}else{
			?bkWait(Days, Hours, Minutes);
			.send(Manager, achieve, forecast(SpotType, VehicleType, Days, Hours));
		}
	}
	.wait(200).

+!chooseBestOffer : true <-
	?offersList(OfL);
	-+bestOffer(["none",500,500]);
	for( .member(Offer, OfL) ){
		?bestOffer([MG, P, R]);
		.nth(1, Offer, Pr);
		.nth(2, Offer, Rn); 
		if( Pr < P | ( Pr == P & Rn < R ) ){
			-+bestOffer(Offer);
		}
	}
	?bestOffer([Mngr, Price, Rand]);
	if( Mngr == "none" ){
		if(showAllConsole){.print("Unable to find acceptable parking");}
		+noParkings;
	}else{
		.delete([Mngr, Price, Rand], OfL, New_OfL);
		-+offersList(New_OfL);
		-+chosenOffer(Mngr, Price);
		if(showAllConsole){.print("Best offer: ",Mngr," ",Price);}
	}.

+!offer(Manager, Price) : vehicleType(VehicleType) & priceRange(MinP,MaxP,InstAcpt) <-
	.send(Manager, achieve, driverWrite("StartingOffer"));
	if( Price <= MinP + MinP*InstAcpt ){
		-priceOffer(Manager,Vl,MgP);
		+priceOffer(Manager,Price,Price);
		if(showNegotiation){.print("Accepted offer to ",Manager," - ",Price," | ",MinP," - ",VehicleType);}
		.send(Manager, achieve, offer(VehicleType, Price));
	}elif( Price <= MaxP ){
		Value = (MinP+Price)/(2/(0.9+math.random(0.1)));
		-priceOffer(Manager,Vl,MgP);
		+priceOffer(Manager,Value,Price);
		if(showNegotiation){.print("Counteroffer to ",Manager," - ",Price," > ",Value);}
		.send(Manager, achieve, offer(VehicleType, Value));
	}else{
		if(showNegotiation){.print("Refused offer to ",Manager," - ",Price," : Starting price too high");}
		.send(Manager, achieve, refusedOffer(VehicleType, Price) );
	}.

+!counterOffer(Price)[source(Manager)] : vehicleType(VehicleType) & priceRange(MinP,MaxP,InstAcpt) <-
	?priceOffer(Manager,LastPrice,MngrPrice);
	if( 0.1 >= math.abs(MngrPrice - Price) ){
		if( Price <= MaxP ){
			if(showNegotiation){.print("Negotiation with ",Manager," - ",Price," | ",LastPrice," ended");}
			?offersList(OfrsList);
			.concat(OfrsList,[[Manager, Price, math.random(10)]], New_OfrsList);
			-+offersList(New_OfrsList);
			.send(Manager, achieve, acceptedOffer(VehicleType, Price));	
		}else{
			if(showNegotiation){.print("Refused counteroffer from ",Manager," - ",Price);}
			.send(Manager, achieve, refusedOffer(VehicleType, Price));
		}
	}else{
		if( Price <= MinP + MinP*InstAcpt ){
			if(showNegotiation){.print("Accepted counteroffer from ",Manager," - ",Price);}
			?offersList(OfrsList);
			.concat(OfrsList,[[Manager, Price, math.random(10)]], New_OfrsList);
			-+offersList(New_OfrsList);
			
			if( prechosen(Mngr,PrePrc) ){
				if( Price < PrePrc ){
					-+prechosen(Manager,Price);
				}	
			}else{
				+prechosen(Manager,Price);
			}
			.send(Manager, achieve, acceptedOffer(VehicleType, Price));
		}elif( Price <= MaxP ){
			Value = (LastPrice+Price)/(2/(0.9+math.random(0.1)));
			-priceOffer(Manager,LP,MgP);
			+priceOffer(Manager,Value,Price);
			if(showNegotiation){.print("Counteroffer to ",Manager," - ",Price," > ",Value);}
			.send(Manager, achieve, offer(VehicleType, Value));
		}else{
			if(showNegotiation){.print("Refused counteroffer from ",Manager," - ",Price);}
			.send(Manager, achieve, refusedOffer(VehicleType, Price));
		}
	}.

+!reserveOffer(ParkType, Price, FutureDay, FutureHour, DateTime)[source(Manager)] : vehicleType(VehicleType) & mySpotType(SpotType)  <-
	-reservation(Manager, D, H, DT);
	+reservation(Manager, FutureDay, FutureHour, DateTime);
	DestDist = 20+math.floor(math.random(580+1));
	checkOffer(ParkType, Price, DestDist, Answer, Value);
	
	if( Answer == "Sim" ){
		if(showNegotiation){.print("Accepted reservation offer to ",Manager," - ",ParkType," | ",Price," | ",DestDist);}
		.send(Manager, achieve, offer(SpotType, VehicleType, Price, DestDist));
	}elif( Answer == "preco" ){
		if(showNegotiation){.print("Reservation Counteroffer to ",Manager," - ",ParkType," | ",Value," | ",DestDist);}
		.send(Manager, achieve, offer(SpotType, VehicleType, Value, DestDist));
	}else{
		if(showNegotiation){
		if( Answer == "tipo"){
			.print("Refused reservation to ",Manager," - ",ParkType," | ",Price," | ",DestDist," : incompatible park type");
		}elif( Answer == "distanciadestino"){
			.print("Refused reservation to ",Manager," - ",ParkType," | ",Price," | ",DestDist," : distance too high");
		}else{
			.print("Refused reservation to ",Manager," - ",ParkType," | ",Price," | ",DestDist," : unkown reason");
		}}
	}.

+!acceptedOffer(Price)[source(Manager)] : vehicleType(VehicleType) & priceRange(MinP,MaxP,InstAcpt) <-
	if( Price <= MaxP ){
		?offersList(OfrsList);
		.concat(OfrsList,[[Manager, Price, math.random(10)]], New_OfrsList);
		-+offersList(New_OfrsList);
		if(showNegotiation){.print("Accepted offer from ",Manager," - ",Price);}
		.send(Manager, achieve, acceptedOffer(VehicleType, Price));
	}else{
		if(showNegotiation){.print("Refused offer from ",Manager," - ",Price," : price too high");}	
		.send(Manager, achieve, refusedOffer(VehicleType, Price));
	}.

+!refusedOffer(Answer, Value)[source(Manager)] : true <-
	if(showNegotiation){
	if( Answer == "preco" ){
		.print(Manager," refused the offer : price too low");
	}elif( Answer == "tipo"){
		.print(Manager," refused the offer : incompatible vehicle type");
	}elif( Answer == "distanciadestino"){
		.print(Manager," refused the offer : distance too high");
	}elif( Answer == "tipovaga"){
		.print(Manager," refused the offer : spot type unavailable");
	}elif( Answer == "semreserva"){
		.print(Manager," refused the offer : reservation unavailable");
	}else{
		.print(Manager," refused the offer : unkown reason");
	}}.

+!askSpot(Manager) : chosenOffer(Manager, Price) & vehicleType(VehicleType) & timeToSpend(Hours, Minutes) <-
	if(showNegotiation){
		if( not booking ){
			.print("Asking for spot. Will pay R$",Price*(2*Hours + math.ceil(Minutes/30)),
			" to spend ",Hours," hours and ",Minutes," minutes");
		}
	}
	.send(Manager, achieve, askSpot(VehicleType,Hours,Minutes)).

+!spotNotSecured[source(Manager)] <-
	!setAttributes.

+!park(ID, Sector)[source(Manager)] : spotOk & timeToSpend(Hours, Minutes) <- 
	if(showAllConsole){.print("Parking at the spot: ",Sector,ID," for ",Hours,":",Minutes);}
	.send(Manager, achieve, driverWrite("Parking"));
	+spot(Manager, ID, Sector);
	-+wait(Hours*60+Minutes);
	+leave;
	+waiting.

+!park(ID, Sector)[source(Manager)] : true <- 
	if(showAllConsole){.print("No spot was delivered!");}
	!setAttributes. 

+!leaveSpot : spot(Manager, ID, Sector) <- 	
	if(showAllConsole){.print("Leaving the parking...");}	
	.send(Manager, achieve, leaveSpot(ID, Sector)).

+!confirmLeaveSpot[source(Manager)] <-
	-spot(Manager, ID, Sector);
	!setAttributes.

+!askLeaveAgain[source(Manager)] : true <-
	if(showAllConsole){.print("Asking to leave again");}
	!leaveSpot.

