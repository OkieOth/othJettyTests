/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
class Test_Run_MemcachedServer {
    def pathSeparator = ':';

    public Test_Run_MemcachedServer() {
    }

    @BeforeClass
    public static void setUpClass() {
        File f = new File ('MemcachedServer_9003.pid');
        if (f.exists())
            f.delete()
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

        File pidFile = new File ('./MemcachedServer_9003.pid');
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
        def command = [javaProg,'-cp',classPath,'de.othsoft.examples.jetty.MemcachedServer','-p','9003','-t','2','-k', 'unitTestMemcached'];
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
