package com.grunick.familytree.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.grunick.familytree.data.Obiterator;
import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Response;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.tools.MaxEntropyModel;
import com.grunick.familytree.tools.NamedEntityExtractor;
import com.grunick.familytree.util.EntityUtils;
import com.grunick.familytree.util.InputOutputUtils;

public class SimpleBinaryMaxEntropyStrategy implements IStrategy {
	
	protected String entropyDir;
	protected String persistDir;
	protected Map<String,MaxEntropyModel> models;
	protected NamedEntityExtractor extractor;
	
	protected final static String[] predictions = new String[] {"PARENT", "CHILD", "SPOUSE"};
		
	public SimpleBinaryMaxEntropyStrategy(String entityExtractorConfig, String entropyDir, String persistDir) {
		this.entropyDir = entropyDir;
		this.persistDir = persistDir;
		this.extractor = new NamedEntityExtractor(entityExtractorConfig);
		this.models = new HashMap<String, MaxEntropyModel>();
	}

	public void trainModel(File obitDir, File responseDir) throws Exception {
		for (String prediction : predictions ) {
			Obiterator trainIter = new Obiterator(obitDir);
			int count = 0;
			List<String> trainingLines = new ArrayList<String>();
			while (trainIter.hasNext()) {
				Obituary obit = trainIter.next();
				Response response = InputOutputUtils.loadResponseFromFile(new File(responseDir, obit.getInputFile().getName()));
				List<List<Term>> sentences = extractor.tagEntities(obit);
				if (++count % 10 == 0)
					System.out.println("   processed "+count+" examples.");
				for (List<Term> sentence : sentences) {
					List<String> lines = encodeTerms(sentence, response, true, prediction);
					trainingLines.addAll(lines);
				}
			}

			File entropyFile = new File(entropyDir, "maxent-"+prediction.toLowerCase()+"-train.np");
			File persistFile = new File(entropyDir, "maxent-"+prediction.toLowerCase()+".tgz");
			FileUtils.writeLines(entropyFile,  trainingLines);
			models.put(prediction, new MaxEntropyModel(entropyFile, persistFile));
		}
	}
		
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	protected List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTrainingSet, String predictionType) throws Exception {
		sentence = EntityUtils.combineEntities(sentence);
		List<String> terms = new ArrayList<String>();
		for (int i=0; i < sentence.size(); i++) {
			if (EntityUtils.isPerson(sentence.get(i))) {
				terms.add(encodeTerm(sentence, i, prediction, isTrainingSet, predictionType));
			}
		}
		return terms;
		
	}
	
	protected String encodeTerm(List<Term> sentence, int idx, Response prediction, boolean isTrainingSet, String predictionType) {
		Term term = sentence.get(idx);
		
		String text = term.getText().trim();
		StringBuilder builder = new StringBuilder();

		builder.append("curToken=").append(text);
		builder.append(" entityTag=").append(term.getNETag());
		builder.append(" isPerson=").append(EntityUtils.isPerson(term));		
		
		if (idx > 0) {
			Term prev = sentence.get(idx-1);
			builder.append(" prevToken=").append(prev.getText().trim()); 
		}
		
		if (idx > 1) {
			Term prev = sentence.get(idx-2);
			builder.append(" prevprevToken=").append(prev.getText().trim());  
		}
		
		if (idx < sentence.size()-1) {
			Term next = sentence.get(idx+1);
			builder.append(" nextToken=").append(next.getText().trim());
		}
		
		if (idx < sentence.size()-2) {
			Term next = sentence.get(idx+2);
			builder.append(" nextnextToken=").append(next.getText().trim());
		}

		predictionType = predictionType.toLowerCase();
		if (isTrainingSet && prediction != null) {// training set
			builder.append(" is"+predictionType+"=");
			String entity = term.getText().replaceAll("_", " ");
			if ("PARENT".equalsIgnoreCase(predictionType)) {
				builder.append(prediction.getParents().contains(entity));
			} else if ("SPOUSE".equalsIgnoreCase(predictionType)) {
				builder.append(prediction.getSpouses().contains(entity));
			} else if ("CHILD".equalsIgnoreCase(predictionType)) {
				builder.append(prediction.getChildren().contains(entity));
			}
		} else
			builder.append(" is"+predictionType+"=");
		return builder.toString();
	}

	@Override
	public void detectRelationships(Obituary obit) throws Exception {
		List<List<Term>> sentences = extractor.tagEntities(obit);
		for (List<Term> sentence : sentences) {
			sentence = EntityUtils.combineEntities(sentence);
			
			for (int i=0; i< sentence.size(); i++) {
				if (!EntityUtils.isPerson(sentence.get(i))) {
					continue;
				}
				String name = sentence.get(i).getText().replaceAll("_", " ");
				Map<String,Double> scores = new HashMap<String,Double>();
				for (String prediction : predictions) {
					String line = encodeTerm(sentence, i, null, false, prediction);
					scores.put(prediction, models.get(prediction).predict(line, "true"));
				}
				String maxType = null;
				double maxScore = 0.0d;
				for (String key : scores.keySet()) {
					if (scores.get(key) > maxScore) {
						maxScore = scores.get(key);
						maxType = key;
					}
				}
				if (maxScore >= 0.37d) {
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

}
