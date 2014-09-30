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
import io.github.malapert.jworkflow.exception.AIPException;
import io.github.malapert.jworkflow.exception.TaskHandlerException;
import io.github.malapert.jworkflow.model.Message;
import io.github.malapert.jworkflow.validation.FitsValidation;
import io.github.malapert.jworkflow.validation.PositionValidation;
import io.github.malapert.jworkflow.validation.Validation;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class ValidationHandler extends AbstractTaskHandler implements ITaskHandler {
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
    public final static String PROCESSING_NAME = "Validating the file";

    /**
     *
     */
    public ValidationHandler() {
        this(PROCESSING_NAME);
    }
    
    /**
     *
     * @param customizedName
     */
    public ValidationHandler(final String customizedName) {
        this.customizedName = customizedName;
    }      

    /**
     *
     * @param aip
     * @throws TaskHandlerException
     */
    @Override
    protected void processTask(IAIP aip) throws TaskHandlerException {
        Validation fits = new FitsValidation();
        Validation posValidation = new PositionValidation();
        fits.setNext(posValidation);
        fits.validate(aip.getPreserveFile());
        boolean isValid = fits.isValid();
        if (isValid) {
            aip.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, getName(), "AIP is valid", 0L);
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setTitle(String.format("AIP %s is valid", aip.getCore().get(IAIP.AIP_ID)));          
        } else {
            aip.addRecordMgt(Message.SecurityLevel.ERROR, getName(), "AIP is not valid : "+fits.getErrors(), 0L);
            throw new AIPException("AIP is not valid : "+fits.getErrors());
        }
    }

    /**
     *
     * @param aip
     * @throws TaskHandlerException
     */
    @Override
    protected void unprocessTask(IAIP aip) throws TaskHandlerException {
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
