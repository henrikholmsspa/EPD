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
package dk.dma.epd.ship.layers;

import java.awt.event.MouseEvent;

import dk.dma.epd.common.prototype.layers.EPDLayerCommon;
import dk.dma.epd.ship.gui.MainFrame;
import dk.dma.epd.ship.gui.MapMenu;

/**
 * General layer for EPDShip that may be sub-classed by other layers.
 */
public class GeneralLayer extends EPDLayerCommon {

    private static final long serialVersionUID = 1L;

    /**
     * Provides default behavior for right-clicks by
     * showing the general menu.
     * 
     * @param evt the mouse event
     */
    @Override
    public boolean mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            mapMenu.generalMenu(true);
            mapMenu.setVisible(true);

            if (mainFrame.getHeight() < evt.getYOnScreen() + mapMenu.getHeight()) {
                mapMenu.show(this, evt.getX() - 2, evt.getY() - mapMenu.getHeight());
            } else {
                mapMenu.show(this, evt.getX() - 2, evt.getY() - 2);
            }
            return true;
        }

        return false;
    }

    /**
     * Returns a reference to the main frame
     * @return a reference to the main frame
     */
    @Override
    public MainFrame getMainFrame() {
        return (MainFrame)mainFrame;
    }   

    /**
     * Returns a reference to the map menu
     * @return a reference to the map menu
     */
    @Override
    public MapMenu getMapMenu() {
        return (MapMenu)mapMenu;
    }   
}
