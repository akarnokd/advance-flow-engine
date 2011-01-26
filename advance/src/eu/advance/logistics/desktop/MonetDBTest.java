/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.desktop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple test program to communicate with the local MonetDB instance on port 50000.
 * @author karnokd
 */
public final class MonetDBTest {
	/** Utility class. */
	private MonetDBTest() {
		
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
		Connection conn = DriverManager.getConnection("jdbc:monetdb://localhost/demo", "monetdb", "monetdb");
		conn.setAutoCommit(false);
		try {
			System.out.println(detectVersion(conn));
			testUnicodeVarchar(conn);
		} finally {
			conn.close();
		}
	}
	/** The JDBC version enumeration. */
	enum JDBCVersion {
		/** Not known. */
		V_UNKNOWN,
		/** Version 1.0 The first version introduced in JDK 1.0. */
		V_10,
		/** Version 2.0 introduced in JDK 1.2. */
		V_21,
		/** Version 3.0 introduced in JDK 1.4. See JSR 54. */
		V_30,
		/** Version 4.0 introduced in JDK 1.6. See JSR 221. */
		V_40,
		/** Version 4.1 introduced in JDK 1.7. */
		V_41,
	}
	/**
	 * Detect the JDBC version from the supplied connection object.
	 * @param conn the connection
	 * @return the detected version
	 */
	public static JDBCVersion detectVersion(Connection conn) {
		Class<?> conClass = conn.getClass();
		
		try {
			Method m = conClass.getMethod("getTypeMap"); // Introduced in JDBC 2.0
			m.invoke(conn);
		} catch (NoSuchMethodException e) {
			return JDBCVersion.V_10;
		} catch (SecurityException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalAccessException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			return JDBCVersion.V_10;
		}
		
		try {
			Method m = conClass.getMethod("getHoldability"); // Introduced in JDBC 3.0
			m.invoke(conn);
		} catch (NoSuchMethodException e) {
			return JDBCVersion.V_21;
		} catch (SecurityException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalAccessException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			return JDBCVersion.V_21;
		}

		try {
			Method m = conClass.getMethod("isValid", Integer.TYPE); // Introduced in JDBC 4.0
			m.invoke(conn, 100);
		} catch (NoSuchMethodException e) {
			return JDBCVersion.V_30;
		} catch (SecurityException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalAccessException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			return JDBCVersion.V_30;
		}
		
		try {
			Method m = conClass.getMethod("getSchema"); // Introduced in JDBC 4.1
			m.invoke(conn);
		} catch (NoSuchMethodException e) {
			return JDBCVersion.V_40;
		} catch (SecurityException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalAccessException e) {
			return JDBCVersion.V_UNKNOWN;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			return JDBCVersion.V_40;
		}

		return JDBCVersion.V_41;
	}
	/**
	 * Test if the VARCHAR column accepts multilingual text.
	 * @param conn the connection object
	 * @throws SQLException on error
	 */
	public static void testUnicodeVarchar(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		try {
			stmt.executeUpdate("CREATE TABLE a (text VARCHAR(50))");
		} catch (SQLException ex) {
			conn.rollback();
		}
		try {
	//		queryTables(conn);
			try {
				long t0 = System.currentTimeMillis();
				for (int j = 0; j < 1; j++) {
					PreparedStatement pstmt = conn.prepareStatement("INSERT INTO a VALUES (?) ");
					for (int i = 0; i < 1000000; i++) {
						pstmt.setString(1, "ÁRVÍZTŰRŐ TÜKÖRFÚRÓGÉP");
						pstmt.addBatch();
						if (i % 1000 == 0 && i > 0) {
							System.out.print(".");
							if (i % 50000 == 0) {
								System.out.println();
							}
						}
					}
					System.out.println();
					pstmt.executeBatch();
					conn.commit();
				}
				
				System.out.printf("Insert time: %ds%n", (System.currentTimeMillis() - t0) / 1000);
				
				long t1 = System.currentTimeMillis();
				ResultSet rs = stmt.executeQuery("SELECT * FROM a");
				while (rs.next()) {
					rs.getString(1);
				}
				System.out.printf("Select time: %ds%n", (System.currentTimeMillis() - t1) / 1000);
			} catch (SQLException ex) {
				conn.rollback();
			}
		} finally {
			stmt.executeUpdate("DROP TABLE a");
			conn.commit();
		}		
	}
	/**
	 * Query the list of tables.
	 * @param conn the connection
	 * @throws SQLException on error
	 */
	public static void queryTables(Connection conn) throws SQLException {
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = dmd.getTables(null, null, null, null);
		try {
			ResultSetMetaData rsm = rs.getMetaData();
			for (int i = 1; i <= rsm.getColumnCount(); i++) {
				System.out.print(rsm.getColumnLabel(i));
				System.out.print("\t");
			}
			System.out.println();
			while (rs.next()) {
				for (int i = 1; i <= rsm.getColumnCount(); i++) {
					System.out.print(rs.getString(i));
					System.out.print("\t");
				}
				System.out.println();
			}
		} finally {
			rs.close();
		}
	}

}
