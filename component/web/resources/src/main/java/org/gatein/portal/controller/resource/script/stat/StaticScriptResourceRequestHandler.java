/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
    *
    * This is free software; you can redistribute it and/or modify it
    * under the terms of the GNU Lesser General Public License as
    * published by the Free Software Foundation; either version 2.1 of
    * the License, or (at your option) any later version.
    *
    * This software is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    * Lesser General Public License for more details.
    *
    * You should have received a copy of the GNU Lesser General Public
    * License along with this software; if not, write to the Free
    * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
    */

package org.gatein.portal.controller.resource.script.stat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class StaticScriptResourceRequestHandler extends WebRequestHandler {
    private static final Log log = ExoLogger.getLogger(StaticScriptResourceRequestHandler.class);

    /**
     *
     */
    public StaticScriptResourceRequestHandler() {
        super();
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#getHandlerName()
     */
    @Override
    public String getHandlerName() {
        return "staticScriptResource";
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#execute(org.exoplatform.web.ControllerContext)
     */
    @Override
    public boolean execute(ControllerContext context) throws Exception {
        String resourcePath = context.getParameter(ResourceRequestHandler.RESOURCE_QN);
        if (resourcePath != null) {
            resourcePath = new StringBuilder(1+resourcePath.length()).append('/').append(resourcePath).toString();
        }
        JavascriptConfigService service = (JavascriptConfigService) PortalContainer.getComponent(JavascriptConfigService.class);
        StaticScriptResource r = service.getStaticScriptResource(resourcePath);
        if (r != null) {

            String targetContextPath = r.getContextPath();
            PortalContainer portalContainer = PortalContainer.getInstance();
            ServletContext mergedContext = portalContainer.getPortalContext();
            ServletContext targetContext = mergedContext.getContext(targetContextPath);

            String resourceURI = r.getDirectoryAndPath();
            HttpServletRequestWrapper req = new CustomRequest(context.getRequest(), resourceURI);
            HttpServletResponse res = context.getResponse();
            RequestDispatcher dispatcher = targetContext.getRequestDispatcher(resourceURI);
            dispatcher.forward(context.getRequest(), res);
            return true;
        }
        return false;
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#getRequiresLifeCycle()
     */
    @Override
    protected boolean getRequiresLifeCycle() {
        return false;
    }

    public static class CustomRequest extends HttpServletRequestWrapper {
        private final String uri;

        /**
         * @param request
         * @param uri
         */
        public CustomRequest(HttpServletRequest request, String uri) {
            super(request);
            this.uri = uri;
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
         */
        @Override
        public String getRequestURI() {
            return uri;
        }

    }

}
