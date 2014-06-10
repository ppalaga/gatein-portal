/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.exoplatform.web.controller.QualifiedName;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.script.ScriptGraph.ScriptGraphBuilder;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class ScriptGroup extends BaseScriptResource<ScriptGroup> {

    static class ScriptGroupBuilder extends BaseScriptResourceBuilder<ScriptResource> {

        private final Set<ResourceId> dependencies;
        private final String contextPath;
        private ScriptGroup result;

        ScriptGroupBuilder(ScriptGraphBuilder scriptGraphBuilder, ResourceId grpId, String contextPath) {
            super(grpId, scriptGraphBuilder);
            this.dependencies = new HashSet<ResourceId>();
            this.contextPath = contextPath;
        }

        String getContextPath() {
            return contextPath;
        }

        boolean hasDependencies() {
            return dependencies.isEmpty();
        }

        void addDependency(ResourceId id) {
            dependencies.add(id);
        }

        void removeDependency(ResourceId id) {
            dependencies.remove(id);
        }

        ScriptGroup build() {
            if (result == null) {
                result = new ScriptGroup(id, parameters, parametersMap, minParameters, minParametersMap, contextPath, dependencies);
            }
            return result;
        }
    }

    private final Set<ResourceId> scripts;
    private final String contextPath;

    private ScriptGroup(ResourceId id,
            Map<QualifiedName, String> parameters,
            Map<Locale, Map<QualifiedName, String>> parametersMap, Map<QualifiedName, String> minParameters,
            Map<Locale, Map<QualifiedName, String>> minParametersMap,
            String contextPath, Set<ResourceId> scripts) {
        super(id, parameters, parametersMap, minParameters, minParametersMap);
        this.scripts = Collections.unmodifiableSet(new HashSet<ResourceId>(scripts));
        this.contextPath = contextPath;
    }

    @Override
    public Set<ResourceId> getDependencies() {
        return scripts;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptGroup other = (ScriptGroup) obj;
        return getId().equals(other.getId());
    }

    ScriptGroupBuilder newBuilder(ScriptGraphBuilder scriptGraphBuilder) {
        return new ScriptGroupBuilder(scriptGraphBuilder, this.id, contextPath);
    }

}
