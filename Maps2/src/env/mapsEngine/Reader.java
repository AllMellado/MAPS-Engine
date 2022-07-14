package mapsEngine;

import java.io.*;

public class Reader {
	private String[][] legendData;
	private String[] legendHeader;
	private String[][] matrixData;
	private String[] header;
	
	private static Reader single_instance = null;
	
	public static Reader getInstance(String filePath) {
		if( single_instance == null) {
			single_instance = new Reader(filePath);
		}
		
		return single_instance;
	}
	
	private Reader( String filePath ) {
		readCsv(filePath);
	}
	
	public void readCsv(String filePath) {
		BufferedReader csvReader = null;
		String row;
		String[] data;
		int nrRowsLgnd, nrColsLgnd;
		int nrRows, nrCols;
		
		try {
			csvReader = new BufferedReader(new FileReader(filePath));			
			this.legendHeader = csvReader.readLine().split(",");
			nrColsLgnd = legendHeader.length;
			
			nrRowsLgnd = 0;
			while(!(row = csvReader.readLine()).equals(",,") ) {
				nrRowsLgnd++;
			}
			
			this.header = csvReader.readLine().split(",");
			nrCols = header.length;
			
			nrRows = 0;
			while((row = csvReader.readLine()) != null) {
				nrRows++;
			}
			
			csvReader.close();
			csvReader = new BufferedReader(new FileReader(filePath));
			
			this.legendData = new String[nrRowsLgnd][nrColsLgnd];
			this.matrixData = new String[nrRows][nrCols];
						
			int i = 0;
			csvReader.readLine();
			while ( !(row = csvReader.readLine()).equals(",,") ) {
				data = row.split(",");
				for (int j = 0; j < nrColsLgnd; j++) {
					this.legendData[i][j] = data[j];
				}
				i++;
			}
			
			i = 0;
			csvReader.readLine();
			while ((row = csvReader.readLine()) != null) {
				data = row.split(",");
				for (int j = 0; j < nrCols; j++) {
					this.matrixData[i][j] = data[j];
				}
				i++;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String[][] getLegendData(){
		return this.legendData;
	}
	
	public String[] getLegendHeader() {
		return this.legendHeader;
	}
	
	public String[][] getMatrixData(){
		return this.matrixData;
	}
	
	public String[] getHeader() {
		return this.header;
	}

	
}
