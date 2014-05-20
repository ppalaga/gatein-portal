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

package org.exoplatform.web.application.javascript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.portal.controller.resource.script.StaticScriptResource;

/**
 * A container for script data that needs to be registered and also unregistered from a {@link JavascriptConfigService}.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ScriptResources {
    private static final Log log = ExoLogger.getLogger(ScriptResources.class);

    /**
     * An immutable variant of {@link ScriptResources}.
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    public static final class ImmutableScriptResources extends ScriptResources {
        public ImmutableScriptResources(ScriptResources scriptResources) {
            super(Collections.unmodifiableList(new ArrayList<ScriptResourceDescriptor>(
                    scriptResources.scriptResourceDescriptors)), Collections
                    .unmodifiableList(new ArrayList<StaticScriptResource>(scriptResources.staticScriptResources)),
                    new ImmutablePathsBuilder(scriptResources.paths).build());
        }

        public ImmutableScriptResources toImmutable() {
            return this;
        }
    }

    public static class ImmutablePathsBuilder {
        public static Map<String, List<String>> emptyPaths() {
            return Collections.emptyMap();
        }

        private final Map<String, List<String>> paths;

        /**
         * @param paths
         */
        public ImmutablePathsBuilder() {
            super();
            this.paths = new LinkedHashMap<String, List<String>>();
        }

        public ImmutablePathsBuilder(Map<String, List<String>> paths) {
            super();
            this.paths = new LinkedHashMap<String, List<String>>();
            for (Map.Entry<String, List<String>> en : this.paths.entrySet()) {
                en.setValue(Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
            }
        }

        public ImmutablePathsBuilder put(String prefix, List<String> paths) {
            this.paths.put(prefix, Collections.unmodifiableList(new ArrayList<String>(paths)));
            return this;
        }

        /**
         * Adds those elements of {@code pathEntries} to {@link #paths} which are not there yet and
         * removes those elements from {@code pathEntries} which are available in {@link #paths} already.
         * Hence, {@code pathEntries} can be modified during the call.
         *
         * @param pathEntries
         * @return
         */
        public ImmutablePathsBuilder accept(Map<String, List<String>> pathEntries) {
            for (Iterator<Map.Entry<String, List<String>>> it = pathEntries.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, List<String>> en = it.next();
                List<String> availableValue = this.paths.get(en.getKey());
                if (availableValue != null) {
                    it.remove();
                    log.warn("Ignoring path entry " + en + " because the given resource path was already provided: "
                            + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding path entry " + en);
                    }
                    this.paths.put(en.getKey(), Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
                }
            }
            return this;
        }

        public Map<String, List<String>> build() {
            return Collections.unmodifiableMap(paths);
        }

        /**
         * @param toRemovePaths
         * @return
         */
        public ImmutablePathsBuilder removeAll(Map<String, List<String>> toRemove) {
            for (String prefix : toRemove.keySet()) {
                paths.remove(prefix);
            }
            return this;
        }
    }

    public static class ImmutableStaticScriptResourcesBuilder {
        public static Map<String, StaticScriptResource> emptyStaticScriptResources() {
            return Collections.emptyMap();
        }

        private final Map<String, StaticScriptResource> staticScriptResources;

        /**
         * @param staticScriptResources
         */
        public ImmutableStaticScriptResourcesBuilder(Map<String, StaticScriptResource> staticScriptResources) {
            super();
            this.staticScriptResources = new HashMap<String, StaticScriptResource>(staticScriptResources);
        }

        /**
         * Adds those elements of {@code toAdd} to {@link #staticScriptResources} which are not there yet and
         * removes those elements from {@code toAdd} which are available in {@link #staticScriptResources} already.
         * Hence, {@code toAdd} can be modified during the call.
         *
         * @param toAdd entriess to add
         * @return
         */
        public ImmutableStaticScriptResourcesBuilder accept(Collection<StaticScriptResource> toAdd) {
            for (Iterator<StaticScriptResource> it = toAdd.iterator(); it.hasNext();) {
                StaticScriptResource staticScriptResource = it.next();
                String resourcePath = staticScriptResource.getResourcePath();
                StaticScriptResource availableValue = staticScriptResources.get(resourcePath);
                if (availableValue != null) {
                    /* remove from toAdd */
                    it.remove();
                    log.warn("Ignoring " + StaticScriptResource.class.getSimpleName() + " " + staticScriptResource
                            + " because the given resource path was already provided by " + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding " + staticScriptResource);
                    }
                    staticScriptResources.put(resourcePath, staticScriptResource);
                }
            }
            return this;
        }

        public Map<String, StaticScriptResource> build() {
            return Collections.unmodifiableMap(staticScriptResources);
        }

        /**
         * @param toRemoveStaticScriptResources
         * @return
         */
        public ImmutableStaticScriptResourcesBuilder removeAll(Collection<StaticScriptResource> toRemove) {
            for (StaticScriptResource staticScriptResource : toRemove) {
                staticScriptResources.remove(staticScriptResource.getResourcePath());
            }
            return this;
        }
    }

    private final List<ScriptResourceDescriptor> scriptResourceDescriptors;
    private final List<StaticScriptResource> staticScriptResources;
    private final Map<String, List<String>> paths;

    /**
     * @param scriptResourceDescriptors
     * @param staticScriptResources
     * @param paths
     */
    public ScriptResources(List<ScriptResourceDescriptor> scriptResourceDescriptors,
            List<StaticScriptResource> staticScriptResources, Map<String, List<String>> paths) {
        this.scriptResourceDescriptors = scriptResourceDescriptors;
        this.staticScriptResources = staticScriptResources;
        this.paths = paths;
    }

    public ScriptResources() {
        this.scriptResourceDescriptors = new ArrayList<ScriptResourceDescriptor>();
        this.staticScriptResources = new ArrayList<StaticScriptResource>();
        this.paths = new LinkedHashMap<String, List<String>>();
    }

    /**
     * @return the scriptResourceDescriptors
     */
    public List<ScriptResourceDescriptor> getScriptResourceDescriptors() {
        return scriptResourceDescriptors;
    }

    /**
     * @return the staticScriptResources
     */
    public List<StaticScriptResource> getStaticScriptResources() {
        return staticScriptResources;
    }

    /**
     * @return the paths
     */
    public Map<String, List<String>> getPaths() {
        return paths;
    }

    /**
     * Returns an {@link ImmutableScriptResources} instance based on this {@link ScriptResources}.
     *
     * @return a new {@link ImmutableScriptResources} instance.
     */
    public ImmutableScriptResources toImmutable() {
        return new ImmutableScriptResources(this);
    }

    public boolean isEmpty() {
        return scriptResourceDescriptors.isEmpty() && staticScriptResources.isEmpty() && paths.isEmpty();
    }
}
