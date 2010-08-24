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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

import org.freedesktop.cairo.Context;
import org.gnome.gdk.InterpType;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Allocation;
import org.gnome.gtk.Widget;

class ProcessedImage {
    public Pixbuf getOriginal() {
        return original;
    }

    public Pixbuf getScaled() {
        return scaled;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public double getScale() {
        return scale;
    }

    public Circle getCircleForPoint(int x, int y) {
        int index = hitmap[y * allocW + x];
        if (index == -1) {
            return null;
        } else {
            return circles.get(index);
        }
    }

    final private Pixbuf original;

    final private Widget widget;

    private Pixbuf scaled;

    final private List<Circle> circles;

    private int hitmap[];

    double scale;

    int allocW;

    int allocH;

    boolean showCircles;

    public ProcessedImage(Widget widget, Pixbuf original, List<Circle> circles) {
        this.widget = widget;
        this.original = original;
        this.circles = circles;

        rescale();
    }

    public void rescale() {
        Allocation a = widget.getAllocation();
        int allocW = a.getWidth();
        int allocH = a.getHeight();

        if ((this.allocW == allocW) && (this.allocH == allocH)) {
            return;
        }

        this.allocW = allocW;
        this.allocH = allocH;

        double aspect = (double) original.getWidth()
                / (double) original.getHeight();

        double windowAspect = (double) allocW / (double) allocH;

        int w = allocW;
        int h = allocH;

        // is window wider than pixbuf?
        if (aspect < windowAspect) {
            // calc width from height
            w = (int) (h * aspect);
            scale = (double) allocH / (double) original.getHeight();
        } else {
            // calc height from width
            h = (int) (w / aspect);
            scale = (double) allocW / (double) original.getWidth();
        }

        scaled = original.scale(w, h, InterpType.BILINEAR);

        // generate hitmap
        hitmap = createHitmap(circles, allocW, allocH, scale);

        widget.queueDraw();
    }

    private static int[] createHitmap(List<Circle> circles, int w, int h,
            double scale) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        // clear
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(-1, true));
        g.fillRect(0, 0, w, h);

        // draw circles
        Shape circle = new Ellipse2D.Double(-1, -1, 2, 2);

        int color = 0;
        for (Circle c : circles) {
            g.setColor(new Color(color, true));

            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);

            at.translate(c.getX(), c.getY());
            at.rotate(c.getT());
            at.scale(c.getA(), c.getB());

            Shape s = at.createTransformedShape(circle);
            g.fill(s);
            g.draw(s);

            color++;
        }

        g.dispose();

        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }

    private void drawCircles(Context cr, Circle.Filter filter) {
        for (Circle c : circles) {
            c.draw(cr, scale, filter.filter(c) ? Circle.Fill.SOLID
                    : Circle.Fill.DASHED);
        }
    }

    void setShowCircles(boolean state) {
        if (state != showCircles) {
            showCircles = state;
            widget.queueDraw();
        }
    }

    public void drawToWidget(Circle.Filter filter) {
        rescale();

        Context cr = new Context(widget.getWindow());

        cr.setSource(getScaled(), 0, 0);
        cr.paint();

        if (showCircles) {
            drawCircles(cr, filter);
        }
    }
}
