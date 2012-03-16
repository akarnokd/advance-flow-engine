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

package eu.advance.logistics.flow.engine.block.db;

import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.xml.XElement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to convert to and from SQL and XML representation of values in JDBC queries.
 * @author karnokd, 2012.02.28.
 */
public final class JDBCConverter {

	/** Utility class. */
	private JDBCConverter() { }
	/**
	 * Converts the current row of the resultset into the appropriate ADVANCE XML type.
	 * @param resolver the data resolver used for the conversion
	 * @param rs the resultset
	 * @param rsmd the resultset metadata
	 * @return the XML map of the key-value pairs from the row
	 * @throws SQLException on error
	 */
	public static XElement create(DataResolver<XElement> resolver, ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
		final Map<XElement, XElement> data = new HashMap<XElement, XElement>();

		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			XElement value = null;

			switch (rsmd.getColumnType(i)) {
			case java.sql.Types.BOOLEAN:
				value = resolver.create(rs.getBoolean(i));
				break;
			case java.sql.Types.INTEGER:
				value = resolver.create(rs.getInt(i));
				break;
			case java.sql.Types.DOUBLE:
				value = resolver.create(rs.getDouble(i));
				break;
			case java.sql.Types.DATE:
				value = resolver.create(rs.getDate(i));
				break;
			case java.sql.Types.BIGINT:
				value = resolver.create(rs.getBigDecimal(i));
				break;
			case java.sql.Types.FLOAT:
				value = resolver.create(rs.getFloat(i));
				break;
			case java.sql.Types.TIME:
				value = resolver.create(rs.getTime(i));
				break;
			case java.sql.Types.TIMESTAMP:
				value = resolver.create(rs.getTimestamp(i));
				break;
			default:
				value = resolver.create(rs.getString(i));
				break;
			}
			if (value != null) {
				data.put(resolver.create(rsmd.getColumnName(i)), value);
			}
		}

		return resolver.create(data);
	}
    /**
     * Sets the given primitive XML value (string, int, real, timestamp, boolean)
     * on the current prepared statement field.
     * @param resolver the data resolver to extract the actual primitive type
     * @param value the value to use
     * @param pstm the target statement
     * @param counter the current index
     * @return the next index
     * @throws SQLException on error
     */
    public static int convert(DataResolver<XElement> resolver, 
    		XElement value, PreparedStatement pstm, int counter) throws SQLException {

        final String val = AdvanceData.realName(value).first;
        if (val.equalsIgnoreCase("integer")) {
            pstm.setInt(counter, resolver.getInt(value));
            counter++;
        } else if (val.equalsIgnoreCase("real")) {
            pstm.setDouble(counter, resolver.getDouble(value));
            counter++;
        } else if (val.equalsIgnoreCase("boolean")) {
            pstm.setBoolean(counter, resolver.getBoolean(value));
            counter++;
        } else if (val.equalsIgnoreCase("timestamp")) {
        	try {
	            pstm.setTimestamp(counter, new Timestamp(resolver.getTimestamp(value).getTime()));
	            counter++;
        	} catch (ParseException ex) {
        		throw new SQLException("Invalid timestamp format: " + value);
        	}
        } else if (val.equalsIgnoreCase("bigdecimal")) {
            pstm.setBigDecimal(counter, resolver.getBigDecimal(value));
            counter++;
        } else if (val.equalsIgnoreCase("float")) {
            pstm.setFloat(counter, resolver.getFloat(value));
            counter++;
        } else {
            pstm.setString(counter, resolver.getString(value));
            counter++;
        }

        return counter;
    }
}
