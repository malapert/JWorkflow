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

import io.github.malapert.jworkflow.exception.AIPException;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Jean-Christophe Malapert
 */
public interface IAIP extends IPackage {

    /**
     *
     */
    public static final String AIP_ORIGIN_FILE_ID = "ORIGIN_FILE_ID";
    public static final String SIP_ID = "SIP_ID";

    /**
     *
     */
    public static final String AIP_CHECKSUM = "CHECKSUM";

    public static final String AIP_EXTENSION = ".aip";

    /**
     *
     * @param key
     * @param value
     * @param processName
     * @param className
     * @throws AIPException
     */
    public void addMetadata(String key, String value, String processName, Class className) throws AIPException;

    /**
     *
     * @param key
     * @param processName
     * @throws AIPException
     */
    public void removeMetadata(String key, String processName) throws AIPException;

    /**
     *
     * @param level
     * @param stepName
     * @param event
     * @param elapsedTimeSec
     */
    public void addRecordMgt(Message.SecurityLevel level, String stepName, String event, Long elapsedTimeSec);

    /**
     *
     * @return
     */
    public Map getMetadata();
    
    public String getMetadata(String key);
    
    public boolean containsMetadata(String key);

    /**
     *
     * @param dest
     * @param processName
     * @throws AIPException
     */
    public void renameTo(File dest, String processName) throws AIPException;

    /**
     *
     */
    public void remove();

}
