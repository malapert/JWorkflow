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

/**
 * Algorithm for boolean datatype.
 * 
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class ValueValidationBoolean implements ValueValidationInterface {

    private final boolean value;
    
    /**
     *
     * @param value
     */
    public ValueValidationBoolean(final boolean value) {
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
        Boolean objBool = Boolean.valueOf(obj);
        return value == objBool;
    }
}
