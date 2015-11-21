/*
Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
See the NOTICE file distributed with this work for additional information regarding copyright ownership.  
The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
specific language governing permissions and limitations under the License.
 */


package de.othsoft.examples.jetty

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author eiko
 */
class Test_Run_SimpleServer {
    def pathSeparator = ':';

    public Test_Run_SimpleServer() {
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

    @Test
    public void testRunWithoutPortArgument() {
        // started without port arguement
        def javaProg='java'
        String classPath=null;
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs().each {
            if (classPath!=null) {
                classPath += pathSeparator;
            }
            classPath += it.getFile();
        }        
        def command = [javaProg,'-cp',classPath,'de.othsoft.examples.jetty.SimpleServer'];
        def workingDir = '.';
        def process = new ProcessBuilder(command)
                                    .redirectErrorStream(true) 
                                    .start();
        process.inputStream.eachLine {
            println it
        }
        process.waitFor();
        assertEquals('process don\'t exit with 1',1,process.exitValue());
    }

    @Test
    public void testRunWithPortArgumentButWithoutPortValue() {
        // started without port arguement
        def javaProg='java'
        String classPath=null;
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs().each {
            if (classPath!=null) {
                classPath += pathSeparator;
            }
            classPath += it.getFile();
        }        
        def command = [javaProg,'-cp',classPath,'de.othsoft.examples.jetty.SimpleServer','-p'];
        def process = new ProcessBuilder(command)
                                    .redirectErrorStream(true) 
                                    .start();
        process.inputStream.eachLine {
            println it
        }
        process.waitFor();
        assertEquals('process don\'t exit with 1',1,process.exitValue());
    }
    
    @Test
    public void testForPidFile() {
        // test if this port is already used
        def javaProg='java'
        String classPath=null;
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs().each {
            if (classPath!=null) {
                classPath += pathSeparator;
            }
            classPath += it.getFile();
        }        

        File pidFile = new File ('./SimpleServer_9003.pid');
        boolean pidFileExist = false;
        int threadRuns=0;
        Thread.start {
            // this thread will check in the currend directory for the pid file
            while (threadRuns==0) {
                if (!pidFileExist) {
                    pidFileExist=pidFile.exists();
                }
                println 'wait for server stop ...'
                sleep(100);
            }
            threadRuns = 2;
        };
        def command = [javaProg,'-cp',classPath,'de.othsoft.examples.jetty.SimpleServer','-p','9003','-t','2'];
        // server start with a timeout of 5 seconds
        def process = new ProcessBuilder(command)
                                    .redirectErrorStream(true) 
                                    .start();
        process.inputStream.eachLine {
            println it
        }
        process.waitFor();
        threadRuns = 1;
        while (threadRuns!=2) {
            // now it's time to wait for finishing of the check thread
            println 'wait for check thread stop ...'
            Thread.sleep(100);
        }
        assertTrue("pid file ${pidFile.getName()} not found",pidFileExist);
    }
}
