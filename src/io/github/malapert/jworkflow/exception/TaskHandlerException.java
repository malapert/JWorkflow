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
package io.github.malapert.jworkflow.exception;

import io.github.malapert.jworkflow.model.IPackage;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class TaskHandlerException extends TasksManagerException {
    private static final long serialVersionUID = 8544477628870158314L;

    private IPackage pack;
    
    public TaskHandlerException() {
        super();
    }
    /**
     *
     * @param pack 
     */
    public TaskHandlerException(IPackage pack) {
        super();
        this.pack = pack;
    }
    
    /**
     *
     * @param message
     * @param pack
     */
    public TaskHandlerException(String message, IPackage pack) {
        super(message);
        this.pack = pack;
    }    
    
    /**
     *
     * @param cause
     * @param pack
     */
    public TaskHandlerException(Throwable cause, IPackage pack) {
        super(cause);
        this.pack = pack;
    }
    
    /**
     *
     * @param message
     * @param cause
     * @param aip
     */
    public TaskHandlerException(String message, Throwable cause, IPackage pack) {
        super(message, cause);
        this.pack = pack;
    }
    
    public IPackage getPackage() {
        return this.pack;
    }
    
}
