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
 * Algorithm for string validation.
 * 
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class ValueValidationString implements ValueValidationInterface {
    
    private final String value;
    
    /**
     *
     * @param value
     */
    public ValueValidationString(String value) {
        this.value = value;
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    @Override
    public boolean validateRange(String min, String max) {
        int length = value.length();
        return (length >= Integer.valueOf(min) && length <= Integer.valueOf(max));
    }

    /**
     *
     * @param objs
     * @return
     */
    @Override
    public boolean validateEnumeration(String[] objs) {
        boolean result = false;
        for (int i=0 ; i < objs.length && result == false ; i++) {
            String valueObj = objs[i];
            result = valueObj.equals(value);
        }
        return result;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean validateSingleValue(String obj) {
        String valueObj = obj;
        return valueObj.equals(value);
    }

}
