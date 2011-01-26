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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Fill pattern usable for rendering a schedule box.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public enum FillPattern {
	/** No filling. */
	NONE(Paints.PAINT_NONE),
	/** Solid filling. */
	SOLID(Paints.PAINT_SOLID),
	/** Simple horizontal dash. */
	DASH(Paints.PAINT_DASH),
	/** Simple vertical dash. */
	PIPE(Paints.PAINT_PIPE),
	/** Forward slashes. */
	SLASH(Paints.PAINT_SLASH),
	/** Backward slash. */
	BACKSLASH(Paints.PAINT_BACKSLASH),
	/** Plus signal. */
	PLUS(Paints.PAINT_PLUS),
	/** Cross. */
	CROSS(Paints.PAINT_CROSS),
	/** Dotted. */
	DOTTED(Paints.PAINT_DOTTED),
	/** Squares. */
	SQUARE(Paints.PAINT_SQUARE),
	/** Diamonds. */
	DIAMOND(Paints.PAINT_DIAMOND)
	;
	/** The texture paint. */
	private final TextureProvider texture;
	/**
	 * Constructor.
	 * @param texture the texture provider.
	 */
	private FillPattern(TextureProvider texture) {
		this.texture = texture;
	}
	/**
	 * @return the texture associated with the paint.
	 */
	public TextureProvider getProvider() {
		return texture;
	}
	/**
	 * Allows the caller to create a custom colored
	 * shaped painted object. Note that
	 * the caller might need to cache the
	 * Pattern+Color pairs to avoid memory overflow.
	 * @author karnokd, 2008.02.06.
	 * @version $Revision 1.0$
	 */
	public interface TextureProvider {
		/**
		 * Get a texture paint with the given foreground color.
		 * @param foreground the foreground color
		 * @return the new painter object.
		 */
		BufferedImage getTexture(Color foreground);
	}
	/**
	 * Helper class to store paint patterns.
	 * @author karnokd, 2008.02.05.
	 * @version $Revision 1.0$
	 */
	private static final class Paints {
		/** No filling. */
		private static final TextureProvider PAINT_NONE;
		/** Solid filling. */
		private static final TextureProvider PAINT_SOLID;
		/** Simple horizontal dash. */
		private static final TextureProvider PAINT_DASH;
		/** Simple vertical dash. */
		private static final TextureProvider PAINT_PIPE;
		/** Forward slashes. */
		private static final TextureProvider PAINT_SLASH;
		/** Backward slash. */
		private static final TextureProvider PAINT_BACKSLASH;
		/** Plus signal. */
		private static final TextureProvider PAINT_PLUS;
		/** Cross. */
		private static final TextureProvider PAINT_CROSS;
		/** Dotted. */
		private static final TextureProvider PAINT_DOTTED;
		/** Square. */
		private static final TextureProvider PAINT_SQUARE;
		/** Diamond. */
		private static final TextureProvider PAINT_DIAMOND;
		static {
			PAINT_NONE = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					return img;
				}
			};
			// SOLID
			PAINT_SOLID = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.fillRect(0, 0, 7, 7);
					return img;
				}
			};
			// DASH
			PAINT_DASH = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(0, 3, 6, 3);
					return img;
				}
			};
			// PIPE
			PAINT_PIPE = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(3, 0, 3, 6);
					return img;
				}
			};
			// SLASH
			PAINT_SLASH = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(6, 0, 0, 6);
					return img;
				}
			};
			// BACKSLASH
			PAINT_BACKSLASH = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(0, 0, 6, 6);
					return img;
				}
			};
			// PLUS
			PAINT_PLUS = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(0, 3, 6, 3);
					g.drawLine(3, 0, 3, 6);
					return img;
				}
			};
			// CROSS
			PAINT_CROSS = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(0, 0, 6, 6);
					g.drawLine(6, 0, 0, 6);
					return img;
				}
			};
			// DOTTED
			PAINT_DOTTED = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(3, 3, 3, 3);
					return img;
				}
			};
			
			// SQUARE
			PAINT_SQUARE = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawRect(1, 1, 5, 5);
					return img;
				}
			};

			// DIAMOND
			PAINT_DIAMOND = new TextureProvider() {
				@Override
				public BufferedImage getTexture(Color foreground) {
					// NONE
					BufferedImage img = new BufferedImage(7, 7, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)img.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, 7, 7);
					g.setColor(foreground);
					g.drawLine(3, 1, 1, 3);
					g.drawLine(3, 1, 5, 3);
					g.drawLine(5, 3, 3, 5);
					g.drawLine(1, 3, 3, 5);
					return img;
				}
			};
		}
		/** Private constructor. */
		private Paints() {
			throw new AssertionError("Utility class.");
		}
	}
}
