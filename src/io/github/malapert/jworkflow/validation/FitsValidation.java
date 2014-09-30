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

import io.github.malapert.jworkflow.validation.parser.FitsValidation.Validation;
import io.github.malapert.jworkflow.validation.parser.KeywordType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.Cursor;

/**
 * FITS file Validation according to the BESS requirement document.
 * <p>
 * The BeSS database contains the complete catalog of classical Be stars and
 * Herbig Ae/Be stars, and assembles spectra obtained by professional and
 * amateur astronomers of those stars. A user can upload a new spectrum so that
 * it is archived in BeSS. Before to archive the FITS file, some checks are done
 * according to the following interface control document :
 * http://basebe.obspm.fr/basebe/Accueil.php?flag_lang=en
 * 
 * This class is a part of the chain of resposability.
 * </p>
 *
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public final class FitsValidation extends io.github.malapert.jworkflow.validation.Validation {

    /**
     * The data structure for the configuration file.
     */
    private final Map<String, Object> dicoVal = new HashMap<>();
    /**
     * The error message related to the check process.
     */
    private final List<String> errorMessage = new ArrayList<>();
    /**
     * The error message related to the check process.
     */
    private final List<String> warningMessage = new ArrayList<>();    

    /**
     * The element that contains the validations rules.
     */
    private static final String RULES = "rules_verification";

    /**
     * Creates a new instance of FitsValidation with the default configuration
     * file.
     *
     * <p>
     * Throw a ValidationError when an unexpected error happens
     * </p>
     */
    public FitsValidation() {
        try {
            InputStream in = loadDefaultConfigurationFile();
            io.github.malapert.jworkflow.validation.parser.FitsValidation conf = loadConfValidation(in);
            createDicoVal(conf);
        } catch (JAXBException ex) {
            Logger.getLogger(FitsValidation.class.getName()).log(Level.SEVERE,
                    "Error while loading the configuration "
                    + "file for the FITS validation library", ex);
            throw new ValidationError("Error while loading the configuration "
                    + "file for the FITS validation library", ex);
        }
    }

    /**
     * Creates a new instance of FitsValidation with a customized configuration
     * file.
     * <p>
     * Throw a ValidationError when an unexpected error happens
     * </p>
     *
     * @param confValidation The configuration file where the rules are
     * expressed
     */
    public FitsValidation(final File confValidation) {
        try {
            io.github.malapert.jworkflow.validation.parser.FitsValidation conf = loadConfValidation(new FileInputStream(confValidation));
            createDicoVal(conf);
        } catch (FileNotFoundException | JAXBException ex) {
            Logger.getLogger(FitsValidation.class.getName()).log(Level.SEVERE,
                    "Error while loading the configuration "
                    + "file for the FITS validation library", ex);
            throw new ValidationError("Error while loading the configuration "
                    + "file for the FITS validation library", ex);
        }
    }

    /**
     * Creates the data structure where the configuration file for the FITS
     * validation is stored.
     *
     * @param conf The configuration file for the FITS validation
     */
    private void createDicoVal(final io.github.malapert.jworkflow.validation.parser.FitsValidation conf) {
        List<KeywordType> keywords = conf.getKeyword();
        for (KeywordType keyword : keywords) {
            this.dicoVal.put(keyword.getName(), keyword);
        }
        this.dicoVal.put(RULES, conf.getValidation());
    }

    /**
     * Loads the default configuration file as a stream.
     *
     * @return the configuration file contain
     */
    private InputStream loadDefaultConfigurationFile() {
        String packageName = this.getClass().getPackage().getName();
        packageName = packageName.replaceAll("\\.", "\\/");
        String confFile = "/" + packageName + "/FitsValidation.xml";
        InputStream in = getClass().getResourceAsStream(confFile);
        return in;
    }

    /**
     * Loads the configuration file
     *
     * @param confValidation the configuration file where the rules are set
     * @return The configuration file
     * @throws JAXBException Happens when the configuration file is not
     * compliant with the schema
     */
    private io.github.malapert.jworkflow.validation.parser.FitsValidation loadConfValidation(final InputStream confValidation) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(io.github.malapert.jworkflow.validation.parser.ObjectFactory.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        io.github.malapert.jworkflow.validation.parser.FitsValidation confFile = (io.github.malapert.jworkflow.validation.parser.FitsValidation) unmarshaller.unmarshal(confValidation);
        return confFile;
    }

    /**
     * Validates a Header FITS according to the configuration file.
     * <p>
     * In this method, we check the value for each keyword according to the
     * rules, which are set in the configuration file. And, we check the rules
     * that are defined as an expression.
     * </p>
     *
     * @param header The Header to check
     * @return True when the FITS is valid otherwise False
     */
    private boolean validateHeaderAgainstConf(final Header header) {
        List keywordToSkip = Arrays.asList("COMMENT", "END", "HISTORY");
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("ECMAScript");

        boolean result = true;
        Cursor iter = header.iterator();
        while (iter.hasNext()) {
            HeaderCard obj = (HeaderCard) iter.next();
            String key = obj.getKey();
            if (keywordToSkip.contains(key)) {
                continue;
            }
            String value = obj.getValue();
            try {
                boolean isValid = validateKeywordAgainstDico(key, value);
                if (!isValid) this.errorMessage.add(key + "=" + value + " is not valid ");
                scriptEngine.put(key.replaceAll("-", "_"), isValid);
                Logger.getLogger(FitsValidation.class.getName()).log(Level.FINE, "{0} = {1}  => {2}", new Object[]{key, value, isValid});
                result = (result) ? isValid : result;
            } catch (Error err) {
                scriptEngine.put(key.replaceAll("-", "_"), false);
                Logger.getLogger(FitsValidation.class.getName()).log(Level.FINE, "{0} = {1}  => false", new Object[]{key, value});
                result = (result) ? false : result;
                this.errorMessage.add(key + "=" + value + " => " + err.getMessage());
            }
        }
        boolean validationRules = validateRules(scriptEngine);
        return (result) ? validationRules : result;
    }

    /**
     * Validates the rules defined as an expression
     *
     * @param scriptEngine The engine to run expression as a string.
     * @return True when the rules are valid otherwise false
     */
    private boolean validateRules(final ScriptEngine scriptEngine) {
        boolean result = true;
        List<Validation> validations = (List<Validation>) dicoVal.get(RULES);
        for (Validation validation : validations) {
            boolean resultCurrentValidation = true;
            String expression = validation.getValue();
            String message = validation.getMessage().get(0);
            expression = expression.replaceAll(",", "&&");
            expression = expression.replaceAll("\\|", "\\|\\|");
            expression = expression.replaceAll("-", "_");
            boolean running = true;
            while (running) {
                try {
                    resultCurrentValidation = (Boolean) scriptEngine.eval(expression);
                    if (!resultCurrentValidation && "error".equals(message)) {
                        Logger.getLogger(FitsValidation.class.getName()).log(Level.SEVERE, "{0} is false, no insertion", expression);
                        resultCurrentValidation = false;
                        errorMessage.add(expression+ " is wrong");
                    } else if (!resultCurrentValidation && "warning".equals(message)) {
                        Logger.getLogger(FitsValidation.class.getName()).log(Level.WARNING, "{0} is false", expression);
                        resultCurrentValidation = true;
                        warningMessage.add(expression+ " is wrong");
                    }
                    running = false;
                } catch (ScriptException ex) {
                    Pattern p = Pattern.compile(".*ReferenceError:\\s*\"(.*)\"");
                    Matcher m = p.matcher(ex.getMessage());
                    if (m.find()) {
                        scriptEngine.put(m.group(1), false);
                        running = true;
                    } else {
                        Logger.getLogger(FitsValidation.class.getName()).log(Level.SEVERE, "evaluate " + expression + " failed", ex);
                        running = false;
                        resultCurrentValidation = false;
                        errorMessage.add("evaluate " + expression + " failed");
                    }
                }
                result = (result) ? resultCurrentValidation : result;
            }
        }
        return result;
    }

    /**
     * Validates the keyword value of the Header FITS according to the rules
     * that are defined in the configuration file.
     *
     * @param keyword keyword to check
     * @param value value the value to check
     * @return True when the keyword value is correct otherwise false
     */
    private boolean validateKeywordAgainstDico(final String keyword, final String value) {
        boolean result;
        if (this.dicoVal.containsKey(keyword)) {
            KeywordType keywordType = (KeywordType) this.dicoVal.get(keyword);
            String datatype = keywordType.getDatatype().get(0);
            String validation = keywordType.getValue();
            ValueValidationInterface validationImpl = ValueValidationAlgorithmFactory.create(value, datatype);
            result = ValueValidationAlgorithmFactory.analyze(validationImpl, validation);
        } else {
            Logger.getLogger(FitsValidation.class.getName()).log(Level.WARNING, "No entry in the ICD to ckeck " + keyword, value);
            warningMessage.add("No entry in the ICD to ckeck " + keyword);
            result = true;
        }
        return result;
    }

    /**
     * Check whether the Fits fils is valid.
     *
     * @param fits the Fits file to check
     * @return True when the FITS files is valid otherwise false
     * @throws when happens an error with the FITS reading
     */
    private boolean isValid(Fits fits) throws FitsException, IOException {
        boolean result;
        BasicHDU basicHDU = fits.readHDU();
        Header header = basicHDU.getHeader();
        result = validateHeaderAgainstConf(header);
        return result;
    }

    /**
     * Checks whether the FITS file given in a constructor is valid.
     *
     * <p>
     * Throw a ValidationError when an unexpected error happens
     * </p>
     *
     * @param file The file to validate
     * @param errors list of errors to add
     * @param warnings list of warnings to add
     * @return True when the FITS file is valid otherwise false
     */
    @Override
    protected boolean processValidation(File file, List<String> errors, List<String> warnings) {
        try {
            boolean result = isValid(new Fits(file));
            if (result) {
                Logger.getLogger(FitsValidation.class.getName()).log(Level.INFO, file.getName()+ "{0} is valid {1}", new Object[]{file.getName(), errorMessage.toString()});
            } else {
                Logger.getLogger(FitsValidation.class.getName()).log(Level.INFO, file.getName()+ "{0} is not valid - cause : {1}", new Object[]{file.getName(), errorMessage.toString()});
            }
            errors.addAll(this.errorMessage);
            warnings.addAll(warningMessage);
            return result;
        } catch (FitsException | IOException ex) {            
            throw new ValidationError(ex);
        }
    }
}
