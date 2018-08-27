/* FontLoader.java
 * =========================================================================
 * This file is part of the JLaTeXMath Library - http://forge.scilab.org/jlatexmath
 *
 * Copyright (C) 2018 DENIZET Calixte
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * Linking this library statically or dynamically with other modules
 * is making a combined work based on this library. Thus, the terms
 * and conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce
 * an executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under terms
 * of your choice, provided that you also meet, for each linked independent
 * module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based
 * on this library. If you modify this library, you may extend this exception
 * to your version of the library, but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your
 * version.
 *
 */

package org.scilab.forge.jlatexmath;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FontLoader {

    private static boolean registerFontExceptionDisplayed = false;
    private static boolean shouldRegisterFonts = true;
    private static Map<Font, String> paths = new HashMap<>();

    public static void registerFonts(boolean b) {
        shouldRegisterFonts = b;
    }

    public static Font createFont(String name) {
        return createFont(null, name);
    }

    public static Font createFont(Object o, String name) {
        try {
            InputStream in;
            if (o == null) {
                in = new BufferedInputStream(new FileInputStream(new File(name)));
            } else {
                in = o.getClass().getResourceAsStream(name);
            }
            return createFont(in, name);
        }  catch (FileNotFoundException e) {
            System.err.println(e);
        }
        return null;
    }

    public static Font createFont(InputStream fontIn, String name) {
        try {
            final Font f = Font.createFont(Font.TRUETYPE_FONT, fontIn).deriveFont((float)(TeXFormula.PIXELS_PER_POINT * TeXFormula.FONT_SCALE_FACTOR));
            GraphicsEnvironment graphicEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            /**
             * The following fails under java 1.5
             * graphicEnv.registerFont(f);
             * dynamic load then
             */
            if (shouldRegisterFonts) {
                try {
                    Method registerFontMethod = graphicEnv.getClass().getMethod("registerFont", new Class[] { Font.class });
                    if ((Boolean) registerFontMethod.invoke(graphicEnv, new Object[] { f }) == Boolean.FALSE) {
                        System.err.println("Cannot register the font " + f.getFontName());
                    }
                } catch (Exception ex) {
                    if (!registerFontExceptionDisplayed) {
                        System.err.println("Warning: Jlatexmath: Could not access to registerFont. Please update to java 6");
                        registerFontExceptionDisplayed = true;
                    }
                }
            }
            paths.put(f, name);
            return f;
        } catch (Exception e) {
            System.err.println("Cannot create the font: " + name);
            e.printStackTrace();
        } finally {
            try {
                if (fontIn != null) {
                    fontIn.close();
                }
            } catch (IOException ioex) {
                throw new RuntimeException("Close threw exception", ioex);
            }
        }

        return null;
    }

}