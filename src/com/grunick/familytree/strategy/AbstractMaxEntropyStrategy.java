package com.grunick.familytree.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.grunick.familytree.data.Obiterator;
import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Response;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.tools.MaxEntropyModel;
import com.grunick.familytree.tools.NamedEntityExtractor;
import com.grunick.familytree.util.InputOutputUtils;

public abstract class AbstractMaxEntropyStrategy implements IStrategy {
	
	protected File entropyFile;
	protected File persistFile;
	protected MaxEntropyModel model;
	protected NamedEntityExtractor extractor;
		
	public AbstractMaxEntropyStrategy(String entityExtractorConfig, File entropyFile, File persistFile) {
		this.entropyFile = entropyFile;
		this.persistFile = persistFile;
		this.extractor = new NamedEntityExtractor(entityExtractorConfig);
	}

	public void trainModel(File obitDir, File responseDir) throws Exception {
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
				List<String> lines = encodeTerms(sentence, response, true);
				trainingLines.addAll(lines);
			}
		}
		
		FileUtils.writeLines(entropyFile, trainingLines);
		this.model = new MaxEntropyModel(entropyFile, persistFile);
	}
	
	public abstract void detectRelationships(Obituary obit) throws Exception;
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	protected abstract List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTraining) throws Exception;
	

}
