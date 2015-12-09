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
package de.othsoft.examples.jetty.handler;

import de.othsoft.cache.base.ICache;
import de.othsoft.cache.base.error.CacheException;
import de.othsoft.cache.memcached.CacheImpl;
import de.othsoft.helper.base.Identifier;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eiko
 */
public class MemcachedServerHandler extends AbstractHandler {
    static ICache cache = null;
    static String appKey = null;
    private static Logger logger = LoggerFactory.getLogger(MemcachedServerHandler.class);
    
    static {
        CacheImpl cI = new CacheImpl();
        cI.setServer("127.0.0.1",11211);
        try {
            appKey = cI.createUniqueAppKey(Identifier.getInst().getName());
            cache = cI;
        }
        catch (CacheException e) {
            logger.error("<<{}>> error while get appKey: [{}] {}",Identifier.getInst().getName(),e.getClass().getName(),e.getMessage());
        }
    }
    
    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException {
        logger.info("<<{}>> handleRequest from {} for {}",Identifier.getInst().getName(),request.getRemoteAddr(),request.getRequestURI());
        if (cache==null) {
            logger.error("<<{}>> cache==null",Identifier.getInst().getName());
            errorResponse(baseRequest, response, "cache==null, :'-(");
        }
        else if (appKey==null) {
            logger.error("<<{}>> appKey==null",Identifier.getInst().getName());
            errorResponse(baseRequest, response, "appKey==null, :'-(");            
        }
        else {
            try {
                String contextPath = request.getRequestURI(); 
                if (contextPath==null) {
                        successResponse(baseRequest, response, ":-)"); 
                        return;
                }
                switch (contextPath) {
                    case "/getUserId":
                        String userId = cache.createUniqueUserKey(Identifier.getInst().getName());
                        successResponse(baseRequest, response, "userId="+userId);
                        break;
                    case "/getValue":
                        break;
                    case "/setValue":
                        break;
                    case "/delValue":
                        break;
                    default:
                        successResponse(baseRequest, response, ":-D");
                }
            }
            catch(CacheException e) {
                logger.error("<<{}>> error while contact cache: [{}] {}",Identifier.getInst().getName(),e.getClass().getName(),e.getMessage());
                errorResponse(baseRequest, response, "error while contact cache");            
            }
        }
    }
    
    private void successResponse(Request baseRequest,HttpServletResponse response,String content) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(content);               
    }    

    private void errorResponse(Request baseRequest,HttpServletResponse response,String content) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        baseRequest.setHandled(true);
        response.getWriter().println(content);               
    }    
}
