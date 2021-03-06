/*******************************************************************************
 * Copyright (c) 2014 The University of Reading All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * University of Reading, nor the names of the authors or contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc;

import gov.nasa.worldwind.layers.Layer;

import java.awt.image.BufferedImage;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingStyleParameters;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.widgets.PaletteSelectorWidget.PaletteSelectionHandler;

/**
 * Interface representing a {@link Layer} to display data from the EDAL
 * libraries.
 * 
 * @author Guy Griffiths
 */
public interface EdalDataLayer extends PaletteSelectionHandler, Layer {
    public static final int LEGEND_WIDTH = 20;

    /**
     * Sets the elevation of the data and redraws the layer
     * 
     * @param elevation
     *            The target elevation
     * @param elevationRange
     *            The acceptable elevation range
     */
    public void setDataElevation(double elevation, Extent<Double> elevationRange);

    /**
     * @return The currently selected elevation value
     */
    public Double getDataElevation();

    /**
     * Sets the target time of the data and redraws the layer
     * 
     * @param time
     *            The target time
     * @param timeRange
     *            The acceptable time range
     */
    public void setTime(DateTime time, Extent<DateTime> timeRange);

    /**
     * @return The currently selected time value
     */
    public DateTime getTime();

    /**
     * @return The {@link VariableMetadata} information which applies to the
     *         currently selected layer
     */
    public VariableMetadata getVariableMetadata();

    /**
     * @return The {@link PlottingStyleParameters} information which applies to the
     *         currently selected layer
     */
    public PlottingStyleParameters getPlottingMetadata();

    /**
     * Returns a legend representing the data colour scale
     * 
     * @param size
     *            The desired size of the legend in pixels
     * @param labels
     *            Whether or not to include text labels
     * @return A {@link BufferedImage} containing the legend
     */
    public BufferedImage getLegend(int size, boolean labels);
}
