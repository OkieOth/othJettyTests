/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.othsoft.examples.jetty.handler;

import de.othsoft.examples.jetty.SimpleServer;
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
public class SimpleServerHandler extends AbstractHandler {
    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException {
        logger.info("<<{}>> handleRequest from {} for {}",Identifier.getInst().getName(),request.getRemoteAddr(),request.getRequestURI());
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>I'm "+Identifier.getInst().getName()+" :-)</h1>");
    }

    private static Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);
}
