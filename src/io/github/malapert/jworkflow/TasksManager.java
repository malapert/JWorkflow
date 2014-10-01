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
package io.github.malapert.jworkflow;

import io.github.malapert.jworkflow.TaskHandler.AbstractTaskHandler;
import io.github.malapert.jworkflow.model.IAIP;
import io.github.malapert.jworkflow.TaskHandler.ITaskHandler;
import io.github.malapert.jworkflow.exception.ConversionException;
import io.github.malapert.jworkflow.exception.TasksManagerException;
import io.github.malapert.jworkflow.parser.Workflow;
import io.github.malapert.jworkflow.parser.Workflow.Task;
import io.github.malapert.jworkflow.parser.Workflow.Task.Arg;
import io.github.malapert.jworkflow.exception.TaskHandlerException;
import io.github.malapert.jworkflow.model.IPackage;
import io.github.malapert.jworkflow.model.ISIP;
import io.github.malapert.jworkflow.model.Message;
import io.github.malapert.jworkflow.notification.INotification;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * TasksManager is a class that handles a set of tasks, which are defined in the
 * workflow.xml file. A TasksManager object encapsulates a pipeline of chained
 * tasks {@link io.github.malapert.jworkflow.TaskHandler.AbstractTaskHandler}.
 * Each task has the following functionalities:
 * <ul>
 * <li>process an AIP {@link io.github.malapert.jworkflow.model.IAIP}
 * <li>unprocess an AIP {@link io.github.malapert.jworkflow.model.IAIP}
 * <li>notifies an event
 * {@link io.github.malapert.jworkflow.notification.INotification}
 * </ul>
 *
 * @author Jean-Christophe Malapert
 */
public class TasksManager extends Observable implements INotification {

    /**
     * Message that is notified.
     */
    private volatile Message event = new Message();
    /**
     * List of observers to notify.
     */
    private final List<Observer> observers = new ArrayList<>();
    /**
     * List of tasks to process.
     */
    private final List<ITaskHandler> tasks = new ArrayList<>();
    /**
     * Stop on the first exception in the pipeline.
     */
    private final boolean stopOnFirstException;
    /**
     * Cleanup the task when an error is detected.
     */
    private final boolean cleanUpWhenErrorDetected;
    /**
     * Errors in tasks.
     */
    private final List<String> errors = new ArrayList<>();
    /**
     * Workflow's configuration.
     */
    private transient Workflow workflow;

    /**
     * Constructs a new pipeline of tasks based on the internal configuration
     * file.
     *
     * @param stopOnFirstException Sets to True when the pipeline must stop at
     * the first exception
     * @param cleanUpWhenErrorDetected Sets to True when the pipeline must
     * reverse its action
     * @throws TasksManagerException When the configuration file cannot be
     * parsed
     */
    public TasksManager(boolean stopOnFirstException, boolean cleanUpWhenErrorDetected) throws TasksManagerException {
        this.stopOnFirstException = stopOnFirstException;
        this.cleanUpWhenErrorDetected = cleanUpWhenErrorDetected;
        try {
            InputStream in = loadDefaultConfigurationFile();
            this.workflow = loadConfValidation(in);
        } catch (JAXBException ex) {
            throw new TasksManagerException("Error while loading the configuration "
                    + "file for the FITS validation library", ex);
        }
    }

    /**
     * Constructs a new pipeline based on a configuration file
     *
     * @param stopOnFirstException Sets to True when the pipeline must stop at
     * the first exception
     * @param cleanUpWhenErrorDetected Sets to True when the pipeline must
     * reverse its action
     * @param confValidation The configuration file that contains the list of
     * tasks
     * @throws TasksManagerException When the configuration file cannot be
     * parsed
     */
    public TasksManager(boolean stopOnFirstException, boolean cleanUpWhenErrorDetected, final File confValidation) throws TasksManagerException {
        this.stopOnFirstException = stopOnFirstException;
        this.cleanUpWhenErrorDetected = cleanUpWhenErrorDetected;
        try {
            this.workflow = loadConfValidation(new FileInputStream(confValidation));
        } catch (FileNotFoundException | JAXBException ex) {
            throw new TasksManagerException("Error while loading the configuration "
                    + "file for the FITS validation library", ex);
        }
    }

    /**
     * Executes the pipeline.
     *
     * @param pack
     * @param notification the Notification object
     * @return True when the pipeline is a success otherwise False
     */
    public boolean run(final IPackage pack, final Observer notification) {
        boolean result;
        Date startDate = new Date();
        try {
            getEvent().setTitle(String.format("Starting the ingest of AIP %s", pack.getCore().get(IPackage.ID)));
            getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
            getEvent().setAuthors(Arrays.asList("TaskManager"));
            getEvent().setDateEvent(startDate);
            notifyObservers(this);
            List<Task> tasksWorkflow = this.workflow.getTask();
            List<ITaskHandler> procs = buildProcessingChain(tasksWorkflow);
            for (ITaskHandler proc : procs) {
                ((AbstractTaskHandler) proc).addObserver(notification);
                ((AbstractTaskHandler) proc).getObservers().addAll(getObservers());
                this.appendHandlerToChain(proc);
            }
            result = this.processChain(pack);
            Date stopDate = new Date();
            if (result) {
                getEvent().setTitle(String.format("AIP %s has been archived in %s seconds", pack.getCore().get(IPackage.ID), AbstractTaskHandler.getDateDiff(startDate, stopDate, TimeUnit.SECONDS)));
                getEvent().setLevel(Message.SecurityLevel.INFORMATIONAL);
                getEvent().setAuthors(Arrays.asList("TaskManager"));
            } else {
                getEvent().setTitle(String.format("Cannot archive AIP %s after %s seconds", pack.getCore().get(IPackage.ID), AbstractTaskHandler.getDateDiff(startDate, stopDate, TimeUnit.SECONDS)));
                getEvent().setLevel(Message.SecurityLevel.ERROR);
                getEvent().setAuthors(Arrays.asList("TaskManager"));
            }
            notifyObservers(this);
        } catch (TasksManagerException | RuntimeException ex) {
            result = false;
            Date stopDate = new Date();
            getErrors().add(ex.getMessage());
            getEvent().setTitle(String.format("Cannot archive AIP %s after %s seconds", pack.getCore().get(IPackage.ID), AbstractTaskHandler.getDateDiff(startDate, stopDate, TimeUnit.SECONDS)));
            getEvent().setLevel(Message.SecurityLevel.ERROR);
            getEvent().setAuthors(Arrays.asList("TaskManager"));
            getEvent().setDateEvent(stopDate);
            notifyObservers(this);
        }
        return result;
    }

    /**
     * Creates a unique identifier.
     *
     * @param aipID AIP identifier
     * @param date date
     * @return A unique identifier as a String
     */
    public static String generatedID(final String aipID, final Date date) {
        return aipID + "-" + date.getTime();
    }

    /**
     * Appends a new task in the pipeline.
     *
     * @param task Processing task
     */
    private void appendHandlerToChain(final ITaskHandler task) {
        this.tasks.add(task);
    }

    /**
     *
     * @param errorFound
     * @return
     */
    private boolean stopConditionErrorDetected(boolean errorFound) {
        return (stopOnFirstException) ? errorFound : false;
    }

    /**
     * Processes an package.
     *
     * @param pack
     * @return True when the processing is a success
     */
    protected boolean processChain(IPackage pack) {
        boolean errorFound = false;
        int i;
        for (i = 0; i < tasks.size() && !stopConditionErrorDetected(errorFound); i++) {
            ITaskHandler task = tasks.get(i);
            try {
                task.process(pack);
            } catch (ConversionException ex) {
                ISIP sip = (ISIP) pack;
                pack = sip.toIAIP();
                try {
                    task.process(pack);
                } catch (TaskHandlerException ex1) {
                    errorFound = true;
                    getErrors().add(ex.getMessage());
                }
            } catch (TaskHandlerException ex) {
                errorFound = true;
                getErrors().add(ex.getMessage());
            }
        }
        if (cleanUpWhenErrorDetected && errorFound) {
            for (int j = i - 1; j >= 0; j--) {
                ITaskHandler task = tasks.get(j);
                try {
                    task.unprocess(pack);
                } catch (ConversionException ex) {
                    ISIP sip = (ISIP) pack;
                    pack = sip.toIAIP();
                } catch (TaskHandlerException ex) {

                }
            }
        }
        return !errorFound;
    }

    /**
     * Returns the raised up errors.
     *
     * @return the errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Parses the configuration file of a Task.
     *
     * @param task task to parse
     * @return the constructor of the Task
     * @throws TasksManagerException when the type of argument is not supported
     */
    private TaskConstructor parseConfForTask(Task task) throws TasksManagerException {
        List<Arg> args = task.getArg();
        List<Class> argumentsType = new ArrayList<>();
        List<Object> arguments = new ArrayList<>();
        for (Arg arg : args) {
            if (arg.isBoolean() != null) {
                argumentsType.add(Boolean.class);
                arguments.add(arg.isBoolean());
            } else if (arg.getFile() != null) {
                argumentsType.add(File.class);
                arguments.add(new File(arg.getFile()));
            } else if (arg.getString() != null) {
                argumentsType.add(String.class);
                arguments.add(arg.getString());
            } else {
                throw new TasksManagerException("This type of argument is not supported in the workflow configuration");
            }
        }
        Class[] argument = new Class[argumentsType.size()];
        Object[] argumentValue = new Object[argumentsType.size()];
        for (int i = 0; i < argumentsType.size(); i++) {
            argument[i] = argumentsType.get(i);
            argumentValue[i] = arguments.get(i);
        }
        return new TaskConstructor(argument, argumentValue, task.getVersion());
    }

    /**
     * Builds a processing chain and validates the version of each task
     * according to the configuration file.
     *
     * @param tasks All the tasks of the workflow
     * @return chained tasks
     * @throws TasksManagerException When the major version of a current task is
     * different of a major version of the configuration file
     */
    private List<ITaskHandler> buildProcessingChain(final List<Task> tasks) throws TasksManagerException {
        List<ITaskHandler> procs = new ArrayList<>();
        for (Task task : tasks) {
            try {
                Class aClass = Class.forName(task.getName());
                TaskConstructor cons = parseConfForTask(task);
                Constructor constructor = aClass.getConstructor(cons.getArgType());
                Object obj = constructor.newInstance(cons.getArgValue());
                ITaskHandler procTask = (ITaskHandler) obj;
                String version = procTask.getVersion();
                int majorVersionInTask = Integer.valueOf(version.substring(0, version.indexOf(".")));
                int majorVersionInConfig = Integer.valueOf(task.getVersion().substring(0, task.getVersion().indexOf(".")));
                if (majorVersionInConfig != majorVersionInTask) {
                    throw new TasksManagerException(String.format("The task version %s of %s is not compatible with "
                            + "the task version %s of the configuration file", new Object[]{version, procTask.getName(), task.getVersion()}));
                }
                procs.add(procTask);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new TasksManagerException(ex);
            }
        }
        return procs;
    }

    /**
     * Loads the default configuration file as a stream.
     *
     * @return the configuration file contain
     */
    private InputStream loadDefaultConfigurationFile() {
        String packageName = this.getClass().getPackage().getName();
        packageName = packageName.replaceAll("\\.", "\\/");
        String confFile = "/" + packageName + "/workflow.xml";
        InputStream in = getClass().getResourceAsStream(confFile);
        return in;
    }

    /**
     * Loads the configuration file.
     *
     * @param confValidation the configuration file where the rules are set
     * @return The configuration file
     * @throws JAXBException Happens when the configuration file is not
     * compliant with the schema
     */
    private Workflow loadConfValidation(final InputStream confValidation) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(io.github.malapert.jworkflow.parser.ObjectFactory.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Workflow confFile = (Workflow) unmarshaller.unmarshal(confValidation);
        return confFile;
    }

    /**
     * Returns the list of observers to notify.
     *
     * @return the observers
     */
    public List<Observer> getObservers() {
        return observers;
    }

    /**
     * Returns the event to notify.
     *
     * @return the event
     */
    @Override
    public Message getEvent() {
        return event;
    }

    /**
     * Reads dynamically a constructor.
     */
    public class TaskConstructor {

        private Class[] argType;
        private Object[] argValue;
        private String version;

        /**
         * Constructs a task constructor
         *
         * @param argType type for each argument
         * @param argValue value for each argument
         * @param version version of the class
         */
        public TaskConstructor(Class[] argType, Object[] argValue, String version) {
            this.argType = argType;
            this.argValue = argValue;
            this.version = version;
        }

        /**
         * Returns the type for each argument.
         *
         * @return the argType
         */
        public Class[] getArgType() {
            return argType;
        }

        /**
         * Sets the type for each argument.
         *
         * @param argType the argType to set
         */
        public void setArgType(Class[] argType) {
            this.argType = argType;
        }

        /**
         * Returns the list of argument of a constructor.
         *
         * @return the argValue
         */
        public Object[] getArgValue() {
            return argValue;
        }

        /**
         * Sets the list of arguments of a constructor.
         *
         * @param argValue the argValue to set
         */
        public void setArgValue(Object[] argValue) {
            this.argValue = argValue;
        }

        /**
         * Returns the version of a task.
         *
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Sets the version of a task.
         *
         * @param version the version to set
         */
        public void setVersion(String version) {
            this.version = version;
        }

    }

    @Override
    public synchronized void addObserver(Observer o) {
        this.getObservers().add(o);
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        this.getObservers().remove(o);
    }

    @Override
    public void notifyObservers(Object arg) {
        for (Observer obs : getObservers()) {
            obs.update(this, arg);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer obs : getObservers()) {
            obs.update(this, null);
        }
    }

}
