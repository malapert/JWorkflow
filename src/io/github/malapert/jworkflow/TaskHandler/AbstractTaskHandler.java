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
package io.github.malapert.jworkflow.TaskHandler;

import io.github.malapert.jworkflow.model.IAIP;
import io.github.malapert.jworkflow.exception.TaskHandlerException;
import io.github.malapert.jworkflow.model.Message;
import io.github.malapert.jworkflow.notification.INotification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import org.restlet.ext.atom.Content;
import org.restlet.representation.StringRepresentation;

/**
 * AbstractTaskHandler is an abstract base class to define a task.
 * @author Jean-Christophe Malapert
 */
public abstract class AbstractTaskHandler extends Observable implements ITaskHandler, INotification {

    private final List<Observer> observaters = new ArrayList<>();

    private volatile Message event = new Message();

    /**
     * Returns the major version.
     * @return the major version
     */
    protected abstract int getMajorVersion();

    /**
     * Returns the minor version
     * @return the minor version
     */
    protected abstract int getMinorVersion();

    /**
     * Returns the patch version
     * @return the patch version
     */
    protected abstract int getPatchVersion();    

    /**
     * Returns the version.
     * @return the version
     */
    @Override
    public String getVersion() {
        return getMajorVersion() + "." + getMinorVersion() + "." + getPatchVersion();
    }

    @Override
    public synchronized void addObserver(Observer o) {
        this.getObservers().add(o);
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        this.getObservers().remove(o);
    }

    @Override
    public void notifyObservers(Object arg) {
        for (Observer obs : getObservers()) {
            obs.update(this, arg);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer obs : getObservers()) {
            obs.update(this, null);
        }
    }

    /**
     * Returns the elapsed time between two dates.
     * @param date1 first date
     * @param date2 second date
     * @param timeUnit the time unit
     * @return the elapsed time between two dates
     */
    public static long getDateDiff(final Date date1, final Date date2, final TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Processes the task AIP.
     * @param aip the AIP
     * @throws TaskHandlerException When an error happens
     */
    protected abstract void processTask(final IAIP aip) throws TaskHandlerException;

    /**
     * Unprocesses the task AIP.
     * @param aip the AIP
     * @throws TaskHandlerException When an error happens
     */
    protected abstract void unprocessTask(final IAIP aip) throws TaskHandlerException;

    /**
     * Processes the task and handles Exception
     * @param aip the AIP
     * @throws TaskHandlerException When an error happens 
     */
    @Override
    public final void process(final IAIP aip) throws TaskHandlerException {
        Date startDate = new Date();
        try {
            processTask(aip);
            if (getEvent().getTitle().isEmpty()) {
                getEvent().setTitle(String.format("%s for %s is successed", getName(), aip.getCore().get(IAIP.AIP_ID)));
            }
            if (getEvent().getSummmary().isEmpty()) {
                getEvent().setSummmary(String.format("The file %s has successfully passed this task", aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID)));
            }
            if (getEvent().getLevel() == null) {
                getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            }
        } catch (TaskHandlerException ex) {
            getEvent().setLevel(Message.SecurityLevel.ERROR);
            getEvent().setTitle(String.format("%s when %s was being processed in the task %s", Message.SecurityLevel.ERROR.getDescription(), aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID), getName()));
            getEvent().setSummmary(String.format("%s\nDetail error message : %s\ncause : %s",Message.SecurityLevel.ERROR.getGeneralDescription(), ex.getMessage(), ex.getCause()));
            StringBuilder content = new StringBuilder();
            content.append("Information about the class:\n");
            content.append("  - Task Name : ").append(getName()).append("\n");
            content.append("  - Version : ").append(getVersion());
            Content contentEvent = new Content();
            contentEvent.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentEvent);
            throw new TaskHandlerException(ex);
        } catch (RuntimeException err) {
            getEvent().setLevel(Message.SecurityLevel.CRITCAL);
            getEvent().setTitle(String.format("%s when %s was being processed in the task %s", Message.SecurityLevel.CRITCAL.getDescription(), aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID), getName()));
            getEvent().setSummmary(String.format("%s\nDetail error message : %s\ncause : %s",Message.SecurityLevel.ERROR.getGeneralDescription(), err.getMessage(), err.getCause()));
            StringBuilder content = new StringBuilder();
            content.append("Information about the class:\n");
            content.append("  - Task Name : ").append(getName()).append("\n");
            content.append("  - Version : ").append(getVersion());
            Content contentEvent = new Content();
            contentEvent.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentEvent);           
            throw new TaskHandlerException(err);
        } finally {
            Date stopDate = new Date();
            Long processingTime = getDateDiff(startDate, stopDate, TimeUnit.SECONDS);           
            getEvent().setProcessingTime(processingTime);
            getEvent().setDateEvent(new Date());
            getEvent().setAuthors(Arrays.asList("AbstractTaskHandler system"));
            notifyObservers(this);
        }
    }

    /**
     * Unprocesses the task and handles the error.
     * @param aip the AIP
     * @throws TaskHandlerException When an error happens
     */
    @Override
    public final void unprocess(final IAIP aip) throws TaskHandlerException {
        Date startDate = new Date();
        try {
            unprocessTask(aip);
            if (getEvent().getTitle().isEmpty()) {
                getEvent().setTitle(String.format("clean up of %s for %s is successed", getName(), aip.getCore().get(IAIP.AIP_ID)));
            }
            if (getEvent().getSummmary().isEmpty()) {
                getEvent().setSummmary(String.format("The added information has been successfully removed", aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID)));
            }
            if (getEvent().getProcessingTime() == null) {
                getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            }            
        } catch (TaskHandlerException ex) {
            getEvent().setLevel(Message.SecurityLevel.ERROR);
            getEvent().setTitle(String.format("%s during the clean up of AIP %s", Message.SecurityLevel.ERROR.getDescription(), aip.getCore().get(IAIP.AIP_ID)));
            getEvent().setSummmary(String.format("%s\n\nDetail error message : %s \n\n cause : %s",Message.SecurityLevel.ERROR.getGeneralDescription(), ex.getMessage(), ex.getCause()));
            StringBuilder content = new StringBuilder();
            content.append("Information about the class\n");
            content.append(String.format("   Task Name : \n", getName()));
            content.append(String.format("   version : \n", getVersion()));
            Content contentEvent = new Content();
            contentEvent.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentEvent);            
            throw new TaskHandlerException(ex);
        } catch (RuntimeException err) {
            getEvent().setLevel(Message.SecurityLevel.CRITCAL);
            getEvent().setTitle(String.format("%s during the clean up of AIP %s", Message.SecurityLevel.CRITCAL.getDescription(), aip.getCore().get(IAIP.AIP_ID)));
            getEvent().setSummmary(String.format("%s\n\nDetail error message : %s \n\n cause : %s",Message.SecurityLevel.ERROR.getGeneralDescription(), err.getMessage(), err.getCause()));
            StringBuilder content = new StringBuilder();
            content.append("Information about the class\n");
            content.append(String.format("   Task Name : \n", getName()));
            content.append(String.format("   version : \n", getVersion()));
            Content contentEvent = new Content();
            contentEvent.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentEvent);             
            throw new TaskHandlerException(err.getMessage(), err.getCause());
        } finally {
            Date stopDate = new Date();
            Long processingTime = getDateDiff(startDate, stopDate, TimeUnit.SECONDS);           
            getEvent().setProcessingTime(processingTime);
            getEvent().setDateEvent(new Date());
            getEvent().setAuthors(Arrays.asList("AbstractTaskHandler system"));
            notifyObservers(this);
        }
    }

    /**
     * Returns the event to notify.
     * @return the event
     */
    @Override
    public Message getEvent() {
        return event;
    }

    /**
     * Returns the list of observers.
     * @return the observaters
     */
    public List<Observer> getObservers() {
        return observaters;
    }
}
