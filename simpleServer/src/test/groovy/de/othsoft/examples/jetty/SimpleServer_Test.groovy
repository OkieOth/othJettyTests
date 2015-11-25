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
import de.othsoft.helper.jetty.EmbeddedJettyMainFuncs
import de.othsoft.helper.jetty.ListenTests
import de.othsoft.helper.jetty.ListenTests
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
    
    @Test
    public void test9090OnAllAdresses() {
        ListenTests.startServerOnPortAndCheck('9090',null,new SimpleServerHandler());
    }

    @Test
    public void test9092OnAllAdresses() {
        ListenTests.startServerOnPortAndCheck('9092',null,new SimpleServerHandler());
    }

    @Test
    public void test9095OnLocalhostAdresses() {
        ListenTests.startServerOnPortAndCheck('9095','127.0.0.1',new SimpleServerHandler());
    }
}
