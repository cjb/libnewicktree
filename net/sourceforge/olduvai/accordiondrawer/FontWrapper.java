/*
 * ============================================================
 * jGLChartUtil: A simple, easy to use charting library for
 * rendering in Java OpenGL.
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jglchartutil
 * Project Lead:  Peter McLachlan <spark343@cs.ubc.ca>
 *
 * Copyright (c) 2005, Peter McLachlan
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of
 * 		conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list
 * 		of conditions and the following disclaimer in the documentation and/or other
 * 		materials provided with the distribution.
 * * Neither the name of the University of British Columbia nor the names of its
 * 		contributors may be used to endorse or promote products derived from this
 * 		software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package net.sourceforge.olduvai.accordiondrawer;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * This is a wrapper to the jFTGL library intended to present an interface compatible with the 
 * older BitmapFont library.  Once the jFTGL library API has stabilized, this class may disappear
 * in favor of direct calls.  It also caches generated font types & sizes so they don't need to be 
 * regenerated.  
 * 
 * @author Peter McLachlan <spark343@cs.ubc.ca>
 *
 */
public class FontWrapper {
	
//	/** Old */
//	public static final int FTGL_PIXMAP = 0;
//	public static final int FTGL_OUTLINE = 1;
//	public static final int FTGL_POLYGON = 2;
//	public static final int FTGL_EXTRUDE = 3;
//	public static final int FTGL_TEXTURE = 4;

	/**
	 * Table to cache font information; previously created font rendering objects (TextRenderer) are stored according to their Font object.
	 * TODO: check the Font comparison technique, are we checking objects or font properties?
	 */
	private HashMap<Font, TextRenderer> fontTable = new HashMap<Font, TextRenderer>();
	
	/**
	 * Accordion drawer that uses this font wrapper.  Each drawer should have its own.
	 */
	private AccordionDrawer ad;

	 /**
	  * Font wrapper constructor.  Sets the drawer. 
	  * @param ad Drawer that is associated with this font wrapper.
	  */
	public FontWrapper (AccordionDrawer ad) {
		this.ad = ad;
	}


	/**
	 * Create a font renderer.  If the renderer already exists in the cache, use it instead of making a new one.
	 * @param font Font to check or create.
	 * @return Retrieved renderer from the cache or a new renderer created from the input that has just been stored.
	 */
	private TextRenderer checkCreateFont(Font font) {
		TextRenderer renderer = fontTable.get(font);
		if (renderer == null) { 
			renderer = new TextRenderer(font, true, true);
			fontTable.put(font, renderer);
		} 		
		return renderer;
	}

	
	/**
	 * Get the width of the string in pixels.  Used to compute bounding boxes and object label positions/overlaps.
	 * 
	 * @param text Series of characters to get width.
	 * @param font Font the characters are drawn in.
	 * @return The number of pixels wide for the given string.
	 */
	public int stringWidth(String text, Font font) { 
		if (text == null) {
			Exception e = new Exception("Error, stringwidth on null string: " + text);
			e.printStackTrace();
			return 0;
		}
		if (text.equals("") || text.equals("\n"))
			return 0;
		
		final TextRenderer renderer = checkCreateFont(font);
		
		Rectangle2D r = renderer.getBounds(text); 
		final int width = (int) (r.getMaxX() - r.getMinX());
		return width;
	}
	
	/**
	 * Get the height of the string in pixels.  Used to compute bounding boxes and object label positions/overlaps.
	 * 
	 * @param text Series of characters to get height.
	 * @param font Font the characters are drawn in.
	 * @return The number of pixels high for the given string.
	 */	
	public int stringHeight(String text, Font font) { 
		if (text.equals("") || text.equals("\n"))
			return 0;

		final TextRenderer renderer = checkCreateFont(font);
		Rectangle2D r = renderer.getBounds(text); 
		final int height = (int) (r.getMaxY() - r.getMinY());
		return height;
	}

	/**
	 * Get the descent (number of pixels below the baseline) of the string in pixels.  Used to compute bounding boxes and object label positions/overlaps.
	 * 
	 * @param text Series of characters to get descent.
	 * @param f Font the characters are drawn in.
	 * @return The number of pixels descended for the given string.
	 */
	public int getDescent(String text, Font f) { 
		final TextRenderer renderer = checkCreateFont(f);
		final FontRenderContext context = renderer.getFontRenderContext();
		LineMetrics lm = f.getLineMetrics(text, context); 
		return (int) lm.getDescent();
	}
	

	/**
	 * Draw the text string at the given location.
	 * 
	 * @param gl GL context
	 * @param pos 2D Location to place the text in world coordinates
	 * @param text Text to be rendered
	 * @param zPlane Vertical plane to use for this font (determines visibility, should be over all drawn objects)
	 * @param font Font object to use
	 * @param color Color for this string
	 */
	public void drawString(GL gl, Point2D pos, double zPlane, String text, Font font, Color color) { 
		final int X = AccordionDrawer.X;
		final int Y = AccordionDrawer.Y;
		final int width = ad.getCanvas().getWidth();
		final int height = ad.getCanvas().getHeight();
		
		if (text == null) { 
			Exception e = new Exception("Cannot render null text");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		TextRenderer renderer = checkCreateFont(font);

//		System.out.println("FontWrapper: " + this);
//		System.out.println("Drawing font for GL:" + gl);
//		System.out.println("Text renderer: " + renderer);
		
	    renderer.beginRendering(width, height);
	    renderer.setColor(color);
	    renderer.draw(text, ad.w2s(pos.getX(), X), height - ad.w2s(pos.getY(), Y));
	    renderer.endRendering();
	}
}
