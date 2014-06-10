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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.portal.controller.resource.Resource;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.ScriptGraph.ScriptGraphBuilder;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @author <a href="mailto:ppalaga@redhat.com>Peter Palaga</a>
 */
public abstract class BaseScriptResource<R extends Resource<R>> extends Resource<R> {

    public static class BaseScriptResourceBuilder<R extends Resource<R>> {
        protected ScriptGraphBuilder scriptGraphBuilder;
        protected ResourceId id;
        protected Map<QualifiedName, String> parameters;
        protected Map<Locale, Map<QualifiedName, String>> parametersMap;
        protected Map<QualifiedName, String> minParameters;
        protected Map<Locale, Map<QualifiedName, String>> minParametersMap;

        public BaseScriptResourceBuilder(ResourceId id, ScriptGraphBuilder scriptGraphBuilder) {
            super();
            this.id = id;
            this.scriptGraphBuilder = scriptGraphBuilder;

            //
            Map<QualifiedName, String> parameters = createBaseParameters(id.getScope(), id.getName());

            //
            Map<QualifiedName, String> minifiedParameters = new HashMap<QualifiedName, String>(parameters);
            minifiedParameters.put(ResourceRequestHandler.COMPRESS_QN, "min");

            //
            this.parameters = parameters;
            this.minParameters = minifiedParameters;
            this.parametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
            this.minParametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
        }

        void addSupportedLocale(Locale locale) {
            if (!parametersMap.containsKey(locale)) {
                Map<QualifiedName, String> localizedParameters = new HashMap<QualifiedName, String>(parameters);
                localizedParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
                parametersMap.put(locale, localizedParameters);
                Map<QualifiedName, String> localizedMinParameters = new HashMap<QualifiedName, String>(minParameters);
                localizedMinParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
                minParametersMap.put(locale, localizedMinParameters);
            }
        }

    }

    /**
     * This is quite closely tied to what is set in {@code controller.xml}.
     * @return
     */
    public static Map<QualifiedName, String> createBaseParameters(ResourceScope scope, String resourceName) {
        Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
        parameters.put(WebAppController.HANDLER_PARAM, ResourceRequestHandler.SCRIPT_HANDLER_NAME);
        parameters.put(ResourceRequestHandler.COMPRESS_QN, "");
        parameters.put(ResourceRequestHandler.VERSION_QN, ResourceRequestHandler.VERSION);
        parameters.put(ResourceRequestHandler.LANG_QN, "");
        parameters.put(ResourceRequestHandler.RESOURCE_QN, resourceName);
        parameters.put(ResourceRequestHandler.SCOPE_QN, scope.name());
        return parameters;
    }

    /** . */
    private final Map<QualifiedName, String> parameters;

    /** . */
    private final Map<Locale, Map<QualifiedName, String>> parametersMap;

    /** . */
    private final Map<QualifiedName, String> minParameters;

    /** . */
    private final Map<Locale, Map<QualifiedName, String>> minParametersMap;

    protected BaseScriptResource(ResourceId id, Map<QualifiedName, String> parameters,
            Map<Locale, Map<QualifiedName, String>> parametersMap, Map<QualifiedName, String> minParameters,
            Map<Locale, Map<QualifiedName, String>> minParametersMap) {
        super(id);
        this.parameters = Collections.unmodifiableMap(new HashMap<QualifiedName, String>(parameters));
        this.parametersMap = Collections.unmodifiableMap(new HashMap<Locale, Map<QualifiedName, String>>(parametersMap));
        this.minParameters = Collections.unmodifiableMap(new HashMap<QualifiedName, String>(minParameters));
        this.minParametersMap = Collections.unmodifiableMap(new HashMap<Locale, Map<QualifiedName, String>>(minParametersMap));
    }

    public Map<QualifiedName, String> getParameters(boolean minified, Locale locale) {
        Map<Locale, Map<QualifiedName, String>> map = minified ? minParametersMap : parametersMap;
        for (Locale current = locale; current != null; current = I18N.getParent(current)) {
            Map<QualifiedName, String> ret = map.get(locale);
            if (ret != null) {
                return ret;
            }
        }
        return minified ? minParameters : parameters;
    }

}
