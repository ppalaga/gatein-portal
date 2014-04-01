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

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class StaticScriptResource {

    /**
     * The path of the context where this resource lives.
     */
    private final String contextPath;

    private final String directory;

    private final String resourcePath;

    private final String directoryAndPath;

    /**
     * @param contextPath
     * @param directory
     * @param resourceURI
     */
    public StaticScriptResource(String contextPath, String directory, String resourceURI) {
        super();
        validate("contextPath", contextPath);
        if (directory != null) {
            validate("directory", directory);
        }
        validate("resourceURI", resourceURI);

        this.contextPath = contextPath;
        this.directory = directory;
        this.resourcePath = resourceURI;
        this.directoryAndPath = directory == null ? resourcePath : directory + resourcePath;

    }

    /**
     * @param contextPath2
     */
    private static void validate(String argName, String path) {
        if (path == null) {
            throw new IllegalArgumentException(argName +" cannot be null");
        }
        if (path.length() < 1) {
            throw new IllegalArgumentException(argName +" cannot be shorter than 1");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException(argName +" must start with '/'; actual: '" + path + "'");
        }
        int contextPathLenght = path.length();
        if (contextPathLenght >= 2 && path.charAt(contextPathLenght -1) == '/') {
            throw new IllegalArgumentException(argName +" cannot end with '/'; actual: '" + path + "'");
        }
    }

    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @return the resourcePath
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "StaticScriptResource [contextPath=" + contextPath + ", directory=" + directory + ", resourcePath="
                + resourcePath + "]";
    }

    /**
     * @return the directoryAndPath
     */
    public String getDirectoryAndPath() {
        return directoryAndPath;
    }

}
