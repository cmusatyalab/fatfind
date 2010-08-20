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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.gnome.gdk.Pixbuf;

class Circle {
    final private float x;

    final private float y;

    final private float a;

    final private float b;

    final private float t;

    final private boolean inResult;

    public Circle(float x, float y, float a, float b, float t, boolean inResult) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        this.t = t;
        this.inResult = inResult;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getA() {
        return a;
    }

    public float getB() {
        return b;
    }

    public float getT() {
        return t;
    }

    public boolean isInResult() {
        return inResult;
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

        ProcessBuilder pb = new ProcessBuilder("/tmp/zzff/bin/fatfind-runner",
                Double.toString(minSharpness));
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
}
