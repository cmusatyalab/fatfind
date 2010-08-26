package edu.cmu.cs.diamond.fatfind;

import java.util.HashMap;

import com.sun.jna.Library;
import com.sun.jna.Native;

import gobject.introspection.cairo.Context;

interface Cairo extends Library {
    Cairo INSTANCE = (Cairo) Native.loadLibrary(Cairo.class,
            new HashMap<String, Object>() {
                {
                    put(Library.OPTION_FUNCTION_MAPPER, CairoFunctionMapper
                            .getInstance());
                }
            });

    void save(Context cr);

    void scale(Context cr, double sx, double sy);

    void translate(Context cr, double tx, double ty);

    void rotate(Context cr, double angle);

    void moveTo(Context cr, double x, double y);

    void arc(Context cr, double xc, double yc, double radius, double angle1,
            double angle2);

    void restore(Context cr);

    void setLineWidth(Context cr, double width);

    void setDash(Context cr, double[] dashes, int numDashes, double offset);

    void setSourceRgb(Context cr, double r, double g, double b);

    void strokePreserve(Context cr);

    void setSourceRgba(Context cr, double r, double g, double b, double a);

    void fill(Context cr);

    void stroke(Context cr);
}
