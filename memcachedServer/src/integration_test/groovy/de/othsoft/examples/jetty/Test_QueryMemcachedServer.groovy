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
class Test_QueryMemcachedServer {
    private static String vmIp = null;
    private int portStart=9001;
    private int portEnd=9003;

    public Test_QueryMemcachedServer() {
    }

    @BeforeClass
    public static void setUpClass() {
        File whereIAm = new File('.');
        String path2IpAddressFile = whereIAm.canonicalPath + '/src/integration_test/vagrant/ubuntu_memcached/run/ip_address.txt'
        new File(path2IpAddressFile).eachLine {
            if (vmIp==null) {
                // the file with the ip addresses contains all interface addresses of the vm ...
                // ... I try on what address a normal ping will success
                def ip = it;
                "ping -W 1 -c 1 $ip".execute().in.eachLine {
                    if (it.indexOf(', 0% packet loss,')!=-1) {
                        println "ip for tests: $ip";
                        vmIp = ip;                        
                    }
                }
            }
        }
        
        // start the SimpleServer instances
        def process = "$whereIAm/src/integration_test/scripts/startMemcachedServer.sh start 3".execute();
        process.waitFor();
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
    public void connectOneClient() {
        assertNotNull ('no ip address found',vmIp)


        // maybe the servers are not ready to handle requests ... so wait for a while
        for (int i=0;i<10000;i++) {
            try {
                def urlTxt = "http://$vmIp:$portStart";
                new URL(urlTxt).getText();
                break;
            }
            catch(ConnectException e) {
                sleep(100);
            }
        }

        def serverPort1 = portStart
        def serverPort2 = serverPort1 + 1
        def serverPort3 = serverPort2 + 1
        
        def userId1 = new URL("http://$vmIp:$serverPort1/getUserId").getText();
        def userId2 = new URL("http://$vmIp:$serverPort2/getUserId").getText();
        assertNotNull ('userId1 is null',userId1)
        assertNotNull ('userId2 is null',userId2)
        assertTrue('userId1: response content do not start with userId=',userId1.indexOf('userId=')==0);
        assertTrue('userId2: response content do not start with userId=',userId2.indexOf('userId=')==0);
        userId1 = userId1.substring ('userId='.length())
        userId2 = userId2.substring ('userId='.length())
        assertFalse('userIDs are equal',userId1.equals(userId2))

        /*
        for (int i=0;i<10;i++) {
            for (int j=portStart;j<portEnd;j++) {
                def urlTxt = "http://$vmIp:$j";
                println "request $urlTxt, run $i"
                def responseTxt = new URL(urlTxt).getText();
                assertNotNull("no response from $urlTxt",responseTxt);
                assertTrue("wrong response from $urlTxt",responseTxt.indexOf(':-)')!=-1)
            }
        }
        */
    }
}
