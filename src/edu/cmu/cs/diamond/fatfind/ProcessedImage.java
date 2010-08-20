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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.List;

import org.gnome.gdk.InterpType;
import org.gnome.gdk.Pixbuf;

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

    public int[] getHitmap() {
        return hitmap;
    }

    public double getScale() {
        return scale;
    }

    final private Pixbuf original;

    private Pixbuf scaled;

    final private List<Circle> circles;

    private int hitmap[];

    double scale;

    int maxW;

    int maxH;

    private ProcessedImage(Pixbuf original, List<Circle> circles, int maxW,
            int maxH) {
        this.original = original;
        this.circles = circles;

        rescale(maxW, maxH);
    }

    public void rescale(int maxW, int maxH) {
        if ((this.maxW == maxW) && (this.maxH == maxH)) {
            return;
        }

        this.maxW = maxW;
        this.maxH = maxH;

        double aspect = (double) original.getWidth()
                / (double) original.getHeight();

        double windowAspect = (double) maxW / (double) maxH;

        int w = maxW;
        int h = maxH;

        // is window wider than pixbuf?
        if (aspect < windowAspect) {
            // calc width from height
            w = (int) (h * aspect);
            scale = (double) maxH / (double) original.getHeight();
        } else {
            // calc height from width
            h = (int) (w / aspect);
            scale = (double) maxW / (double) original.getWidth();
        }

        scaled = original.scale(w, h, InterpType.BILINEAR);

        // generate hitmap
        hitmap = createHitmap(circles, w, h, scale);
    }

    public static ProcessedImage createProcessedImage(Pixbuf image,
            double minSharpness, int maxW, int maxH) throws IOException {
        List<Circle> circles = Circle.createFromPixbuf(image, minSharpness);

        return new ProcessedImage(image, circles, maxW, maxH);
    }

    private static int[] createHitmap(List<Circle> circles, int w, int h,
            double scale) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        int color = 1;
        for (Circle c : circles) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setColor(new Color(color));
            g2.scale(scale, scale);

            g2.translate(c.getX(), c.getY());
            g2.rotate(c.getT());
            g2.scale(c.getA(), c.getB());

            g2.drawOval(1, 1, 2, 2);
            g2.fillOval(1, 1, 2, 2);

            color++;
            g2.dispose();
        }

        g.dispose();

        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }
}
