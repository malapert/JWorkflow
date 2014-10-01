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

import io.github.malapert.jworkflow.exception.SIPException;
import java.io.File;

/**
 *
 * @author Jean-Christophe Malapert
 */
public interface ISIP extends IPackage {
    /**
     *
     */
    public static final String SIP_ORIGIN_FILE_ID = "ORIGIN_FILE_ID";

    /**
     *
     */
    public static final String SIP_CHECKSUM = "CHECKSUM";  
    
    public static final String SIP_EXTENSION = ".sip";    
    
    
    public String getComment();
    
    /**
     *
     * @param dest
     * @param processName
     * @throws SIPException
     */
    public void renameTo(File dest, String processName) throws SIPException;

    /**
     *
     */
    public void remove(); 
    
    public IAIP toIAIP();
}
