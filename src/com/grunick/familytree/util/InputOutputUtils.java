package com.grunick.familytree.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Response;


public class InputOutputUtils {
	
	private InputOutputUtils() {}
	
	public static Obituary loadObituaryFromFile(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file);
		if (lines.size() < 3) 
			throw new IOException("Obituary is too short! "+file.getPath());
		
		if (lines.get(1).trim().length() > 0)
			throw new IOException("Invalid Obituary format! Line 2 must be blank! "+file.getPath());
		
		List<String> trimmed = new ArrayList<String>();
		for (int i=2; i<lines.size(); i++) {
			trimmed.add(lines.get(i).trim());
		}

		return new Obituary(lines.get(0), trimmed, file);
		
	}
	
	public static Response loadResponseFromFile(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file);
		Response response = new Response();
		for (String line : lines) {
			String[] pieces = line.split("\t");
			if ("CHILD".equalsIgnoreCase(pieces[0]))
				response.addChild(pieces[1].trim());
			else if ("SPOUSE".equalsIgnoreCase(pieces[0]))
				response.addSpouse(pieces[1].trim());
			else if ("PARENT".equalsIgnoreCase(pieces[0]))
				response.addParent(pieces[1].trim());
		}
		
		return response;
	}
	
	public static void writePrediction(File outputPath, Obituary obit) throws IOException {
		File file = new File(outputPath, obit.getInputFile().getName());
		obit.setOutputFile(file);
		List<String> lines = new ArrayList<String>();
		for (String parent : obit.getParents()) 
			lines.add("PARENT\t"+parent);
		for (String spouse : obit.getSpouses()) 
			lines.add("SPOUSE\t"+spouse);
		for (String child : obit.getChildren()) 
			lines.add("CHILD\t"+child);
		FileUtils.writeLines(file, lines);
		
	}

}
