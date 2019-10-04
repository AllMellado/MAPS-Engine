// CArtAgO artifact code for project testMaps

package mapsEngine;

import cartago.*;

public class Price extends Artifact {

	void init(String msg) {
		System.out.println("System message: " + msg);
	}

	private double price;
	private double newPrice;
	private String[][] matrixData;
	private String[] header;

	public Price(float Bp) {
		price = Bp;
		newPrice = Bp;
	}


	public void getMatrixData(String filePath) {
		Reader file = new Reader();
		file.readCsv(filePath);
		this.matrixData = file.getMatrixData();
		this.header = file.getHeader();
	}

	public double updatePrice(String terms) {
		String[] auxTerms = terms.split("/");
		String[] tempTerms;
		int rule = 0;
		double value;
		int columns = this.matrixData[0].length;

		for(int i = 0; i < auxTerms.length; i++) {
			rule = findTerm(auxTerms[i]);
			
			tempTerms = auxTerms[i].split(";");
			
			System.out.print("\nA regra ");
			for(int j = 0; j < tempTerms.length; j++) {
				System.out.print(tempTerms[j]+" ");
			}
			
			if(rule == -1) {
				System.out.println("não foi encontrada.");
				return rule;
			}
			
			value = Double.parseDouble(this.matrixData[rule][columns-1]);
			
			if(value >= 0) {
				System.out.print("fez o preço aumentar de R$"+newPrice);
			}else {
				System.out.print("fez o preço diminuir de R$"+newPrice);
			}
			
			this.newPrice += price*value;

			System.out.println(" para: R$"+ newPrice);
		}
		System.out.println();
		return newPrice;
	}
	
	public int findTerm(String terms) {
		
		String[] auxTerms = terms.split(";");
		int[] idArray = new int[this.matrixData.length];
		int[] proxIdArray = new int[this.matrixData.length];
		
		for(int i = 0; i < idArray.length; i++ ) {
			idArray[i] = i;
		}
		
		int size = this.matrixData.length;
		int nextSize;
		int column = 0;
		int end  = 0;
		while (end != 1) {
			nextSize = 0;
			for (int i = 0; i < size; i++) {
				if (this.matrixData[idArray[i]][column].equals(auxTerms[column]) || this.matrixData[idArray[i]][column].equals("?")) {
					proxIdArray[nextSize++] = idArray[i];
				}
			}
			idArray = proxIdArray;
			column++;
			size = nextSize;
			if (size <= 1) {
				end = 1;
			}
		}

		if (size == 0)
			return -1;

		return idArray[0];
	}
}
