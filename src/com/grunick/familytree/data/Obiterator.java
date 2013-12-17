package com.grunick.familytree.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.grunick.familytree.util.InputOutputUtils;


public class Obiterator implements Iterator<Obituary>{
	
	protected List<File> files;
	
	public Obiterator(File dir) throws IOException {
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Invalid directory for Obituaries - "+dir.getPath());
		files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
	}

	@Override
	public boolean hasNext() {
		return files.size() > 0;
	}

	@Override
	public Obituary next() {
		while (true) {
			if (files.size() == 0)
				throw new NoSuchElementException();
			File file = files.remove(0);
			try {
				return InputOutputUtils.loadObituaryFromFile(file);
			} catch (IOException e) {}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Obiterator is read only!");
		
	}

}
