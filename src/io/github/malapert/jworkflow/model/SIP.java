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
package io.github.malapert.jworkflow.model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.malapert.jworkflow.exception.SIPException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean-Christophe Malapert
 */
public final class SIP implements Serializable, ISIP {

    private static final long serialVersionUID = 1L;
    private final static String SIP_FTL = "/sip.ftl";
    private File preserveFile;
    private Map<String, Object> core = new HashMap<>();
    private String comment;
    private boolean saveAsBinary = true;

    /**
     *
     * @param preserveFile
     * @return
     */
    public static SIP create(File preserveFile) {
        SIP sip = new SIP(preserveFile.getName(), preserveFile);
        return sip;
    }

    /**
     *
     * @param uniqueIdentifier
     * @param preserveFile
     * @return
     */
    public static SIP create(String uniqueIdentifier, File preserveFile) {
        SIP sip = new SIP(uniqueIdentifier, preserveFile);
        return sip;
    }

    /**
     *
     */
    public SIP() {

    }

    private SIP(SIP sip) {
        setPreserveFile(sip.getPreserveFile());
        setCore(sip.getCore());
    }

    private SIP(String uniqueIdentifier, File mainData) {
        setPreserveFile(mainData);
        init(uniqueIdentifier);
    }

    private void init(String uniqueIdentifier) {
        this.getCore().put(ID, uniqueIdentifier);
    }

    /**
     *
     * @param key
     * @return
     */
    public Object getCore(String key) {
        return this.getCore().get(key);
    }

    /**
     * @return the core
     */
    @Override
    public Map<String, Object> getCore() {
        return core;
    }

    /**
     * @param core the core to set
     */
    public void setCore(Map<String, Object> core) {
        this.core = core;
    }

    @Override
    public File getPreserveFile() {
        return this.preserveFile;
    }

    /**
     * @param preserveFile the preserveFile to set
     */
    private void setPreserveFile(File preserveFile) {
        this.preserveFile = preserveFile;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return the saveAsBinary
     */
    public boolean isSaveAsBinary() {
        return saveAsBinary;
    }

    /**
     * @param saveAsBinary the saveAsBinary to set
     */
    public void setSaveAsBinary(boolean saveAsBinary) {
        this.saveAsBinary = saveAsBinary;
    }    

    @Override
    public void renameTo(File file, String processName) throws SIPException {
        try {
            File directoryDest = new File(file.getParent());
            directoryDest.mkdirs();
            boolean result = this.getPreserveFile().renameTo(file);
            if (result) {
                this.setPreserveFile(file);
                save(new File(this.getPreserveFile().getAbsolutePath() + SIP_EXTENSION), isSaveAsBinary());
            } else {
                throw new SIPException(String.format("Cannot rename the file %s to %s", this.getPreserveFile().getName(), file.getName()), this);
            }
        } catch (RuntimeException ex) {
            throw new SIPException(ex, this);
        }
    }
    
    /**
     *
     * @param file
     * @param asBinary
     * @return
     */
    public static AIP readFromCache(File file, boolean asBinary) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        AIP aip = null;
        try {
            fis = new FileInputStream(file);
            if (asBinary) {
                ois = new ObjectInputStream(fis);
                aip = (AIP) ois.readObject();
            } else {
                XStream xstream = createXstream();
                aip = (AIP) xstream.fromXML(fis);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return aip;
    }

    /**
     *
     * @param file
     */
    public void cacheTo(File file) {
        save(file, isSaveAsBinary());
    }
    

    private static XStream createXstream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("SIP", SIP.class);
        NamedMapConverter namedMapConverter = new NamedMapConverter(xstream.getMapper(), "keyword", "name", String.class, null, String.class, true, false, xstream.getConverterLookup());
        xstream.registerConverter(namedMapConverter);
        return xstream;
    }

    private void save(File file, boolean asBinary) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            if (asBinary) {
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this);
            } else {
                //XMLEncoder encoder = new XMLEncoder(fos);
                //encoder.writeObject(this);
                //encoder.flush();
                //encoder.close();              
                XStream xstream = createXstream();
                xstream.toXML(this, fos);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SIP.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void remove() {
        File aip = new File(this.getPreserveFile() + SIP_EXTENSION);
        aip.delete();
        this.getPreserveFile().delete();
    }
    
    @Override
    public String toString() {
        String message;
        try {
            Configuration cfg = new Configuration();
            ClassTemplateLoader loader = new ClassTemplateLoader(getClass(), "");
            cfg.setTemplateLoader(loader);
            Template template = cfg.getTemplate(SIP_FTL);
            Map dataModel = new HashMap();
            dataModel.put("core", this.getCore());
            dataModel.put("comment", this.comment);
            Writer out = new StringWriter();
            template.process(dataModel, out);
            message = out.toString();
        } catch (IOException | TemplateException ex) {
            Logger.getLogger(SIP.class.getName()).log(Level.SEVERE, null, ex);
            message = "";
        }
        return message;
    }

    @Override
    public IAIP toIAIP() {
        IAIP aip = AIP.create(getPreserveFile());
        aip.getCore().put(IAIP.SIP_ID, getCore().get(ISIP.ID));
        return aip;
    }
    

}
