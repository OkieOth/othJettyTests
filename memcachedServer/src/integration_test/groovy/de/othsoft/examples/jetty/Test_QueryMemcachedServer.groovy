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
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.params.HttpParams
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.net.URLEncoder

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
        
        println("userId1='$userId1', userId2='$userId2'")

        String key1 = 'testKey1'
        String key2 = 'testKey2'
        
        checkAllServerForEmptyValue(userId1,key1);
        checkAllServerForEmptyValue(userId2,key1);
        
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
    
    /*
    private void checkAllServerForEmptyKey(String userId,String key) {
        def serverPort1 = portStart
        def serverPort2 = serverPort1 + 1
        def serverPort3 = serverPort2 + 1
        def response1 = new URL("http://$vmIp:$serverPort1/getValue?user=$userId&key=$key").getText();
        def response2 = new URL("http://$vmIp:$serverPort2/getValue?user=$userId&key=$key").getText();
        def response3 = new URL("http://$vmIp:$serverPort3/getValue?user=$userId&key=$key").getText();
        assertNotNull ('response1 is null',response1)
        assertNotNull ('response2 is null',response2)
        assertNotNull ('response3 is null',response3)
        assertTrue("response1: response content do not start with $key=",response1.indexOf("$key=")==0);
        assertTrue("response2: response content do not start with $key=",response2.indexOf("$key=")==0);
        assertTrue("response3: response content do not start with $key=",response3.indexOf("$key=")==0);

        def value1 = response1.substring ("$key=".length());
        def value2 = response2.substring ("$key=".length());
        def value3 = response3.substring ("$key=".length());

        assertTrue("value1 is not empty for key=$key",value1.length());
        assertTrue("value2 is not empty for key=$key",value2.length());
        assertTrue("value3 is not empty for key=$key",value3.length());        
    }
    */
    
    private String sendRequestToServer(String host,int port,String path,String user,String key,String value) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(host).setPort(port).setPath(path)
        .setParameter("user", URLEncoder.encode(user))
            .setParameter("key", URLEncoder.encode(key));
           
        if (value!=null)
            builder.setParameter("action", "finish");
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(builder.build());
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        String ret = EntityUtils.toString(httpEntity);
        EntityUtils.consume(httpEntity);
        println ret;
        return ret;
    }

    private void checkAllServerForEmptyValue(String userId,String key) {
        def serverPort1 = portStart
        def serverPort2 = serverPort1 + 1
        def serverPort3 = serverPort2 + 1
        def response1 = sendRequestToServer (vmIp,serverPort1,'/getValue',userId,key,null);
        def response2 = sendRequestToServer (vmIp,serverPort2,'/getValue',userId,key,null);
        def response3 = sendRequestToServer (vmIp,serverPort3,'/getValue',userId,key,null);

        assertNotNull ('response1 is null',response1)
        assertNotNull ('response2 is null',response2)
        assertNotNull ('response3 is null',response3)
        assertTrue("response1: response content do not start with $key=",response1.indexOf("$key=")==0);
        assertTrue("response2: response content do not start with $key=",response2.indexOf("$key=")==0);
        assertTrue("response3: response content do not start with $key=",response3.indexOf("$key=")==0);

        def value1 = response1.substring ("$key=".length());
        def value2 = response2.substring ("$key=".length());
        def value3 = response3.substring ("$key=".length());

        assertTrue("value1 is not empty for key=$key",value1.trim().length()==0);
        assertTrue("value2 is not empty for key=$key",value2.trim().length()==0);
        assertTrue("value3 is not empty for key=$key",value3.trim().length()==0);        
    }
}
