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
package dk.dma.epd.common;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler for uncaught exceptions. 
 */
public class ExceptionHandler implements UncaughtExceptionHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught exception from thread " + t.getName(), e);
        JOptionPane.showMessageDialog(null, "An error has occured! If the problem persists please restart the software and contact an administrator.", "Application error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

}
