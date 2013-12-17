
== To compile the code: ==

cd PROJECT_ROOT/src
javac -cp "../lib/*:." com/grunick/familytree/*.java com/grunick/familytree/*/*.java

== To run the main Family Tree Extractor: ==

cd PROJECT_ROOT/src
java -cp "../lib/*:." -Xmx2048M com.grunick.familytree.FamilyTreeExtractor

Note that the configuration for the Family Tree Extractor is found in PROJECT_ROOT/config/extractor.properties

== To run the scorer: ==

cd PROJECT_ROOT/src
java -cp "../lib/*:." com.grunick.familytree.Scorer KEY_OUTPUT SYSTEM_OUTPUT

example: 
java -cp "../lib/*:." com.grunick.familytree.Scorer ../input/dev_answer ../output

== To run a specific model: ==

You can switch between the testing models by changing the strategy value in the extractors.properties

Valid values are:

	keywordEntity
	simpleMaxEntropy
	simpleBinaryMaxEntropy
	posMaxEntropy
	parseTreeMaxEntropy
	bioChunkMaxEntropy
	combinedMaxEntropy
	combinedBinaryMaxEntropy
	
NOTE: the following models take a while (10 minutes?) to run since they generate parse trees: parseTreeMaxEntropy, combinedMaxEntropy, combinedBinaryMaxEntropy

NOTE: This was compiled and ran on Java version "1.6.0_31"