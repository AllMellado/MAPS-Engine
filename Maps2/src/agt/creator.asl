// Agent creator in project mapsEngine

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

/* Initial beliefs and rules */

managersList([]).
driversList([]).

parkConfs([[vehicle,7,0.5],[interval,7,0.5],[tree,7],[treeiter,7],[occupancy,6.5],[module,6.5]]).
set_manager(0).

driverConfs([["Motocicleta",0.85],["Carro",0.94],["Caminhonete",1]],
			 [0.25,0.15],
			 [[8,6.6],[7.8,6.4],[7.6,6.2],[7.5,6],[7.2,5.8]]).
set_driver(0,0,0).

// NrVagas, NrManagers, NrDrivers
scene(20,6,300).

arqConfInfo("M:/Downloads/IC_DATA","conf1").
arffPath("M:/Downloads/IC_DATA").
logPath("M:/Downloads/IC_DATA").
//booking("noreserv",0).

tickTime(400).
/* Initial goals */

test.

!setupSystem.

/* Plans */

+!setupSystem : scene(N_spots,N_mngrs,N_drvrs) & logPath(LogPath) & 
				arqConfInfo(ArqConf_Path,ArqConf_Name)  <-
	.concat(LogPath,"/log_",N_mngrs,"managers_",N_drvrs,"drivers_",N_spots,"spots",LogFile);
	//.concat(LogFile,"_",BkStatus,"_",ArqConf_Name,LogFileWrite);
	.concat(LogFile,"_",ArqConf_Name,LogFileWrite);
	makeArtifact("a_Log","mapsEngine.Log", [LogFileWrite, N_mngrs], ArtLog);
	.concat(LogPath,"/log_base",LogFileRead);
	+logFolderName(LogFileRead);
	
	!createAgents.

+?managerReady[source(Manager)] : scene(N_spots,N_mngrs,N_drvrs) <-
	+mReady(Manager);
	?managersList(ML);
	.concat(ML, [Manager], NewML);
	-+managersList(NewML);
	if( .count(mReady(Ag), N) & N == N_mngrs ){
		!divCreateDrivers;
	}.

+?driverReady[source(Driver)] : scene(N_spots,N_mngrs,N_drvrs) <-
	+ready(Driver);
	
	.count(ready(Ag), N);
	if( N mod (0.1*N_drvrs) == 0 ){
		print(" | ",N);
	}
	if( N == N_drvrs ){
		print("\n");
		!forceUpdate;
	}.

+!forceUpdate <-
	while(true){
		forceUpdate;
		.wait(200);
	}.

+!createAgents : scene(N_spots,N_mngrs,N_drvrs) & logFolderName(LogFile) &
				 parkConfs(Confs)  & arqConfInfo(ArqConf_Path,ArqConf_Name) & arffPath(ArrfPath)<-
	for(.range(I,1, N_mngrs)){	
		?set_manager(Set_M);
		.nth(Set_M,Confs,Conf);
		.concat("manager_", I, Mngr_Name);
		.create_agent(Mngr_Name,"manager.asl");
		.send(Mngr_Name, tell, pathsInfo(ArqConf_Path,ArqConf_Name,ArrfPath,LogFile));
		.send(Mngr_Name, tell, parkConf(Conf,N_spots));
		
		-+set_manager(Set_M+1);
	}.

+!divCreateDrivers : scene(N_spots,N_mngrs,N_drvrs) <-
	print("Number of loaded drivers: 1 ");
	!createDrivers(1,N_drvrs).
//	DIV = 1;
//	for( .range(N, 0, DIV-1) ){
//		!createDrivers( (N*N_drvrs/DIV)+1,(N+1)*N_drvrs/DIV);
//		while( .count(ready(Ag), C) & C < (N+1)*N_drvrs/DIV ){
//			.wait(500);
//		}
//	}.

+!createDrivers(Start,End) : driverConfs(VehicleData, InstantAccept, Price) & arffPath(ArrfPath) <-
	for(.range(J, Start, End)){
		?set_driver(VD,IA,PR);
		.nth(VD, VehicleData, Drvr_VD);
		.nth(IA, InstantAccept, Drvr_IA);
		.nth(PR, Price, Drvr_PR);
		.concat("driver_", J, Drvr_Name);
		.create_agent(Drvr_Name,"driver.asl");
		.send(Drvr_Name, tell, driverConf(Drvr_VD, Drvr_IA, Drvr_PR));
		
		//.print(Drvr_Name,"  ",Drvr_VD,"  ",Drvr_IA,"  ",Drvr_PR);
		
		AuxPR = PR+1;
		if( AuxPR >= .length(Price)){
			NewPR = 0;
			AuxIA = IA+1;
		}else{
			NewPR = AuxPR;
			AuxIA = IA;
		}
		if( AuxIA >= .length(InstantAccept)){
			NewIA = 0;
			AuxVD = VD+1;
		}else{
			NewIA = AuxIA;
			AuxVD = VD;
		}
		if( AuxVD >= .length(VehicleData)){
			NewVD = 0;
		}else{
			NewVD = AuxVD;
		}
		-+set_driver(NewVD,NewIA,NewPR);
	}.
