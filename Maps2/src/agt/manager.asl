// Agent manager in project testMaps

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }


/* Initial beliefs and rules */

//showAllConsole.
//showNegotiation.
//showReservation.

// Soma deve ser igual a 1 (String, Percentage)
sectorList([["A",0.5],["B",0.5]]).

// Soma deve ser igual a 1 (String, Percentage)
spotTypeList([ ["Standard",1]]).//,["Elderly",0.1],["Disabled",0.1] ]).

prevUpdate(0).

vehicleTypeRange("Motocicleta",6.5).
vehicleTypeRange("Carro",7).
vehicleTypeRange("Caminhonete",7.5).

start.

/* Initial goals */

!setupParking.

+updateDateTime(Y,M,D,H,Mi) : start <-
	-+dateTime(Y,M,D,H,Mi);
	-start;
	!openParking.
	
+updateDateTime(Y,M,D,H,Mi) : treeiter & H == 0 & D mod 5 == 0 <-
	updateArff;
	classifyArff.

+updateDateTime(Y,M,D,H,Mi) : reservations <-
	-+dateTime(Y,M,D,H,Mi);
	for( reservation(Driver, SpotType, VehicleType, Price, DateTime) ){
		//.print(D," ",H," ",D*24+H," | ",FutureDay," ",FutureHour," ",FutureDay*24+FutureHour," ",Driver);	
		if( Y*12*30*24+M*30*24+D*24+H >= DateTime-2 & Y*12*30*24+M*30*24+D*24+H <= DateTime+1 ){
			!fileWrite(Driver,"?","?",SpotType,VehicleType,"PreparingReservation","?","?");
			!prepareSpot(Driver, SpotType, VehicleType);
			if( not allSpotsUsed(Driver) ){
				!fileWrite(Driver,"?","?",SpotType,VehicleType,"ReservationPrepared","?","?");
				-reservation(Driver, SpotType, VehicleType, Price, DateTime);
				+spotPrepared(Driver, DateTime);
			}else{
				-allSpotsUsed(Driver);
				if(showReservation){.print("Preparation failed for ",Driver,"'s reservation");}
				!fileWrite(Driver,"?","?",SpotType,VehicleType,"PrepareFailed","?","?");
			}
		}
	}
	for( spotPrepared(Driver, DT)){
		if( Y*12*30*24+M*30*24+D*24+H >= DateTime-2){
			if( spot(ID, S, ST, Driver, VT, "allocated")){
				.print(Driver," is late for his reservation. Removing it");
				!fileWrite(Driver,"?","?",SpotType,VehicleType,"DriverLate","?","?");
			}
			-spotPrepared(Driver, FD, FH);
		}
	}
	.

+updateDateTime(Y,M,D,H,Mi) : occupancy & basePrice(PF) & spotData(NM,N,Occ) <-
	if( Occ >= 20 ){
		if( Occ >= 40 ){
			if( Occ >= 60 ){
				if( Occ >= 80 ){
					-+currentPrice(PF+PF*0.20);
				}else{
					-+currentPrice(PF+PF*0.15);
				}
			}else{
				-+currentPrice(PF+PF*0.10);
			}
		}else{
			-+currentPrice(PF+PF*0.05);
		}
	}else{
		-+currentPrice(PF);
	}.
	
+updateDateTimeDay(Y,M,D,H,Mi) : module & basePrice(PF) <-
	-+dateTime(Y,M,D,H,Mi);
	updatePrice(D, NewMod, Rule);
	if( currentPrice(CP) & not (PF+PF*NewMod) == CP ){
		-+currentPrice(PF+PF*NewMod);
		if( treedynamic ){
			?parkConf(Conf, NSpots, MainWeigths, PrWeigths, VhWeights);
			setWeights(PF*NewMod, MainWeigths, PrWeigths, VhWeights);
			classifyArff;
		}
	}
	!fileWrite("?","?","?","?","?","CurrentRule",Rule,"?").
	
+!setupParking <- 
  	.wait(300);
  	.my_name(N);
  	lookupArtifact("a_Log", ArtLog);
  	focus(ArtLog);
  	
	.concat("a_Gate", N, ArtGateName);
	makeArtifact(ArtGateName, "mapsEngine.Gate", [], ArtGate);
	focus(ArtGate);
	
	?pathsInfo(ArqConf_Path,ArqConf_Name,Arff_Path,LogFile);
	?parkConf(Conf, NSpots);
	.nth(0,Conf,Method); +Method;
	.nth(1,Conf,Price);
  	
  	+basePrice(Price);
	+currentPrice(Price);
	if( vehicle ){
		.nth(2,Conf,Range);
		+vehiclePrice("Motocicleta",Price-Range);
		+vehiclePrice("Carro",Price);
		+vehiclePrice("Caminhonete",Price+Range);
	}
	if( interval ){
		.nth(2,Conf,Range);
		+intervalPrice(Price+Range,Price-Range);
	}
	if( tree | treeiter ){
		.concat("a_Arff", N, ArtArffName);
		makeArtifact(ArtArffName,"mapsEngine.ArffMethods", [Arff_Path, 0], ArtArff);
		focus(ArtArff);
		
		setPathName(N, LogFile);
//		.print("Creating Aff");
//		createArff;
//		classifyArff;
		getDecisionTree;
	}
  	if( module ){
	    .concat("a_PriceEngine", N, ArtPEName);
		makeArtifact(ArtPEName, "mapsEngine.PriceEngine", [Price,N,LogFile], ArtPE);
		focus(ArtPE);    
		.concat(ArqConf_Path,"/",ArqConf_Name,".csv", ArqConf_File);
		getConfigurationData(ArqConf_File);		
	}
	
	+spotData(NSpots, 0, 0);	
  	!updateOccupancy;
  	
	if( reserving ){
		//createForecastArff;
		//classifyForecastArff;
		getPredictionTree;
		.print("'s Parking accepting reservations with ",NSpots," spots and base price of R$",Price," per 30 minutes.");
	}else{		
  		.print("'s Parking with ",NSpots," spots and base price of ",Price);
  	}
	!createSpots;
	.send("creator",askOne,managerReady).

+!openParking <-
	!organizeSpots;
	!updateOccupancy;
	.print("'s Parking has opened");
	!fileWrite("?", "?", "?", "?", "?", "ParkingOpened", "?","?").

+!fileWrite(Driver, Sec, ID, ST, VT, Desc, Rule, NP) : spotData(NM,NU,Occ) & nrfreeSpots(NrFree) & basePrice(BP) & currentPrice(CP) <-
	.my_name(N);
	fileWrite(N, Driver, Sec, ID, "?", ST, VT, Desc, Rule, CP, NP, Occ, NM, NU, NrFree).

+!driverWrite(Desc)[source(Driver)] : spotData(NM,NU,Occ) & nrfreeSpots(NrFree) & basePrice(BP) & currentPrice(CP) <-
	.my_name(N);
	fileWrite(N, Driver, "?", "?", "?", "?", "?", Desc, "?", CP, "?", Occ, NM, NU, NrFree).

+!createSpots:  spotTypeList(Type_List) & sectorList(Sect_List) & spotData(NM,NU,Occ) <- 
	+nr(0);

	for(.member(Type,Type_List)){
		?nr(NTy);
		
		.nth(0,Type, String);
		.nth(1,Type, Perc);
		
		// ID , STRING, nRequests, Percentage, nSpotsMax, nSpotsUsed
		+spotTypeData(NTy, String, 0, Perc, Perc*NM, 0);
		
		-+nr(NTy+1);
	}
	
	-+nr(0);
	
	for(.member(Sector,Sect_List)){
		?nr(NSc);
		
		.nth(0,Sector,String);
		.nth(1,Sector,Perc);
		
		// ID, STRING, Percentage, nSpotsMax, nSpotsUsed
		+sectorData(NSc, String, Perc, Perc*NM, 0);
		-+nr(NSc+1);
	}
	
	-nr(N);
	
	for(sectorData(SectID, Sector, SPerc, SMax, SUsed)){
		for( .range(I,1,SMax) ){
			//	 		SpotType Driver VehicleType Status
			+spot(I,Sector,"none","none","none","free");
		}
	}
	.//!printSpots.

+!changeOrganization:  spotData(NM,NU,Occ) <-
	if(not changing){
		?spotTypeList(Type_List);
		
		for(spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed)){
			if(exists(ID)){
				-spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed);	
			}else{
				+exists(ID);
			}
		}
		
		+nr_total_req(0);
		for( spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed) ){
			-exists(ID);
			-nr_total_req(RT);
			+nr_total_req(RT+TReq);
		}
		+changing;
		+percs([]);
		?nr_total_req(RT);
		-nr_total_req(RT);
//		.print("***************************************************************************************************");
//		.print(" Number of requests yesterday: ",RT);
//		.print(" New percentages: ");
		for( spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed) ){
			-spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed);
			if( RT == 0 ){
				+spotTypeData(ID, TY, 0, TReq, math.floor((TReq)*NM), 0); // DANGER: RESETING SPOTS USED
//				.print(" >> ",TY,": ",TReq," > ",TReq);
			}else{
				+spotTypeData(ID, TY, 0, TReq/RT, math.floor((TReq/RT)*NM), 0); // DANGER: RESETING SPOTS USED
//				.print(" >> ",TY,": ",TReq," > ",TReq/RT);
			}
			
		}
		
		for( spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed) ){
			if(TPerc < 0.1 ){	
//				.print(" Adjusting type: ", TY);
				-spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed);
				+spotTypeData(ID, TY, TReq, TPerc+0.1, math.floor(TMax + 0.1*NM), TUsed);
			}
		}
		-changing;
		!organizeSpots;
	}.
	   
+!organizeSpots: spotData(NM,NU,Occ) <-
		
		!fileWrite("?","?","?","?","?","StartingOrganization","?","?");
		
		+totalSpots(0);
		for( spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed) ){
			-totalSpots(TS);
			+totalSpots(TS + TMax);	
		}
		
		if( totalSpots(TS) & not TS == NM ){
			+larger(0,0);
			for(spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed)){
				-larger(L_ID,L);
				if( TMax > L ){
					+larger(ID,TMax);
				}else{
					+larger(L_ID,L);
				}
			}
			?larger(ID_N,TMax_N);
			-larger(ID_N,TMax_N);
//			.print("Adjusting total spots: ",TS," Needs to be: ",NM);
			-spotTypeData(ID_N, TY_N, TReq_N, TPerc_N, TMax_N, TUsed_N);
			+spotTypeData(ID_N, TY_N, TReq_N, TPerc_N, TMax_N-TS+NM, TUsed_N);
			-totalSpots(TS);
		}
		
//		.print("Organizing Spots:");
		for( spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed) ){
			+nSpots(ID,TMax);
//			.print(" > ",TY,": ",TMax);
		}
		
		//!printSpots;
		
		?sectorList(Sect_List);
		.length(Sect_List, Slen);
		
		for(sectorData(SectorID, Sec, SPerc, SMax, SUsed)){
			for(spotTypeData(ID, TY, TReq, TPerc, TMax, TUsed)){
				+nTypeSector(SectorID, Sec, ID, TMax/Slen - (TMax/Slen mod 1));
				
				if(not (TMax/Slen mod 1) == 0 & not tyRem(ID)){
					+tyRem(ID);
				}
			}
		}
		
		for(tyRem(ID)){
			+adding;
			while(tyRem(ID)){
				for(sectorData(SectorID, Sec, SPerc, SMax, SUsed) & adding){
					+sum(0);
					
					for(nTypeSector(SectorID, Sec, TyID, NTS)){
						-sum(Sm);
						+sum(Sm+NTS);	
					}
					
					if(sum(Sum) & Sum < SMax){
						-nTypeSector(SectorID,Sec,ID,NTS);
						+nTypeSector(SectorID,Sec,ID,NTS+1);
						-adding;
						-tyRem(ID);	
					}
					
					-sum(Sm);
				}
			}
		}
		
		for(nTypeSector(SectID, Sect, IDs, Noy)){
			if(Noy == 0){
				-nTypeSector(SectID, Sect, IDs, Noy);
			}else{
//				.print(" Sector ",Sect,": TypeID(",IDs,")>> ",Noy);
			}
		}
		 
		?spotTypeList(Type_List);
		.length(Type_List, Len);
		-+randList([]);
		for(.range(I,0,Len-1)){
			?randList(RL);
			.concat(RL,[[0,I]],RLn);
			-+randList(RLn);
		} 
		
		for(spot(ID, SECTOR, TYPE, USER, VTYPE, STATUS)){
			//.print("=================== MODIFYING SPOT: ",SECTOR,ID," ===================");
			
			+flag;
			while(flag){
				?randList(Types);
				if(Types == []){
					.print("NO MORE TYPES AVAILABLE");
				}
				.random(Types,TID);
				.nth(1,TID,TypeID);
				if( nSpots(TypeID, NS) & nTypeSector(SectorID, SECTOR, TypeID, NTS) & NS > 0 & NTS > 0 ){
					?spotTypeData(TypeID, TY_O, TReq_O, TPerc_O, TMax_O, TUsed_O);
					//.print("=================== NOW IS: ",SECTOR,ID," ",TY_O," LEFT>>>",NTS-1," ===================");
					-spot(ID, SECTOR, TYPE, USER, VTYPE, STATUS);
					+spot(ID, SECTOR, TY_O, USER, VTYPE, STATUS);
					
					-nSpots(TypeID, NS);
					+nSpots(TypeID, NS-1);
					
					-nTypeSector(SectorID,SECTOR,TypeID, NTS);
					+nTypeSector(SectorID,SECTOR,TypeID, NTS-1);
					
					-flag;
				}
				
				if(nTypeSector(SectorID, SECTOR, TypeID, NTSn) & NTSn <= 0 ){
					-nTypeSector(SectorID, SECTOR, TypeID, 0);
					//.print(" # SECTOR> ",SECTOR," - ",TY_O," FINISHED ");
				}
				
				if(nSpots(TypeID, NSn) & NSn <= 0){
					-nSpots(TypeID, 0);
					
					.delete([0,TypeID],Types,NewList);
					-+randList(NewList);
					
					//.print(" @ TYPE ",TypeID," ",TY_O," COMPLETED ",NewList);
				}
			}
		}
		
		for(spot(SpID, SpSec, SpTy, SpUser)){
			if(not SpUser == "free"){
				-spotTypeData(TypeID_Att, SpTy, TReq_Att, TPerc_Att, TMax_Att, TUsed_Att);
				+spotTypeData(TypeID_Att, SpTy, TReq_Att, TPerc_Att, TMax_Att, TUsed_Att+1);
			}
		}
		
		!fileWrite("?","?","?","?","?","OrganizationChanged","?","?");     
//		for(spotTypeData(TypeID_Att, SpTy, TReq_Att, TPerc_Att, TMax_Att, TUsed_Att)){
//			.print(" ",SpTy," ",TUsed_Att,"/",TMax_Att);
//		}
//		.print("***************************************************************************************************");
		.

+!forecast(SpotType, VehicleType, Days, Hours)[source(Driver)] : basePrice(BP) <-
 	if( reserving ){
	 	getDayWeek(Days, Hours, FutureDay, FutureDayWeek, FutureHour, DateTime);
	 	priceForecast(SpotType, VehicleType, FutureDay, FutureDayWeek, FutureHour, PredictedPrice);
	 	.send(Driver, achieve, reserveOffer( PredictedPrice+2*BP, FutureDay, FutureHour, DateTime));
	 }else{
	 	.send(Driver, achieve, refusedOffer("semreserva",-1));
	 }.

+!offer(VehicleType, Price)[source(Driver)] : currentPrice(CP) <-
	!fileWrite(Driver,"?","?","?", VehicleType,"ReceivedOffer","?",Price);
	if( spot(ID,Sector,SpotType,"none","none","free") ){
		if( tree | treeiter ){ 
			getHourWeek(Hour,DayWeek);
			checkOffer(VehicleType, Price, Hour, Answer, Value);
			if( Answer == "Sim" ){
				if(showNegotiation){.print("Accepted Offer from ",Driver," - ",VehicleType," | ",Price," : ",Answer," ; ",Value);}
				.send(Driver, achieve, acceptedOffer(Price));
				!fileWrite(Driver,"?","?","?", VehicleType,"OfferAccepted","?",Price);
			}elif( Answer == "preco" ){
				if(showNegotiation){.print("Counteroffer to ",Driver," - ",VehicleType," | ",Price," : ",Answer," ; ",Value);}
				.send(Driver, achieve, counterOffer(Value));
				!fileWrite(Driver,"?","?","?", VehicleType,"CounterOffer","?",Value);
			}else{
				if(showNegotiation){.print("Refused Offer from ",Driver," - ",VehicleType," | ",Value," : ",Answer," ; ",Value);}
				.send(Driver, achieve, refusedOffer(Answer, Value));
				!fileWrite(Driver,"?","?","?", VehicleType,"OfferRefused","?",Price);
			}
		}elif( vehiclePrice(VehicleType, VPrice) ){
			if( Price >= VPrice  ){
				if(showNegotiation){.print("Accepted Offer from ",Driver," - ",VehicleType," | ",Price);}
				.send(Driver, achieve, acceptedOffer(VPrice));
				!fileWrite(Driver,"?","?","?", VehicleType,"OfferAccepted","?",VPrice);
			}else{
				if(showNegotiation){.print("Counteroffer to ",Driver," - ",VehicleType," | ",Price," > ",VPrice);}
				.send(Driver, achieve, counterOffer(VPrice));
				!fileWrite(Driver,"?","?","?", VehicleType,"CounterOffer","?",VPrice);
			}
		}elif(intervalPrice(WantPrice, MinPrice)){
			if( priceOffer(Driver,PrvP) ){
				LastPrice = PrvP;
			}else{
				LastPrice = WantPrice; 		
			}
			if( Price >= WantPrice ){
				if(showNegotiation){.print("Accepted Offer from ",Driver," - ",VehicleType," | ",Price);}
				.send(Driver, achieve, acceptedOffer(WantPrice));
				!fileWrite(Driver,"?","?","?", VehicleType,"OfferAccepted","?",WantPrice);
			}else{
				if( vehicleTypeRange(VehicleType,Range) & Price < Range ){
					!fileWrite(Driver,"?","?","?", VehicleType,"PriceTooLow","?",Price);
					Value = (Range+LastPrice)/(2/(0.98+math.random(0.03)));
				}else{
					!fileWrite(Driver,"?","?","?", VehicleType,"PriceInRange","?",Price);
					Value = (Price+LastPrice)/(2/(0.98+math.random(0.03)));
				}
				-priceOffer(Driver,Vl);
				+priceOffer(Driver,Value);
				if(showNegotiation){.print("Counteroffer to ",Driver," - ",VehicleType," | ",Price," > ",Value);}
				.send(Driver, achieve, counterOffer(Value));
				!fileWrite(Driver,"?","?","?", VehicleType,"CounterOffer","?",Value);
			}
		}elif( Price >= CP ){
			if(showNegotiation){.print("Accepted Offer from ",Driver," - ",VehicleType," | ",Price);}
			.send(Driver, achieve, acceptedOffer(CP));
			!fileWrite(Driver,"?","?","?", VehicleType,"OfferAccepted","?",CP);
		}else{
			if(showNegotiation){.print("Counteroffer to ",Driver," - ",VehicleType," | ",Price," > ",CP);}
			.send(Driver, achieve, counterOffer(CP));
			!fileWrite(Driver,"?","?","?", VehicleType,"CounterOffer","?",CP);
		}
	}else{
		if(showNegotiation){.print("Spot type: ",SpotType, " unavailable!");}
		.send(Driver, achieve, refusedOffer("tipovaga", -1));
		!fileWrite(Driver,"?","?","?", VehicleType,"SpotTypeUnavailable","?",Price);
	}.

+!acceptedOffer(VehicleType, Price)[source(Driver)]: true <-
	-priceOffer(Driver,Vl);
	!fileWrite(Driver,"?","?","?",VehicleType,"DriverOfferAccepted","?",Price).

+!refusedOffer(VehicleType, Price)[source(Driver)]: true <-
	-priceOffer(Driver,Vl);
	!fileWrite(Driver,"?","?","?",VehicleType,"DriverOfferRefused","?",Price).

+?secureSpot(SpotType, VehicleType, Price, TotalPrice)[source(Driver)] : true <- 
	+freeSpots(Driver, []);
	for( spot(ID, Sector, SpotType, "none", "none", "free") ){
		-freeSpots(Driver, SpotsList);
		.concat(SpotsList, [[ID, Sector]], New_SpotsList);
		+freeSpots(Driver, New_SpotsList);
	}
	
	while( not allocated(Driver) & not allSpotsUsed(Driver) & not giveUp(Driver) ){
		!chooseFreeSpot(Driver, SpotType, VehicleType, TotalPrice);
	}
	-freeSpots(Driver, FS);
	if( allSpotsUsed(Driver) ){
		-allSpotsUsed(Driver);
		.drop_intention;
	}
	if( giveUp(Driver) ){
		-giveUp(Driver);
		if(showAllConsole){.print(Driver," give up spot");}
		!fileWrite(Driver,"?","?",SpotType,VehicleType,"DriverCancellation","?","?");	
		if( spot(SpotID, SpotSector, SpotType, Driver, VehicleType, "allocated") ){
			!cancelSpot(Driver);
		}
		.drop_intention;
	}
	
	if(showAllConsole){.print(Driver,"'s Securing completed")};
	!fileWrite(Driver,"?","?",SpotType,VehicleType,"SpotSecured","?",Price).

+!giveUpSpot[source(Driver)] <-
	+giveUp(Driver).

+!cancelSpot(Driver) <- 
	+cancelTurn(Driver);
	while( not cancelledSpot(Driver)){
		?cancelTurn(Agent);
		if( not cancellingSpot & Driver == Agent ){
			+cancellingSpot;
			if( spot(ID, SS, ST, Driver, VT, "allocated") ){
				-spot(ID, SS, ST, Driver, VT, "allocated")
				+spot(ID, SS, ST, "none", "none", "free");
				+cancelledSpot(Driver);
				if(showNegotiation){.print("Spot ",SS,ID," cancelled by ",Driver);}
				!fileWrite(Driver,SS,ID,ST,VT,"SpotCancelled","?","?");
			}else{
				if(showNegotiation){.print("Cancelled spot by ",Driver," doesnt exist!");}
				!fileWrite(Driver,"?","?","?","?","CancelledSpotDoesntExist","?","?");
			}
			-cancelTurn(Driver);
			-cancellingSpot;
		}
	}.

+?reserveSpot(SpotType, VehicleType, Price, FutureDay, FutureHour, DateTime)[source(Driver)]  <-
	+reservation(Driver, SpotType, VehicleType, Price, DateTime);
	+reservations;
	if(showReservation){.print(Driver,"'s Reservation completed for day ",FutureDay," at hour ",FutureHour);}
	.concat("SpotReserved",FutureDay,"-",FutureHour,String);
	!fileWrite(Driver,"?","?",SpotType,VehicleType,String,"?",Price).

+!prepareSpot(Driver, SpotType, VehicleType) : true <- 
	+freeSpots(Driver, []);
	for( spot(ID, Sector, SpotType, "none", "none", "free") ){
		-freeSpots(Driver, SpotsList);
		.concat(SpotsList, [[ID, Sector]], New_SpotsList);
		+freeSpots(Driver, New_SpotsList);
	}
	
	while( not allocated(Driver) & not allSpotsUsed(Driver) ){
		!chooseFreeSpot(Driver, SpotType, VehicleType);
	}
	-freeSpots(Driver, FS);
	if( allSpotsUsed(Driver) ){
		-allSpotsUsed(Driver);
		if(showReservation){.print("Couldn't prepare spot for ",Driver,"'s reservation");}
		!fileWrite(Driver,"?","?",SpotType,VehicleType,"CouldntPrepareSpot","?","?");
		.drop_intention;
	}
	if(showReservation){.print(Driver,"'s Reserving completed");}
	!fileWrite(Driver,"?","?",SpotType,VehicleType,"SpotPrepared","?","?").

+?confirmReservation[source(Driver)] <- 
	if( not spot(SpotID, SpotSector, SpotType, Driver, VehicleType, "allocated") ){
		-reservation(Driver, SpotType, VehicleType, Price, DateTime);
		.print("Reserved Spot for ", Driver," not found. Refunding driver: R$",(Price+(Price*0.2)));
		!fileWrite(Driver,"?","?","?","?","ReservationNotFound","?",-1*(Price+(Price*0.2)));
		.drop_intention;
	}
	?spot(SpotID, SpotSector, SpotType, Driver, VehicleType, "allocated");
	!fileWrite(Driver,SpotSector,SpotID,SpotType,VehicleType,"ReservationConfirmed","?","?");
	if(showReservation){.print(Driver,"'s reservation confirmed");}.

+!chooseFreeSpot(Driver, SpotType, VehicleType, Price) : freeSpots(Driver, FreeSpots) <-
	if( not FreeSpots == [] ){
		+chooseTurn(Driver);
		.random(FreeSpots, Spot);
		
		-freeSpots(Driver, FreeSpots)
		.delete(Spot, FreeSpots, New_FreeSpots);
		+freeSpots(Driver, New_FreeSpots);
		
		.nth(0, Spot, SpotID);
		.nth(1, Spot, SpotSector);
		
		while( not allocated(Driver) & not failed(Driver) & not full ){
			?chooseTurn(Drvr);
			if( not full & not choosingSpot & Driver == Drvr ){
				+choosingSpot;
				if( spot(SpotID, SpotSector, SpotType, "none", "none", "free") ){
					-spot(SpotID, SpotSector, SpotType, "none", "none", "free");
					+spot(SpotID, SpotSector, SpotType, Driver, VehicleType, "allocated");
					+allocated(Driver);
					if(showAllConsole){.print("Spot ",SpotSector,SpotID," chosen for ",Driver);}
					!fileWrite(Driver,SpotSector,SpotID,SpotType,VehicleType,"SpotAllocated","?",Price);
					!updateOccupancy;
				}else{
					+failed(Driver);
					if(showNegotiation){.print("Spot ",SpotSector,SpotID," lost during assignement to ",Driver,"!");}
					?spot(SpotID, SpotSector, SpotType, DR, VT, ST);
					.concat("SpotLostTo",DR,String);
					!fileWrite(Driver,SpotSector,SpotID,SpotType,VehicleType,String,"?","?");
				}
				-chooseTurn(Driver);
				-choosingSpot;
			}
			if( giveUp(Driver) ){
				+failed(Driver);
			}
		}
		-chooseTurn(Driver);
		-failed(Driver);
		if( full ){
			+allSpotsUsed(Driver);
			if(showNegotiation){.print("All ",SpotType," Spots used before choosing for ",Driver,"!");}
			!fileWrite(Driver,"?","?",SpotType,VehicleType,"AllSpotsUsed","?","?");
		}
	}else{
		+allSpotsUsed(Driver);
		if(showNegotiation){.print("All ",SpotType," Spots used before choosing for ",Driver,"!");}
		!fileWrite(Driver,"?","?",SpotType,VehicleType,"AllSpotsUsed","?","?");
	}.

+!saveRequest(TYPE): true <- 
	+typeErrorReq(RT,TYPE);
	while(typeErrorReq(RT,TYPE)){
		-typeErrorReq(RT,TYPE);
		if(spotTypeData(ID, TYPE, TReq, TPerc, TMax, TUsed)){
			-spotTypeData(ID, TYPE, TReq, TPerc, TMax, TUsed);
			+spotTypeData(ID, TYPE, TReq+1, TPerc, TMax, TUsed);
		}else{
			+typeErrorReq(RT,TYPE);
		}
	}.

+!updateOccupancy : spotData(NM,NU,OC) <-
	.count( spot(ID, Sec, Ty, Dr, VT, "parked"), N );
	.count( spot(IDf, Secf, Tyf, Drf, VTf, "free"), N_F );
	.count( spot(IDa, Seca, Tya, Dra, VTa, "allocated"), N_A );
												
	Occ = N*100/NM;
	-+nrfreeSpots([N_A,N_F]);
	-+spotData(NM,N,Occ);
	if( Occ >= 100 ){
		+full;
	}else{
		-full;
	}
	if( ( N + N_F + N_A < NM-1 | N + N_F + N_A > NM+1) & not start){
		.print("Illegal number of spots: (Park_",N,", Alloc_",N_A,", Free_",N_F,")");
		.concat("IllegalOccupancy",N,"|",N_A,"|",N_F,String);
		!fileWrite("?","?","?","?","?",String,"?","?");
	}.

+!askSpot(VType,Hours,Minutes)[source(Driver)] <-
	if( spot(ID, Sector, Type, Driver, VType, "allocated") ){
		+askTurn(Driver);
		while( not changed(Driver) & not full ){
			?askTurn(Drvr);
			if( not full & not askingSpot & Driver == Drvr ){
				+askingSpot;
				-spot(ID, Sector, Type, Driver, VType, "allocated");
				+spot(ID, Sector, Type, Driver, VType, "parked");
				!updateOccupancy;
				+changed(Driver);
				-askTurn(Driver);
				-askingSpot;
			}
		}
		-changed(Driver);
		if(showAllConsole){.print("Allocated Spot ",Sector,ID," ",Type," given to the agent: ",Driver);}
		-allocated(Driver);
		.send(Driver, tell, spotOk); 
		.send(Driver, achieve, park(ID,Sector));
		.concat("SpotParkedFor",Hours,"h",X);
		!fileWrite(Driver,Sector,ID,Type,VType,X,"?","?");
	}else{
		.print("\n\n\n\n 	 ERROR: ",Driver,"'S PRE ALLOCATED SPOT DOESN'T EXIST   \n\n\n\n");
		.send(Driver, achieve, spotNotSecured);
		!fileWrite(Driver,"?","?","?",VType,"AllocatedSpotDoesntExist","?","?");
	}.
 	
+!printSpots : true <-
	.print("PRINTING SPOTS:");
	for(spot(Id,Sector,Type,User,VType,Status)){
		.print("Spot: ",Id," - Sector: ",Sector," - Type: ",Type," - User: ",User);	     
	}.
	      
+!leaveSpot(ID, Sector)[source(Driver)] : true <-
	if(spot(ID, Sector, Type, Driver, VType, "parked")){
		+leaveTurn(Driver);
		while( not changed(Driver) ){
			?leaveTurn(Drvr);
			if( not leavingSpot & Driver == Drvr ){
				+leavingSpot;
				-spot(ID, Sector, Type, Driver, VType, "parked");
				+spot(ID, Sector, Type, "none", "none", "free");
				!updateOccupancy;
				+changed(Driver);
				-leaveTurn(Driver);
				-leavingSpot;
			}
		}
		.send(Driver, achieve, confirmLeaveSpot);
		-changed(Driver);
		
		!fileWrite(Driver,Sector,ID,Type,VType,"LeavingSpot","?","?");
	}else{
		.print("Information about spot unavailable. Unable to free spot!");
		!fileWrite(Driver,Sector,ID,Type,VType,"UnableToLeave","?","?");
		.send(Driver, tell, leaveFailed);
		.send(Driver, achieve, askLeaveAgain);
	}.
	     
	

