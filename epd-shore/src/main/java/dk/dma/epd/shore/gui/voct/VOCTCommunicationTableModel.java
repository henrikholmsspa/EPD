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
package dk.dma.epd.shore.gui.voct;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.text.Formatter;
import dk.dma.epd.shore.voct.SRU;
import dk.dma.epd.shore.voct.SRU.sru_status;
import dk.dma.epd.shore.voct.SRUManager;
import dk.dma.epd.shore.voct.VOCTManager;

/**
 * Table model for SRUManagerDialog
 */
public class VOCTCommunicationTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory
            .getLogger(VOCTCommunicationTableModel.class);

    private static final String[] COLUMN_NAMES = { "Send", "Name", "Status",
            "SAR Data", "OA", "Search Pattern" };

    private List<VOCTCommunicationTableEntry> tableContent = new ArrayList<VOCTCommunicationTableEntry>();

    private SRUManager sruManager;
    private VOCTManager voctManager;

    public VOCTCommunicationTableModel(SRUManager sruManager,
            VOCTManager voctManager) {
        super();
        this.sruManager = sruManager;
        this.voctManager = voctManager;
    }

    public void updateCalculateTable() {
        tableContent.clear();
        for (int i = 0; i < sruManager.getSRUs().size(); i++) {

            boolean isSend = false;
            boolean isSar = false;
            boolean isAO = false;
            boolean isRoute = false;

            // Can we send it?
            if (sruManager.getSRUs().get(i).getStatus() == sru_status.AVAILABLE) {
//                    || sruManager.getSRUs().get(i).getStatus() == sru_status.AVAILABLE
//                    || sruManager.getSRUs().get(i).getStatus() == sru_status.DECLINED
//                    || sruManager.getSRUs().get(i).getStatus() == sru_status.INVITED) {
                isSend = true;
            }

            // If we are sending, what can we send
//            if (isSend) {

                isSar = true;

                // Do we have effort allocation data for the SRU
                if (voctManager.getSarData().getEffortAllocationData().size() > i) {
                    isAO = true;

                    // We have effort allocation data, do we have a route?
                    if (voctManager.getSarData().getEffortAllocationData()
                            .get(i).getSearchPatternRoute() != null) {
                        isRoute = true;
                    }

                }

//            } else {
//
//            }

            // Add the entry
            tableContent.add(new VOCTCommunicationTableEntry(isSend, isSar,
                    isAO, isRoute));

        }
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount() {
        return sruManager.getSRUCount();
        // return 0;
        // return voctManager.getSarData().getEffortAllocationData().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SRU sru = sruManager.getSRUs().get(rowIndex);
        
        
        System.out.println("updating table! " + sru.getStatus());
        // EffortAllocationData effortAllocationData =
        // voctManager.getSarData().getEffortAllocationData().get(rowIndex);
        switch (columnIndex) {
        case 0:
            return tableContent.get(rowIndex).isSend();
        case 1:
            return Formatter.formatString(sru.getName());
        case 2:
            return sru.getStatus();
        case 3:
            return tableContent.get(rowIndex).isSarData();
        case 4:
            return tableContent.get(rowIndex).isAO();
        case 5:
            return tableContent.get(rowIndex).isSearchPattern();
        default:
            LOG.error("Unknown column " + columnIndex);
            return new String("");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("Set value at, aValue: " + aValue + " rowIndex: "
                + rowIndex + " columIndex: " + columnIndex);
        SRU sru = sruManager.getSRUs().get(rowIndex);
        switch (columnIndex) {
        case 0:
            // Send - can we change the status
            if (sru.getStatus() == sru_status.ACCEPTED
                    || sru.getStatus() == sru_status.AVAILABLE
                    || sru.getStatus() == sru_status.DECLINED
                    || sru.getStatus() == sru_status.INVITED) {

                tableContent.get(rowIndex).setSend((boolean) aValue);
            }
            break;
        case 3:
            // SAR data - we always have this
            tableContent.get(rowIndex).setSarData((boolean) aValue);
            System.out.println("Remove SAR Data Mark");
            if (!(boolean) aValue){
                System.out.println("removing ao and search pattern");
                tableContent.get(rowIndex).setAO(false);
                tableContent.get(rowIndex).setSearchPattern(false);
            }
            break;
        case 4:
            tableContent.get(rowIndex).setAO((boolean) aValue);
            if (!(boolean) aValue){
                tableContent.get(rowIndex).setSearchPattern(false);
            }
            break;
        case 5:

            tableContent.get(rowIndex).setSearchPattern((boolean) aValue);
            // if (voctManager.getSarData().getEffortAllocationData().size() >
            // rowIndex){
            // calculate.set(rowIndex, (Boolean) aValue);
            // }
            //

            // sru.setVisible((Boolean)aValue);
            // sruManager.toggleSRUVisiblity(rowIndex, (Boolean)aValue);
            break;
        default:
            break;
        }
//        fireTableCellUpdated(rowIndex, columnIndex);
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        SRU sru = sruManager.getSRUs().get(rowIndex);
        switch (columnIndex) {
        case 0:
            // Can we toggle send or not
            if (sru.getStatus() == sru_status.ACCEPTED
                    || sru.getStatus() == sru_status.AVAILABLE
                    || sru.getStatus() == sru_status.DECLINED
                    || sru.getStatus() == sru_status.INVITED) {
                return true;

            }
            // We can always toggle SAR data
        case 3:
            return true;
            // Can we send AO data?
            // If we have toggled to send SAR data and we have effort allocation
            // data for the target
        case 4:
            return tableContent.get(rowIndex).isSarData() && voctManager
                    .getSarData().getEffortAllocationData().size() > rowIndex;
            // Can we toggle to send route? if previous is set and we have a
            // route
        case 5:
            if (tableContent.get(rowIndex).isAO()) {
                return voctManager.getSarData().getEffortAllocationData()
                        .get(rowIndex).getSearchPatternRoute() != null;

            }
        default:
            return false;

        }

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

}
