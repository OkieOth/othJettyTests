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
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.othsoft.helper.base.Identifier;
import de.othsoft.helper.jetty.EmbeddedJettyMainFuncs;
import de.othsoft.helper.jetty.EmbeddedJettyRunOptions;
import de.othsoft.helper.jetty.StopJettyTimerThread;
import de.othsoft.helper.main.PidFile;

/**
 *
 * @author eiko
 */
public class SimpleServer {
    public static void main(String[] args) {
        try {
            EmbeddedJettyRunOptions jettyOptions = new EmbeddedJettyRunOptions(SimpleServer.class.getName());
            jettyOptions.parse(args);
            jettyOptions.ifMissOptionPrintUsageAndExit("p");

            String portStr = jettyOptions.getValue("p");
            // do a reinit after the port is clear
            Identifier.init(SimpleServer.class,portStr);
            PidFile.getInst().createPidFileAndExitWhenAlreadyExist();
            String addressStr = jettyOptions.hasOption("a") ? jettyOptions.getValue("a") : null;
            Server server = EmbeddedJettyMainFuncs.buildServer(portStr, addressStr, new SimpleServerHandler(),logger);
            if (jettyOptions.hasOption("t")) {
                String secondsToStopStr = jettyOptions.getValue("t");
                logger.info("<<{}>> found 't' option, will stop the server in {} seconds",Identifier.getInst().getName(),secondsToStopStr);
                long secondsToStop = Long.parseLong(secondsToStopStr);
                StopJettyTimerThread timerThread = new StopJettyTimerThread(server,secondsToStop);
                timerThread.start();
            }            
            EmbeddedJettyMainFuncs.runServer(server,logger);
        } catch (Exception e) {
            logger.error("<<{}>> {}: ", Identifier.getInst().getName(), e.getClass().getName(), e);
            System.exit(1);
        }
    }
            
    static {
        Identifier.init(SimpleServer.class);
    }
    
    private static Logger logger = LoggerFactory.getLogger(SimpleServer.class);
}
