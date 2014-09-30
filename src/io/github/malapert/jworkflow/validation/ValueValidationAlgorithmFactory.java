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
 * Factory to check to select the correct algorithm and analyzer.
 * <p>
 * Th input of the value validation is processed in different ways according
 * to the datatype.
 * </p>
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public abstract class ValueValidationAlgorithmFactory {

    /**
     *
     * @param value
     * @param datatype
     * @return
     */
    public static ValueValidationInterface create(final String value, final String datatype) {
        if (datatype.equals("int")) {
            return new ValueValidationInteger(Integer.valueOf(value));
        } else if (datatype.equals("float")) {
            return new ValueValidationFloat(Float.valueOf(value));
        } else if (datatype.equals("boolean")) {
            return new ValueValidationBoolean(Boolean.valueOf(value));
        } else if (datatype.equals("string")) {
            return new ValueValidationString(value);
        } else if (datatype.equals("date")) {
            return new ValueValidationDate(value);
        } else {
            throw new IllegalArgumentException((String) value + " " + datatype);
        }
    }

    /**
     * Analyze the value with an algorithm.
     * @param algorithm algorithm to analyze the keyword value
     * @param valueToAnalyze value to analyze
     * @return True when the validation is OK otherwise False
     */
    public static boolean analyze(final ValueValidationInterface algorithm, final String valueToAnalyze) {
        if (valueToAnalyze.length() == 0) {
            return true;
        }
        boolean result;
        String validation = valueToAnalyze.substring(1, valueToAnalyze.length() - 1);
        if (validation.contains(":")) {
            String[] range = validation.split(":");
            result = algorithm.validateRange(range[0], range[1]);
        } else if (validation.contains(",")) {
            String[] possibleValues = validation.split(",");
            result = algorithm.validateEnumeration(possibleValues);
        } else {
            result = algorithm.validateSingleValue(validation);
        }
        return result;
    }
}
