package com.grunick.familytree.data;

import static com.grunick.familytree.util.TermUtils.nonNull;

public class Term {

	protected String text;
	protected String neTag;
	protected String speechType;
	protected String relation;
	protected String bioChunk;
	protected String prediction = null;
	
	public Term(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setNETag(String tag) {
		this.neTag = tag;
	}
	
	public String getNETag() {
		return neTag;
	}
	
	public void setPartOfSpeech(String s) {
		speechType = s;
	}
	
	public String getPartOfSpeech() {
		return speechType;
	}
	
	public void setBIOChunk(String s) {
		bioChunk = s;
	}

	public String getBIOChunk() {
		return bioChunk;
	}
	
	public void setRelation(String s) {
		this.relation = s;
	}
	
	public String getRelation() {
		return relation;
	}
	
	public String toString() {
		return text+"/"+nonNull(neTag)+"/"+nonNull(bioChunk)+"/"+nonNull(speechType)+"/"+nonNull(relation)+"/"+nonNull(prediction);
	}
	
	
	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}
	
	public String getPrediction() {
		return prediction;
	}
	
}
