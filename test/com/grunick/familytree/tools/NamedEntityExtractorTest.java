package com.grunick.familytree.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.data.Term;
import com.grunick.familytree.tools.NamedEntityExtractor;

public class NamedEntityExtractorTest {
	
	@Test
	public void testTagEntities() {
		Obituary obit = new Obituary("J.J. SMITH", Arrays.asList(new String[] {
				"There once was a man named J.J. Smith who was born in California. " +
				"He was born in 1922 on a peach farm which was in the middle of the desert." +
				"He worked for U.S. Steel and served in the French Foreign Legion.",
				"He married a woman named Kate and they had five children: Billy Bob, " +
				"Mary, Jane, Sammy and Joe. Joe Smith later became a famous painter " +
				"and had a painting in the Metropolitan Museum of Art. The painting " +
				"was not that great but they were happy."
		}), new File("tmp.txt") );
		NamedEntityExtractor ner = new NamedEntityExtractor(null);
		List<List<Term>> sentences = ner.tagEntities(obit);
		assertEquals(sentences.size(), 3);
		assertEquals(sentences.get(0).get(1).getText(), "once");
		assertEquals(sentences.get(1).get(1).getText(), "married");
		assertEquals(sentences.get(2).get(1).getText(), "Smith");

	}

}
