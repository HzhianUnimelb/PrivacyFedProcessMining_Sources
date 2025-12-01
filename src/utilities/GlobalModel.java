package utilities;

import model.FPTA;


public class GlobalModel {
	private FPTA fpta;
	private double currentFitness;
	private double previousFitness;
	private double size;
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public GlobalModel() {
		previousFitness=Double.MAX_VALUE;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public GlobalModel(FPTA fpta) {
		this.fpta=fpta;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public FPTA getFpta() {
		return fpta;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public void setFpta(FPTA fpta) {
		this.fpta = fpta;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/

	public double getCurrentFitness() {
		return currentFitness;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setCurrentFitness(double currentFitness) {
		this.currentFitness = currentFitness;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public double getPreviousFitness() {
		return previousFitness;
	}
	/*-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	public void setPreviousFitness(double previousFitness) {
		this.previousFitness = previousFitness;
	}
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}

}
