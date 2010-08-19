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

class SavedSearch {
    private final String searchName;

    private final double rMin;

    private final double rMax;

    private final double maxEccentricity;

    private final double minSharpness;

    public SavedSearch(String searchName, double rMin, double rMax,
            double maxEccentricity, double minSharpness) {
        this.searchName = searchName;
        this.rMin = rMin;
        this.rMax = rMax;
        this.maxEccentricity = maxEccentricity;
        this.minSharpness = minSharpness;
    }

    public String getSearchName() {
        return searchName;
    }

    public double getrMin() {
        return rMin;
    }

    public double getrMax() {
        return rMax;
    }

    public double getMaxEccentricity() {
        return maxEccentricity;
    }

    public double getMinSharpness() {
        return minSharpness;
    }
}
