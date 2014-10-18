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
import io.github.malapert.jworkflow.model.IPackage;
import io.github.malapert.jworkflow.model.ISIP;
import io.github.malapert.jworkflow.model.Message;
import io.github.malapert.jworkflow.validation.PositionValidation;
import org.restlet.ext.atom.Content;
import org.restlet.representation.StringRepresentation;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class LocationHandler extends AbstractTaskHandler {

    private String customizedName;

    /**
     *
     */
    public final static int MAJOR = 1;

    /**
     *
     */
    public final static int MINOR = 0;

    /**
     *
     */
    public final static int PATCH = 0;

    /**
     *
     */
    public final static String PROCESSING_NAME = "Computing object name from position";

    /**
     *
     */
    public LocationHandler() {
        this(PROCESSING_NAME);
    }

    /**
     *
     * @param customizedName
     */
    public LocationHandler(final String customizedName) {
        this.customizedName = customizedName;
    }

    @Override
    protected void processTask(IPackage pack) throws TaskHandlerException {
        IAIP aip;
        if (pack instanceof ISIP) {
            throw new io.github.malapert.jworkflow.exception.ConversionException();        
        } else if (!(pack instanceof IAIP)) {
            throw new TaskHandlerException("Cannot support this package", pack);
        }
        aip = (IAIP) pack; 
        try {
            if (!aip.containsMetadata("OBJNAME")) {
                throw new IllegalArgumentException(String.format("The keyword OBJNAME is not present in the file %s", aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID)));
            }
            String objName = (String) aip.getMetadata("OBJNAME");
            String[] pos = PositionValidation.computePosFromObjectName(objName);
            aip.addMetadata("RA", pos[0], getName(), this.getClass());
            aip.addMetadata("DEC", pos[1], getName(), this.getClass());
            StringBuilder content = new StringBuilder();
            content.append(String.format("%s : (RA/DEC) = %s / %s", objName, pos[0], pos[1]));
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setTitle(String.format("Retrieve RA/DEC of %s from CDS for AIP %s", objName, aip.getCore().get(IAIP.ID)));
            getEvent().setSummmary(String.format("This task has been successfully passed"));
            Content contentMessage = new Content();
            contentMessage.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentMessage);
        } catch (Exception ex) {
            throw new TaskHandlerException(ex, aip);
        }
    }

    /**
     *
     * @throws TaskHandlerException
     */
    @Override
    protected void unprocessTask(IPackage pack) throws TaskHandlerException {
        IAIP aip;
        if (pack instanceof ISIP) {
            ISIP sip = (ISIP) pack;
            pack = sip.toIAIP();            
        } else if (!(pack instanceof IAIP)) {
            throw new TaskHandlerException("Cannot support this package", pack);
        }
        aip = (IAIP) pack;       
        aip.removeMetadata("RA", getName());
        aip.removeMetadata("DEC", getName());
        getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
        getEvent().setTitle(String.format("Metadata removed from AIP %s", aip.getCore().get(IAIP.ID)));

    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return customizedName;
    }

    /**
     *
     * @return
     */
    @Override
    protected int getMajorVersion() {
        return MAJOR;
    }

    /**
     *
     * @return
     */
    @Override
    protected int getMinorVersion() {
        return MINOR;
    }

    /**
     *
     * @return
     */
    @Override
    protected int getPatchVersion() {
        return PATCH;
    }

}
