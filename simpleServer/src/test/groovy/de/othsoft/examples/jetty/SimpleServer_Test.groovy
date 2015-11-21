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
import de.othsoft.examples.jetty.handler.SimpleServerHandler
import org.eclipse.jetty.server.Server

/**
 *
 * @author eiko
 */
class SimpleServer_Test {

    public SimpleServer_Test() {
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
    
    private boolean doListen(regExpPort,boolean listen) {
        boolean ret = false;
        'netstat -nat'.execute().in.eachLine {
            if ( it ==~ regExpPort ) {
                if (listen) {
                    ret = it ==~ /.*\sLISTEN\s*$/;
                }
                else
                    ret = true;
            }
        }
        return ret;
    }
    
    @Test
    public void test9090OnAllAdresses() {
        // test if this port is already used
        assertFalse ('port 9090 already in use', doListen (/.*:9090\s.*/,false));
        print 'test9090OnAllAdresses started ...';
        SimpleServerHandler handler = new SimpleServerHandler();
        Server server = SimpleServer.buildServer('9090',null,handler);
        boolean threadRuns = false;
        Thread.start {
            threadRuns = true;
            println 'Server started'
            SimpleServer.runServer(server);
            println 'Server stopped'
            threadRuns = false;
        };
        Thread.sleep(1000);
        // now a service listen on port 9090
        assertTrue ('can not find server listen on port 9090',doListen (/.*\s:::9090\s.*/,true));
        server.stop();
        while (threadRuns) {
            Thread.sleep(10);
        }
        println 'test9090OnAllAdresses finished';
    }

    @Test
    public void test9092OnAllAdresses() {
        // test if this port is already used
        assertFalse ('port 9092 already in use', doListen (/.*\s:::9092\s.*/,false));
        print 'test9092OnAllAdresses started ...';
        SimpleServerHandler handler = new SimpleServerHandler();
        Server server = SimpleServer.buildServer('9092',null,handler);
        boolean threadRuns = false;
        Thread.start {
            threadRuns = true;
            println 'Server started'
            SimpleServer.runServer(server);
            print 'Server stopped'
            threadRuns = false;
        };
        sleep(1000);
        // now a service listen on port 9092
        assertTrue ('can not find server listen on port 9092',doListen (/.*:9092\s.*/,true));
        server.stop();
        while (threadRuns) {
            sleep(10);
        }
        println 'test9092OnAllAdresses finished';
    }

    @Test
    public void test9095OnLocalhostAdresses() {
        // test if this port is already used
        assertFalse ('port 9095 already in use', doListen (/.*:9095\s.*/,false));
        print 'test9095OnLocalhostAdresses started ...';
        SimpleServerHandler handler = new SimpleServerHandler();
        Server server = SimpleServer.buildServer('9095','127.0.0.1',handler);
        boolean threadRuns = false;
        Thread.start {
            threadRuns = true;
            println 'Server started'
            SimpleServer.runServer(server);
            println 'Server stopped'
            threadRuns = false;
        };
        sleep(1000);
        // now a service listen on port 9095
        assertTrue ('can not find server listen on port 9095',doListen (/.*\s127\.0\.0\.1:9095\s.*/,true));
        server.stop();
        while (threadRuns) {
            sleep(10);
        }
        println 'test9095OnLocalhostAdresses finished';
    }
}
