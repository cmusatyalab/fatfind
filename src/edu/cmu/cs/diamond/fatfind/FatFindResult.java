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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;
import gobject.introspection.GdkPixbuf.Pixbuf;

class FatFindResult {
    final private List<Circle> circles;

    final private ObjectIdentifier id;

    final private BufferedImage originalThumb;

    final private int originalWidth;

    public FatFindResult(List<Circle> circles, ObjectIdentifier id,
            BufferedImage originalThumb, int originalWidth) {
        this.circles = circles;
        this.id = id;
        this.originalThumb = originalThumb;
        this.originalWidth = originalWidth;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public ObjectIdentifier getId() {
        return id;
    }

    public BufferedImage getOriginalThumb() {
        return originalThumb;
    }

    public String createThumbnailTitle() {
        int circlesInResult = 0;
        for (Circle c : circles) {
            if (c.isInResult()) {
                circlesInResult++;
            }
        }

        if (circlesInResult == 1) {
            return "1 circle";
        } else {
            return circlesInResult + " circles";
        }
    }

    public Pixbuf createThumbnail() {
        BufferedImage thumb = new BufferedImage(originalThumb.getWidth(),
                originalThumb.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // copy from original
        g.drawImage(originalThumb, 0, 0, null);

        // draw circles
        for (Circle c : circles) {
            c.draw(g, (double) originalThumb.getWidth()
                    / (double) originalWidth,
                    c.isInResult() ? Circle.Fill.SOLID : Circle.Fill.DASHED);
        }

        g.dispose();

        return Main.createPixbuf(thumb);
    }
}
