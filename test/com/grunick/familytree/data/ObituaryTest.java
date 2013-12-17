package com.grunick.familytree.data;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import com.grunick.familytree.data.Obituary;

public class ObituaryTest {
	
	@Test
	public void testObituary() {
		Obituary obit = new Obituary("Paul Frank", Arrays.asList(new String[] {
			"This is one sentence about Mr. Frank",
			"This is another sentence about Mr. Frank."
		}), new File("tmp.txt"));
		
		assertEquals(obit.getName(), "Paul Frank");
		assertEquals(obit.getParagraphs().size(), 2);
		assertEquals(obit.getParagraphs().get(0), "This is one sentence about Mr. Frank");
		assertEquals(obit.getInputFile().getPath(), "tmp.txt");
	}
	
	public void testPredictions() {
		Obituary obit = new Obituary("Paul Frank", Arrays.asList(new String[] {
				"This is one sentence about Mr. Frank",
				"This is another sentence about Mr. Frank."
			}), new File("tmp.txt"));
		obit.addSpouse("Jane Doe");
		obit.addSpouse("Jill Moe");
		obit.addParent("Bill Frank");
		obit.addParent("Mary Frank");
		obit.addChild("Billy");
		obit.addChild("John Frank");
		
		assertEquals(obit.getParents(), Arrays.asList(new String[] {"Bill Frank", "Mary Frank"}));
		assertEquals(obit.getChildren(), Arrays.asList(new String[] {"Billy", "John Frank"}));
		assertEquals(obit.getSpouses(), Arrays.asList(new String[] {"Jane Doe", "Jill Moe"}));

	}

}
