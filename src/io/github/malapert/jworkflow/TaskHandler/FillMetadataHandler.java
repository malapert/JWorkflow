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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.Cursor;
import org.restlet.ext.atom.Content;
import org.restlet.representation.StringRepresentation;

/**
 * This task fills an AIP from the keywords that have been extracted from a
 * simple FITS file.
 *
 * @author Jean-Christophe Malapert
 */
public class FillMetadataHandler extends AbstractTaskHandler {

    private String customizedName;

    /**
     * Major version
     */
    public final static int MAJOR = 1;

    /**
     * Minor version.
     */
    public final static int MINOR = 0;

    /**
     * Patch version.
     */
    public final static int PATCH = 0;  

    /**
     * processing name.
     */
    public final static String PROCESSING_NAME = "Filling the AIP";

    /**
     * keywords to skip
     */
    public final static List keywordToSkip = Arrays.asList("COMMENT", "END", "HISTORY");

    /**
     * Fills metadata.
     */
    public FillMetadataHandler() {
        this(PROCESSING_NAME);
    }

    /**
     * Fills metadat.
     * @param customizedName
     */
    public FillMetadataHandler(final String customizedName) {
        this.customizedName = customizedName;
    }

    @Override
    protected void processTask(final IAIP aip) throws TaskHandlerException {
        try {
            int nbKeywordsFilled = 0;
            StringBuilder content = new StringBuilder();
            Fits fits = new Fits(aip.getPreserveFile());
            BasicHDU basicHDU = fits.readHDU();
            Header header = basicHDU.getHeader();
            Cursor iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard obj = (HeaderCard) iter.next();
                String key = obj.getKey();
                if (keywordToSkip.contains(key)) {
                    continue;
                }
                String value = obj.getValue();
                aip.addMetadata(key, value, getName());
                content.append(String.format("keyword %s has been added in the AIP with the value %s\n", key, value));
                nbKeywordsFilled++;
            }
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setTitle(String.format("Filling Metadata for AIP %s", aip.getCore().get(IAIP.AIP_ID)));
            getEvent().setSummmary(String.format("%s keywords have been extracted from %s in order to be fill in the AIP %s", nbKeywordsFilled, aip.getCore().get(IAIP.AIP_ORIGIN_FILE_ID), aip.getCore().get(IAIP.AIP_ID)));
            Content contentMessage = new Content();
            contentMessage.setInlineContent(new StringRepresentation(content));
            getEvent().setContent(contentMessage);
        } catch (FitsException | IOException ex) {
            throw new TaskHandlerException(ex);
        }
    }

    @Override
    protected void unprocessTask(final IAIP aip) throws TaskHandlerException {
        try {
            Fits fits = new Fits(aip.getPreserveFile());
            BasicHDU basicHDU = fits.readHDU();
            Header header = basicHDU.getHeader();
            Cursor iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard obj = (HeaderCard) iter.next();
                String key = obj.getKey();
                if (keywordToSkip.contains(key)) {
                    continue;
                }
                aip.removeMetadata(key, getName());
            }
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setTitle(String.format("Metadata removed from AIP %s", aip.getCore().get(IAIP.AIP_ID)));          
        } catch (FitsException | IOException ex) {
            throw new TaskHandlerException(ex);
        }
    }

    @Override
    public String getName() {
        return customizedName;
    }

    @Override
    protected int getMajorVersion() {
        return MAJOR;
    }

    @Override
    protected int getMinorVersion() {
        return MINOR;
    }

    @Override
    protected int getPatchVersion() {
        return PATCH;
    }

}
