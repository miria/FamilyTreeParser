package com.grunick.familytree.util;

import java.util.ArrayList;
import java.util.List;

import com.grunick.familytree.data.Term;

import edu.stanford.nlp.trees.Tree;


public class TermUtils {
	
	protected TermUtils() {}
		
	public static String nonNull(String str) {
		return str == null ? "" : str;
	}
	
	public static String termListToString(List<Term> sentence) {
		StringBuilder builder = new StringBuilder(sentence.get(0).getText()); 
		for (int i=1; i<sentence.size(); i++){
			builder.append(" ").append(sentence.get(i).getText());
		}
		return builder.toString();
	}

	public static boolean isHeadToken(List<Term> sentence, int idx) {
		String bioChunk = sentence.get(idx).getBIOChunk();
		String nextBIOChunk = idx < sentence.size()-1 ? sentence.get(idx+1).getBIOChunk() : "O";
		if (bioChunk.startsWith("I-")) {
			return !nextBIOChunk.startsWith("I-");
		}
		
		if (bioChunk.startsWith("B-")) {
			return !nextBIOChunk.startsWith("I-");
		}
		
		return false;
	}

	
	public static String getCurrentPhrase(List<Term> sentence, int idx) {

		Term current = sentence.get(idx);
		String curBIO = current.getBIOChunk();
		if ("O".equalsIgnoreCase(curBIO)) {
			return current.getText();
		}
		
		List<Term> phrase = new ArrayList<Term>();
		// iterate forward to get the rest of the phrase
		phrase.add(current);
		int iteridx = idx+1;
		Term next = iteridx<sentence.size() ? sentence.get(iteridx) : null;
		while (next != null && next.getBIOChunk().startsWith("I")) {
			phrase.add(next);
			iteridx++;
			next = iteridx<sentence.size() ? sentence.get(iteridx) : null;
		}
			
		// iterate backward to get the start token as well.
		if (curBIO.startsWith("I")) {
			iteridx = idx-1;
			Term prev = sentence.get(iteridx);
			while (prev.getBIOChunk().startsWith("I")) {
				phrase.add(0, prev);
				prev = sentence.get(--iteridx);
			}
			phrase.add(0, prev); // This should be the head.
		}
		
		StringBuilder builder = new StringBuilder();
		for (Term term : phrase) {
			builder.append(term.getText()).append("_");
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	public static String getBIOChunkType(Term bioChunk) {
		String[] chunk = bioChunk.getBIOChunk().split("-");
		return chunk.length >1 ? chunk[1] : chunk[0];
	}
	
	public static String previousType(List<Term> sentence, int idx, String prefix) {
		for (int i=idx-1; i>=0; i--) {
			if (TermUtils.nonNull(sentence.get(i).getPartOfSpeech()).startsWith(prefix)) 
				return sentence.get(i).getText();
		}
		return null;
	}
	
	public static String nextType(List<Term> sentence, int idx, String prefix) {
		for (int i=idx+1; i<sentence.size(); i++) {
			if (TermUtils.nonNull(sentence.get(i).getPartOfSpeech()).startsWith(prefix)) 
				return sentence.get(i).getText();
		}
		return null;
		
	}
	
	public static String getCurrentTag(List<Term> sentence, int idx, Tree tree) {
		List<Tree> children = tree.getLeaves();
	    return children.get(idx).parent(tree).label().value();
	}
	
	public static String getConsituentTerms(List<Term> sentence, int idx, Tree tree) {
		List<Tree> children = tree.getLeaves();
		List<Tree> constituents =  children.get(idx).parent(tree).parent(tree).getLeaves();
		StringBuilder builder = new StringBuilder();
		
		for (Tree constituent : constituents) {
			builder.append(constituent.label().value()).append("_");
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	public static String getConsituentTags(List<Term> sentence, int idx, Tree tree) {
		List<Tree> children = tree.getLeaves();
		List<Tree> constituents =  children.get(idx).parent(tree).parent(tree).getLeaves();
		StringBuilder builder = new StringBuilder();

		for (Tree constituent : constituents) {
			builder.append(constituent.parent(tree).label().value()).append("_");
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	public static String getShortestPath(List<Term> sentence, int idx, int destIdx,  Tree parse) throws Exception {
	    if (idx == destIdx)
	    	return "";

		String[] terms = new String[sentence.size()];
		for (int i=0; i<sentence.size(); i++) {
			terms[i] = sentence.get(i).getText();
		}

	    List<Tree> children = parse.getLeaves();
	    Tree headTree = children.get(destIdx);
	    Tree curTree = children.get(idx).parent(parse);
	    List<Tree> queue = new ArrayList<Tree>();
	    
	    while (curTree.parent(parse) != null) {
	    	Tree parent = curTree.parent(parse);
	    	if (parent.dominates(headTree)) {
	    		List<Tree> path = parent.dominationPath(headTree);
	    		queue.addAll(path);
	    		break;
	    	}
	    	queue.add(parent);
	    	curTree = parent;
	    }
	    
	    if (queue.size() < 2) 
	    	return "";
	    
	    
	    StringBuilder builder = new StringBuilder();
	    builder.append(queue.get(1).label().value());
	    for (int i=2; i< queue.size()-2; i++) {
	    	builder.append("_").append(queue.get(i).label().value());
	    }

	    return builder.toString();
	}

}
