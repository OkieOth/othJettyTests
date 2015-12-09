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
import de.othsoft.examples.jetty.handler.MemcachedServerHandler
import de.othsoft.helper.jetty.ListenTests

/**
 *
 * @author eiko
 */
class MemcachedServer_Tests {

    public MemcachedServer_Tests() {
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
        ListenTests.startServerOnPortAndCheck('9090',null,new MemcachedServerHandler());
    }

    @Test
    public void test9092OnAllAdresses() {
        ListenTests.startServerOnPortAndCheck('9092',null,new MemcachedServerHandler());
    }

    @Test
    public void test9095OnLocalhostAdresses() {
        ListenTests.startServerOnPortAndCheck('9095','127.0.0.1',new MemcachedServerHandler());
    }

}
