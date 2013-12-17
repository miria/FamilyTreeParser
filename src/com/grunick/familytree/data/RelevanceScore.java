package com.grunick.familytree.data;

public class RelevanceScore {

	protected int system = 0;
	protected int answer = 0;
	protected int correct = 0;

	public void incrSystem(int i) {
		system += i;
	}
	
	public void incrAnswer(int i) {
		answer += i;
	}

	public void incrCorrect(int i) {
		correct += i;
	}
	
	public double getPrecision() {
		if (correct == 0.0d && system == 0.0d && getRecall() == 1.0d)
			return 1.0d;
		if (system == 0.0d)
			return 0.0d;
		return ((double)correct)/((double)system);
	}
	
	public double getRecall() {
		if (correct == 0.0d && answer == 0.0d)
			return 1.0d;
		if (answer == 0.0d)
			return 0.0d;
		return ((double)correct)/((double)answer);
	}
	
	public double getF1Score() {
		return 2/((1.0d/getRecall())+(1.0d/getPrecision()));
	}
	
	public int getSystem() {
		return system;
	}
	
	public int getAnswer() {
		return answer;
	}
	
	public int getCorrect() {
		return correct;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder("System: ");
		builder.append(system).append(" Answer: ").append(answer).append(" Correct: ").append(correct);
		builder.append("\nPrecision: ").append(getPrecision());
		builder.append("\nRecall:    ").append(getRecall());
		builder.append("\nF1 Score:  ").append(getF1Score());
		return builder.toString();
		
	}
}
