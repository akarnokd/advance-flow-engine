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

package eu.advance.logistics.xml;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class to format and parse XSD dateTime values.
 * Thread safe.
 * See http://www.w3.org/TR/xmlschema-2/#dateTime for the format specification
 * @author karnokd, 2007.12.14.
 * @version $Revision 1.0$
 */
public final class XsdDateTime {
	/**
	 * Private constructor.
	 */
	private XsdDateTime() {
		throw new AssertionError("Utility class");
	}
	/**
	 * Gregorian calendar for XSD dateTime.
	 */
	private static final ThreadLocal<GregorianCalendar> XSD_CALENDAR = new ThreadLocal<GregorianCalendar>() {
		@Override
		protected GregorianCalendar initialValue() {
			return new GregorianCalendar();
		}
	};
	/**
	 * Convert the given date to string.
	 * Always contains the milliseconds and timezone.
	 * @param date the date, not null
	 * @return the formatted date
	 */
	public static String format(Date date) {
		StringBuilder b = new StringBuilder(24);
		
		GregorianCalendar cal = XSD_CALENDAR.get();
		cal.setTime(date);
		
		int value = 0;
		
		// Year-Month-Day
		value = cal.get(GregorianCalendar.YEAR);
		b.append(value);
		b.append('-');
		value = cal.get(GregorianCalendar.MONTH) + 1;
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		b.append('-');
		value = cal.get(GregorianCalendar.DATE);
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		
		b.append('T');
		// hour:minute:second:milliseconds
		value = cal.get(GregorianCalendar.HOUR_OF_DAY);
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		b.append(':');
		value = cal.get(GregorianCalendar.MINUTE);
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		b.append(':');
		value = cal.get(GregorianCalendar.SECOND);
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		b.append('.');
		
		value = cal.get(GregorianCalendar.MILLISECOND);
		// add leading zeros if needed
		if (value < 100) {
			b.append('0');
		}
		if (value < 10) {
			b.append('0');
		}
		b.append(value);
		
		value = cal.get(GregorianCalendar.DST_OFFSET) + cal.get(GregorianCalendar.ZONE_OFFSET);
		
		if (value == 0) {
			b.append('Z');
		} else {
			if (value < 0) {
				b.append('-');
				value = -value;
			} else {
				b.append('+');
			}
			int hour = value / 3600000;
			int minute = value / 60000 % 60;
			if (hour < 10) {
				b.append('0');
			}
			b.append(hour);
			b.append(':');
			if (minute < 10) {
				b.append('0');
			}
			b.append(minute);
		}
		
		
		return b.toString();
	}
	/**
	 * Parse an XSD dateTime.
	 * @param date the date string
	 * @return the date
	 * @throws ParseException format exception
	 */
	public static Date parse(String date) throws ParseException {
		GregorianCalendar cal = XSD_CALENDAR.get();
		cal.set(GregorianCalendar.MILLISECOND, 0);
		// format yyyy-MM-dd'T'HH:mm:ss[.sss][zzzzz] no milliseconds no timezone
		int offset = 0;
		try {
			offset = 0;
			cal.set(GregorianCalendar.YEAR, Integer.parseInt(date.substring(offset, offset + 4)));
			offset = 5;
			cal.set(GregorianCalendar.MONTH, Integer.parseInt(date.substring(offset, offset + 2)) - 1);
			offset = 8;
			cal.set(GregorianCalendar.DATE, Integer.parseInt(date.substring(offset, offset + 2)));
			offset = 11;
			cal.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(date.substring(offset, offset + 2)));
			offset = 14;
			cal.set(GregorianCalendar.MINUTE, Integer.parseInt(date.substring(offset, offset + 2)));
			offset = 17;
			cal.set(GregorianCalendar.SECOND, Integer.parseInt(date.substring(offset, offset + 2)));
			
			if (date.length() > 19) {
				offset = 19;
				char c = date.charAt(offset);
				// check milliseconds
				if (c == '.') {
					offset++;
					int endOffset = offset;
					// can be multiple
					while (endOffset < date.length() && Character.isDigit(date.charAt(endOffset))) {
						endOffset++;
					}
					int millisec = Integer.parseInt(date.substring(offset, endOffset));
					int len = endOffset - offset - 1;
					if (len >= 3) {
						while (len-- >= 3) {
							millisec /= 10;
						}
					} else {
						while (++len < 3) {
							millisec *= 10;
						}
					}
					cal.set(GregorianCalendar.MILLISECOND, millisec);
					if (date.length() > endOffset) {
						offset = endOffset;
						c = date.charAt(offset);
					} else {
						c = '\0';
					}
				}
				if (c == 'Z') {
					cal.set(GregorianCalendar.ZONE_OFFSET, 0);
				} else
				if (c == '-' || c == '+') {
					int sign = c == '-' ? -1 : 1;
					offset++;
					int tzHour = Integer.parseInt(date.substring(offset, offset + 2));
					offset += 3;
					int tzMinute = Integer.parseInt(date.substring(offset, offset + 2));
					cal.set(GregorianCalendar.ZONE_OFFSET, sign * (tzHour * 3600000 + tzMinute * 60000));
				} else
				if (c != '\0') {
					throw new ParseException("Unknown milliseconds or timezone", offset);
				}
			}
		} catch (NumberFormatException ex) {
			throw new ParseException(ex.toString(), offset);
		} catch (IndexOutOfBoundsException ex) {
			throw new ParseException(ex.toString(), offset);
		}
		return cal.getTime();
	}
}
