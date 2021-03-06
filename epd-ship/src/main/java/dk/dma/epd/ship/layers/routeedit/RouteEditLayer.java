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
package dk.dma.epd.ship.layers.routeedit;

import dk.dma.epd.common.prototype.layers.routeedit.RouteEditLayerCommon;
import dk.dma.epd.ship.event.RouteEditMouseMode;

/**
 * Layer for drawing new route. When active it will use a panning mouse mode.
 */
public class RouteEditLayer extends RouteEditLayerCommon {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor
     */
    public RouteEditLayer() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getMouseModeServiceList() {
        String[] ret = new String[1];
        ret[0] = RouteEditMouseMode.MODE_ID;
        return ret;
    }
}
