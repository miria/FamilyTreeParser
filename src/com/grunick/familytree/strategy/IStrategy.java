package com.grunick.familytree.strategy;
import java.io.File;

import com.grunick.familytree.data.Obituary;



public interface IStrategy {
	
	public void trainModel(File obitDir, File responseDir) throws Exception;
	
	public void detectRelationships(Obituary obit) throws Exception;
	
	public String toString();

}
