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
import java.util.List;

import org.freedesktop.cairo.Context;
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

    final private DataColumnString savedSearchName = new DataColumnString();

    final private DataColumnReference savedSearchObject = new DataColumnReference();

    final private ListStore savedSearchStore = new ListStore(new DataColumn[] {
            savedSearchName, savedSearchObject });

    final private DataColumnPixbuf foundItemThumbnail = new DataColumnPixbuf();

    final private DataColumnString foundItemTitle = new DataColumnString();

    final private DataColumnReference foundItemResult = new DataColumnReference();

    final private ListStore foundItems = new ListStore(new DataColumn[] {
            foundItemThumbnail, foundItemTitle, foundItemResult });

    final private DataColumnPixbuf calibrationImagesThumbnail = new DataColumnPixbuf();

    final private DataColumnString calibrationImagesFilename = new DataColumnString();

    final private ListStore calibrationImagesModel = new ListStore(
            new DataColumn[] { calibrationImagesThumbnail,
                    calibrationImagesFilename });

    private ProcessedImage calibrationImage;

    private final File dir;

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

        dir = index.getParentFile();

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
        // get index
        BufferedReader f = new BufferedReader(new FileReader(file));

        try {
            // create the model

            // get all the thumbnails
            String line;
            while ((line = f.readLine()) != null) {
                String filename = line.trim();
                Pixbuf pix = new Pixbuf(new File(dir, filename).getPath(), 150,
                        -1, true);
                TreeIter row = calibrationImagesModel.appendRow();
                calibrationImagesModel.setValue(row,
                        calibrationImagesThumbnail, pix);
                calibrationImagesModel.setValue(row, calibrationImagesFilename,
                        filename);
            }

            // set it up
            calibrationImages.setModel(calibrationImagesModel);
            calibrationImages.setPixbufColumn(calibrationImagesThumbnail);
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
        calibrationImages.connect(new IconView.SelectionChanged() {
            @Override
            public void onSelectionChanged(IconView source) {
                // TODO Auto-generated method stub

                // reset reference image
                setReferenceCircle(-1);

                // load the image, find circles
                for (TreePath path : source.getSelectedItems()) {
                    TreeIter item = calibrationImagesModel.getIter(path);
                    String filename = calibrationImagesModel.getValue(item,
                            calibrationImagesFilename);

                    try {
                        Pixbuf calibrationPix = new Pixbuf(new File(dir,
                                filename).getPath());

                        // busy cursor
                        fatfind.getWindow().setCursor(Cursor.BUSY);

                        // compute the circles
                        Allocation a = selectedImage.getAllocation();
                        calibrationImage = ProcessedImage.createProcessedImage(
                                calibrationPix, 1.0, a.getWidth(), a
                                        .getHeight());
                        System.out.println(calibrationImage.getCircles());

                        selectedImage.queueDraw();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        fatfind.getWindow().setCursor(Cursor.NORMAL);
                    }
                }
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
        selectedImage.connect(new ExposeEvent() {
            @Override
            public boolean onExposeEvent(Widget source, EventExpose event) {
                if (calibrationImage != null) {
                    Allocation a = source.getAllocation();
                    calibrationImage.rescale(a.getWidth(), a.getHeight());

                    Context cr = new Context(source.getWindow());

                    cr.setSource(calibrationImage.getScaled(), 0, 0);
                    cr.paint();

                    drawCircles(cr, calibrationImage.getCircles(),
                            calibrationImage.getScale());
                }

                return true;
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
                // TODO
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

    static void drawCircles(Context cr, List<Circle> circles, double scale) {
        for (Circle c : circles) {
            // g_debug("drawing %g %g %g %g %g", x, y, a, b, t);

            // draw
            cr.save();
            cr.scale(scale, scale);

            cr.translate(c.getX(), c.getY());
            cr.rotate(c.getT());
            cr.scale(c.getA(), c.getB());

            cr.moveTo(1.0, 0.0);
            cr.arc(0.0, 0.0, 1.0, 0.0, 2 * Math.PI);

            cr.restore();

            if (c.isInResult()) {
                // show fill, no dash
                cr.setLineWidth(2.0);
                cr.setDash(new double[0]);
                cr.setSource(1.0, 0.0, 0.0);
                cr.strokePreserve();
                cr.setSource(1.0, 0.0, 0.0, 0.2);
                cr.fill();
            } else {
                double dash[] = { 5.0 };
                cr.setLineWidth(1.0);
                cr.setDash(dash);
                cr.setSource(1.0, 0.0, 0.0);
                cr.stroke();
            }
        }
    }

    private void setReferenceCircle(int i) {
        // TODO
    }
}
