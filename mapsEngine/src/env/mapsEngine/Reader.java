package mapsEngine;

import java.io.*;

public class Reader {

	private String[][] matrixData;
	private String[] header;
	
	public Reader() {
		
	}
	
	public void readCsv(String filePath) {
		BufferedReader csvReader = null;
		String row;
		String[] data;
		int nrRows, nrCols;
		
		try {
			csvReader = new BufferedReader(new FileReader(filePath));			
			this.header = csvReader.readLine().split(",");
			nrCols = header.length;
			
			nrRows = 1;
			while((row = csvReader.readLine()) != null) {
				nrRows++;
			}
			
			csvReader.close();
			csvReader = new BufferedReader(new FileReader(filePath));
			
			this.matrixData = new String[nrRows][nrCols];

			int i = 0;
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
	
	public String[][] getMatrixData(){
		return this.matrixData;
	}
	
	public String[] getHeader() {
		return this.header;
	}

	
}
