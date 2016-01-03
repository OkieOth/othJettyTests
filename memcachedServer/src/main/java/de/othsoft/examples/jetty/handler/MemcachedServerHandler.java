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
    ICache cache = null;
    String appKey = null;
    private static Logger logger = LoggerFactory.getLogger(MemcachedServerHandler.class);
    
    private void init() {
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
    
    public MemcachedServerHandler() {
        init();
    }
    
    public MemcachedServerHandler(String appKeyStr) {
        init();
        if (appKeyStr!=null)
            appKey = appKeyStr;
    }

    @Override
    public void doStop() {
        logger.info("<<{}>> doStop {}",Identifier.getInst().getName());
        ((CacheImpl)cache).closeServerCon();
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
            return;
        }
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
                return;
            case "/getValue":
                // http://my_server/getValue?user=userKey&key=keyName
                {
                    String[] userAndKey = getUserAndKeyFromRequest(baseRequest, response, request);
                    if (userAndKey==null) return;
                    logger.info("<<{}>> cache getValue({},{},{})",Identifier.getInst().getName(),appKey,userAndKey[0], userAndKey[1]);
                    String value = cache.getStrValue(appKey,userAndKey[0], userAndKey[1]);
                    if (value!=null)
                        successResponse(baseRequest, response, userAndKey[1]+"="+value);
                    else
                        successResponse(baseRequest, response, userAndKey[1]+"=");                    
                }
                break;
            case "/setValue":
                // http://my_server/setValue?user=userKey&key=keyName&value=valueToSave
                {
                    String[] userAndKey = getUserAndKeyFromRequest(baseRequest, response, request);
                    if (userAndKey==null) return;
                    String value = request.getParameter("value");
                    if (value==null) {
                        errorResponse(baseRequest, response,"no value set for request");
                        return;
                    }
                    logger.info("<<{}>> cache setStrValue({},{},{},{})",Identifier.getInst().getName(),appKey,userAndKey[0], userAndKey[1],value);
                    cache.setStrValue(appKey,userAndKey[0], userAndKey[1],value);
                    successResponse(baseRequest, response, ":-)");
                }
                break;
            case "/delValue":
                {
                    String[] userAndKey = getUserAndKeyFromRequest(baseRequest, response, request);
                    if (userAndKey==null) return;
                    logger.info("<<{}>> cache removeValue({},{},{})",Identifier.getInst().getName(),appKey,userAndKey[0], userAndKey[1]);
                    cache.removeValue(appKey,userAndKey[0], userAndKey[1]);
                    successResponse(baseRequest, response, ":-)");
                }
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
    
    private String[] getUserAndKeyFromRequest(Request baseRequest,HttpServletResponse response,HttpServletRequest request) throws IOException {
        String key = request.getParameter("key");
        if (key==null) {
            errorResponse(baseRequest, response,"no key set for request");
            return null;
        }
        String user = request.getParameter("user");
        if (user==null) {
            errorResponse(baseRequest, response,"no user set for request");
            return null;            
        }
        String[] ret = new String[2];
        ret[0] = user;
        ret[1] = key;
        return ret;
    }
    
    private void successResponse(Request baseRequest,HttpServletResponse response,String content) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(content);               
    }    

    private void errorResponse(Request baseRequest,HttpServletResponse response,String content) throws IOException {
        logger.error("<<{}>> errorResponse: {}",Identifier.getInst().getName(),content);
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        baseRequest.setHandled(true);
        response.getWriter().println(content);               
    }    
}
