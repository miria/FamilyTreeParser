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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

public class CombinedMaxEntropyStrategy extends AbstractMaxEntropyStrategy {

	protected PhraseChunker chunker;
	protected LexicalizedParser parser;
	protected MaxentTagger tagger;
	
	public CombinedMaxEntropyStrategy(String entityExtractorConfig,
			File entropyFile, File persistFile, String phraseChunkerConfig,
			String parserModel, String taggerModelPath) throws IOException, ClassNotFoundException {
		super(entityExtractorConfig, entropyFile, persistFile);
		this.chunker = new PhraseChunker(phraseChunkerConfig);
		this.parser = LexicalizedParser.loadModel(parserModel);
		this.tagger = new MaxentTagger(taggerModelPath);
	}

	protected List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTrainingSet) throws Exception {
		sentence = EntityUtils.combineEntities(sentence);
		chunker.addBIOChunks(sentence);
		tagSentence(sentence);
		Tree tree = getTree(sentence);
		List<String> terms = new ArrayList<String>();
		for (int i=0; i < sentence.size(); i++) {
			if (EntityUtils.isPerson(sentence.get(i))) {
				terms.add(encodeTerm(sentence, i, prediction, isTrainingSet, tree));
			}
		}
		return terms;
		
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

	
	protected String encodeTerm(List<Term> sentence, int idx, Response prediction, boolean isTrainingSet, Tree tree) throws Exception {
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
			tagSentence(sentence);
			Tree tree = getTree(sentence);
			List<String> lines = new ArrayList<String>();
			List<Term> terms = new ArrayList<Term>();
			for (int i=0; i < sentence.size(); i++) {
				if (EntityUtils.isPerson(sentence.get(i))) {
					lines.add(encodeTerm(sentence, i, null, false, tree));
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
