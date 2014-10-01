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
import io.github.malapert.jworkflow.validation.ChecksumValidation;
import java.io.File;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class OnlineStoreHandler extends AbstractTaskHandler {

    private final File directory;
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
    public final static String PROCESSING_NAME = "Storing the file";

    /**
     *
     * @param directory
     */
    public OnlineStoreHandler(final File directory) {
        this(PROCESSING_NAME, directory);
    }

    /**
     *
     * @param customizedName
     * @param directory
     */
    public OnlineStoreHandler(final String customizedName, final File directory) {
        this.customizedName = customizedName;
        this.directory = directory;
    }

    /**
     *
     * @param checksum
     * @return
     */
    public static String computeRelativePath(final String checksum) {
        StringBuilder pathBuilder = new StringBuilder();
        int nbDirectories = checksum.length() / 3;
        for (int i = 0; i < nbDirectories; i++) {
            String currentDirectory = checksum.substring(i * 3, (i + 1) * 3);
            pathBuilder.append(File.separator).append(currentDirectory);
        }
        String filename = checksum.substring(3 * (nbDirectories), checksum.length());
        pathBuilder.append(filename);
        return pathBuilder.toString();
    }

    @Override
    protected void processTask(IPackage pack) throws TaskHandlerException {
        IAIP aip;
        if (pack instanceof ISIP) {
            ISIP sip = (ISIP) pack;
            pack = sip.toIAIP();            
        } else if (!(pack instanceof IAIP)) {
            throw new TaskHandlerException("Cannot support this package", pack);
        }
        aip = (IAIP) pack; 
        String aip_id = (String) aip.getCore().get(IAIP.ID);
        String checksum = ChecksumValidation.computeChecksumName(aip_id, ChecksumValidation.ALGORITHM.SHA1);
        String relativePath = computeRelativePath(checksum);
        String path = this.directory.getAbsolutePath() + relativePath;
        File dest = new File(path);
        aip.renameTo(dest, getName());//copyTo(dest);   
        getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
        getEvent().setTitle(String.format("Storing the AIP %s in %s as %s", aip.getCore().get(IAIP.ID), dest, getName()));
    }

    @Override
    protected void unprocessTask(IPackage pack) throws TaskHandlerException {
        IAIP aip;
        if (pack instanceof ISIP) {
            throw new io.github.malapert.jworkflow.exception.ConversionException();           
        } else if (!(pack instanceof IAIP)) {
            throw new TaskHandlerException("Cannot support this package", pack);
        }
        aip = (IAIP) pack; 
        String checksum = (String) aip.getCore().get(IAIP.ID);
        String relativePath = computeRelativePath(checksum);
        String path = this.directory.getAbsolutePath() + relativePath;
        File dest = new File(path);
        aip.remove();
        boolean isRemovable = true;
        while (isRemovable) {
            isRemovable = dest.delete();
            dest = dest.getParentFile();
        }
        getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
        getEvent().setTitle(String.format("Removing the AIP %s/%s", aip.getCore().get(IAIP.ID), dest, getName()));

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
