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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.text.Formatter;

/**
 * Implementation of a notification.
 */
public class Notification<T, I> implements Serializable {

    /**
     * Defines the notification severity
     */
    public enum NotificationSeverity {
        MESSAGE, WARNING, ALERT;
    }

    private static final long serialVersionUID = 1L;
    
    protected NotificationType type = NotificationType.NOTIFICATION;
    protected NotificationSeverity severity = NotificationSeverity.MESSAGE;
    protected List<NotificationAlert> alerts = new ArrayList<>();
    protected T value;
    protected I id;
    protected String title;
    protected String description;
    protected Position location;
    protected boolean read;
    protected boolean acknowledged;
    protected Date date = new Date();
    
    /**
     * Constructor
     * 
     * @param value
     * @param id
     * @param type
     */
    public Notification(T value, I id, NotificationType type) {
        this.value = value;
        this.id = id;
        this.type = type;
    }
    
    /**
     * Returns the type of this notification
     * @return the type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets the notification type
     * @param type the notification type
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * Returns the notification severity
     * @return the notification severity
     */
    public NotificationSeverity getSeverity() {
        return severity;
    }
    
    /**
     * Sets the notification severity
     * @param severity the notification severity
     */
    public void setSeverity(NotificationSeverity severity) {
        this.severity = severity;
    }

    /**
     * Returns the list of notification alerts associated with this notification
     * @return the list of notification alerts associated with this notification
     */
    public List<NotificationAlert> getAlerts() {
        return alerts;
    }
    
    /**
     * Sets the notification alerts
     * @param alerts the notification alerts
     */
    public void setAlerts(List<NotificationAlert> alerts) {
        this.alerts = alerts;
    }
    
    /**
     * Adds notification alerts
     * @param alerts the notification alerts to add
     */
    public void addAlerts(NotificationAlert... alerts) {
        for (NotificationAlert alert : alerts) {
            this.alerts.add(alert);
        }
    }

    /**
     * Returns the entity associated with the notification
     * @return the entity associated with the notification
     */
    public T get() {
        return value;
    }

    /**
     * Returns a unique id for the notification
     * @return a unique id for the notification
     */
    public I getId() {
        return id;
    }
    
    /**
     * Sets a unique id for the notification
     * @param id a unique id for the notification
     */
    public void setId(I id) {
        this.id = id;
    }
    
    /**
     * Sets the entity associated with the notification
     * @param value the entity associated with the notification
     */    
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Returns a title for the notification
     * @return a title for the notification
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Returns a title for the notification
     * @return a title for the notification
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns a description for the notification
     * @return a description for the notification
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns a description for the notification
     * @return a description for the notification
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a location associated with the notification
     * @return a location associated with the notification
     */
    public Position getLocation() {
        return location;
    }
    
    /**
     * Returns a location associated with the notification
     * @return a location associated with the notification
     */
    public void setLocation(Position location) {
        this.location = location;
    }

    /**
     * Returns whether the notification has been read or not
     * @return whether the notification has been read or not
     */
    public boolean isRead() {
        return read;
    }
    
    /**
     * Returns whether the notification has been read or not
     * @return whether the notification has been read or not
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Returns whether the notification has been acknowledged or not
     * @return whether the notification has been acknowledged or not
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * Returns whether the notification has been acknowledged or not
     * @return whether the notification has been acknowledged or not
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    /**
     * Returns the date of the notification
     * @return the date of the notification
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date of the notification
     * @param date the date of the notification
     */
    public void setDate(Date date) {
        this.date = date;
    }
    
    /**
     * Returns a HTML description of this notification
     * @return a HTML description of this notification
     */
    public String toHtml() {
        StringBuilder html = new StringBuilder("<html>");
        html.append(String.format("<h2>%s</h2>", title));
        html.append(String.format("Date: %s <p/>", Formatter.formatShortDateTime(date)));            
        html.append(String.format("Description:<br> %s <p/>", description));            
        return html.append("</html>").toString();
    }
}