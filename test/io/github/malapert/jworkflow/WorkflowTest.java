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

import io.github.malapert.jworkflow.exception.TasksManagerException;
import io.github.malapert.jworkflow.model.AIP;
import io.github.malapert.jworkflow.model.SIP;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class WorkflowTest {
    
    public WorkflowTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class Workflow.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        try {
            TasksManager chainManager = new TasksManager(true, true);
            SIP sip = SIP.create(new File("/tmp/_axmon_20140317_854_cbuil.fit"));
            sip.setSaveAsBinary(false);
            boolean result = chainManager.run(sip, Notification.getInstance());
            if (!result)
                System.out.println(chainManager.getErrors());
            AIP aip = AIP.readFromCache(new File("/tmp/aip/48e/f23/cab/987/eaa/09f/e16/fba/039/53c/8f7/d79/5375.aip"), false);
            System.out.println(aip.toString());
        } catch (TasksManagerException ex) {
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
