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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.freedesktop.cairo.Context;
import org.gnome.gdk.Pixbuf;

class Circle {
    public interface Filter {
        boolean filter(Circle c);
    }

    public static Filter FILTER_BY_IN_RESULT = new Filter() {
        @Override
        public boolean filter(Circle c) {
            return c.isInResult();
        }
    };

    public enum Fill {
        SOLID, DASHED, HAIRLINE;
    }

    final private double x;

    final private double y;

    final private double a;

    final private double b;

    final private double t;

    private boolean inResult;

    public Circle(double x, double y, double a, double b, double t,
            boolean inResult) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        this.t = t;
        this.inResult = inResult;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getT() {
        return t;
    }

    public boolean isInResult() {
        return inResult;
    }

    public void setInResult(boolean inResult) {
        this.inResult = inResult;
    }

    @Override
    public String toString() {
        return "{ x: + " + x + ", y: " + y + ", a: " + a + ", b: " + b
                + ", t: " + t + ", inResult: " + inResult + "}";
    }

    public static List<Circle> createFromPixbuf(Pixbuf buf, double minSharpness)
            throws IOException {
        if (buf.getNumChannels() != 3) {
            throw new IllegalArgumentException(
                    "buf must have exactly 3 channels");
        }

        int w = buf.getWidth();
        int h = buf.getHeight();
        byte data[] = buf.getPixels();

        ProcessBuilder pb = new ProcessBuilder("fatfind-runner", Double
                .toString(minSharpness));
        Process p = null;
        BufferedReader in = null;
        OutputStream out = null;

        try {
            p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            out = new BufferedOutputStream(p.getOutputStream());

            // write width
            out.write(Integer.toString(w).getBytes("utf-8"));
            out.write('\n');

            // write height
            out.write(Integer.toString(h).getBytes("utf-8"));
            out.write('\n');

            // write data
            out.write(data);

            out.flush();

            // read in circles
            String line;
            List<Circle> circles = new ArrayList<Circle>();
            while ((line = in.readLine()) != null) {
                String items[] = line.split(" ");
                circles.add(new Circle(Float.parseFloat(items[0]), Float
                        .parseFloat(items[1]), Float.parseFloat(items[2]),
                        Float.parseFloat(items[3]), Float.parseFloat(items[4]),
                        Boolean.parseBoolean(items[5])));
            }
            return circles;
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    void draw(Graphics2D g, double scale, Fill fill) {
        Shape circle = new Ellipse2D.Double(-1, -1, 2, 2);

        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);

        at.translate(x, y);
        at.rotate(t);
        at.scale(a, b);

        Shape s = at.createTransformedShape(circle);
        switch (fill) {
        case DASHED:
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 1f, new float[] { 5.0f }, 0));
            g.setPaint(Color.RED);
            g.draw(s);
            break;

        case SOLID:
            g.setStroke(new BasicStroke(2.0f));
            g.setPaint(new Color(1.0f, 0.0f, 0.0f, 0.2f));
            g.fill(s);
            g.setPaint(Color.RED);
            g.draw(s);
            break;

        case HAIRLINE:
            g.setStroke(new BasicStroke(1.0f));
            g.setPaint(Color.RED);
            g.draw(s);
            break;
        }
    }

    void draw(Context cr, double scale, Fill fill) {
        // draw
        cr.save();
        cr.scale(scale, scale);

        cr.translate(getX(), getY());
        cr.rotate(getT());
        cr.scale(getA(), getB());

        cr.moveTo(1.0, 0.0);
        cr.arc(0.0, 0.0, 1.0, 0.0, 2 * Math.PI);

        cr.restore();

        switch (fill) {
        case SOLID:
            // show fill, no dash
            cr.setLineWidth(2.0);
            cr.setDash(new double[0]);
            cr.setSource(1.0, 0.0, 0.0);
            cr.strokePreserve();
            cr.setSource(1.0, 0.0, 0.0, 0.2);
            cr.fill();
            break;

        case DASHED:
            cr.setLineWidth(1.0);
            cr.setDash(new double[] { 5.0 });
            cr.setSource(1.0, 0.0, 0.0);
            cr.stroke();
            break;

        case HAIRLINE:
            cr.setLineWidth(1.0);
            cr.setDash(new double[0]);
            cr.setSource(1.0, 0.0, 0.0);
            cr.stroke();
            break;
        }
    }

    public double getQuadraticMeanRadius() {
        double aa;
        double bb;
        if (b > a) {
            aa = b;
            bb = a;
        } else {
            aa = a;
            bb = b;
        }

        return Math.sqrt((3.0 * aa * aa + bb * bb) / 4.0);
    }

    public double getEccentricity() {
        double aa;
        double bb;
        if (b > a) {
            aa = b;
            bb = a;
        } else {
            aa = a;
            bb = b;
        }

        return Math.sqrt(1 - ((bb * bb) / (aa * aa)));
    }

    public static List<Circle> createFromDiamondResult(byte[] buf) {
        List<Circle> circles = new ArrayList<Circle>();

        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        while (bb.hasRemaining()) {
            circles.add(new Circle(bb.getFloat(), bb.getFloat(), bb.getFloat(),
                    bb.getFloat(), bb.getFloat(), bb.getInt() != 0));
        }

        return circles;
    }
}
