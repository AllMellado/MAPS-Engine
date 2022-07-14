// CArtAgO artifact code for project testMaps

package mapsEngine;

import cartago.*;

public class Price extends Artifact {

	
	
	private double basePrice;
	//private double currentPrice;
	private String[][] matrixData;
	//private String[] header;

	
	void init(float Bp) {
		this.basePrice = Bp;
	}
	
	@OPERATION
	public void getMatrixData(String filePath) {
		Reader file = Reader.getInstance(filePath);
		this.matrixData = file.getMatrixData();
	}
	
	@OPERATION
	public void nothing(String ax){
		System.out.println("HUE\n");
	}
	
	@OPERATION
	public void updatePrice(String terms, double currentPrice, String option, OpFeedbackParam<Object> Res) {
		
		String[] auxTerms = terms.split("/");
		String[] tempTerms;
		int rule = 0;
		double value;
		int columns = this.matrixData[0].length;
		
		if( auxTerms[0].equals("Start") ) {
			Res.set(currentPrice);
			return;
		}
		
		for(int i = 0; i < auxTerms.length; i++) {
			rule = findTerm(auxTerms[i]);
		
			tempTerms = auxTerms[i].split(":");
			
			System.out.print("\nA regra ");
			for(int j = 0; j < tempTerms.length; j++) {
				System.out.print(tempTerms[j]+" ");
			}
			
			if(rule == -1) {
				System.out.println("não foi encontrada.");
				Res.set(rule);
				return;
			}
			
			value = Double.parseDouble(this.matrixData[rule][columns-1]);
			
			if(option.equals("add")){
				System.out.print(" foi inserida: R$"+currentPrice);
				currentPrice += this.basePrice*value;
			}else if(option.equals("rm")) {
				System.out.print(" foi removida: R$"+currentPrice);
				currentPrice -= this.basePrice*value;
			}else {
				System.out.println("foi usada incorretamente.");
				Res.set(-1);
				return;
			}
			
			System.out.println(" --> R$"+ currentPrice);
		}
		System.out.println();
		Res.set(currentPrice);
	}
	
	
	public int findTerm(String terms) {
		
		String[] auxTerms = terms.split(":");
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
