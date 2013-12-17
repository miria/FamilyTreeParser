package com.grunick.familytree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.grunick.familytree.data.Obiterator;
import com.grunick.familytree.data.Obituary;
import com.grunick.familytree.strategy.BIOChunkMaxEntropyStrategy;
import com.grunick.familytree.strategy.CombinedBinaryMaxEntropyStrategy;
import com.grunick.familytree.strategy.CombinedMaxEntropyStrategy;
import com.grunick.familytree.strategy.IStrategy;
import com.grunick.familytree.strategy.KeywordEntityStrategy;
import com.grunick.familytree.strategy.POSMaxEntropyStrategy;
import com.grunick.familytree.strategy.ParseTreeMaxEntropyStrategy;
import com.grunick.familytree.strategy.SimpleBinaryMaxEntropyStrategy;
import com.grunick.familytree.strategy.SimpleMaxEntropyStrategy;
import com.grunick.familytree.util.InputOutputUtils;

public class FamilyTreeExtractor {
	
	protected static File inputPath;
	protected static File outputPath;
	protected static File trainInputPath;
	protected static File trainResultPath;
	protected static File maxentPersistPath;
	protected static File maxentTmpPath;

	
	public static IStrategy getStrategy(Properties properties) throws IOException, ClassNotFoundException {
		String strategyConfig = properties.getProperty("strategy");
		if ("keywordEntity".equalsIgnoreCase(strategyConfig))
			return new KeywordEntityStrategy(properties.getProperty("classifier.path"));
		if ("simpleMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new SimpleMaxEntropyStrategy(properties.getProperty("classifier.path"),
					maxentPersistPath, maxentTmpPath);
		if ("simpleBinaryMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new SimpleBinaryMaxEntropyStrategy(properties.getProperty("classifier.path"),
					properties.getProperty("maxent.tmp.dir"),
					properties.getProperty("maxent.persist.dir"));
		if ("posMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new POSMaxEntropyStrategy(properties.getProperty("classifier.path"),
					maxentPersistPath, maxentTmpPath, properties.getProperty("postagger.model.path"));
		if ("parseTreeMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new ParseTreeMaxEntropyStrategy(properties.getProperty("classifier.path"),
					maxentPersistPath, maxentTmpPath, properties.getProperty("parser.model.path"));
		if ("bioChunkMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new BIOChunkMaxEntropyStrategy(properties.getProperty("classifier.path"),
					maxentPersistPath, maxentTmpPath, properties.getProperty("chunker.model.path"));
		if ("combinedMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new CombinedMaxEntropyStrategy(properties.getProperty("classifier.path"),
					maxentPersistPath, maxentTmpPath, properties.getProperty("chunker.model.path"),
					properties.getProperty("parser.model.path"), properties.getProperty("postagger.model.path"));
		if ("combinedBinaryMaxEntropy".equalsIgnoreCase(strategyConfig))
			return new CombinedBinaryMaxEntropyStrategy(properties.getProperty("classifier.path"),
					properties.getProperty("maxent.tmp.dir"), properties.getProperty("maxent.persist.dir"), 
					properties.getProperty("chunker.model.path"), properties.getProperty("parser.model.path"), 
					properties.getProperty("postagger.model.path"));
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		String path = "../config/extractor.properties";
		if (args.length > 0)
			path = args[0];
		
		System.out.println("Reading properties file from "+path);
		Properties properties = new Properties();
		properties.load(new FileReader(path));
		loadFiles(properties);
		
		IStrategy strategy = getStrategy(properties);
		if (strategy == null) {
			System.out.println("Unknown strategy "+properties.getProperty("strategy"));
			return;
		}
		System.out.println("Loaded strategy "+strategy);

		
		long start = System.currentTimeMillis();
		System.out.println("Starting to train system....");
		strategy.trainModel(trainInputPath, trainResultPath);
		System.out.println("System training complete.");
		
		Obiterator iter = new Obiterator(inputPath);

		while (iter.hasNext()) {
			start = System.currentTimeMillis();
			Obituary obit = iter.next();
			System.out.println("Processing "+obit.getInputFile().getName());
			strategy.detectRelationships(obit);
			InputOutputUtils.writePrediction(outputPath, obit);
			System.out.println("Wrote "+obit.getOutputFile().getName()+" ("+(System.currentTimeMillis()-start)+" ms)");
			
		}

	}
	
	protected static void loadFiles(Properties properties) throws IOException {
		String tmp = properties.getProperty("input.dir");
		inputPath = new File(tmp);
		
		tmp = properties.getProperty("output.dir");
		outputPath = new File(tmp);

		tmp = properties.getProperty("train.input.dir");
		trainInputPath = new File(tmp);
		
		tmp = properties.getProperty("train.result.dir");
		trainResultPath = new File(tmp);
		
		tmp = properties.getProperty("maxent.persist.file");
		maxentPersistPath = new File(tmp);
		
		tmp = properties.getProperty("maxent.tmp.file");
		maxentTmpPath = new File(tmp);
		
	}
}
