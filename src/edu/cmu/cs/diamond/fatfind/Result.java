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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;

class Result {
    final private List<Circle> circles;

    final private ObjectIdentifier id;

    public Result(List<Circle> circles, ObjectIdentifier id) {
        this.circles = Collections.unmodifiableList(new ArrayList<Circle>(
                circles));
        this.id = id;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public ObjectIdentifier getId() {
        return id;
    }
}
