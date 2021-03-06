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
package dk.dma.epd.ship.ais;

import net.jcip.annotations.ThreadSafe;
import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.prototype.ais.AisHandlerCommon;
import dk.dma.epd.common.prototype.sensor.pnt.PntData;
import dk.dma.epd.common.prototype.settings.AisSettings;
import dk.dma.epd.common.prototype.settings.SensorSettings;
import dk.dma.epd.ship.EPDShip;

/**
 * Class for handling incoming AIS messages and maintainer of AIS target tables
 */
@ThreadSafe
public class AisHandler extends AisHandlerCommon {

    private final double aisRange;

    /**
     * Constructor
     * @param sensorSettings
     * @param aisSettings
     */
    public AisHandler(SensorSettings sensorSettings, AisSettings aisSettings) {
        super(aisSettings);
        aisRange = sensorSettings.getAisSensorRange();
    }

    /**
     * Determine if position is within range
     * 
     * @param pos
     * @return
     */
    @Override
    protected boolean isWithinRange(Position pos) {
        if (getAisRange() <= 0) {
            return true;
        }
        PntData pntData = EPDShip.getInstance().getPntHandler().getCurrentData();
        if (pntData == null) {
            return false;
        }
        double distance = pntData.getPosition().rhumbLineDistanceTo(pos) / 1852.0;
        return distance <= aisRange;
    }

    /**
     * Returns the Ais range
     * @return the Ais range
     */
    public double getAisRange() {
        return aisRange;
    }
}
