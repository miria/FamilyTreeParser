package com.grunick.familytree.util;

import java.util.ArrayList;
import java.util.List;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Term;


public class EntityUtils {
	
	private EntityUtils() {}
	
	public static boolean isPerson(String tag) {
		return "PERSON".equalsIgnoreCase(tag);
	}
	
	public static boolean isPerson(Term term) {
		return "PERSON".equalsIgnoreCase(term.getNETag());
	}
	
	public static boolean isParenthesisInName(int idx, List<Term> sentence) {
		Term term = sentence.get(idx);
		if (idx < sentence.size()-1 && isPerson(sentence.get(idx+1)) && term.getText().equals("-LRB-"))
			return true;
		if (idx > 0 && isPerson(sentence.get(idx-1)) && term.getText().equals("-RRB-"))
			return true;
		return false;
		
	}
	
	public static List<String> getEntities(List<Term> sentence, Obituary obit) {
		List<String> entities = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (int i=0; i< sentence.size(); i++) {
			Term term = sentence.get(i);
			if (isPerson(term)) {
				builder.append(term.getText()).append(" ");
			} else if (i < sentence.size()-1 && isPerson(sentence.get(i+1)) &&
					term.getText().equals("-LRB-")) {
				builder.append("(");
			} else if (i > 0 && isPerson(sentence.get(i-1)) &&
					term.getText().equals("-RRB-")) {
				builder.deleteCharAt(builder.length()-1);
				builder.append(") ");
			} else if (builder.length() > 0) {
				builder.deleteCharAt(builder.length()-1);
				if (!obit.getName().equalsIgnoreCase(builder.toString())) {
					entities.add(builder.toString());
				}
				builder = new StringBuilder();
			}
		}
		
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length()-1);
			
			if (!obit.getName().equalsIgnoreCase(builder.toString())) {
				entities.add(builder.toString());
			}
		}
		
		return entities;
	}
	
	
	public static String getEnclosingEntity(List<Term> sentence, int idx) {
		
		if (!isPerson(sentence.get(idx)) && !isParenthesisInName(idx, sentence)) 
			return null;
		int start = idx;
		int end = idx;
		
		// Check backwards
		for (int i=idx; i>=0; i--) {
			if (isPerson(sentence.get(i)) || isParenthesisInName(i, sentence))
				start = i;
			else
				break;	
		}
		
		for (int i=idx; i<sentence.size(); i++) {
			if (isPerson(sentence.get(i)) || isParenthesisInName(i, sentence))
				end = i;
			else
				break;	
		}
		
		List<Term> name = sentence.subList(start, end+1);
		StringBuilder builder = new StringBuilder();
		for (int i=0; i< name.size(); i++) {
			Term term = name.get(i);
			if (i < name.size()-1 && isPerson(name.get(i+1)) && term.getText().equals("-LRB-")) {
				builder.append("(");
			} else if (i > 0 && isPerson(name.get(i-1)) && term.getText().equals("-RRB-")) {
				builder.deleteCharAt(builder.length()-1);
				builder.append(") ");
			} else {
				builder.append(term.getText()).append(" ");
			}	
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
		
	}
	
	public static List<Term> combineEntities(List<Term> sentence) {
		List<Term> newSentence = new ArrayList<Term>();
		
		int idx = 0;
		StringBuilder builder = new StringBuilder();
		while (idx < sentence.size()) {
			Term term = sentence.get(idx);
			if (isPerson(term)) {
				builder.append(term.getText()).append("_");
			} else if (idx < sentence.size()-1 && isPerson(sentence.get(idx+1)) && term.getText().equals("-LRB-")) {
				builder.append("(");
			} else if (idx > 0 && isPerson(sentence.get(idx-1)) && term.getText().equals("-RRB-")) {
				builder.deleteCharAt(builder.length()-1);
				builder.append(")_");
			} else {
				if (builder.length() > 0) {
					builder.deleteCharAt(builder.length()-1);
					Term newTerm = new Term(builder.toString());
					newTerm.setNETag("PERSON");
					newSentence.add(newTerm);
					builder = new StringBuilder();
				}
				newSentence.add(term);
			} 
			idx += 1;
			
		}
		
		return newSentence;
		
	}

}
