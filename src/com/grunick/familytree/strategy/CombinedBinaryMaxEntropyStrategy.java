package com.grunick.familytree.strategy;

import java.io.File;
import java.io.IOException;
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
import com.grunick.familytree.tools.PhraseChunker;
import com.grunick.familytree.util.EntityUtils;
import com.grunick.familytree.util.InputOutputUtils;
import com.grunick.familytree.util.TermUtils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

public class CombinedBinaryMaxEntropyStrategy implements IStrategy {

	protected String entropyDir;
	protected String persistDir;
	protected Map<String,MaxEntropyModel> models;
	protected NamedEntityExtractor extractor;
	protected PhraseChunker chunker;
	protected LexicalizedParser parser;
	protected MaxentTagger tagger;
	
	protected final static String[] predictions = new String[] {"PARENT", "CHILD", "SPOUSE"};
		
	public CombinedBinaryMaxEntropyStrategy(String entityExtractorConfig,
			String entropyDir, String persistDir, String phraseChunkerConfig,
			String parserModel, String taggerModelPath) throws IOException, ClassNotFoundException {

		this.entropyDir = entropyDir;
		this.persistDir = persistDir;
		this.extractor = new NamedEntityExtractor(entityExtractorConfig);
		this.models = new HashMap<String, MaxEntropyModel>();
		this.chunker = new PhraseChunker(phraseChunkerConfig);
		this.parser = LexicalizedParser.loadModel(parserModel);
		this.tagger = new MaxentTagger(taggerModelPath);
	}
	
	protected Tree getTree(List<Term> sentence) {
	    List<CoreLabel> rawWords = new ArrayList<CoreLabel>();
	    for (Term term : sentence) {
	      CoreLabel l = new CoreLabel();
	      l.setWord(term.getText());
	      rawWords.add(l);
	    }
	    return parser.apply(rawWords);
		
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
	
	protected void tagSentence(List<Term> sentence) {
		String tagged = tagger.tagTokenizedString(TermUtils.termListToString(sentence));
		String[] tagTerms = tagged.split(" ");
		if (tagTerms.length != sentence.size())
			throw new RuntimeException(tagTerms+" != "+sentence);
		for (int i=0; i<tagTerms.length; i++) {
			String[] pieces = tagTerms[i].split("_");
			sentence.get(i).setPartOfSpeech(pieces[pieces.length-1]);
		}
		
	}
	
	protected List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTrainingSet, String predictionType) throws Exception {
		sentence = EntityUtils.combineEntities(sentence);
		chunker.addBIOChunks(sentence);
		tagSentence(sentence);
		Tree tree = getTree(sentence);
		List<String> terms = new ArrayList<String>();
		for (int i=0; i < sentence.size(); i++) {
			if (EntityUtils.isPerson(sentence.get(i))) {
				terms.add(encodeTerm(sentence, i, prediction, isTrainingSet, predictionType, tree));
			}
		}
		return terms;
		
	}
	
	protected String encodeTerm(List<Term> sentence, int idx, Response prediction, boolean isTrainingSet, String predictionType, Tree tree) throws Exception {
		Term term = sentence.get(idx);
		
		String text = term.getText().trim();
		StringBuilder builder = new StringBuilder();

		builder.append("curToken=").append(text);
		builder.append(" entityTag=").append(term.getNETag());
		builder.append(" isPerson=").append(EntityUtils.isPerson(term));	
		
		builder.append(" isHeadToken=").append(TermUtils.isHeadToken(sentence, idx)); 
		builder.append(" bioTag=").append(term.getBIOChunk()); 
		builder.append(" curPhrase=").append(TermUtils.getCurrentPhrase(sentence, idx)); 
		
		builder.append(" curTag=").append(TermUtils.getCurrentTag(sentence, idx, tree));
		builder.append(" constituentTags=").append(TermUtils.getConsituentTags(sentence, idx, tree)); 
		
		String tmp = TermUtils.previousType(sentence, idx, "VB");
		if (tmp != null)  
			builder.append(" previousVB=").append(tmp);
		
		tmp = TermUtils.previousType(sentence, idx, "NN");
		if (tmp != null)
			builder.append(" previousNN=").append(tmp);
		
		if (idx > 0) {
			Term prev = sentence.get(idx-1);
			builder.append(" prevToken=").append(prev.getText().trim()); 
			builder.append(" prevIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx-1)); 
			builder.append(" prevBioTag=").append(prev.getBIOChunk());  
			builder.append(" prevChunkType=").append(TermUtils.getBIOChunkType(prev)); 
			builder.append(" prevPOS=").append(prev.getPartOfSpeech());
		}
		
		if (idx > 1) {
			Term prev = sentence.get(idx-2);
			builder.append(" prevprevToken=").append(prev.getText().trim());  
			builder.append(" prevprevIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx-2)); 
			builder.append(" prevprevBioTag=").append(prev.getBIOChunk()); 
			builder.append(" prevprevPOS=").append(prev.getPartOfSpeech());
			builder.append(" shortestPathToPrevPrev=").append(TermUtils.getShortestPath(sentence, idx, idx-2, tree)); 

		}
		
		if (idx < sentence.size()-1) {
			Term next = sentence.get(idx+1);
			builder.append(" nextToken=").append(next.getText().trim());
			builder.append(" nextIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx+1)); 
			builder.append(" nextBioTag=").append(next.getBIOChunk());
			builder.append(" nextChunkType=").append(TermUtils.getBIOChunkType(next));
			builder.append(" nextPOS=").append(next.getPartOfSpeech()); 
			builder.append(" shortestPathToNext=").append(TermUtils.getShortestPath(sentence, idx, idx+1, tree)); 

		}
		
		if (idx < sentence.size()-2) {
			Term next = sentence.get(idx+2);
			builder.append(" nextnextToken=").append(next.getText().trim());
			builder.append(" nextnextIsHeadToken=").append(TermUtils.isHeadToken(sentence, idx+2)); 
			builder.append(" nextnextPOS=").append(next.getPartOfSpeech()); 
			builder.append(" shortestPathToNextNext=").append(TermUtils.getShortestPath(sentence, idx, idx+2, tree)); 


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
			chunker.addBIOChunks(sentence);
			tagSentence(sentence);
			Tree tree = getTree(sentence);
			
			for (int i=0; i< sentence.size(); i++) {
				if (!EntityUtils.isPerson(sentence.get(i))) {
					continue;
				}
				String name = sentence.get(i).getText().replaceAll("_", " ");
				Map<String,Double> scores = new HashMap<String,Double>();
				for (String prediction : predictions) {
					String line = encodeTerm(sentence, i, null, false, prediction, tree);
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
