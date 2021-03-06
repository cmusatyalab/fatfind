/*
 *  FatFind
 *  A Diamond application for adipocyte image exploration
 *  Version 1
 *
 *  Copyright (c) 2006, 2010 Carnegie Mellon University
 *  All Rights Reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.fatfind;

import org.freedesktop.cairo.Context;
import org.gnome.gdk.Color;

class AddingCircle {

    final private int startX;

    final private int startY;

    private int currentX;

    private int currentY;

    public AddingCircle(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;

        setCurrent(startX, startY);
    }

    public void setCurrent(int x, int y) {
        currentX = x;
        currentY = y;
    }

    public void draw(Context cr) {
        cr.setSource(Color.RED);
        cr.arc(startX, startY, getR(), 0.0, 2.0 * Math.PI);
        cr.setLineWidth(1.0);
        cr.stroke();
    }

    public double getR() {
        int xd = currentX - startX;
        int yd = currentY - startY;
        return Math.sqrt((xd * xd) + (yd * yd));
    }

    public Circle createCircle(double scale) {
        double r = getR();

        return new Circle(startX / scale, startY / scale, r / scale, r / scale,
                0, true);
    }
}
