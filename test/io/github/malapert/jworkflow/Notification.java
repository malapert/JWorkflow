/*
 * Copyright (C) 2014 Jean-Christophe Malapert
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.malapert.jworkflow;

import io.github.malapert.jworkflow.model.Message;
import io.github.malapert.jworkflow.notification.INotification;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class Notification implements Observer {

    private Notification() {

    }

    private static class MonitoringHolder {

        private final static Notification instance = new Notification();
    }

    public static Notification getInstance() {
        return MonitoringHolder.instance;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof INotification) {
            INotification notification = (INotification) o;
            Message message = notification.getEvent();
            System.out.println(message);
        }
    }

}
