package mapsEngine;

import java.time.*;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;

public class TimeSimulation extends Artifact {
	
	final static long TICK_TIME = 1;
	
	private String[] dateTime;
	private int millsec;
	private int micsec;
	private int sec;
	private int nano;
	
	public void setDateTime(String[] dateTime) {
		this.dateTime = dateTime;
	}

	public String[] getDateTime() {
		return this.dateTime;
	}
	
	public TimeSimulation() {
		this.sec = LocalDateTime.now().getSecond();
		this.nano = LocalDateTime.now().getNano();
		//								  Y,    M,   D,   H,   M,   S
		this.dateTime = new String[] { "2020","01","01","00","00","00" };
		this.millsec = 0;
		this.micsec = 0;
		
		try {
			timeFlow();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private static TimeSimulation single_instance = null;
	
	
//	public static TimeSimulation getInstance() {
//		if( single_instance == null) {
//			single_instance = new TimeSimulation();
//		}
//		System.out.println("ON");
//		return single_instance;
//	}
	
//	private TimeSimulation() {
//		this.dateTime = "2020/01/01-00:00:00";
//	}
	
//	void init() {
//		System.out.println("YES");
//		//								Y,    M,   D,   H,   M,   S
//		defineObsProperty("dateTime","2020","01","01","00","00","00");
//		
//	}
	
	void strToNumber(Object[] dateTime, OpFeedbackParam<int[]> numberDateTime) {
		int seg,min,hour,day,month,year;
		
		seg   = Integer.parseInt(dateTime[5].toString());
		min   = Integer.parseInt(dateTime[4].toString());
		hour  = Integer.parseInt(dateTime[3].toString());
		day   = Integer.parseInt(dateTime[2].toString());
		month = Integer.parseInt(dateTime[1].toString());
		year  = Integer.parseInt(dateTime[0].toString());
		
		numberDateTime.set( new int[]{year,month,day,hour,min,seg} );
	}

	void addDateTime(int days, int hour, int minute, int stayHours, int stayMinutes, OpFeedbackParam<String[]> arriveDate, OpFeedbackParam<String[]> leaveTime ) {
		int day, month, year;
		String arriveMinute, arriveHour, arriveDay, arriveMonth, arriveYear;
		String leaveMinute, leaveHour, leaveDay, leaveMonth, leaveYear;
		ObsProperty dT = null;
		
		day   = Integer.parseInt(dT.stringValue(2));
		month = Integer.parseInt(dT.stringValue(1));
		year  = Integer.parseInt(dT.stringValue(0));
		
		// ####################### 			ARRIVE DATE 		#######################
		
		int aux;
		if( day+days > 30) {
			int months = (day+days)/30; 
			aux = day+days - 30*months;
			
			if(aux >= 10) {
				arriveDay = String.valueOf(aux);
			}else {
				arriveDay = "0"+String.valueOf(aux);
			}
			
			if( month+months > 12 ) {
				int years = (month+months)/12;
				aux = month+months - 12*years;
				
				if(aux >= 10) {
					arriveMonth = String.valueOf(aux);
				}else {
					arriveMonth = "0"+String.valueOf(aux);
				}
				
				arriveYear = String.valueOf(year+years);
			}else {
				if( month+months >= 10 ) {
					arriveMonth = String.valueOf(month+months);
				}else {
					arriveMonth = "0"+String.valueOf(month+months);
				}
				arriveYear  = dT.stringValue(0);
			}
		}else {
			if( day+days >= 10 ) {
				arriveDay = String.valueOf(day+days);
			}else {
				arriveDay = "0"+String.valueOf(day+days);
			}
			
			arriveMonth = dT.stringValue(1);
			arriveYear  = dT.stringValue(0);
		}
		
		if(hour >= 10) {
			arriveHour = String.valueOf(hour);
		}else {
			arriveHour = "0"+String.valueOf(hour);
		}
		
		if(minute >= 10) {
			arriveMinute = String.valueOf(minute);
		}else {
			arriveMinute = "0"+String.valueOf(minute);
		}
		
		// ####################### 			LEAVE DATE 			#######################
		
		if(stayMinutes + minute >= 60) {
			int hours = (stayMinutes + minute)/60;
			aux = stayMinutes + minute - hours*60;
			
			if(aux >= 10) {
				leaveMinute = String.valueOf(aux);
			}else {
				leaveMinute = "0"+String.valueOf(aux);
			}
			
			if(hour+stayHours+hours >= 24) {
				int dys = (hour+stayHours+hours)/24;
				aux = hour+stayHours+hours - dys*24;
				
				if(aux >= 10) {
					leaveHour = String.valueOf(aux);
				}else {
					if(aux == 0) {
						leaveHour = "02";
					}else {
						leaveHour = "0"+String.valueOf(aux);
					}
				}
				
				if(day+days+dys > 30) {
					int mths = (day+days+dys)/30;
					aux = day+days+dys - mths*30;
					
					if(aux >= 10) {
						leaveDay = String.valueOf(aux);
					}else {
						leaveDay = "0"+String.valueOf(aux);
					}
					
					if(month+mths > 12) {
						int yrs = (month+mths)/12;
						aux = month+mths - yrs*12;
						
						if(aux >= 10) {
							leaveMonth = String.valueOf(aux);
						}else {
							leaveMonth = "0"+String.valueOf(aux);							
						}
						
						leaveYear = String.valueOf(year+yrs);
					}else {
						if( month+mths >= 10 ) {
							leaveMonth = String.valueOf(month+mths);
						}else {
							leaveMonth = "0"+String.valueOf(month+mths);
						}
						leaveYear = arriveYear;
					}
				}else {
					if( day+days+dys >= 10 ) {
						leaveDay = String.valueOf(day+days+dys);
					}else {
						leaveDay = "0"+String.valueOf(day+days+dys);
					}
					
					leaveMonth = arriveMonth;
					leaveYear = arriveYear;
				}
			}else {
				if(hour+stayHours+hours >= 10) {
					leaveHour = String.valueOf(hour+stayHours+hours);
				}else {
					if(hour+stayHours+hours == 0) {
						leaveHour = "02";
					}else {
						leaveHour = "0"+String.valueOf(hour+stayHours+hours);
					}
				}
				
				leaveMonth = arriveMonth;
				leaveYear = arriveYear;
				leaveDay = arriveDay;
			}
		}else {
			if(stayMinutes + minute >= 10) {
				leaveMinute = String.valueOf(stayMinutes + minute);
			}else {
				leaveMinute = "0"+String.valueOf(stayMinutes + minute);
			}
			
			if(hour+stayHours >= 24) {
				int dys = (hour+stayHours)/24;
				aux = hour+stayHours - dys*24;
				
				if(aux >= 10) {
					leaveHour = String.valueOf(aux);
				}else {
					if(aux == 0) {
						leaveHour = "02";
					}else {
						leaveHour = "0"+String.valueOf(aux);
					}
				}
				
				if(day+days+dys > 30) {
					int mths = (day+days+dys)/30;
					aux = day+days+dys - mths*30;
					
					if(aux >= 10) {
						leaveDay = String.valueOf(aux);
					}else {
						leaveDay = "0"+String.valueOf(aux);
					}
					
					if(month+mths > 12) {
						int yrs = (month+mths)/12;
						aux = month+mths - yrs*12;
						
						if(aux >= 10) {
							leaveMonth = String.valueOf(aux);
						}else {
							leaveMonth = "0"+String.valueOf(aux);							
						}
						
						leaveYear = String.valueOf(year+yrs);
					}else {
						if( month+mths >= 10 ) {
							leaveMonth = String.valueOf(month+mths);
						}else {
							leaveMonth = "0"+String.valueOf(month+mths);
						}
						leaveYear = arriveYear;
					}
				}else {
					if( day+days+dys >= 10 ) {
						leaveDay = String.valueOf(day+days+dys);
					}else {
						leaveDay = "0"+String.valueOf(day+days+dys);
					}
					
					leaveMonth = arriveMonth;
					leaveYear = arriveYear;
				}
			}else {
				if(hour+stayHours >= 10) {
					leaveHour = String.valueOf(hour+stayHours);
				}else {
					if(hour+stayHours == 0) {
						leaveHour = "02";
					}else {
						leaveHour = "0"+String.valueOf(hour+stayHours);
					}
				}
				
				leaveMonth = arriveMonth;
				leaveYear = arriveYear;
				leaveDay = arriveDay;
			}
		}
		
		leaveTime.set( new String[]{leaveYear,leaveMonth,leaveDay,leaveHour,leaveMinute,"00"} );
		arriveDate.set( new String[]{arriveYear,arriveMonth,arriveDay,arriveHour,arriveMinute,"00"} );
		
	}
		
	private void printDateTime() {
		int seg = LocalDateTime.now().getSecond();
		int nano = LocalDateTime.now().getNano();
		System.out.println("printing> "+LocalDateTime.now()+" "+seg+" "+nano);
		
		System.out.println("DATETIME> "+this.dateTime[0]+"-"+
				this.dateTime[1]+"-"+
				this.dateTime[2]+" # "+
				this.dateTime[3]+":"+
				this.dateTime[4]+":"+
				this.dateTime[5]);
	}
	
	public void timeFlow() throws InterruptedException{
		System.out.println("Starting timeflow");
		
		Thread th = new Thread( () -> {
			int year,month,day,hour,min,seg;
			int auxHour = -1;
			while(true) {
				seg   = Integer.parseInt(this.dateTime[5]);
				min   = Integer.parseInt(this.dateTime[4]);
				hour  = Integer.parseInt(this.dateTime[3]);
				day   = Integer.parseInt(this.dateTime[2]);
				month = Integer.parseInt(this.dateTime[1]);
				year  = Integer.parseInt(this.dateTime[0]);
				
				if( auxHour != hour ) {
					printDateTime();
					auxHour = hour;
				}
				
				this.micsec+=500;
				
				if( this.micsec >= 1000 ) {
					this.micsec=0;
					this.millsec+=2;
				}
				
				if( this.millsec == 1000 ) {
					this.millsec=0;
					seg+=1;
				}
				
				if( seg == 60 ) {
					seg=0;
					min+=1;
				}
				
				if( min == 60 ) {
					min=0;
					hour+=1;
				}
				
				if( hour == 24 ) {
					hour=0;
					day+=1;
				}
				
				if( day == 31 ) {
					day=1;
					month+=1;
				}
				
				if( month == 13 ) {
					month=1;
					year+=1;
				}
				
				if( seg < 10 ) {
					this.dateTime[5] = "0"+String.valueOf(seg);
				}else {
					this.dateTime[5] = String.valueOf(seg);
				}
				
				if( min < 10 ) {
					this.dateTime[4] = "0"+String.valueOf(min);
				}else {
					this.dateTime[4] = String.valueOf(min);
				}
				
				if( hour < 10 ) {
					this.dateTime[3] = "0"+String.valueOf(hour);
				}else {
					this.dateTime[3] = String.valueOf(hour);
				}
				
				if( day < 10 ) {
					this.dateTime[2] = "0"+String.valueOf(day);
				}else {
					this.dateTime[2] = String.valueOf(day);
				}
				
				if( month < 10 ) {
					this.dateTime[1] = "0"+String.valueOf(month);
				}else {
					this.dateTime[1] = String.valueOf(month);
				}
				
				this.dateTime[0] = String.valueOf(year);
			}
		});
		th.start();
	}
	
	/*
	//@OPERATION
	public void timeFlow( OpFeedbackParam<Integer> Check ){
		System.out.println("\n"+this.dateTime+"\n");
		Random gr = new Random();
		
		String[] temp = this.dateTime.split("-");
		String[] date = temp[0].split("/");
		String[] time = temp[1].split(":");
		
		int aux = gr.nextInt(60);
		if( aux < 10 ) {
			time[1] = "0"+String.valueOf(aux);
		}else {
			time[1] = String.valueOf(aux);
		}
		aux = gr.nextInt(60);
		if( aux < 10 ) {
			time[2] = "0"+String.valueOf(aux);
		}else {
			time[2] = String.valueOf(aux);
		}
		
		int Hour = Integer.parseInt(time[0]);
		int Day = Integer.parseInt(date[2]);
 		int Month = Integer.parseInt(date[1]);
		int Year = Integer.parseInt(date[0]);
		
		// Time progresses 1h per execution of function;
		Hour = Hour + 1;
		
		if( Hour % 3 == 0 ) {
			//signal("check1");
			Check.set(1);
		}else {
			Check.set(0);
		}
		
		if( Hour >= 24 ) {
			time[0] = "00";
			Check.set(2);
			Day++;
			if( Day > 30  ){
				date[2] = "01";
				
				Month++;
				if( Month >= 12  ){
					date[1] = "01";
					date[0] = String.valueOf(++Year);
					
				}else {
					if( Month < 10 ) {
						date[1] = "0"+String.valueOf(Month);
					}else {
						date[1] = String.valueOf(Month);
					}
				}
			}else {
				if( Day < 10 ) {
					date[2] = "0"+String.valueOf(Day);
				}else {
					date[2] = String.valueOf(Day);
				}
			}
		}else {
			if( Hour < 10 ) {
				time[0] = "0"+String.valueOf(Hour);
			}else {
				time[0] = String.valueOf(Hour);
			}
		}
		this.dateTime = date[0]+"/"+date[1]+"/"+date[2]+"-"+time[0]+":"+time[1]+":"+time[2];
		
		//timeFlow(Check);
		//System.out.println(dateTime);
	}
	
	public String getDateTime() {
		return this.dateTime;
	}*/
}
