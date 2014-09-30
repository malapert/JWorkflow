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
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.FitsException;

/**
 *
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class BESS {

    /**
     * @param args the command line arguments
     * @throws nom.tam.fits.FitsException
     */
    public static void main(String[] args) throws FitsException, Exception {
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (String arg : args) {
            System.out.println(MessageFormat.format("Processing {0}", arg));
            System.out.println("-------------------------------------------------");
            File file = new File(arg);
            Validation fits = new FitsValidation();
            Validation checksum = new ChecksumValidation(ChecksumValidation.computeChecksumFile(file, ChecksumValidation.ALGORITHM.SHA1),ChecksumValidation.ALGORITHM.SHA1);
            Validation posValidation = new PositionValidation();
            fits.setNext(checksum);
            checksum.setNext(posValidation);
            fits.validate(file);
            String check = ChecksumValidation.computeChecksumFile(file, ChecksumValidation.ALGORITHM.SHA1);
            //String path = StoreAIPTask.computeRelativePath(check);
            //File ff = new File(path);
            //System.out.println(check+" "+path+" "+ff.getParent());
        }        
    }

}
