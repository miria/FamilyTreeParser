package com.grunick.familytree.strategy;

import java.io.File;
import java.util.List;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.tools.NamedEntityExtractor;
import com.grunick.familytree.util.EntityUtils;
import com.grunick.familytree.util.TermUtils;


public class KeywordEntityStrategy implements IStrategy {
	
	protected static final String[] spouseWords = new String[] {"married", "wed", "marriage", "wife", "husband"};
	protected static final String[] parentWords = new String[] {"son of", "daughter of"};
	protected static final String[] childWords = new String[] {"born", "daughter", "daughters", "son", "sons", "child", "children"};
	protected NamedEntityExtractor extractor;

	public KeywordEntityStrategy(String entityExtractorConfig) {
		extractor = new NamedEntityExtractor(entityExtractorConfig);
	}
	
	public String toString() {
		return "KeywordEntityStrategy";
	}
	
	@Override
	public void trainModel(File obitDir, File responseDir) {
		// Do nothing.
	}

	@Override
	public void detectRelationships(Obituary obit) {
		
		List<List<Term>> sentences = extractor.tagEntities(obit);
		for (List<Term> sentence : sentences) {
			if (sentence.size() == 0)
				return;
			
			analyzeSentence(sentence, obit);
		}
		
	}
	
	protected void analyzeSentence(List<Term> sentence, Obituary obit) {
		String strSentence = TermUtils.termListToString(sentence).toLowerCase();
		
		// Find spouses first.
		for (String term : spouseWords) {
			if (strSentence.indexOf(term) > -1) {
				obit.addSpouses(EntityUtils.getEntities(sentence, obit));
				System.out.println(strSentence);
				System.out.println(sentence);
				System.out.println("SPOUSES: "+EntityUtils.getEntities(sentence, obit));
				return;
			}
		}
		
		// Find parents next.
		for (String term : parentWords) {
			if (strSentence.indexOf(term) > -1) {
				obit.addParents(EntityUtils.getEntities(sentence, obit));
				System.out.println(strSentence);
				System.out.println(sentence);

				System.out.println("PARENTS: "+EntityUtils.getEntities(sentence, obit));
				return;
			}
		}
		
		// Find children last.
		for (String term : childWords) {
			if (strSentence.indexOf(term) > -1) {
				obit.addChildren(EntityUtils.getEntities(sentence, obit));
				System.out.println(strSentence);
				System.out.println(sentence);

				System.out.println("CHILDREN: "+EntityUtils.getEntities(sentence, obit));
				return;
			}
		}

	}

}
