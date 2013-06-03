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
package dk.dma.epd.shore.layers.ais;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMList;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.prototype.ais.AisHandlerCommon.AisMessageExtended;
import dk.dma.epd.common.prototype.ais.AisTarget;
import dk.dma.epd.common.prototype.ais.IAisTargetListener;
import dk.dma.epd.common.prototype.ais.VesselPositionData;
import dk.dma.epd.common.prototype.ais.VesselTarget;
import dk.dma.epd.common.prototype.layers.ais.AisTargetGraphic;
import dk.dma.epd.common.prototype.layers.ais.IntendedRouteLegGraphic;
import dk.dma.epd.common.prototype.layers.ais.IntendedRouteWpCircle;
import dk.dma.epd.shore.ais.AisHandler;
import dk.dma.epd.shore.event.DragMouseMode;
import dk.dma.epd.shore.event.NavigationMouseMode;
import dk.dma.epd.shore.event.SelectMouseMode;
import dk.dma.epd.shore.gui.views.ChartPanel;
import dk.dma.epd.shore.gui.views.JMapFrame;
import dk.dma.epd.shore.gui.views.MainFrame;
import dk.dma.epd.shore.gui.views.MapMenu;
import dk.dma.epd.shore.gui.views.StatusArea;

/**
 * The class AisLayer is the layer containing all AIS targets. The class handles the drawing of vessels on the chartPanel.
 */
@ThreadSafe
public class AisLayer extends OMGraphicHandlerLayer implements Runnable, IAisTargetListener, MapMouseListener {
    private static final long serialVersionUID = 1L;

    @GuardedBy("list")
    private final OMGraphicList list = new OMGraphicList();

    private volatile AisHandler aisHandler;
    private AisInfoPanel aisInfoPanel;
    private StatusArea statusArea;
    private ChartPanel chartPanel;
    private MainFrame mainFrame;
    private JMapFrame jMapFrame;
    private final IntendedRouteInfoPanel intendedRouteInfoPanel = new IntendedRouteInfoPanel();
    private final PastTrackInfoPanel pastTrackInfoPanel = new PastTrackInfoPanel();
    private MapMenu aisTargetMenu;

    // private HighlightInfoPanel highlightInfoPanel = null;
    @GuardedBy("drawnVessels")
    private final Map<Long, Vessel> drawnVessels = new HashMap<Long, Vessel>();

    private volatile boolean shouldRun = true;
    private volatile float mapScale;

    private final Thread aisThread;
    // private OMGraphic highlighted;
    // private VesselLayer highlightedVessel;

    private volatile OMGraphic closest;
    private final AisTargetGraphic aisTargetGraphic = new AisTargetGraphic();

    /**
     * Keeps the AisLayer thread alive
     */
    @Override
    public void run() {

        while (shouldRun) {
            try {
                drawVessels();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                drawVessels();
            }

        }
        synchronized (drawnVessels) {
            drawnVessels.clear();
        }
        synchronized (list) {
            list.clear();
            list.add(aisTargetGraphic);
        }
    }

    /**
     * Starts the AisLayer thread
     */
    public AisLayer() {
        synchronized (list) {
            list.add(aisTargetGraphic);
        }
        aisThread = new Thread(this);
        aisThread.start();
    }

    public Thread getAisThread() {
        return aisThread;
    }

    /**
     * Kills the AisLayer thread
     */
    public void stop() {
        shouldRun = false;
    }

    /**
     * Clears all targets from the map and in the local memory
     */
    public void mapClearTargets() {
        synchronized (list) {
            list.clear();
            list.add(aisTargetGraphic);
        }
        synchronized (drawnVessels) {
            drawnVessels.clear();
        }
    }

    public void removeSelection() {
        aisTargetGraphic.setVisible(false);

        synchronized (drawnVessels) {
            if (mainFrame.getSelectedMMSI() != -1) {
                Vessel vessel = drawnVessels.get(mainFrame.getSelectedMMSI());
                if (vessel != null) {
                    vessel.getPastTrackGraphic().setVisible(false);
                }
            } else {
                for (Vessel vessel : drawnVessels.values()) {
                    vessel.getPastTrackGraphic().setVisible(false);
                }

            }
        }

        mainFrame.setSelectedMMSI(-1);
        // selectedMMSI = -1;

        statusArea.removeHighlight();

        doPrepare();
    }

    /**
     * Draws or updates the vessels on the map
     */
    private void drawVessels() {
        if (aisHandler == null) {
            return;
        }

        boolean selectionOnScreen = false;

        if (chartPanel != null) {

            if (chartPanel.getMap().getScale() != mapScale) {
                mapScale = chartPanel.getMap().getScale();
                mapClearTargets();
            }

            // if ((highlightedMMSI != 0 && highlightedMMSI !=
            // statusArea.getHighlightedVesselMMSI())
            // || statusArea.getHighlightedVesselMMSI() == -1) {
            // highlightInfoPanel.setVisible(false);
            // highlighted = null;
            // highlightedMMSI = 0;
            // }

            Point2D lr = chartPanel.getMap().getProjection().getLowerRight();
            Point2D ul = chartPanel.getMap().getProjection().getUpperLeft();
            double lrlat = lr.getY();
            double lrlon = lr.getX();
            double ullat = ul.getY();
            double ullon = ul.getX();

            List<AisMessageExtended> shipList = aisHandler.getShipList();
            for (int i = 0; i < shipList.size(); i++) {
                if (aisHandler.getVesselTargets().containsKey(shipList.get(i).MMSI)) {
                    // Get information
                    AisMessageExtended vessel = shipList.get(i);
                    VesselTarget vesselTarget = aisHandler.getVesselTargets().get(vessel.MMSI);
                    VesselPositionData location = vesselTarget.getPositionData();

                    // Check if vessel is near map coordinates or it's
                    // sending
                    // an intended route
                    boolean t1 = location.getPos().getLatitude() >= lrlat;
                    boolean t2 = location.getPos().getLatitude() <= ullat;
                    boolean t3 = location.getPos().getLongitude() >= ullon;
                    boolean t4 = location.getPos().getLongitude() <= lrlon;

                    if (!(t1 && t2 && t3 && t4)) {

                        if (!vesselTarget.hasIntendedRoute()) {
                            continue;
                        }
                    }

                    double trueHeading = location.getTrueHeading();
                    if (trueHeading == 511) {
                        trueHeading = location.getCog();
                    }

                    synchronized (drawnVessels) {
                        if (!drawnVessels.containsKey(vessel.MMSI)) {
                            Vessel vesselComponent = new Vessel(vessel.MMSI);
                            synchronized (list) {
                                list.add(vesselComponent);
                            }
                            drawnVessels.put(vessel.MMSI, vesselComponent);
                        }
                        drawnVessels.get(vessel.MMSI).updateLayers(trueHeading, location.getPos().getLatitude(),
                                location.getPos().getLongitude(), vesselTarget.getStaticData(), location.getSog(),
                                Math.toRadians(location.getCog()), mapScale, vesselTarget);

                        if (vesselTarget.getMmsi() == mainFrame.getSelectedMMSI()) {
                            aisTargetGraphic.moveSymbol(vesselTarget.getPositionData().getPos());
                            selectionOnScreen = true;

                            // if (mainFrame.getSelectedMMSI() != -1 &&
                            // drawnVessels.containsKey(mainFrame.getSelectedMMSI())){
                            drawnVessels.get(mainFrame.getSelectedMMSI()).updatePastTrack(
                                    aisHandler.getPastTrack().get(mainFrame.getSelectedMMSI()));
                            // System.out.println("hide it");
                            // }

                            setStatusAreaTxt();
                        }
                    }

                }

            }
        }

        // if (mainFrame.getSelectedMMSI() != -1 &&
        // drawnVessels.containsKey(mainFrame.getSelectedMMSI())){
        // drawnVessels.get(mainFrame.getSelectedMMSI()).updatePastTrack(aisHandler.getPastTrack().get(mainFrame.getSelectedMMSI()));
        // System.out.println("hide it");
        // }

        if (!selectionOnScreen) {
            aisTargetGraphic.setVisible(false);
            synchronized (drawnVessels) {
                for (Vessel vessel : drawnVessels.values()) {
                    vessel.getPastTrackGraphic().setVisible(false);
                }
            }
        }

        doPrepare();

    }

    @Override
    public OMGraphicList prepare() {
        synchronized (list) {
            list.project(getProjection());
        }
        return list;
    }

    public MapMouseListener getMapMouseListener() {
        return this;
    }

    @Override
    public void findAndInit(Object obj) {
        if (obj instanceof AisHandler) {
            aisHandler = (AisHandler) obj;
            aisHandler.addListener(this);
        }
        if (obj instanceof ChartPanel) {
            chartPanel = (ChartPanel) obj;
        }
        if (obj instanceof StatusArea) {
            statusArea = (StatusArea) obj;
        }
        if (obj instanceof JMapFrame) {
            jMapFrame = (JMapFrame) obj;
            // highlightInfoPanel = new HighlightInfoPanel();
            // jMapFrame.getGlassPanel().add(highlightInfoPanel);
            aisInfoPanel = new AisInfoPanel();
            jMapFrame.getGlassPanel().add(aisInfoPanel);
            jMapFrame.getGlassPanel().add(intendedRouteInfoPanel);
            jMapFrame.getGlassPanel().add(pastTrackInfoPanel);
            jMapFrame.getGlassPanel().setVisible(true);
        }
        if (obj instanceof MainFrame) {
            mainFrame = (MainFrame) obj;
        }
        if (obj instanceof MapMenu) {
            aisTargetMenu = (MapMenu) obj;
        }
    }

    @Override
    public String[] getMouseModeServiceList() {
        String[] ret = new String[3];
        ret[0] = DragMouseMode.MODEID; // "DragMouseMode"
        ret[1] = NavigationMouseMode.MODEID; // "ZoomMouseMode"
        ret[2] = SelectMouseMode.MODEID; // "SelectMouseMode"
        return ret;
    }

    @Override
    public boolean mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseClicked(MouseEvent e) {
        OMGraphic newClosest = null;
        OMList<OMGraphic> allClosest = null;
        synchronized (list) {
            allClosest = list.findAll(e.getX(), e.getY(), 3.0f);
        }

        for (OMGraphic omGraphic : allClosest) {

            if (omGraphic instanceof VesselLayer || omGraphic instanceof IntendedRouteWpCircle
                    || omGraphic instanceof IntendedRouteLegGraphic) {
                newClosest = omGraphic;
                break;
            }
        }

        if (e.getButton() == MouseEvent.BUTTON1) {

            if (allClosest.size() == 0) {
                removeSelection();
            }

            if (newClosest != null && newClosest instanceof VesselLayer) {
                synchronized (drawnVessels) {
                    if (mainFrame.getSelectedMMSI() != -1 && drawnVessels.containsKey(mainFrame.getSelectedMMSI())) {
                        drawnVessels.get(mainFrame.getSelectedMMSI()).getPastTrackGraphic().setVisible(false);
                    }

                    mainFrame.setSelectedMMSI(((VesselLayer) newClosest).getMMSI());

                    aisTargetGraphic.setVisible(true);

                    aisTargetGraphic.moveSymbol(Position.create(((VesselLayer) newClosest).getLat(),
                            ((VesselLayer) newClosest).getLon()));

                    long mmsi = ((VesselLayer) newClosest).getMMSI();

                    // Hide all past tracks
                    if (mainFrame.getSelectedMMSI() != -1 && drawnVessels.containsKey(mainFrame.getSelectedMMSI())) {
                        drawnVessels.get(mainFrame.getSelectedMMSI()).getPastTrackGraphic().setVisible(false);
                    }

                    if (aisHandler.getPastTrack().get(mmsi) != null) {

                        // highlightedMMSI = mmsi;
                        ((VesselLayer) newClosest).getVessel().updatePastTrack(aisHandler.getPastTrack().get(mmsi));

                        // for (int i = 0; i <
                        // aisHandler.getPastTrack().get(mmsi).size(); i++) {
                        // System.out.println(aisHandler.getPastTrack().get(mmsi).get(i));
                        // }

                    }
                }

                doPrepare();

                setStatusAreaTxt();

            }

        }

        if (e.getButton() == MouseEvent.BUTTON3 && newClosest != null) {

            if (newClosest instanceof VesselLayer) {

                VesselLayer vesselLayer = (VesselLayer) newClosest;

                aisTargetMenu.aisMenu(vesselLayer.getVessel().getVesselTarget());

                // aisTargetMenu.aisSuggestedRouteMenu(vesselLayer.getVessel().getVesselTarget());

                aisTargetMenu.setVisible(true);
                aisTargetMenu.show(this, e.getX() - 2, e.getY() - 2);

                return true;

            } else if (newClosest instanceof IntendedRouteWpCircle) {

                IntendedRouteWpCircle wpCircle = (IntendedRouteWpCircle) newClosest;
                VesselTarget vesselTarget = wpCircle.getIntendedRouteGraphic().getVesselTarget();

                aisTargetMenu.aisSuggestedRouteMenu(vesselTarget);
                aisTargetMenu.setVisible(true);
                aisTargetMenu.show(this, e.getX() - 2, e.getY() - 2);

                return true;
            } else if (newClosest instanceof IntendedRouteLegGraphic) {

                IntendedRouteLegGraphic wpCircle = (IntendedRouteLegGraphic) newClosest;
                VesselTarget vesselTarget = wpCircle.getIntendedRouteGraphic().getVesselTarget();
                aisTargetMenu.aisSuggestedRouteMenu(vesselTarget);
                aisTargetMenu.setVisible(true);
                aisTargetMenu.show(this, e.getX() - 2, e.getY() - 2);

                return true;
            }

        }
        return false;
    }

    private void setStatusAreaTxt() {
        HashMap<String, String> info = new HashMap<String, String>();
        Vessel vessel;
        synchronized (drawnVessels) {
            vessel = this.drawnVessels.get(mainFrame.getSelectedMMSI());
        }
        if (vessel != null) {

            info.put("MMSI", Long.toString(vessel.getMMSI()));
            info.put("Name", vessel.getName());
            info.put("COG", vessel.getHeading());
            info.put("Call sign", vessel.getCallSign());
            info.put("LAT", vessel.getLat());
            info.put("LON", vessel.getLon());
            info.put("SOG", vessel.getSog());
            info.put("ETA", vessel.getEta());
            info.put("Type", vessel.getShipType());
            statusArea.receiveHighlight(info, vessel.getMMSI());

            // statusArea.receiveHighlight(info, vessel.getMMSI());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseMoved(MouseEvent e) {
        OMGraphic newClosest = null;
        OMList<OMGraphic> allClosest;
        synchronized (list) {
            allClosest = list.findAll(e.getX(), e.getY(), 3.0f);
        }
        for (OMGraphic omGraphic : allClosest) {
            newClosest = omGraphic;
            break;
        }

        if (allClosest.size() == 0) {
            aisInfoPanel.setVisible(false);
            intendedRouteInfoPanel.setVisible(false);
            pastTrackInfoPanel.setVisible(false);
            closest = null;
            return false;
        }

        if (newClosest != closest) {
            Point containerPoint = SwingUtilities.convertPoint(chartPanel, e.getPoint(), jMapFrame);

            if (newClosest instanceof PastTrackWpCircle) {
                closest = newClosest;
                PastTrackWpCircle wpCircle = (PastTrackWpCircle) newClosest;
                pastTrackInfoPanel.setPos((int) containerPoint.getX(), (int) containerPoint.getY() - 10);
                pastTrackInfoPanel.showWpInfo(wpCircle);
                pastTrackInfoPanel.setVisible(true);
            }

            if (newClosest instanceof IntendedRouteWpCircle) {
                closest = newClosest;
                IntendedRouteWpCircle wpCircle = (IntendedRouteWpCircle) newClosest;
                intendedRouteInfoPanel.setPos((int) containerPoint.getX(), (int) containerPoint.getY() - 10);
                intendedRouteInfoPanel.showWpInfo(wpCircle);
            }

            if (newClosest instanceof VesselLayer) {
                jMapFrame.getGlassPane().setVisible(true);
                closest = newClosest;
                VesselLayer vessel = (VesselLayer) newClosest;
                int x = (int) containerPoint.getX() + 10;
                int y = (int) containerPoint.getY() + 10;
                synchronized (drawnVessels) {
                    aisInfoPanel.showAisInfo(drawnVessels.get(vessel.getMMSI()));
                }                
                if (chartPanel.getMap().getProjection().getWidth() - x < aisInfoPanel.getWidth()) {
                    x -= aisInfoPanel.getWidth() + 20;
                }
                if (chartPanel.getMap().getProjection().getHeight() - y < aisInfoPanel.getHeight()) {
                    y -= aisInfoPanel.getHeight() + 20;
                }
                aisInfoPanel.setPos(x, y);
                aisInfoPanel.setVisible(true);

                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseMoved() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void targetUpdated(AisTarget arg0) {
        // drawVessels();
        // // aisThread.interrupt();
        // System.out.println("UPDATIGN AIS LAYER!");
        //
        //
        // if (aisHandler != null) {
        //
        // if (chartPanel != null) {
        //
        //
        // if (arg0 instanceof VesselTarget){
        // VesselTarget vesselTarget = (VesselTarget) arg0;
        //
        // location = vesselTarget.getPositionData();
        //
        // double trueHeading = location.getTrueHeading();
        // if (trueHeading == 511) {
        // trueHeading = location.getCog();
        // }
        //
        // if (!drawnVessels.containsKey(arg0.getMmsi())) {
        // vesselComponent = new Vessel(arg0.getMmsi());
        // list.add(vesselComponent);
        // drawnVessels.put(arg0.getMmsi(), vesselComponent);
        // }
        // drawnVessels.get(arg0.getMmsi()).updateLayers(trueHeading,
        // location.getPos().getLatitude(),
        // location.getPos().getLongitude(),
        // vesselTarget.getStaticData(),
        // location.getSog(),
        // Math.toRadians(location.getCog()), mapScale,
        // vesselTarget);
        //
        // if (vesselTarget.getMmsi() == mainFrame
        // .getSelectedMMSI()) {
        // aisTargetGraphic.moveSymbol(vesselTarget
        // .getPositionData().getPos());
        // selectionOnScreen = true;
        //
        // // if (mainFrame.getSelectedMMSI() != -1 &&
        // // drawnVessels.containsKey(mainFrame.getSelectedMMSI())){
        // drawnVessels
        // .get(mainFrame.getSelectedMMSI())
        // .updatePastTrack(
        // aisHandler.getPastTrack()
        // .get(mainFrame
        // .getSelectedMMSI()));
        // // System.out.println("hide it");
        // // }
        //
        // setStatusAreaTxt();
        // }
        //
        // }
        //
        // }
        // }
        //
        //
        // doPrepare();
    }

}
