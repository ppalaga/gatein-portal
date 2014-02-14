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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class StaticScriptResources {
    private final Log log = ExoLogger.getLogger(StaticScriptResources.class);


    private final Map<String, StaticScriptResource> resources = new HashMap<String, StaticScriptResource>();

    /**
     *
     */
    public StaticScriptResources() {
        super();
    }

    /**
     * @param staticScriptResource
     */
    public void add(StaticScriptResource staticScriptResource) {
        String resourcePath = staticScriptResource.getResourcePath();
        if (resources.containsKey(resourcePath)) {
            log.warn("Ignoring "+ StaticScriptResource.class.getSimpleName() +" with an already available resource path: "+ staticScriptResource);
        } else {
            /* add only if not there already */
            log.info("Adding "+ staticScriptResource);
            resources.put(resourcePath, staticScriptResource);
        }
    }

    /**
     * @param resourcePath
     * @return
     */
    public StaticScriptResource get(String resourcePath) {
        return resources.get(resourcePath);
    }

}
