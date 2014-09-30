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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Algorithm for data datatype.
 * 
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class ValueValidationDate implements ValueValidationInterface {

    private final String value;

    /**
     *
     * @param value
     */
    public ValueValidationDate(final String value) {
        this.value = value;
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    @Override
    public boolean validateRange(final String min, final String max) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param objs
     * @return
     */
    @Override
    public boolean validateEnumeration(final String[] objs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean validateSingleValue(final String obj) {
        boolean result;
        if (obj.equals("{DATE_ISO}")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                sdf.parse(this.value);
                result = true;
            } catch (ParseException ex) {
                Logger.getLogger(ValueValidationDate.class.getName()).log(Level.SEVERE, this.value, ex);
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }
}
