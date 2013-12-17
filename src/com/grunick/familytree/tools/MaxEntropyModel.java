package com.grunick.familytree.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.maxent.BasicEventStream;
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.maxent.io.GISModelWriter;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.EventStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;


public class MaxEntropyModel {
	
	protected MaxentModel model;
	protected File entropyFile;
	protected File persistFile = null;

	
	public MaxEntropyModel(File entropyFile, File persistFile) throws Exception  {
		this.entropyFile = entropyFile;
		this.persistFile = persistFile;
		getMaxEntropyModel();
	}

	protected void getMaxEntropyModel() throws IOException {
        EventStream es = new BasicEventStream(new PlainTextByLineDataStream(new FileReader(entropyFile)));
        GISModel tmpmodel = GIS.trainModel(es, 100, 4);
    	GISModelWriter writer = new SuffixSensitiveGISModelWriter(tmpmodel, persistFile);
    	writer.persist();
    	model = new GenericModelReader(persistFile).getModel();
	}
	
	public double predict(List<String> lines, int idx, String prediction) throws Exception {
		return getPredictions(lines, idx).get(prediction);
	}
	
	public Map<String,Double> getPredictions(List<String> lines, int idx) throws Exception {
		double[] outcomes = model.eval(lines.get(idx).trim().split(" "));
		return parseOutcomes(model.getAllOutcomes(outcomes));
	}
	
	public double predict(String line, String prediction) throws Exception {
		return getPredictions(line).get(prediction);
	}
	
	public Map<String,Double> getPredictions(String line) throws Exception {
		double[] outcomes = model.eval(line.trim().split(" "));
		return parseOutcomes(model.getAllOutcomes(outcomes));
	}
	
	protected static Pattern pattern = Pattern.compile("(.*?)=(.*?)\\[(.*?)\\]"); 
	
	protected Map<String,Double> parseOutcomes(String outcomes) throws Exception {
		HashMap<String,Double> map = new HashMap<String,Double>();
		Matcher matcher = pattern.matcher(outcomes);
		while(matcher.find())  {
			String key = matcher.group(2);
			double value = Double.parseDouble(matcher.group(3));
			map.put(key, value);
		}
		return map;
	}

}
