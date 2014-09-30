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
import java.util.Date;
import java.util.List;
import org.restlet.ext.atom.Content;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title = "";
    private String summmary = "";
    private Content content;
    private Date dateEvent;
    private List<String> authors;
    private SecurityLevel level;
    private Long processingTime;
    
    /**
     *
     */
    public Message() {
        
    }

    /**
     *
     * @param title
     * @param summary
     * @param authors
     * @param level
     */
    public Message(String title, String summary, List<String> authors, SecurityLevel level) {
        this.title = title;
        this.summmary = summary;
        this.authors = authors;
        this.level = level;
        this.dateEvent = new Date();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the summmary
     */
    public String getSummmary() {
        return summmary;
    }

    /**
     * @param summmary the summmary to set
     */
    public void setSummmary(String summmary) {
        this.summmary = summmary;
    }

    /**
     * @return the content
     */
    public Content getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @return the dateEvent
     */
    public Date getDateEvent() {
        return dateEvent;
    }

    /**
     * @param dateEvent the dateEvent to set
     */
    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    /**
     * @return the authors
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * @return the level
     */
    public SecurityLevel getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(SecurityLevel level) {
        this.level = level;
    }

    /**
     * @return the processingTime
     */
    public Long getProcessingTime() {
        return processingTime;
    }

    /**
     * @param processingTime the processingTime to set
     */
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
    
    /**
     * RFC 5424 (http://tools.ietf.org/html/rfc5424#section-6.5) defines eight severity levels
     */
    public enum SecurityLevel {

        /**
         *
         */
        EMERGENCY("emerg", "System is unusable", "A \"panic\" condition usually affecting multiple apps/servers/sites. At this level it would usually notify all tech staff on call"),

        /**
         *
         */
        ALERT("alert","Action must be taken immediately", "Should be corrected immediately, therefore notify staff who can fix the problem"),

        /**
         *
         */
        CRITCAL("crit","Critical conditions", "Should be corrected immediately, but indicates failure in a secondary system"),

        /**
         *
         */
        ERROR("err","Error conditions", "Non-urgent failures, these should be relayed to developers or admins; each item must be resolved within a given time."),

        /**
         *
         */
        WARNING("warning", "Warning conditions", "Warning messages, not an error, but indication that an error will occur if action is not taken, e.g. file system 85% full - each item must be resolved within a given time."),

        /**
         *
         */
        NOTICE("notice", "Normal but significant condition", "Events that are unusual but not error conditions - might be summarized in an email to developers or admins to spot potential problems - no immediate action required."),

        /**
         *
         */
        INFORMATIONAL("info", "Informational messages", "Normal operational messages - may be harvested for reporting, measuring throughput, etc. - no action required."),

        /**
         *
         */
        DEBUG("debug", "Debug-level messages", "Info useful to developers for debugging the application, not useful during operations.");
        
        private final String keyword;
        private final String description;
        private final String generalDescription;

        private SecurityLevel(String keyword, String description, String generalDescription) {
            this.keyword = keyword;
            this.description = description;
            this.generalDescription = generalDescription;
        }

        /**
         * @return the keyword
         */
        public String getKeyword() {
            return keyword;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return the generalDescription
         */
        public String getGeneralDescription() {
            return generalDescription;
        }                
    }    

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder(String.format("--> %s\n", getTitle()));
        message.append("Date event : ").append(getDateEvent()).append("\n");
        message.append(getLevel().getKeyword()).append(" : ").append(getSummmary()).append("\n");
        message.append("Author : ").append(getAuthors()).append("\n");
        if (getContent() != null) {
            message.append(String.format("  %s\n", getContent().getInlineContent()));
        }
        message.append("\n");
        return message.toString();
    }
}
