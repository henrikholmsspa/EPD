/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.epd.shore.layers.voyage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphicList;

import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.shore.voyage.Voyage;

/**
 * Graphic for showing routes
 */
public class VoyageGraphic extends OMGraphicList {

    private static final long serialVersionUID = 1L;

    private Voyage voyage;
    private LinkedList<RouteWaypoint> routeWaypoints;
    private List<VoyageLegGraphic> routeLegs = new ArrayList<>();

    protected Stroke voyageStroke;
    protected Color color;

    private int voyageIndex;

    boolean animation;

    public VoyageGraphic(Voyage voyage, int voyageIndex, Color color) {
        super();
        this.voyage = voyage;
        this.voyageIndex = voyageIndex;

        voyageStroke = new BasicStroke(12.0f, // Width
//        stroke = new BasicStroke(5.0f, // Width
                BasicStroke.CAP_SQUARE, // End cap
                BasicStroke.JOIN_MITER, // Join style
                12.0f, // Miter limit
                new float[] { 1.0f}, // Dash pattern
                12.0f);
        
        this.color = color;
        initGraphics();
    }



    public VoyageGraphic(Stroke stroke, Color color) {
        super();
        this.voyageStroke = stroke;
        this.color = color;
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
        initGraphics();
    }

    public void initGraphics() {
        routeWaypoints = voyage.getRoute().getWaypoints();
//        int i = 0;
        for (RouteWaypoint routeWaypoint : routeWaypoints) {
            if (routeWaypoint.getOutLeg() != null) {
                RouteLeg routeLeg = routeWaypoint.getOutLeg();
                VoyageLegGraphic voyageLegGraphic = new VoyageLegGraphic(routeLeg,
                        voyageIndex, this.color, this.voyageStroke);
                add(voyageLegGraphic);
                routeLegs.add(0, voyageLegGraphic);
            }

            
            //No waypoint circles?
//            VoyageWaypointGraphic voyageWaypointGraphic = new VoyageWaypointGraphic(
//                    route, routeIndex, i, routeWaypoint, this.color, 5, 5);
//            add(0, voyageWaypointGraphic);
//            i++;
        }
    }

}
