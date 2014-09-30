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
package io.github.malapert.jworkflow.model;

import java.io.Serializable;

/**
 *
 * @author Jean-Christophe Malapert
 */
public final class RecordManagement implements Serializable {

    private static final long serialVersionUID = 1L;
    private Message message;

    /**
     *
     */
    public RecordManagement() {

    }

    /**
     *
     * @param message
     */
    public RecordManagement(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        String message;
        if (getMessage().getProcessingTime() == null || getMessage().getProcessingTime() == 0) {
            message = String.format("%s - %s [%s] %s", this.getMessage().getDateEvent(), this.getMessage().getAuthors().toString(), this.getMessage().getLevel().getKeyword(), this.getMessage().getTitle());
        } else {
            message = String.format("%s - %s [%s] %s, completed in %s seconds", this.getMessage().getDateEvent(), this.getMessage().getAuthors().toString(), this.getMessage().getLevel().getKeyword(), this.getMessage().getTitle(), this.getMessage().getProcessingTime());
        }
        return message;
    }

    /**
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(Message message) {
        this.message = message;
    }

}
