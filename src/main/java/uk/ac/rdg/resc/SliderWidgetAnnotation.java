/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.util.ImageAnnotation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import org.bouncycastle.jce.provider.JDKPKCS12KeyStore.BCPKCS12KeyStore;

import uk.ac.rdg.resc.SliderWidget.SliderWidgetHandler;

public class SliderWidgetAnnotation extends ScreenAnnotation {

    private static final int EDGE_DISTANCE_PX = 100;
    private static final int SLIDER_WIDTH = 20;

    private final SliderWidget sliderWidget;
    private final String orientation;
    private String position;


    public SliderWidgetAnnotation(String id, String orientation, String position, double min, double max, RescWorldWindow wwd,
            final SliderWidgetHandler handler) {//, AnnotationLayer parentLayer) {
        super("", new Point());

        this.orientation = orientation;
        this.position = position;

        AnnotationAttributes attributes = new AnnotationAttributes();
        setupDefaultAttributes(attributes);
        getAttributes().setDefaults(attributes);

        sliderWidget = new SliderWidget(id, orientation, min, min, max, new SliderWidgetHandler() {
            @Override
            public void sliderChanged(String id, double value) {
                /*
                 * Update the value label, then pass the change event onto other
                 * listeners
                 */
                //                System.out.println("Updating value");
                handler.sliderChanged(id, value);
            }
        }, wwd);

        setLayout(new AnnotationFlowLayout(orientation, AVKey.CENTER, 0, 0)); // hgap, vgap
        addChild(sliderWidget);
    }
    
    public void setLimits(double min, double max) {
        sliderWidget.setMin(min);
        sliderWidget.setMax(max);
    }
    
    public void setSliderValue(double value) {
        sliderWidget.setValue(value);
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        /*
         * TODO use the viewport and the position to place and scale this
         */
        Rectangle viewport = dc.getView().getViewport();
        Dimension size = sliderWidget.getAttributes().getSize();

        if (orientation.equals(AVKey.HORIZONTAL)) {
            sliderWidget.getAttributes().setSize(
                    new Dimension(viewport.width * 3 / 4, SLIDER_WIDTH));
            if (position.equals(AVKey.SOUTH)) {
                setScreenPoint(new Point(viewport.x + viewport.width / 2, EDGE_DISTANCE_PX));
            } else if (position.equals(AVKey.NORTH)) {
                setScreenPoint(new Point(viewport.x + viewport.width / 2, viewport.y
                        + viewport.height - EDGE_DISTANCE_PX));
            } else if (position.equals(AVKey.WEST)) {
                setScreenPoint(new Point(viewport.x + EDGE_DISTANCE_PX + size.width / 2
                        - SLIDER_WIDTH / 2, viewport.y + viewport.height / 2));
            } else if (position.equals(AVKey.EAST)) {
                setScreenPoint(new Point(viewport.x + viewport.width - EDGE_DISTANCE_PX
                        - size.width / 2 + SLIDER_WIDTH / 2, viewport.y + viewport.height / 2));
            }
        } else {
            sliderWidget.getAttributes().setSize(
                    new Dimension(SLIDER_WIDTH, viewport.height * 3 / 4));
            if (position.equals(AVKey.SOUTH)) {
                setScreenPoint(new Point(viewport.x + viewport.width / 2, EDGE_DISTANCE_PX));
            } else if (position.equals(AVKey.NORTH)) {
                setScreenPoint(new Point(viewport.x + viewport.width / 2, viewport.y
                        + viewport.height - EDGE_DISTANCE_PX - size.height + SLIDER_WIDTH));
            } else if (position.equals(AVKey.WEST)) {
                setScreenPoint(new Point(viewport.x + EDGE_DISTANCE_PX, viewport.y
                        + viewport.height / 2 - size.height / 2));
            } else if (position.equals(AVKey.EAST)) {
                setScreenPoint(new Point(viewport.x + viewport.width - EDGE_DISTANCE_PX, viewport.y
                        + viewport.height / 2 - size.height / 2));
            }
        }

        super.doRenderNow(dc);
    }

    protected void setupLabel(Annotation annotation) {
        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attributes.setTextColor(Color.white);
        //        attributes.setBackgroundColor(sliderWidget.backgroundColor);
        attributes.setBackgroundColor(new Color(100, 100, 100, 100));
        attributes.setBorderColor(Color.white);
        attributes.setBorderWidth(2);
        attributes.setInsets(new Insets(5, 5, 5, 5));
        attributes.setTextAlign(AVKey.CENTER);
        attributes.setCornerRadius(1);

        annotation.setPickEnabled(false);
        annotation.getAttributes().setDefaults(attributes);
    }

    protected void setupDefaultAttributes(AnnotationAttributes attributes) {
        Color transparentBlack = new Color(0, 0, 0, 0);

        attributes.setBackgroundColor(transparentBlack);
        attributes.setBorderColor(transparentBlack);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        attributes.setHighlightScale(1);
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setLeader(AVKey.SHAPE_NONE);

        /*
         * Container attributes
         */
        attributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attributes.setSize(new java.awt.Dimension(0, 0));
    }
}