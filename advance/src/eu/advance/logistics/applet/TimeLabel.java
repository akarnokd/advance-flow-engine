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
package eu.advance.logistics.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JComponent;
/**
 * The gantt diagram scalable date label.
 * @author karnokd
 * @version $Revision 1.0$
 */
public class TimeLabel extends JComponent {
	/**
	 *
	 */
	private static final long serialVersionUID = 8953602379408254281L;
	/**
	 * Start date.
	 */
	private long startDate;
	/** The end date. */
	private long endDate;
	/**
	 * Scale.
	 */
	private double scale;
	/**
	 * Formatter for months.
	 */
	private SimpleDateFormat fmtMonths = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Formatter for weeks.
	 */
	private SimpleDateFormat fmtWeeks = new SimpleDateFormat("yyyy-MM-dd '('w')'");
	/**
	 * Formatter for days.
	 */
	private SimpleDateFormat fmtDays = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/**
	 * Formatter for hours.
	 */
	private SimpleDateFormat fmtHours = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/**
	 * Formatter for minutes.
	 */
	private SimpleDateFormat fmtMinutes = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	/**
	 * Formatter for seconds.
	 */
	private SimpleDateFormat fmtSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * The calendar..
	 */
	private GregorianCalendar cal = new GregorianCalendar(new Locale("hu"));
	/**
	 * The start time.
	 */
	private long offset;
	/**
	 * The pixel ratio.
	 */
	private double ratioM;
	/**
	 * Date delta.
	 */
	private long dateDelta;
	/**
	 * Rotate 90 degrees transform.
	 */
	private AffineTransform rotate90 = AffineTransform.getRotateInstance(-Math.PI / 2);
	/** The cached time value. */
	private long cachedTime;
	/** The cached offset value. */
	private long cachedOffset;
	/** The cached ratio value. */
	private double cachedRatioM;
	/** The cached start date. */
	private long cachedStartDate;
	/** The zoom factor. */
	private double zoom = 1.0;
	/** Display the label at the top? */
	private boolean topMode;
	/**
	 * Constructor. Initializes the start date.
	 */
	public TimeLabel() {
		super();
	}
	/**
	 * Returns the scaling factor. Milliseconds / pixel
	 * @return the scale value
	 */
	public double getScale() {
		return scale;
	}
	/**
	 * Set the scaling factor. Milliseconds / pixel
	 * @param value the scale value.
	 */
	public void setScale(double value) {
		scale = value;
		if (scale < 0.0) {
			scale = 0.0;
		}
	}
	/**
	 * @return Compute the milliseconds/pixel ratio based on the current timescale and component drawing width
	 */
	public double computeScale() {
		return zoom * (endDate - startDate) / (getWidth() - getFontMetrics(getFont()).getHeight() / 2);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D gr = (Graphics2D)g;
		if (isOpaque()) {
			gr.setColor(getBackground());
			gr.fillRect(0, 0, getWidth(), getHeight());
		}
		if (startDate == endDate) {
			return;
		}
		FontMetrics normalMetrics = gr.getFontMetrics(getFont());
		int timeHeight = normalMetrics.getHeight(); // minimum distance between small ticks
		int timedesc = normalMetrics.getDescent();
		// minutes per pixel
		ratioM = scale / 60000;
		// time between two text marker
		double viewMinutes = ratioM * timeHeight;

		SimpleDateFormat df = null;
		SimpleDateFormat dfm = null;
		int incrementMillis = 0;
		if (viewMinutes < 1) {
			df = fmtMinutes;
			dfm = fmtSeconds;
			incrementMillis = 60;
		} else
		if (viewMinutes < 60) {
			df = fmtHours;
			dfm = fmtMinutes;
			incrementMillis = 3600;
		} else
		if (viewMinutes < 24 * 60) {
			df = fmtDays;
			dfm = fmtHours;
			incrementMillis = 24 * 60 * 60;
		} else
		if (viewMinutes < 7 * 24 * 60) {
			df = fmtWeeks;
			dfm = fmtDays;
			incrementMillis = 7 * 24 * 60 * 60;
		} else {
			df = fmtMonths;
			dfm = fmtWeeks;
			incrementMillis = 30 * 24 * 60 * 60;
		}

		int smallTickCount = (int)(incrementMillis / ratioM / 60 / timeHeight);
		double smallTickDistance = incrementMillis / ratioM / 60 / smallTickCount;

		cal.setTimeInMillis(startDate);
		cal.add(GregorianCalendar.SECOND, -incrementMillis);
		// if there was no change, restore value from cache
		if (cachedOffset == offset && cachedStartDate == startDate && cachedRatioM == ratioM && cachedTime >= 0) {
			cal.setTimeInMillis(cachedTime);
		} else {
			cachedOffset = offset;
			cachedStartDate = startDate;
			cachedRatioM = ratioM;
			cachedTime = -1;
		}

		int height = getHeight();
		Font smallFont = getFont().deriveFont((float)(getFont().getSize() - 2));
		FontMetrics smallMetrics = gr.getFontMetrics(smallFont);
		int smallTimeHeight = smallMetrics.getHeight();
		int smallTimeDesc = smallMetrics.getDescent();

		Font normalFont = getFont().deriveFont(rotate90);
		smallFont = smallFont.deriveFont(rotate90);
		boolean odd = true;
		long currDate = cal.getTimeInMillis(); //(long)(offset - 3 * timeHeight * scale / 2);
		if (offset - startDate < incrementMillis * 1000) {
			currDate = startDate;
		} else {
			currDate = startDate + ((offset - startDate) / incrementMillis / 1000 - 1) * incrementMillis * 1000;
		}
		//cal.getTimeInMillis(); //(long)(startDate + ((offset - startDate) / scale - 2 * timeHeight) * scale); // startDate + ((offset - startDate) / incrementMillis - 2) * incrementMillis
		while (true) {
			double xr = (currDate - offset) / scale + timeHeight / 2.0;
			int x = (int)Math.round(xr);
			if (x >= -timeHeight) {
//				if (cachedTime < 0) {
//					cachedTime = currDate;
//				}
				cal.setTimeInMillis(currDate);
				int dow = cal.get(GregorianCalendar.DAY_OF_WEEK);
				if (dow == GregorianCalendar.SATURDAY || dow == GregorianCalendar.SUNDAY) {
					gr.setColor(Color.RED);
				} else {
					gr.setColor(getForeground());
				}
				if (topMode) {
					gr.drawLine(x, 0, x, 8);
					gr.setFont(normalFont);
					String dateStr = df.format(new Date(currDate + dateDelta));
					gr.drawString(dateStr, x + timeHeight / 2 - timedesc, 10 + normalMetrics.stringWidth(dateStr));
				} else {
					gr.drawLine(x, height - 1, x, height - 8);
					gr.setFont(normalFont);
					gr.drawString(df.format(new Date(currDate + dateDelta)), x + timeHeight / 2 - timedesc, height - 10);
				}
			}
			if (smallTickCount > 1) {
				double xrt = xr + smallTickDistance;
				long smallDate = currDate;
				for (int i = 1; i < smallTickCount; i++) {
					smallDate += 60000 * ratioM * smallTickDistance;
					int ixrt = (int)Math.round(xrt);
					if (ixrt > -smallTimeHeight) {
//						if (cachedTime < 0) {
//							cachedTime = currDate;
//						}
						cal.setTimeInMillis(smallDate + dateDelta);
						int dow = cal.get(GregorianCalendar.DAY_OF_WEEK);
						if (dow == GregorianCalendar.SATURDAY || dow == GregorianCalendar.SUNDAY) {
							gr.setColor(Color.RED);
						} else {
							gr.setColor(getForeground());
						}
						if (topMode) {
							gr.drawLine(ixrt, 0, ixrt, 4);
							gr.setFont(smallFont);
							String dateStr = dfm.format(new Date(smallDate + dateDelta));
							int stringWidth = smallMetrics.stringWidth(dateStr);
//							System.out.printf("%s: %d%n", dateStr, stringWidth);
							gr.drawString(dateStr, ixrt + smallTimeHeight / 2 - smallTimeDesc , 6 + stringWidth);
						} else {
							gr.drawLine(ixrt, height - 1, ixrt, height - 4);
							gr.setFont(smallFont);
							gr.drawString(dfm.format(new Date(smallDate + dateDelta)), ixrt + smallTimeHeight / 2 - smallTimeDesc , height - 6);
						}
					}
					xrt += smallTickDistance;
					odd = !odd;
				}
			} else {
				odd = !odd;
			}
			if (x > getWidth()) {
				break;
			}
			//cal.add(incrementMode, incrementMillis);
			currDate += incrementMillis * 1000L;
		}
	}
	/**
	 * Returns the date offset of the first renderable main tick line.
	 * @return the first tick date
	 */
	public long getFirstTickDate() {
		long incrementMillis = getTimeIncrement();
		if (offset - startDate < incrementMillis * 1000) {
			return startDate;
		}
		return startDate + ((offset - startDate) / incrementMillis / 1000 - 1) * incrementMillis * 1000;
	}
	/**
	 * Returns the large time increment value in milliseconds.
	 * @return the increment in milliseconds
	 */
	public long getTimeIncrement() {
		double lratioM = scale / 60000;
		// time between two text marker
		double viewMinutes = lratioM * getTickSize();
		int incrementMillis = 0;
		if (viewMinutes < 1) {
			incrementMillis = 60;
		} else
		if (viewMinutes < 60) {
			incrementMillis = 3600;
		} else
		if (viewMinutes < 24 * 60) {
			incrementMillis = 24 * 60 * 60;
		} else
		if (viewMinutes < 7 * 24 * 60) {
			incrementMillis = 7 * 24 * 60 * 60;
		} else {
			incrementMillis = 30 * 24 * 60 * 60;
		}
		return incrementMillis * 1000L;
	}
	/**
	 * @return Returns the offset.
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @param offset The offset to set.
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}
	/**
	 * @return Returns the startDate.
	 */
	public long getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return Returns the ratioM.
	 */
	public double getRatioM() {
		return ratioM;
	}
	/**
	 * @return Returns the tick size in pixels.
	 */
	public int getTickSize() {
		Graphics g = getGraphics();
		if (g != null) {
			try {
				return g.getFontMetrics(getFont()).getHeight();
			} finally {
				g.dispose();
			}
		}
		return 0;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		Graphics g = getGraphics();
		if (g != null) {
			try {
				FontMetrics fm = g.getFontMetrics(getFont());
				return new Dimension(super.getPreferredSize().width, fm.stringWidth("9999-99-99 99:99:99") + 12);
			} finally {
				g.dispose();
			}
		}
		return super.getPreferredSize();
	}
	/**
	 * @return the number of ticks between the start and end date.
	 */
	public int getTickCount() {
		return (int)Math.ceil((double)(endDate - startDate) / getTickSize() / scale);
	}
	/**
	 * @return returns the tickcount wich takes the diagram size into account
	 */
	public int getEffectiveTickCount() {
		int tickSize = getTickSize();
		return (int)Math.ceil((double)(endDate - startDate) / tickSize / scale - (double)getWidth() / tickSize);
	}
	/**
	 * @return Returns the dateDelta.
	 */
	public long getDateDelta() {
		return dateDelta;
	}
	/**
	 * @param dateDelta The dateDelta to set.
	 */
	public void setDateDelta(long dateDelta) {
		this.dateDelta = dateDelta;
	}
	/**
	 * @param endDate the end date to set
	 */
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}
	/**
	 * @return the endDate
	 */
	public long getEndDate() {
		return endDate;
	}
	/**
	 * Scroll to the given percent of the start-end time range.
	 * @param percent the percent to scroll to
	 */
	public void scrollToPercent(double percent) {
		offset = (long)(startDate + (endDate - startDate) * percent);
	}
	/**
	 * Scroll to the given tick.
	 * @param tick the tick to scroll to.
	 */
	public void scrollToTick(int tick) {
		offset = (long)(startDate + tick * getTickSize() * scale);
	}
	/**
	 * Scroll to the given pixel offset.
	 * @param pixel the pixel to scroll
	 */
	public void scrollToPixel(int pixel) {
		offset = (long)(startDate + scale * pixel);
	}
	/**
	 * Returns the effective scrollable width of the diagram in pixels.
	 * @return the effective width.
	 */
	public int getEffectiveWidth() {
		return (int)((endDate - startDate) / scale - getWidth() + getTickSize());
	}
	/** 
	 * Set the zoom factor. E.g. proportion to the full draw-width equals full time range mode 
	 * @param zoom the zoom factor
	 */
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
	/**
	 * @return get the zoom factor.
	 */
	public double getZoom() {
		return zoom;
	}
	/**
	 * Set the label display mode to top.
	 * @param topMode display the labels from top.
	 */
	public void setTopMode(boolean topMode) {
		this.topMode = topMode;
	}
	/**
	 * @return is the label displayed from top?
	 */
	public boolean isTopMode() {
		return topMode;
	}
}
