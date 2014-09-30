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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible of the checksum validaton.
 * <p>
 * This class is a part of the chain of responsability.
 * </p>
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class ChecksumValidation extends Validation {

    private final String checksum;
    private ALGORITHM algorithm;
    
    /**
     *
     */
    public enum ALGORITHM {

        /**
         *
         */
        SHA1,

        /**
         *
         */
        MD5
    }

    /**
     *
     * @param checksumValue
     * @param algorithm
     */
    public ChecksumValidation(String checksumValue, ALGORITHM algorithm) {
        this.checksum = checksumValue;
        this.algorithm = algorithm;
    }
    
    /**
     *
     * @param fileName
     * @param algorithm
     * @return
     */
    public static String computeChecksumName(String fileName, ALGORITHM algorithm) {
        String result;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.name());
            byte[] mdbytes = md.digest(fileName.getBytes());
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ChecksumValidation.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        }
        return result;        
    }

    /**
     *
     * @param file
     * @param algorithm
     * @return
     */
    @SuppressWarnings("empty-statement")
    public static String computeChecksumFile(File file, ALGORITHM algorithm) {
        String result;
        try {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChecksumValidation.class.getName()).log(Level.SEVERE, null, ex);
            }
            MessageDigest md = MessageDigest.getInstance(algorithm.name());
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = is.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ChecksumValidation.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        } catch (IOException ex) {
            Logger.getLogger(ChecksumValidation.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        }
        return result;
    }

    @Override
    protected boolean processValidation(File file, List<String> error, List<String> warning) {
        boolean result = (this.checksum.equals(computeChecksumFile(file, this.algorithm)));
        if (result) {
            Logger.getLogger(ChecksumValidation.class.getName()).log(Level.INFO, "{0} is valid", file.getName());
        } else {
            Logger.getLogger(ChecksumValidation.class.getName()).log(Level.INFO, "{0} is not valid", file.getName());
            error.add(String.format("The cheksum of %s is not valid", file.getName()));
        }
        return result;
    }

}
