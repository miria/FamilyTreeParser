package com.grunick.familytree.data;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Response {
	
	protected HashSet<String> parents = new HashSet<String>();
	protected HashSet<String> spouses = new HashSet<String>();
	protected HashSet<String> children = new HashSet<String>();
	
	public void addParent(String parent) {
		parents.add(parent);
	}
	
	public void addChild(String child) {
		children.add(child);
	}
	
	public void addSpouse(String spouse) {
		spouses.add(spouse);
	}
	
	public Set<String> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public Set<String> getSpouses() {
		return Collections.unmodifiableSet(spouses);
	}
	
	public Set<String> getParents() {
		return Collections.unmodifiableSet(parents);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\tParents: ").append(parents);
		builder.append("\n\tSpouses: ").append(spouses);
		builder.append("\n\tChildren: ").append(children);
		return builder.toString();
	}
}
