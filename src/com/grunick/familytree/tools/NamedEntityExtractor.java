package com.grunick.familytree.tools;

import java.util.ArrayList;
import java.util.List;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Term;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;


public class NamedEntityExtractor {
	
	protected AbstractSequenceClassifier<CoreLabel> classifier;
	
	@SuppressWarnings("unchecked")
	public NamedEntityExtractor(String classifierPath) {
	      classifier = CRFClassifier.getClassifierNoExceptions(classifierPath);

	}
	
	public List<List<Term>> tagEntities(Obituary obit) {
		StringBuilder builder = new StringBuilder(obit.getParagraphs().get(0));
		for (int i=1; i<obit.getParagraphs().size(); i++) {
			builder.append("\n").append(obit.getParagraphs().get(i));
		}
		
        List<List<CoreLabel>> out = classifier.classify(builder.toString());
        
        List<List<Term>> sentenceMatches = new ArrayList<List<Term>>();
        for (List<CoreLabel> sentence : out) {
        	boolean hasPerson = false;
        	List<Term> phrase = new ArrayList<Term>();
        	for (CoreLabel word : sentence) {
        		Term term = new Term(word.word());
        		term.setNETag(word.get(AnswerAnnotation.class));
        		phrase.add(term);
        		if ("PERSON".equals(term.getNETag())) 
        			hasPerson = true;
        	}
        	if (hasPerson)
        		sentenceMatches.add(phrase);
        }
        
        return sentenceMatches;

	}

}
