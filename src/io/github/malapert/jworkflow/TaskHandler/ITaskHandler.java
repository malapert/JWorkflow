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

/**
 * Interface to process/unprocess a task.
 * @author Jean-Christophe Malapert
 */
public interface ITaskHandler {

    /**
     * Processes a task.
     * @param aip the AIP
     * @throws TaskHandlerException
     */
    void process(final IAIP aip) throws TaskHandlerException;

    /**
     * Unprocesses a task.
     * @param aip the AIP
     * @throws TaskHandlerException
     */
    void unprocess(final IAIP aip) throws TaskHandlerException;

    /**
     * Returns the version.
     * @return
     */
    String getVersion();

    /**
     * Returns the name of the task.
     * @return
     */
    String getName();
    
}
