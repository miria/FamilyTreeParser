package com.grunick.familytree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.grunick.familytree.data.RelevanceScore;
import com.grunick.familytree.data.Response;
import com.grunick.familytree.util.InputOutputUtils;

public class Scorer {
	
	protected static HashMap<String, File> keyMap = new HashMap<String,File>();
	protected static HashMap<String, File> actualMap = new HashMap<String,File>();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		if (!validateInput(args))
			return;
		
		RelevanceScore totalScore = new RelevanceScore();
		RelevanceScore parentScore = new RelevanceScore();
		RelevanceScore spouseScore = new RelevanceScore();
		RelevanceScore childScore = new RelevanceScore();
		
		List<String> files = new ArrayList<String>(keyMap.keySet());
		Collections.sort(files);
		
		for (String f: files) {
			Response keyResult = InputOutputUtils.loadResponseFromFile(keyMap.get(f));
			Response actualResult = new Response();
			if (actualMap.containsKey(f)) 
				actualResult = InputOutputUtils.loadResponseFromFile(actualMap.get(f));
			
			RelevanceScore currentScore = new RelevanceScore();
			
			// Run children
			Collection<String> childMatches = CollectionUtils.intersection(actualResult.getChildren(), keyResult.getChildren());
			for (RelevanceScore score : new RelevanceScore[] {totalScore, childScore, currentScore}) {
				score.incrCorrect(childMatches.size());
				score.incrAnswer(keyResult.getChildren().size());
				score.incrSystem(actualResult.getChildren().size());
			}
			
			// Run parent 
			Collection<String> parentMatches = CollectionUtils.intersection(actualResult.getParents(), keyResult.getParents());
			for (RelevanceScore score : new RelevanceScore[] {totalScore, parentScore, currentScore}) {
				score.incrCorrect(parentMatches.size());
				score.incrAnswer(keyResult.getParents().size());
				score.incrSystem(actualResult.getParents().size());
			}
			
			// Run spouse
			Collection<String> spouseMatches = CollectionUtils.intersection(actualResult.getSpouses(), keyResult.getSpouses());
			for (RelevanceScore score : new RelevanceScore[] {totalScore, spouseScore, currentScore}) {
				score.incrCorrect(spouseMatches.size());
				score.incrAnswer(keyResult.getSpouses().size());
				score.incrSystem(actualResult.getSpouses().size());
			}

			
			System.out.println("--File: "+f+"--");
			System.out.println("Expected:");
			System.out.println(keyResult);
			System.out.println("Actual:");
			System.out.println(actualResult);
			System.out.println(currentScore);
			
		}

		System.out.println("\n\n--Parent Only--");
		System.out.println(parentScore);

		System.out.println("--Spouse Only--");
		System.out.println(spouseScore);
		
		System.out.println("--Child Only--");
		System.out.println(childScore);
		
		System.out.println("--Overall--");
		System.out.println(totalScore);

			
	}
	
	@SuppressWarnings("unchecked")
	protected static boolean validateInput(String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: Usage is: Scorer KEY_DIRECTORY SYSTEM_DIRECTORY");
			return false;
		}
		
		File key = new File(args[0]);
		File actual = new File(args[1]);
		if (!key.isDirectory() || !key.canRead()) {
			System.out.println("ERROR: Key directory is not valid! "+key.getPath());
			return false;
		}
		if (!actual.isDirectory() || !actual.canRead()) {
			System.out.println("ERROR: System directory is not valid! "+actual.getPath());
			return false;
		}
		
		for (File f : Arrays.asList(key.listFiles())) 
			keyMap.put(f.getName(), f);
		
		for (File f : Arrays.asList(actual.listFiles()))
			actualMap.put(f.getName(), f);
		
		Collection<String> col = CollectionUtils.disjunction(keyMap.keySet(), actualMap.keySet());
		if (col.size() > 0) {
			System.out.println("WARN: Key set contains files missing from the system directory:");
			StringBuilder builder = new StringBuilder();
			for (String f : col) {
				builder.append("\t").append(f).append("\n");
			}
			System.out.println(builder.toString());
		}
		
		col = CollectionUtils.disjunction(actualMap.keySet(), keyMap.keySet());
		if (col.size() > 0) {
			System.out.println("WARN: System set contains files missing from the key directory:");
			StringBuilder builder = new StringBuilder();
			for (String f : col) {
				builder.append("\t").append(f).append("\n");
			}
			System.out.println(builder.toString());
		}
		
		return true;
	}
	
	
	

}
