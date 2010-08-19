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
}
