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

import java.awt.geom.Point2D;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.joda.time.DateTime;

/**
 * Contains a memory mapped consignment column.
 * @author karnokd, 2012.01.24.
 */
public class MappedColumn implements Closeable {
	/** The memory mapped buffer. */
	protected MappedByteBuffer column;
	/** The backing random access file. */
	protected RandomAccessFile raf;
	/** Bytes per row. */
	protected int bytesPerRow;
	/** The file length. */
	public long length;
	/**
	 * Opens the specified column.
	 * @param path the path to the column file
	 * @param bytesPerRow bytes per row
	 * @throws IOException on error
	 */
	public MappedColumn(File path, int bytesPerRow) throws IOException {
		raf = new RandomAccessFile(path, "r");
		column = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length());
		this.bytesPerRow = bytesPerRow;
		this.length = raf.length();
	}
	@Override
	public void close() throws IOException {
		column = null;
		if (raf != null) {
			raf.close();
			raf = null;
		}
	}
	/**
	 * @return The total column length in bytes. 
	 */
	public long length() {
		return length;
	}
	/**
	 * @return the number of rows
	 */
	public int rows() {
		return (int)(length / bytesPerRow);
	}
	/**
	 * @return the bytes per row
	 */
	public int bytesPerRow() {
		return bytesPerRow;
	}
	/**
	 * Retrieves a single byte from the target row.
	 * @param row the row index
	 * @return the byte
	 */
	public int getByte(int row) {
		return column.get(row * bytesPerRow) & 0xFF;
	}
	/**
	 * Retrieves a single short from the target row.
	 * @param row the row index
	 * @return the short
	 */
	public int getShort(int row) {
		return column.getShort(row * bytesPerRow) & 0xFFFF;
	}
	/**
	 * Retrieves a single int from the target row.
	 * @param row the row index
	 * @return the int
	 */
	public int getInt(int row) {
		return column.getInt(row * bytesPerRow);
	}
	/**
	 * Retrieves a single long from the target row.
	 * @param row the target row
	 * @return the long
	 */
	public long getLong(int row) {
		return column.getLong(row * bytesPerRow);
	}
	/**
	 * Returns a timestamp value from the row.
	 * @param row the row
	 * @return the timestamp milliseconds
	 */
	public long getTimestamp(int row) {
		return getInt(row) * 60000L;
	}
	/**
	 * Returns a datetime from the given timestamp row.
	 * @param row the row
	 * @return the datetime
	 */
	public DateTime getDateTime(int row) {
		return new DateTime(getTimestamp(row));
	}
	/**
	 * Returns a GPS coordinate from the row or null.
	 * @param row the row
	 * @return the coordinate pair
	 */
	public Point2D.Double getGPS(int row) {
		int x = column.getInt(row * bytesPerRow);
		int y = column.getInt(row * bytesPerRow + 4);
		if (x != Integer.MAX_VALUE) {
			return new Point2D.Double(x / 10000000d, y / 10000000d);
		}
		return null;
	}
}
