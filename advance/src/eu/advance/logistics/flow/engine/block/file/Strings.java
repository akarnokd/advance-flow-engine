/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package eu.advance.logistics.flow.engine.block.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;

import eu.advance.logistics.flow.engine.util.U;

/**
 * Manages a bidi-map of strings.
 * @author karnokd, 2012.01.20.
 */
public class Strings {
	/** The bimap for locating labels and ids. */
	BiMap<String, Integer> stringMap;
	/** The index map. */
	BiMap<Integer, String> indexMap;
	/**
	 * Constructor. Initializes the maps.
	 * @param path the path to the string list.
	 * @throws IOException on error
	 */
	public Strings(String path) throws IOException {
		stringMap = HashBiMap.create();
		indexMap = stringMap.inverse();
		
		File f = new File(path);
		BufferedReader in = Files.newReader(f, Charset.forName("ISO-8859-1"));
		try {
			int i = 0;
			for (String s : U.lines(in)) {
				stringMap.put(s, i);
				i++;
			}
		} finally {
			in.close();
		}
	}
	/**
	 * Retrieve the string for the index.
	 * @param index the index
	 * @return the string
	 */
	public String get(int index) {
		return indexMap.get(index);
	}
	/**
	 * Retrieve the index for the string.
	 * @param s the string
	 * @return the index
	 */
	public int get(String s) {
		return stringMap.get(s); 
	}
}
