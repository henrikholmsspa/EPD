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
package dk.dma.epd.common.prototype.layers.msi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Length;

import dk.frv.enav.common.xml.msi.MsiLocation;
import dk.frv.enav.common.xml.msi.MsiMessage;
import dk.frv.enav.common.xml.msi.MsiPoint;

/**
 * Graphic for MSI location/area 
 */
public class MsiLocationGraphic extends OMGraphicList {
    private static final long serialVersionUID = 1L;
    
    private static final int LOWER_RADIUS_LIMIT = 100; // meters
    
    private MsiMessage msiMessage;
    private Color msiColor = new Color(183, 68, 237, 150);

    private Rectangle hatchFillRectangle;
    private BufferedImage hatchFill;
    
    public MsiLocationGraphic(MsiMessage msiMessage) {
        super();
        this.msiMessage = msiMessage;
        MsiLocation msiLocation = msiMessage.getLocation();
        
        hatchFill = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D big = hatchFill.createGraphics();
        big.setColor(msiColor);
        big.drawLine(0, 0, 10, 10);
        hatchFillRectangle = new Rectangle(0, 0, 10, 10);
        
        switch (msiLocation.getLocationType()) {
        case POINT:
        case POINTS:
            /*
             * For each point that has radius > minlimit
             * Draw pink shaded circle with radius   
             */
            for (MsiPoint point : msiLocation.getPoints()) {
                drawCircle(point);
            }
            break;
        case POLYGON:
            /*
             * Draw pink shaded polygon
             */
            drawPolygon();
            break;
        case POLYLINE:
            /*
             * Draw pink shaded polygon defined by polyline points and 
             * radius  
             */
            drawPolyline();
            break;
        default:
            break;
        }
    }
    
    private void drawCircle(MsiPoint point) {
        if (point.getRadius() < LOWER_RADIUS_LIMIT) {
            return;
        }
        OMCircle radiusCircle = new OMCircle(point.getLatitude(), point.getLongitude(), point.getRadius(), Length.METER);
        radiusCircle.setLinePaint(msiColor);
        radiusCircle.setFillPaint(new Color(0, 0, 0, 1));
        radiusCircle.setTextureMask(new TexturePaint(hatchFill, hatchFillRectangle));
        add(radiusCircle);
    }
    
    private void drawPolygon() {
        MsiLocation msiLocation = msiMessage.getLocation();
        // space for lat-lon points plus first lat-lon pair to close the polygon
        double[] polyPoints = new double[msiLocation.getPoints().size() * 2 + 2];
        int i = 0;
        for (MsiPoint point : msiLocation.getPoints()) {
            polyPoints[i] = point.getLatitude();
            polyPoints[i+1] = point.getLongitude();
            i+=2;
        }
        polyPoints[i] = polyPoints[0];
        polyPoints[i+1] = polyPoints[1];
        OMPoly poly = new OMPoly(polyPoints, OMGraphicConstants.DECIMAL_DEGREES, OMGraphicConstants.LINETYPE_RHUMB, 1);
        poly.setLinePaint(msiColor);
        poly.setFillPaint(new Color(0, 0, 0, 1));
        poly.setTextureMask(new TexturePaint(hatchFill, hatchFillRectangle));
        add(poly);
    }
    
    private void drawPolyline() {
        MsiLocation msiLocation = msiMessage.getLocation();
        double[] polyPoints = new double[msiLocation.getPoints().size() * 2];
        int i = 0;
        for (MsiPoint point : msiLocation.getPoints()) {
            drawCircle(point);
            polyPoints[i] = point.getLatitude();
            polyPoints[i+1] = point.getLongitude();
            i+=2;
        }
        OMPoly poly = new OMPoly(polyPoints, OMGraphicConstants.DECIMAL_DEGREES, OMGraphicConstants.LINETYPE_RHUMB, 1);
        poly.setLinePaint(msiColor);
        add(poly);
    }
    
    @Override
    public void render(Graphics gr) {
        Graphics2D image = (Graphics2D) gr;
        image.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.render(image);
    }
}
