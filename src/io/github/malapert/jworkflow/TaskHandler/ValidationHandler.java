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

import io.github.malapert.jworkflow.exception.SIPException;
import io.github.malapert.jworkflow.exception.TaskHandlerException;
import io.github.malapert.jworkflow.model.IPackage;
import io.github.malapert.jworkflow.model.ISIP;
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
    protected void processTask(IPackage pack) throws TaskHandlerException {
        if (!(pack instanceof ISIP)) {
            throw new TaskHandlerException("Only SIP can be validated", pack);
        }
        ISIP sip = (ISIP) pack;        
        Validation fits = new FitsValidation();
        Validation posValidation = new PositionValidation();
        fits.setNext(posValidation);
        fits.validate(sip.getPreserveFile());
        boolean isValid = fits.isValid();
        if (isValid) {
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setTitle(String.format("SIP %s is valid", sip.getCore().get(ISIP.ID)));          
        } else {
            throw new SIPException("SIP is not valid : "+fits.getErrors(), sip);
        }
    }

    /**
     *
     * @param sip
     * @throws TaskHandlerException
     */
    @Override
    protected void unprocessTask(IPackage sip) throws TaskHandlerException {
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
