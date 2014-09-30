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
import io.github.malapert.jworkflow.exception.AIPException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean-Christophe Malapert
 */
public final class AIP implements Serializable, IAIP {

    private static final long serialVersionUID = 1L;
    private final static String AIP_EXTENSION = ".aip";
    private final static String AIP_FTL = "/aip.ftl";

    private File preserveFile;
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private Map<String, Object> core = new HashMap<>();
    private List<RecordManagement> recordsMgt = new ArrayList<>();
    private boolean saveAsBinary = true;

    /**
     *
     * @param preserveFile
     * @return
     */
    public static AIP create(File preserveFile) {
        AIP aip = new AIP(preserveFile.getName(), preserveFile);
        aip.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, "Creating AIP", preserveFile.getName() + " has been created with the identifier " + preserveFile.getName(), 0L);
        return aip;
    }

    /**
     *
     * @param uniqueIdentifier
     * @param preserveFile
     * @return
     */
    public static AIP create(String uniqueIdentifier, File preserveFile) {
        AIP aip = new AIP(uniqueIdentifier, preserveFile);
        aip.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, "Creating AIP", preserveFile.getName() + " has been created with the identifier " + uniqueIdentifier, 0L);
        return aip;
    }

    /**
     *
     */
    public AIP() {

    }

    private AIP(AIP aip) {
        setPreserveFile(aip.getPreserveFile());
        setMetadata(aip.getMetadata());
        setCore(aip.getCore());
        setRecordsMgt(aip.getRecordsMgt());
        setSaveAsBinary(aip.isSaveAsBinary());
    }

    private AIP(String uniqueIdentifier, File mainData) {
        setPreserveFile(mainData);
        init(uniqueIdentifier);
    }

    private void init(String uniqueIdentifier) {
        this.getCore().put(AIP_ID, uniqueIdentifier);
        this.getCore().put(AIP_ORIGIN_FILE_ID, getPreserveFile().getName());
    }

    /**
     *
     * @param key
     * @return
     */
    public Object getCore(String key) {
        return this.getCore().get(key);
    }

    private static XStream createXstream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("AIP", AIP.class);
        xstream.alias("recordMgt", RecordManagement.class);
        NamedMapConverter namedMapConverter = new NamedMapConverter(xstream.getMapper(), "keyword", "name", String.class, null, String.class, true, false, xstream.getConverterLookup());
        xstream.registerConverter(namedMapConverter);
        return xstream;
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    @Override
    public Map getMetadata() {
        return this.metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    private void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     *
     * @param keyword
     * @param value
     * @param processName
     * @throws AIPException
     */
    @Override
    public void addMetadata(String keyword, String value, String processName) throws AIPException {
        try {
            Object result = this.getMetadata().put(keyword, value);
            if (result == null) {
                this.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, processName, "Add " + keyword + "=" + value, 0L);
            } else {
                this.addRecordMgt(Message.SecurityLevel.WARNING, processName, "Update " + keyword + " by " + keyword + "=" + value, 0L);
            }
        } catch (RuntimeException ex) {
            this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
            throw new AIPException(ex);
        }

    }

    /**
     *
     * @param keyword
     * @param processName
     * @throws AIPException
     */
    @Override
    public void removeMetadata(String keyword, String processName) throws AIPException {
        try {
            if (this.getMetadata().containsKey(keyword)) {
                Object result = this.getMetadata().remove(keyword);
                if (result == null) {
                    this.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, processName, "Remove keywork " + keyword, 0L);
                } else {
                    this.addRecordMgt(Message.SecurityLevel.WARNING, processName, "Cannot remove keyword " + keyword + " because it does not exist", 0L);
                }
            } else {
                this.addRecordMgt(Message.SecurityLevel.WARNING, processName, "Cannot remove keyword " + keyword + " because it does not exist", 0L);

            }
        } catch (RuntimeException ex) {
            this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
            throw new AIPException(ex);
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

//    public void copyTo(File file, String processName) throws AIPException {
//        try {
//            File directoryDest = new File(file.getParent());
//            directoryDest.mkdirs();
//            try {
//                IOUtils.copy(new FileInputStream(this.getPreserveFile()), new FileOutputStream(file));
//            } catch (FileNotFoundException ex) {
//                this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
//                throw new AIPException(ex);
//            } catch (IOException ex) {
//                this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
//                throw new AIPException(ex);
//            }
//            this.setPreserveFile(file);
//            String checksumFile = io.github.malapert.jworkflow.validation.ChecksumValidation.computeChecksumFile(getPreserveFile(), io.github.malapert.jworkflow.validation.ChecksumValidation.ALGORITHM.MD5);
//            if (this.getCore().containsKey(AIP_CHECKSUM)) {
//                if (!checksumFile.equals(this.getCore().get(AIP_CHECKSUM))) {
//                    this.getPreserveFile().delete();
//                    throw new AIPException(MessageFormat.format("The {0} is corrupted, deleting the file", file.getName()));
//                } else {
//                    save(new File(this.getPreserveFile().getAbsoluteFile() + AIP_EXTENSION), isSaveAsBinary());
//                }
//            } else {
//                this.getCore().put(AIP_CHECKSUM, checksumFile);
//                save(new File(this.getPreserveFile().getAbsoluteFile() + AIP_EXTENSION), isSaveAsBinary());
//            }
//        } catch (RuntimeException ex) {
//            this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
//            throw new AIPException(ex);
//        }
//    }

    /**
     *
     * @param file
     * @param processName
     * @throws AIPException
     */
    
    @Override
    public void renameTo(File file, String processName) throws AIPException {
        try {
            File directoryDest = new File(file.getParent());
            directoryDest.mkdirs();
            boolean result = this.getPreserveFile().renameTo(file);
            if (result) {
                this.setPreserveFile(file);
                save(new File(this.getPreserveFile().getAbsolutePath() + AIP_EXTENSION), isSaveAsBinary());
                this.addRecordMgt(Message.SecurityLevel.INFORMATIONAL, processName, "Storing the file to " + file.getAbsolutePath() + " by renaming it", 0L);
            } else {
                this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, String.format("Cannot rename the file %s to %s", this.getPreserveFile().getName(), file.getName()), 0L);
                throw new AIPException(String.format("Cannot rename the file %s to %s", this.getPreserveFile().getName(), file.getName()));
            }
        } catch (RuntimeException ex) {
            this.addRecordMgt(Message.SecurityLevel.CRITCAL, processName, ex.getMessage(), 0L);
            throw new AIPException(ex);
        }
    }

    /**
     *
     */
    @Override
    public void remove() {
        File aip = new File(this.getPreserveFile() + AIP_EXTENSION);
        aip.delete();
        this.getPreserveFile().delete();
    }

    /**
     *
     * @param level
     * @param stepName
     * @param event
     * @param elapsedTimeSec
     */
    @Override
    public void addRecordMgt(Message.SecurityLevel level, String stepName, String event, Long elapsedTimeSec) {
        Message message = new Message(event, null, Arrays.asList(stepName), level);
        message.setProcessingTime(elapsedTimeSec);
        RecordManagement recordMgt = new RecordManagement(message);
        this.getRecordsMgt().add(recordMgt);
    }

    /**
     * @return the recordsMgt
     */
    public List<RecordManagement> getRecordsMgt() {
        return recordsMgt;
    }

    /**
     * @param recordsMgt the recordsMgt to set
     */
    private void setRecordsMgt(List<RecordManagement> recordsMgt) {
        this.recordsMgt = recordsMgt;
    }

    @Override
    public String toString() {
        String message;
        try {
            Configuration cfg = new Configuration();
            ClassTemplateLoader loader = new ClassTemplateLoader(getClass(), "");
            cfg.setTemplateLoader(loader);
            Template template = cfg.getTemplate(AIP_FTL);
            Map dataModel = new HashMap();
            dataModel.put("core", this.getCore());
            dataModel.put("metadata", this.getMetadata());
            dataModel.put("recordsMgt", this.getRecordsMgt());
            Writer out = new StringWriter();
            template.process(dataModel, out);
            message = out.toString();
        } catch (IOException | TemplateException ex) {
            Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
            message = "";
        }
        return message;
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
            Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AIP.class.getName()).log(Level.SEVERE, null, ex);
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
}
