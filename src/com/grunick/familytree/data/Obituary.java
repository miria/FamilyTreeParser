package com.grunick.familytree.data;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Obituary {
	
	protected String name;
	protected File infile;
	protected File outfile = null;
	protected List<String> paragraphs;
	protected List<String> spouses = new ArrayList<String>();
	protected List<String> parents = new ArrayList<String>();
	protected List<String> children = new ArrayList<String>();
	
	public Obituary(String name, List<String> paragraphs, File infile) {
		this.name = name;
		this.paragraphs = paragraphs;
		this.infile = infile;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getParagraphs() {
		return Collections.unmodifiableList(paragraphs);
	}
	
	public void addSpouse(String name) {
		spouses.add(name);
	}
	
	public void addSpouses(List<String> names) {
		spouses.addAll(names);
	}
	
	public List<String> getSpouses() {
		return Collections.unmodifiableList(spouses);
	}
	
	public void addParent(String name) {
		parents.add(name);
	}
	
	public void addParents(List<String> names) {
		parents.addAll(names);
	}
	
	public List<String> getParents() {
		return Collections.unmodifiableList(parents);
	}
	
	public void addChild(String name) {
		children.add(name);
	}
	
	public void addChildren(List<String> names) {
		children.addAll(names);
	}
	
	public List<String> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public File getInputFile() {
		return infile;
	}
	
	public void setOutputFile(File outfile) {
		this.outfile = outfile;
	}
	
	public File getOutputFile() {
		return outfile;
	}

}
