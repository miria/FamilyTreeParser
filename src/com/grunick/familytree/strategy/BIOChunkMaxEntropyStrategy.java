package com.grunick.familytree.strategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Response;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.tools.PhraseChunker;
import com.grunick.familytree.util.EntityUtils;
import com.grunick.familytree.util.TermUtils;

public class BIOChunkMaxEntropyStrategy extends AbstractMaxEntropyStrategy {

	protected PhraseChunker chunker;
	
	public BIOChunkMaxEntropyStrategy(String entityExtractorConfig,
			File entropyFile, File persistFile, String phraseChunkerConfig) throws IOException, ClassNotFoundException {
		super(entityExtractorConfig, entropyFile, persistFile);
		this.chunker = new PhraseChunker(phraseChunkerConfig);
	}

	protected List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTrainingSet) throws Exception {
		sentence = EntityUtils.combineEntities(sentence);
		chunker.addBIOChunks(sentence);
		List<String> terms = new ArrayList<String>();
		for (int i=0; i < sentence.size(); i++) {
			if (EntityUtils.isPerson(sentence.get(i))) {
				terms.add(encodeTerm(sentence, i, prediction, isTrainingSet));
			}
		}
		return terms;
		
	}
	

	
	protected String encodeTerm(List<Term> sentence, int idx, Response prediction, boolean isTrainingSet) {
		Term term = sentence.get(idx);
		
		String text = term.getText().trim();
		StringBuilder builder = new StringBuilder();

		builder.append("curToken=").append(text);
		builder.append(" entityTag=").append(term.getNETag());
		builder.append(" isPerson=").append(EntityUtils.isPerson(term));
		
		builder.append(" isHeadToken=").append(TermUtils.isHeadToken(sentence, idx)); 
		builder.append(" bioTag=").append(term.getBIOChunk()); 
		//builder.append(" chunkType=").append(TermUtils.getBIOChunkType(term)); 
		builder.append(" curPhrase=").append(TermUtils.getCurrentPhrase(sentence, idx)); 
		
		if (idx > 0) {
			Term prev = sentence.get(idx-1);
			builder.append(" prevToken=").append(prev.getText().trim()); 
			builder.append(" prevIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx-1)); 
			builder.append(" prevBioTag=").append(prev.getBIOChunk());  
			builder.append(" prevChunkType=").append(TermUtils.getBIOChunkType(prev)); 
		}
		
		if (idx > 1) {
			Term prev = sentence.get(idx-2);
			builder.append(" prevprevToken=").append(prev.getText().trim());
			builder.append(" prevprevIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx-2)); 
			builder.append(" prevprevBioTag=").append(prev.getBIOChunk()); 
			//builder.append(" prevprevChunkType=").append(TermUtils.getBIOChunkType(prev)); 
		}
		
		if (idx < sentence.size()-1) {
			Term next = sentence.get(idx+1);
			builder.append(" nextToken=").append(next.getText().trim());
			builder.append(" nextIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx+1)); 
			builder.append(" nextBioTag=").append(next.getBIOChunk());
			builder.append(" nextChunkType=").append(TermUtils.getBIOChunkType(next)); 
		}
		
		if (idx < sentence.size()-2) {
			Term next = sentence.get(idx+2);
			builder.append(" nextnextToken=").append(next.getText().trim());
			builder.append(" nextnextIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx+2)); 
			//builder.append(" nextnextBioTag=").append(next.getBIOChunk()); 
			//builder.append(" nextnextChunkType=").append(TermUtils.getBIOChunkType(next)); 
		}

		if (isTrainingSet && prediction != null) {// training set
			builder.append(" relationship=");
			String entity = term.getText().replaceAll("_", " ");
			if (prediction.getParents().contains(entity)) 
				builder.append("PARENT");
			else if (prediction.getSpouses().contains(entity))
				builder.append("SPOUSE");
			else if (prediction.getChildren().contains(entity))
				builder.append("CHILD");
			else if (prediction.getChildren().contains(entity))
				builder.append("NOMATCH");
		} else
			builder.append(" relationship=");
		return builder.toString();
	}

	@Override
	public void detectRelationships(Obituary obit) throws Exception {
		List<List<Term>> sentences = extractor.tagEntities(obit);
		for (List<Term> sentence : sentences) {
			sentence = EntityUtils.combineEntities(sentence);
			chunker.addBIOChunks(sentence);
			List<String> lines = new ArrayList<String>();
			List<Term> terms = new ArrayList<Term>();
			for (int i=0; i < sentence.size(); i++) {
				if (EntityUtils.isPerson(sentence.get(i))) {
					lines.add(encodeTerm(sentence, i, null, false));
					terms.add(sentence.get(i));
				}
			}
			for (int i=0; i < lines.size(); i++) {
				String name = terms.get(i).getText().replaceAll("_", " ");
				Map<String,Double> prediction =  model.getPredictions(lines, i);
				String maxType = null;
				double maxScore = 0.0d;
				for (String key : prediction.keySet()) {
					if (prediction.get(key) > maxScore) {
						maxScore = prediction.get(key);
						maxType = key;
					}
				}
				if (maxType.equals("PARENT"))
					obit.addParent(name);
				else if (maxType.equals("SPOUSE"))
					obit.addSpouse(name);
				else if (maxType.equals("CHILD"))
					obit.addChild(name);
			}
		}
	}

}
