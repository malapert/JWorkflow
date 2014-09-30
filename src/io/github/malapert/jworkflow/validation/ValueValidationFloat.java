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
 * Algorithm for Float datatype.
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class ValueValidationFloat implements ValueValidationInterface {
    private final float value;
    
    /**
     *
     * @param value
     */
    public ValueValidationFloat(float value) {
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
        return (value >= Float.valueOf(min) && value <=Float.valueOf(max));
    }

    /**
     *
     * @param objs
     * @return
     */
    @Override
    public boolean validateEnumeration(final String[] objs) {
        boolean result = false;
        for (int i=0 ; i < objs.length && result == false ; i++) {
            float valueObj = Float.valueOf(objs[i]);
            result = (value == valueObj);
        }
        return result;        
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean validateSingleValue(final String obj) {
        return value == Float.valueOf(obj);
    }
}
