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
package de.othsoft.examples.jetty;

import de.othsoft.examples.jetty.handler.SimpleServerHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eiko
 */
public class SimpleServer {

    public static String identifier = "SimpleServer";
    private static String pidFile=null;

    public static void main(String[] args) {
        Options options = createOptions();
        try {            
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("?")) {
                printUsageAndExit(options);
            }
            if (!cmd.hasOption('p')) {
                System.out.println("the port parameter is needed: for instance -p 9001");
                printUsageAndExit(options);
            }
            String portStr = cmd.getOptionValue("p");
            if (cmd.hasOption('i')) {
                identifier = cmd.getOptionValue('i');
            } else {
                identifier += ("-" + portStr);
            }
            
            if (cmd.hasOption('f')) {
                pidFile = cmd.getOptionValue('f');
            }
            else {
                pidFile = identifier+".pid";
            }
            createPidFile();
            String addressStr = cmd.hasOption('a') ? cmd.getOptionValue('a') : null;
            Server server = buildServer(portStr, addressStr, new SimpleServerHandler(identifier));
            runServer(server);
        } catch (MissingOptionException e) {
            logger.error("<<{}>> {}: ", identifier, e.getClass().getName(), e.getMessage());
            System.out.println(e.getMessage());
            printUsageAndExit(options);
        } catch (Exception e) {
            logger.error("<<{}>> {}: ", identifier, e.getClass().getName(), e);
            System.exit(1);
        }
    }
    
    private static void createPidFile() {
        File f = new File(pidFile);
        if (f.exists()) {
            logger.error("<<{}>> error pid file ({}) exists, does the process already running?",identifier,pidFile);
            System.exit(1);
        }
        try {
            FileOutputStream fout = new FileOutputStream(f);
            f.deleteOnExit();
            String pid = getPid();
            fout.write(pid.getBytes());
            fout.flush();
            fout.close();
        }
        catch(Exception e) {
            logger.error("<<{}>> error while create pid file ({}): {}, {}",identifier,pidFile,e.getClass().getName(),e.getMessage());
        }
    }
    
    private static void printUsageAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java de.othserv.examples.jetty.SimpleServer", options);
        System.exit(1);
    }

    private static Options createOptions() {
        Options options = new Options();
        Option option = new Option("p", "port", true, "port the server listen on");
        option.setRequired(true);
        option.setType(Integer.class);
        options.addOption(option);
        options.addOption("a", "address", true, "the address the server is bind on (default all)");
        options.addOption("i", "identifier", true, "optional identifier for log output");
        options.addOption("f", "pidfile", true, "pid file to use");
        options.addOption("?", "help", false, "show help");
        return options;
    }

    /**
     * extracted for testing
     */
    public static Server buildServer(String portStr, String addressStr, Handler handler) throws Exception {
        int port = Integer.parseInt(portStr);
        InetSocketAddress address = addressStr != null ? new InetSocketAddress(addressStr, port)
                : new InetSocketAddress(port);
        Server server = new Server(address);
        server.setHandler(new SimpleServerHandler(identifier));
        if (addressStr != null) {
            logger.info("<<{}>> bind on address {}, listen on port {}", identifier, addressStr, portStr);
        } else {
            logger.info("<<{}>> bind on all adresses, listen on port {}", identifier, portStr);
        }
        return server;
    }

    public static void runServer(Server server) throws Exception {
        server.start();
        logger.info("<<{}>> server started", identifier);
        server.join();
    }

    public static String getPid() {
        String pidAndStuff = ManagementFactory.getRuntimeMXBean().getName();
        int indexAt = pidAndStuff.indexOf("@");
        String pid = pidAndStuff.substring(0,indexAt);
        logger.info("<<{}>> pid={}", identifier,pid);
        return pid;
    }
    
    private static Logger logger = LoggerFactory.getLogger(SimpleServer.class);
}
