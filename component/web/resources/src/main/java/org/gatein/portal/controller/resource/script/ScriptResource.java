/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.gatein.portal.controller.resource.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.gatein.portal.controller.resource.script.ScriptGraph.ScriptGraphBuilder;
import org.gatein.portal.controller.resource.script.ScriptGroup.ScriptGroupBuilder;

/**
 * This class implements the {@link Comparable} interface, however the natural ordering provided here is not consistent with
 * equals, therefore this class should not be used as a key in a {@link java.util.TreeMap} for instance.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class ScriptResource extends BaseScriptResource<ScriptResource> implements Comparable<ScriptResource> {
    static class ScriptResourceBuilder extends BaseScriptResourceBuilder<ScriptResource> {
        private List<Module> modules;
        private Map<ResourceId, Set<DepInfo>> dependencies;
        private Set<ResourceId> closure;
        private FetchMode fetchMode;
        private String alias;
        private ScriptGroupBuilder group;
        private boolean nativeAmd;

        ScriptResourceBuilder(ScriptGraphBuilder scriptGraphBuilder, ResourceId id, FetchMode fetchMode, String alias, ScriptGroupBuilder group, boolean nativeAmd) {
            super(id, scriptGraphBuilder);
            this.fetchMode = fetchMode;
            this.alias = alias;
            this.group = group;
            this.nativeAmd = nativeAmd;
            this.modules = new ArrayList<Module>();
            this.closure = new HashSet<ResourceId>();
            this.dependencies = new LinkedHashMap<ResourceId, Set<DepInfo>>();
        }

        void closureUpdated(ResourceId id, Set<ResourceId> closure) {
            if (this.closure.contains(id)) {
                this.closure.addAll(closure);
            }
        }

        void addDependency(String contextPath, ResourceId dependencyId, String alias, String pluginRS) throws InvalidResourceException {
            if (dependencyId.equals(this.id)) {
                log.warn("Ignoring self-dependency declared for resource '"+ this.id +"'. To avoid this warning, remove the self-dependency declaration for '"+ this.id +"' declaration from gatein-resources.xml in context '"+ contextPath +"'");
                return;
            }

            ScriptResourceBuilder dependency = scriptGraphBuilder.getResource(dependencyId);

            if (dependency != null) {
                dependency.checkDependentFetchMode(id, fetchMode);
                if (dependency.closure.contains(id)) {
                    /* cycle detected */
                    throw new InvalidResourceException("Adding script dependency "+ dependency.id +" to "+ id +" would introduce a dependency circle");
                }
            }

            // That is important to make closure independent from building order of graph nodes.
            if (dependency != null) {
                closure.addAll(dependency.closure);
            }

            // Update the source's closure
            closure.add(dependencyId);

            // Update any entry that points to the source
            scriptGraphBuilder.closureUpdated(id, closure);

            //
            Set<DepInfo> infos = dependencies.get(dependencyId);
            if (infos == null) {
                dependencies.put(dependencyId, infos = new LinkedHashSet<DepInfo>());
            }
            infos.add(new DepInfo(alias, pluginRS));
        }

        void addLocalModule(String contextPath, Content[] contents, String resourceBundle, int priority) {
            Module.Local module = new Module.Local(this.id, contextPath, contents, resourceBundle, priority);
            modules.add(module);
        }

        void addRemoteModule(String contextPath, String path, int priority) {
            Module.Remote module = new Module.Remote(contextPath, path, priority);
            modules.add(module);
        }

        @Override
        void addSupportedLocale(Locale locale) {
            super.addSupportedLocale(locale);
            if (group != null) {
                group.addSupportedLocale(locale);
            }
        }

        ScriptResource build() {
            if (alias == null) {
                String resName = id.getName();
                alias = resName.substring(resName.lastIndexOf("/") + 1);
            }
            if (FetchMode.ON_LOAD.equals(fetchMode)) {
                Matcher validMatcher = JavascriptConfigService.JS_ID_PATTERN.matcher(alias);
                if (!validMatcher.matches()) {
                    log.warn("alias {} is not valid JS identifier", alias);
                }
            }

            ScriptGroup scriptGroup = group != null ? group.build() : null;
            return new ScriptResource(id,
                    parameters, parametersMap, minParameters, minParametersMap,
                    fetchMode, alias, scriptGroup, nativeAmd, modules, dependencies, closure);
        }

        void checkDependencyFetchMode(ResourceId id, FetchMode expectedFetchMode) throws InvalidResourceException {
            if (this.dependencies.containsKey(id) && !this.fetchMode.equals(expectedFetchMode)) {
                throw new InvalidResourceException("ScriptResource " + this.id + " with fetchMode '"+ this.fetchMode +"' cannot depend on '" + id
                        + "' with fetchMode '"+ fetchMode +"'. The fetchModes must be equal.");
            }
        }

        void checkDependentFetchMode(ResourceId id, FetchMode expectedFetchMode) throws InvalidResourceException {
            if (!expectedFetchMode.equals(this.fetchMode)) {
                throw new InvalidResourceException("ScriptResource " + id + " with fetchMode '"+ expectedFetchMode +"' cannot depend on '" + this.id
                        + "' with fetchMode '"+ this.fetchMode +"'. The fetchModes must be equal.");
            }
        }

        void removeModuleByContextPath(String contextPath) {
            for (Iterator<Module> i = modules.iterator(); i.hasNext();) {
                Module module = i.next();
                if (module.getContextPath().equals(contextPath)) {
                    i.remove();
                }
            }
        }

        boolean isEmpty() {
            return modules.isEmpty();
        }

    }

    private static final Log log = ExoLogger.getLogger(ScriptResource.class);

    /** . */
    private final List<Module> modules;

    /** . */
    private final Map<ResourceId, Set<DepInfo>> dependencies;

    /** . */
    private final Set<ResourceId> closure;

    /** . */
    private final FetchMode fetchMode;

    /** . */
    private final String alias;

    /** . */
    private final ScriptGroup group;

    private final boolean nativeAmd;

    private ScriptResource(ResourceId id,
            Map<QualifiedName, String> parameters,
            Map<Locale, Map<QualifiedName, String>> parametersMap, Map<QualifiedName, String> minParameters,
            Map<Locale, Map<QualifiedName, String>> minParametersMap,
            FetchMode fetchMode, String alias,
            ScriptGroup group, boolean nativeAmd,
            List<Module> modules,
            Map<ResourceId, Set<DepInfo>> dependencies, Set<ResourceId> closure) {
        super(id, parameters, parametersMap, minParameters, minParametersMap);
        this.modules = Collections.unmodifiableList(new ArrayList<Module>(modules));
        LinkedHashMap<ResourceId, Set<DepInfo>> depsCopy = new LinkedHashMap<ResourceId, Set<DepInfo>>(dependencies);
        for (Entry<ResourceId, Set<DepInfo>> en : depsCopy.entrySet()) {
            en.setValue(Collections.unmodifiableSet(new LinkedHashSet<ScriptResource.DepInfo>(en.getValue())));
        }
        this.dependencies = Collections.unmodifiableMap(depsCopy);
        this.closure = Collections.unmodifiableSet(new HashSet<ResourceId>(closure));
        this.fetchMode = fetchMode;
        this.alias = alias;
        this.group = group;
        this.nativeAmd = nativeAmd;
    }

    public boolean isEmpty() {
        return modules.isEmpty();
    }

    public FetchMode getFetchMode() {
        return fetchMode;
    }

    public Set<ResourceId> getClosure() {
        return closure;
    }

    public List<Module> getModules() {
        return modules;
    }

    public int compareTo(ScriptResource o) {
        if (closure.contains(o.id)) {
            return 1;
        } else if (o.closure.contains(id)) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public Set<ResourceId> getDependencies() {
        return dependencies.keySet();
    }

    public Set<DepInfo> getDepInfo(ResourceId id) {
        return dependencies.get(id);
    }

    /**
     * If no alias was set, return the last part of the resource name If resourceID is null, return null
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "ScriptResource[id=" + id + "]";
    }

    public ScriptGroup getGroup() {
        return group;
    }

    /**
     * Returns {@code true} if this is an AMD resource. See the invocations of {@link #isNativeAmd()} in
     * {@link JavascriptConfigService#getScript(ResourceId, Locale)} to learn more about the purpose
     * of this method.
     */
    public boolean isNativeAmd() {
        return nativeAmd;
    }

    public static class DepInfo {
        private final String alias;
        private final String pluginRS;

        DepInfo(String alias, String pluginRS) {
            this.alias = alias;
            this.pluginRS = pluginRS;
        }

        public String getAlias() {
            return alias;
        }

        public String getPluginRS() {
            return pluginRS;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result + ((pluginRS == null) ? 0 : pluginRS.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || !(obj instanceof DepInfo))
                return false;
            DepInfo other = (DepInfo) obj;
            return ((alias == other.alias || alias != null && alias.equals(other.alias)) && (pluginRS == other.pluginRS || pluginRS != null
                    && pluginRS.equals(other.pluginRS)));
        }
    }

    /**
     * @param scriptGraphBuilder
     * @return
     */
    ScriptResourceBuilder newBuilder(ScriptGraphBuilder scriptGraphBuilder, ScriptGroupBuilder groupBuilder) {
        return new ScriptResourceBuilder(scriptGraphBuilder, id, fetchMode, alias, groupBuilder, nativeAmd);
    }
}
