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
package dk.dma.epd.common.prototype.notification;

import static dk.dma.epd.common.graphics.GraphicsUtil.bold;
import static dk.dma.epd.common.graphics.GraphicsUtil.fixSize;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.GridBagConstraints.EAST;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import dk.dma.epd.common.prototype.enavcloud.StrategicRouteService.StrategicRouteMessage;
import dk.dma.epd.common.prototype.gui.notification.NotificationDetailPanel;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.StrategicRouteNegotiationData;
import dk.dma.epd.common.text.Formatter;

/**
 * Base class for panel that displays relevant strategic detail information 
 * for the selected route in the notification center.
 */
public abstract class StrategicRouteNotificationDetailPanelCommon<T extends StrategicRouteNotificationCommon> extends NotificationDetailPanel<T> {

    private static final long serialVersionUID = 1L;

    JLabel transactionTxt = new JLabel(" ");
    JLabel statusTxt = new JLabel(" ");
    
    JPanel messagesPanel = new JPanel(new GridBagLayout());
    
    /**
     * Constructor
     */
    public StrategicRouteNotificationDetailPanelCommon() {
        super();
        
        buildGUI();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildGUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the transaction and status labels
        Insets insets5  = new Insets(5, 5, 5, 5);
        add(bold(new JLabel("Transaction ID:")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
        add(fixSize(transactionTxt, 100), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
        add(bold(new JLabel("Latest status:")), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
        add(fixSize(statusTxt, 80), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));

        // Create the info panel
        JPanel infoPanel =  createInfoPanel();
        add(infoPanel, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0, WEST, HORIZONTAL, insets5, 0, 0));
        
        // Create the messages panel
        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, UIManager.getColor("Separator.shadow")));        
        messagesPanel.setBackground(UIManager.getColor("List.background"));
        add(scrollPane, new GridBagConstraints(0, 2, 4, 1, 1.0, 1.0, WEST, BOTH, insets5, 0, 0));   
    }
    
    /**
     * Creates an information panel for the ship or STCC that 
     * partakes in the transaction
     * @return the info panel
     */
    protected abstract JPanel createInfoPanel();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setNotification(T notification) {
        this.notification = notification;
        
        // If no notification is set, reset all fields
        if (notification == null) {
            transactionTxt.setText("");
            statusTxt.setText("");
            addStrategicRouteMessages(null);
            return;
        }
        
        StrategicRouteNegotiationData routeData = notification.get();
        
        transactionTxt.setText(String.valueOf(notification.getId()));
        statusTxt.setText(routeData.getStatus().toString());
        statusTxt.setForeground(routeData.getStatus().getColor());
        
        addStrategicRouteMessages(notification);
    }   

    /**
     * Adds the strategic route message panels
     * @param notification the route notification
     */
    private void addStrategicRouteMessages(T notification) {
        messagesPanel.removeAll();
        
        if (notification == null) {
            return;
        }

        // for each route message
        Insets insets  = new Insets(5, 5, 0, 5);
        StrategicRouteNegotiationData routeData = notification.get();
        for (int i = 0; i < routeData.getRouteMessage().size(); i++) {
            
            int routeIndex = routeData.getRouteMessage().size() - 1 - i;
            StrategicRouteMessage routeMessage = routeData.getRouteMessage().get(routeIndex);
            StrategicRouteMessage prevRouteMessage = (routeIndex == 0) ? null : routeData.getRouteMessage().get(routeIndex - 1);
            
            String routeChanges = 
                    (prevRouteMessage != null)
                    ? StrategicRouteNotificationCommon.findChanges(
                            new Route(prevRouteMessage.getRoute()), 
                            new Route(routeMessage.getRoute()))
                    : null;
                    
            String title = getMessageViewTitle(routeMessage);
            StrategicNotificationMessageView messageView = 
                    new StrategicNotificationMessageView(title, routeMessage, routeChanges, i);

            messagesPanel.add(messageView, new GridBagConstraints(0, i + 1, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));            
        }
        
        // Add filler
        messagesPanel.add(new JLabel(" "), 
                new GridBagConstraints(0, routeData.getRouteMessage().size() + 10, 1, 1, 1.0, 1.0, WEST, BOTH, insets, 0, 0));            
    }    
    
    /**
     * Installs the given replyPanel at the top of the list of route messages
     * @param replyPanel the reply panel to install
     */
    public void addReplyPanelInMessagesPanel(JPanel replyPanel) {
        Insets insets  = new Insets(5, 5, 0, 5);
        messagesPanel.add(replyPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));            
    }
    
    /**
     * Returns the title of the message panel associated with the give message
     * @param routeMessage the message
     * @return the message panel title
     */
    protected abstract String getMessageViewTitle(StrategicRouteMessage routeMessage);
}


/**
 * Displays the messages associated with a strategic route request
 */
class StrategicNotificationMessageView extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Create the panel.
     * 
     * @param title the title of the message panel
     * @param routeMessage the request message
     * @param routeChanges the route changes
     * @param the index of the message with 0 being the latest
     */
    public StrategicNotificationMessageView(
            String title,
            StrategicRouteMessage routeMessage,
            String routeChanges,
            int index) {

        super(new GridBagLayout());
        
        boolean isLatest = index == 0;
        if (!isLatest) {
            setBackground(Color.darkGray);
        }
        
        Insets insets0  = new Insets(0, 0, 0, 0);
        Insets insets1  = new Insets(5, 5, 5, 5);

        // *************************
        // Create the title panel
        // *************************
        JPanel titlePanel = new JPanel(new GridBagLayout());
        add(titlePanel, 
                new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, WEST, HORIZONTAL, insets0, 0, 0));

        // Title
        if (isLatest) {
            titlePanel.setBackground(titlePanel.getBackground().darker());
        }
        titlePanel.add(bold(new JLabel(title)), 
                new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets1, 0, 0));
        titlePanel.add(new JLabel(Formatter.formatLongDateTime(routeMessage.getSentDate())), 
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, EAST, NONE, insets1, 0, 0));
        
        // Type
        add(new JLabel("Status:"), 
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
        add(new JLabel(getStatusType(routeMessage, isLatest)), 
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));

        // Message label
        JLabel msgLabel = new JLabel("<html>" + Formatter.formatHtml(routeMessage.getMessage()) + "</html>");
        msgLabel.setBorder(new TitledBorder("Message"));
        add(msgLabel, 
                new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, WEST, HORIZONTAL, insets1, 0, 0));
        
        // Route changes
        if (!StringUtils.isBlank(routeChanges)) {
            JLabel routeLabel = new JLabel("<html>" + Formatter.formatHtml(routeChanges) + "</html>");
            routeLabel.setBorder(new TitledBorder("Route changes"));
            add(routeLabel, 
                    new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, WEST, HORIZONTAL, insets1, 0, 0));
        }
    }
    
    /**
     * Converts the status into a textual description
     * @param routeMessage the message containing the status
     * @return the textual description of the status
     */
    private String getStatusType(StrategicRouteMessage routeMessage, boolean isLatest) {
        StringBuilder status = new StringBuilder();
        status.append("<html>").append(routeMessage.getStatus());
        if (isLatest && routeMessage.getCloudMessageStatus() != null) {
            status.append("&nbsp;<small>(" + routeMessage.getCloudMessageStatus().getTitle() + ")</small>");
        }
        return status.append("</html>").toString();
    }
}
