package com.grunick.familytree.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Response;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.util.EntityUtils;
import com.grunick.familytree.util.TermUtils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class ParseTreeMaxEntropyStrategy extends AbstractMaxEntropyStrategy {
	
	protected LexicalizedParser parser;

	public ParseTreeMaxEntropyStrategy(String entityExtractorConfig,
			File entropyFile, File persistFile, String parserModel) {
		super(entityExtractorConfig, entropyFile, persistFile);
		this.parser = LexicalizedParser.loadModel(parserModel);
	}

	protected List<String> encodeTerms(List<Term> sentence, Response prediction, boolean isTrainingSet) throws Exception {
		sentence = EntityUtils.combineEntities(sentence);
		List<String> terms = new ArrayList<String>();

	    Tree tree = getTree(sentence);
	    
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
	


	
	protected String encodeTerm(List<Term> sentence, int idx, Response prediction, boolean isTrainingSet, Tree tree) throws Exception {
		Term term = sentence.get(idx);
		
		String text = term.getText().trim();
		StringBuilder builder = new StringBuilder();

		builder.append("curToken=").append(text);
		builder.append(" entityTag=").append(term.getNETag());
		builder.append(" isPerson=").append(EntityUtils.isPerson(term));
		
		builder.append(" curTag=").append(TermUtils.getCurrentTag(sentence, idx, tree));
		builder.append(" constituentTags=").append(TermUtils.getConsituentTags(sentence, idx, tree)); 
		builder.append(" constituentTerms=").append(TermUtils.getConsituentTerms(sentence, idx, tree)); 
		//builder.append(" shortestPathToStart=").append(TermUtils.getShortestPath(sentence, idx, 0, tree));  //negative
		//builder.append(" shortestPathToEnd=").append(TermUtils.getShortestPath(sentence, idx, sentence.size()-1, tree)); //negative 
		
		if (idx > 0) {
			Term prev = sentence.get(idx-1);
			builder.append(" prevToken=").append(prev.getText().trim());
			//builder.append(" shortestPathToPrev=").append(TermUtils.getShortestPath(sentence, idx, idx-1, tree)); //negative
		}
		
		if (idx > 1) {
			Term prev = sentence.get(idx-2);
			builder.append(" prevprevToken=").append(prev.getText().trim());  
			builder.append(" shortestPathToPrevPrev=").append(TermUtils.getShortestPath(sentence, idx, idx-2, tree)); 
		}
		
		if (idx < sentence.size()-1) {
			Term next = sentence.get(idx+1);
			builder.append(" nextToken=").append(next.getText().trim());
			builder.append(" shortestPathToNext=").append(TermUtils.getShortestPath(sentence, idx, idx+1, tree)); 
		}
		
		if (idx < sentence.size()-2) {
			Term next = sentence.get(idx+2);
			builder.append(" nextnextToken=").append(next.getText().trim());
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
			List<String> lines = new ArrayList<String>();
			List<Term> terms = new ArrayList<Term>();

		    Tree tree = getTree(sentence);
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
