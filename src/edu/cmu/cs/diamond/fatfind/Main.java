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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.gnome.gdk.*;
import org.gnome.glade.Glade;
import org.gnome.glade.XML;
import org.gnome.gtk.*;
import org.gnome.gtk.Window;
import org.gnome.gtk.Button.Clicked;
import org.gnome.gtk.Entry.Changed;
import org.gnome.gtk.IconView.SelectionChanged;
import org.gnome.gtk.Range.ValueChanged;
import org.gnome.gtk.Widget.*;

public class Main {
    final private Window fatfind;

    final private DrawingArea selectedImage;

    final private Image calibrateRefImage;

    final private Label calibrateRefInfo;

    final private Image defineRefImage;

    final private Label defineRefInfo;

    final private DrawingArea simulatedSearch;

    final private AboutDialog aboutdialog1;

    final private Button saveSearchButton;

    final private Button startSearch;

    final private Button stopSearch;

    final private Button defineScope;

    final private IconView searchResults;

    final private Button clearSearch;

    final private Button generateHistogram;

    final private DrawingArea selectedResult;

    final private Window histogramWindow;

    final private IconView calibrationImages;

    final private TreeView definedSearches;

    final DataColumnString savedSearchName = new DataColumnString();

    final DataColumnReference savedSearchObject = new DataColumnReference();

    final ListStore savedSearchStore = new ListStore(new DataColumn[] {
            savedSearchName, savedSearchObject });

    final DataColumnPixbuf foundItemThumbnail = new DataColumnPixbuf();

    final DataColumnString foundItemTitle = new DataColumnString();

    final DataColumnReference foundItemResult = new DataColumnReference();

    final ListStore foundItems = new ListStore(new DataColumn[] {
            foundItemThumbnail, foundItemTitle, foundItemResult });

    private Main(XML glade, File index) throws IOException {
        fatfind = (Window) glade.getWidget("fatfind");
        selectedImage = (DrawingArea) glade.getWidget("selectedImage");
        calibrateRefImage = (Image) glade.getWidget("calibrateRefImage");
        calibrateRefInfo = (Label) glade.getWidget("calibrateRefInfo");
        defineRefImage = (Image) glade.getWidget("defineRefImage");
        defineRefInfo = (Label) glade.getWidget("defineRefInfo");
        simulatedSearch = (DrawingArea) glade.getWidget("simulatedSearch");
        aboutdialog1 = (AboutDialog) glade.getWidget("aboutdialog1");
        saveSearchButton = (Button) glade.getWidget("saveSearchButton");
        startSearch = (Button) glade.getWidget("startSearch");
        stopSearch = (Button) glade.getWidget("stopSearch");
        defineScope = (Button) glade.getWidget("defineScope");
        searchResults = (IconView) glade.getWidget("searchResults");
        clearSearch = (Button) glade.getWidget("clearSearch");
        generateHistogram = (Button) glade.getWidget("generateHistogram");
        selectedResult = (DrawingArea) glade.getWidget("selectedResult");
        histogramWindow = (Window) glade.getWidget("histogramWindow");
        calibrationImages = (IconView) glade.getWidget("calibrationImages");
        definedSearches = (TreeView) glade.getWidget("definedSearches");

        connectSignals(glade);

        setupThumbnails(index);

        setupSavedSearchStore();

        setupResultsStore();
    }

    private void setupResultsStore() {
        searchResults.setModel(foundItems);

        searchResults.setPixbufColumn(foundItemThumbnail);
        searchResults.setTextColumn(foundItemTitle);
    }

    private void setupSavedSearchStore() {
        definedSearches.setModel(savedSearchStore);

        TreeViewColumn column = definedSearches.appendColumn();
        column.setTitle("Name");

        CellRendererText renderer = new CellRendererText(column);
        renderer.setText(savedSearchName);
    }

    private void setupThumbnails(File file) throws IOException {
        String dirname = file.getParent();

        // get index
        BufferedReader f = new BufferedReader(new FileReader(file));

        try {
            // create the model
            DataColumnPixbuf thumbnailColumn = new DataColumnPixbuf();
            DataColumnString filenameColumn = new DataColumnString();
            ListStore s = new ListStore(new DataColumn[] { thumbnailColumn,
                    filenameColumn });

            // get all the thumbnails
            String line;
            while ((line = f.readLine()) != null) {
                String filename = line.trim();
                Pixbuf pix = new Pixbuf(new File(dirname, filename).getPath(),
                        150, -1, true);
                TreeIter row = s.appendRow();
                s.setValue(row, thumbnailColumn, pix);
                s.setValue(row, filenameColumn, filename);
            }

            // set it up
            calibrationImages.setModel(s);
            calibrationImages.setPixbufColumn(thumbnailColumn);
            // calibrationImages.setTextColumn(filenameColumn);
        } finally {
            try {
                f.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("No image index given on command line");
            System.exit(1);
        }

        Gtk.init(args);
        Gtk.setProgramName("FatFind");

        XML glade = Glade.parse("/usr/share/fatfind/glade/fatfind.glade", null);

        Main main = new Main(glade, new File(args[0]));

        main.showFatFind();

        Gtk.main();
    }

    private void showFatFind() {
        fatfind.showAll();
    }

    private void connectSignals(XML glade) {
        // fatfind
        fatfind.connect(new Window.DeleteEvent() {
            @Override
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

        // quit1
        ImageMenuItem quit1 = (ImageMenuItem) glade.getWidget("quit1");
        quit1.connect(new MenuItem.Activate() {
            @Override
            public void onActivate(MenuItem source) {
                Gtk.mainQuit();
            }
        });

        // about1
        ImageMenuItem about1 = (ImageMenuItem) glade.getWidget("about1");
        about1.connect(new MenuItem.Activate() {
            @Override
            public void onActivate(MenuItem source) {
                aboutdialog1.show();
            }
        });

        // calibrationImages
        IconView calibrationImages = (IconView) glade
                .getWidget("calibrationImages");
        calibrationImages.connect(new IconView.SelectionChanged() {
            @Override
            public void onSelectionChanged(IconView source) {
                // TODO Auto-generated method stub
                System.out.println("wee");
            }
        });

        // aboutdialog1
        final AboutDialog aboutdialog1 = (AboutDialog) glade
                .getWidget("aboutdialog1");
        aboutdialog1.connect(new Dialog.Response() {
            @Override
            public void onResponse(Dialog source, ResponseType response) {
                aboutdialog1.hide();
            }
        });

        // selectedImage
        DrawingArea selectedImage = (DrawingArea) glade
                .getWidget("selectedImage");

        selectedImage.connect(new ExposeEvent() {
            @Override
            public boolean onExposeEvent(Widget source, EventExpose event) {
                // TODO
                return false;
            }
        });

        selectedImage.connect(new ButtonPressEvent() {
            @Override
            public boolean onButtonPressEvent(Widget source, EventButton event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedImage.connect(new MotionNotifyEvent() {
            @Override
            public boolean onMotionNotifyEvent(Widget source, EventMotion event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedImage.connect(new EnterNotifyEvent() {

            @Override
            public boolean onEnterNotifyEvent(Widget source, EventCrossing event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedImage.connect(new LeaveNotifyEvent() {

            @Override
            public boolean onLeaveNotifyEvent(Widget source, EventCrossing event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        // radiusLower
        HScale radiusLower = (HScale) glade.getWidget("radiusLower");
        radiusLower.connect(new ValueChanged() {

            @Override
            public void onValueChanged(Range source) {
                // TODO Auto-generated method stub
            }
        });

        // radiusUpper
        HScale radiusUpper = (HScale) glade.getWidget("radiusUpper");
        radiusUpper.connect(new ValueChanged() {

            @Override
            public void onValueChanged(Range source) {
                // TODO Auto-generated method stub
            }
        });

        // maxEccentricity
        HScale maxEccentricity = (HScale) glade.getWidget("maxEccentricity");
        maxEccentricity.connect(new ValueChanged() {

            @Override
            public void onValueChanged(Range source) {
                // TODO Auto-generated method stub
            }
        });

        // minSharpness
        HScale minSharpness = (HScale) glade.getWidget("minSharpness");
        minSharpness.connect(new ValueChanged() {

            @Override
            public void onValueChanged(Range source) {
                // TODO Auto-generated method stub
            }
        });

        // recomputePreview
        Button recomputePreview = (Button) glade.getWidget("recomputePreview");
        recomputePreview.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub
            }
        });

        // simulatedSearch
        Widget simulatedSearch = glade.getWidget("simulatedSearch");
        simulatedSearch.connect(new ExposeEvent() {

            @Override
            public boolean onExposeEvent(Widget source, EventExpose event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        // searchName
        Entry searchName = (Entry) glade.getWidget("searchName");
        searchName.connect(new Changed() {

            @Override
            public void onChanged(Editable source) {
                // TODO
            }
        });

        // saveSearchButton
        saveSearchButton.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // startSearch
        startSearch.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // defineScope
        defineScope.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // searchResults
        searchResults.connect(new SelectionChanged() {

            @Override
            public void onSelectionChanged(IconView source) {
                // TODO Auto-generated method stub

            }
        });

        // stopSearch
        stopSearch.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // clearSearch
        clearSearch.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // generateHistogram
        generateHistogram.connect(new Clicked() {

            @Override
            public void onClicked(Button source) {
                // TODO Auto-generated method stub

            }
        });

        // selectedResult
        selectedResult.connect(new ExposeEvent() {

            @Override
            public boolean onExposeEvent(Widget source, EventExpose event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedResult.connect(new ButtonPressEvent() {
            @Override
            public boolean onButtonPressEvent(Widget source, EventButton event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedResult.connect(new ButtonReleaseEvent() {

            @Override
            public boolean onButtonReleaseEvent(Widget source, EventButton event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedResult.connect(new MotionNotifyEvent() {

            @Override
            public boolean onMotionNotifyEvent(Widget source, EventMotion event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedResult.connect(new EnterNotifyEvent() {

            @Override
            public boolean onEnterNotifyEvent(Widget source, EventCrossing event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        selectedResult.connect(new LeaveNotifyEvent() {

            @Override
            public boolean onLeaveNotifyEvent(Widget source, EventCrossing event) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }
}
