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
package dk.dma.epd.shore.gui.views.menuitems;

import javax.swing.JMenuItem;

import dk.dma.epd.common.prototype.gui.menuitems.event.IMapMenuAction;
import dk.dma.epd.shore.EPDShore;
import dk.dma.epd.shore.gui.route.RoutePropertiesDialog;
import dk.dma.epd.shore.route.RouteManager;



public class VoyageProperties extends JMenuItem implements IMapMenuAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int routeIndex;
    private RouteManager routeManager;

    public VoyageProperties(String text) {
        super();
        setText(text);
    }

    @Override
    public void doAction() {
        RoutePropertiesDialog routePropertiesDialog = 
                new RoutePropertiesDialog(
                        EPDShore.getInstance().getMainFrame(), 
                        EPDShore.getInstance().getMainFrame().getActiveChartPanel(),
                        routeManager, 
                        routeIndex);
        routePropertiesDialog.setVisible(true);
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public void setRouteManager(RouteManager routeManager) {
        this.routeManager = routeManager;
    }

}
