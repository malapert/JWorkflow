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
package io.github.malapert.jworkflow.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chain of responsability for validation.
 * 
 * <p>
 * Validation is an interface you can use to design the workflow of your
 * validation tests.
 * </p>
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public abstract class Validation {

    /**
     * The next element of the chain of responsability
     */
    private Validation next;
    private boolean validationResult = true;
    private boolean isAlreadyRun = false;
    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    
    /**
     * Sets the next validation process.
     * @param validation the validation process
     */
    public void setNext(Validation validation) {
        next = validation;
    }

    /**
     * Validates all elements of the chain.
     * @param file file to validate
     */
    public void validate(File file) {            
        try {
            boolean result = processValidation(file, errors, warnings);
            validationResult = (validationResult) ? result : validationResult;
            if (next != null) {
                next.validate(file);
            } else {
                if (validationResult) {
                    Logger.getLogger(Validation.class.getName()).log(Level.INFO, "Full report : {0} is valid", file.getName());
                } else {
                    Logger.getLogger(Validation.class.getName()).log(Level.INFO, "Full report : {0} is not valid", file.getName());
                }                
            }
        } catch (ValidationError err) {
            validationResult = false;
            errors.add(err.getMessage());
        }
        isAlreadyRun = true;
    }

    /**
     * Process the concrete validation of a chain
     * @param file File to validate
     * @param error list of errors
     * @param warning list of warnings
     * @return True when the validation is OK otherwise False
     */
    protected abstract boolean processValidation(File file, List<String> error, List<String> warning);
    
    /**
     * Returns True when it exists some warnings otherwise False.
     * @return True when it exists some warnings otherwise False
     */
    public boolean hasWarnings() {
        return (!this.warnings.isEmpty());
    }
    
    /**
     * Returns True when it exists some errors otherwise False.
     * @return True when it exists some errors otherwise False
     */    
    public boolean hasErrors() {
        return (!this.warnings.isEmpty());
    }
    
    /**
     * Returns the list of warnings.
     * @return the list of warnings
     */
    public List<String> getWarnings() {
        return this.warnings;
    }
    
    /**
     * Returns the list of errors.
     * @return the list of errors.
     */
    public List<String> getErrors() {
        return this.errors;
    }
    
    /**
     * Returns True when the file is valid otherwise False.
     * 
     * <p>
     * This method must be called after the validate() method. If it is not the
     * case, then an IllegalStateException is thrown.
     * </p>
     * @return True when the file is valid otherwise False
     */
    public boolean isValid() {
        if (isAlreadyRun) {
           return this.validationResult;
        } else {
           throw new IllegalStateException("Please run, validate() before");
        }
    }
}
