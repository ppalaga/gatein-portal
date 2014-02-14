/*
 * Copyright (C) 2009 eXo Platform SAS.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.exoplatform.commons.utils.I18N;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.Javascript.Remote;
import org.gatein.common.xml.XMLTools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.javascript.rhino.TokenStream;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptConfigParser {

    public static final String JAVA_SCRIPT_TAG = "javascript";

    public static final String JAVA_SCRIPT_PARAM = "param";

    public static final String JAVA_SCRIPT_MODULE = "js-module";

    public static final String JAVA_SCRIPT_PATH = "js-path";

    public static final String JAVA_SCRIPT_PRIORITY = "js-priority";

    public static final String JAVA_SCRIPT_PORTAL_NAME = "portal-name";

    public static final String LEGACY_JAVA_SCRIPT = "merged";

    /** . */
    public static final String SCRIPT_TAG = "script";

    public static final String SCRIPTS_TAG = "scripts";

    /** . */
    public static final String PORTLET_TAG = "portlet";

    /** . */
    public static final String PORTAL_TAG = "portal";

    /** . */
    public static final String RESOURCE_TAG = "resource";

    /** . */
    public static final String SCOPE_TAG = "scope";

    /** . */
    public static final String MODULE_TAG = "module";

    /** . */
    public static final String PATH_TAG = "path";

    /** . */
    public static final String DEPENDS_TAG = "depends";

    /** . */
    public static final String URL_TAG = "url";

    /** . */
    public static final String AS_TAG = "as";

    /** . */
    public static final String ADAPTER_TAG = "adapter";

    /** . */
    public static final String INCLUDE_TAG = "include";

    /** . */
    public static final String GROUP_TAG = "load-group";

    public static final String AMD_TAG = "amd";

    public static final String FILESET_TAG = "fileset";

    public static final String DIRECTORY_TAG = "directory";
    public static final String INCLUDES_TAG = "includes";
    public static final String EXCLUDE_TAG = "exclude";
    public static final String EXCLUDES_TAG = "excludes";

    /** . */
    private final ServletContext servletContext;
    private final String contextPath;

    private final Document document;

    private static final String SCRIPT_RESOURCE_DESCRIPTORS_ATTR = "gatein.script.resource.descriptors";

    private static final Log log = ExoLogger.getExoLogger(JavascriptConfigParser.class);

    private static final String[] PARSEABLE_SRIPT_TAGS = new String[] {JAVA_SCRIPT_TAG, MODULE_TAG, SCRIPTS_TAG, PORTLET_TAG, PORTAL_TAG};

    private static final String[] AMD_FILE_EXTENSIONS = new String[] {".js"};

    public JavascriptConfigParser(ServletContext servletContext, InputStream input) throws SAXException, IOException, ParserConfigurationException {
        this.servletContext = servletContext;
        this.contextPath = servletContext.getContextPath();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            this.document = docBuilder.parse(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void processConfigResource(InputStream is, JavascriptConfigService service, ServletContext scontext) throws SAXException, IOException, ParserConfigurationException {
        JavascriptConfigParser parser = new JavascriptConfigParser(scontext, is);
        List<ScriptResourceDescriptor> descriptors = parser.parseScripts();
        JavascriptTask task = new JavascriptTask();
        for (ScriptResourceDescriptor script : descriptors) {
            task.addDescriptor(script);
        }
        scontext.setAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR, Collections.unmodifiableList(descriptors));
        task.execute(service, scontext);
    }

    public static void unregisterResources(JavascriptConfigService service, ServletContext scontext) {
        @SuppressWarnings("unchecked")
        List<ScriptResourceDescriptor> descriptors = (List<ScriptResourceDescriptor>) scontext.getAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR);
        if (descriptors == null)
            return;

        JavascriptUnregisterTask task = new JavascriptUnregisterTask();
        for (ScriptResourceDescriptor script : descriptors) {
            task.addDescriptor(script);
        }
        task.execute(service, scontext);
        scontext.removeAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR);
    }

    public List<ScriptResourceDescriptor> parseScripts() {
        List<ScriptResourceDescriptor> result = new ArrayList<ScriptResourceDescriptor>();
        Element element = document.getDocumentElement();
        for (String tagName : PARSEABLE_SRIPT_TAGS) {
            for (Element childElt : XMLTools.getChildren(element, tagName)) {
                Collection<ScriptResourceDescriptor> descriptors = parseScripts(childElt);
                if (descriptors != null) {
                    result.addAll(descriptors);
                }
            }
        }
        parseAmd(element, result);
        return result;
    }

    private Collection<ScriptResourceDescriptor> parseScripts(Element element) {
        LinkedHashMap<ResourceId, ScriptResourceDescriptor> scripts = new LinkedHashMap<ResourceId, ScriptResourceDescriptor>();
        if (JAVA_SCRIPT_TAG.equals(element.getTagName())) {
            try {
                NodeList nodes = element.getElementsByTagName(JAVA_SCRIPT_PARAM);
                int length = nodes.getLength();
                for (int i = 0; i < length; i++) {
                    Element param_ele = (Element) nodes.item(i);
                    String js_path = param_ele.getElementsByTagName(JAVA_SCRIPT_PATH).item(0).getFirstChild().getNodeValue();

                    //
                    log.warn(
                            "<javascript> tag define for javascript: {} has ben deprecated, please use <scripts> or <module> instead",
                            js_path);

                    //
                    int priority;
                    try {
                        priority = Integer.valueOf(
                                param_ele.getElementsByTagName(JAVA_SCRIPT_PRIORITY).item(0).getFirstChild().getNodeValue())
                                .intValue();
                    } catch (Exception e) {
                        priority = Integer.MAX_VALUE;
                    }
                    String portalName = null;
                    try {
                        portalName = param_ele.getElementsByTagName(JAVA_SCRIPT_PORTAL_NAME).item(0).getFirstChild()
                                .getNodeValue();
                    } catch (Exception e) {
                        // portal-name is null
                    }

                    Javascript js;
                    if (portalName == null) {
                        js = Javascript.create(new ResourceId(ResourceScope.SHARED, LEGACY_JAVA_SCRIPT), js_path, contextPath,
                                priority);
                    } else {
                        js = Javascript
                                .create(new ResourceId(ResourceScope.PORTAL, portalName), js_path, contextPath, priority);
                    }

                    //
                    ScriptResourceDescriptor desc = scripts.get(js.getResource());
                    if (desc == null) {
                        scripts.put(js.getResource(),
                                desc = new ScriptResourceDescriptor(js.getResource(), FetchMode.IMMEDIATE));
                    }
                    desc.modules.add(js);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        } else if (PORTAL_TAG.equals(element.getTagName()) || PORTLET_TAG.equals(element.getTagName())) {
            String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
            ResourceScope resourceScope;
            if (PORTLET_TAG.equals(element.getTagName())) {
                resourceName = contextPath.substring(1) + "/" + resourceName;
                resourceScope = ResourceScope.PORTLET;
            } else {
                resourceScope = ResourceScope.PORTAL;
            }
            ResourceId id = new ResourceId(resourceScope, resourceName);
            FetchMode fetchMode;
            String group = null;

            Element resourceElt = XMLTools.getUniqueChild(element, MODULE_TAG, false);
            if (resourceElt != null) {
                fetchMode = FetchMode.ON_LOAD;
                if (XMLTools.getUniqueChild(resourceElt, URL_TAG, false) == null) {
                    group = parseGroup(resourceElt);
                }
            } else {
                resourceElt = XMLTools.getUniqueChild(element, SCRIPTS_TAG, false);
                fetchMode = FetchMode.IMMEDIATE;
            }

            if (resourceElt != null) {
                ScriptResourceDescriptor desc = scripts.get(id);
                if (desc == null) {
                    boolean isAmd = XMLTools.getUniqueChild(element, AMD_TAG, false) != null;
                    desc = new ScriptResourceDescriptor(id, fetchMode, parseOptString(element, AS_TAG), group, isAmd);
                } else {
                    desc.fetchMode = fetchMode;
                }
                parseDesc(resourceElt, desc);
                scripts.put(id, desc);
            }
        } else if (MODULE_TAG.equals(element.getTagName()) || SCRIPTS_TAG.equals(element.getTagName())) {
            String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
            ResourceId id = new ResourceId(ResourceScope.SHARED, resourceName);
            FetchMode fetchMode;
            String group = null;

            if (MODULE_TAG.equals(element.getTagName())) {
                fetchMode = FetchMode.ON_LOAD;
                if (XMLTools.getUniqueChild(element, URL_TAG, false) == null) {
                    group = parseGroup(element);
                }
            } else {
                fetchMode = FetchMode.IMMEDIATE;
            }

            ScriptResourceDescriptor desc = scripts.get(id);
            if (desc == null) {
                boolean isAmd = XMLTools.getUniqueChild(element, AMD_TAG, false) != null;
                desc = new ScriptResourceDescriptor(id, fetchMode, parseOptString(element, AS_TAG), group, isAmd);
            }
            parseDesc(element, desc);

            scripts.put(id, desc);
        } else {
            // ???
        }

        //
        return scripts.values();
    }

    private void parseDesc(Element element, ScriptResourceDescriptor desc) {
        Element urlElement = XMLTools.getUniqueChild(element, URL_TAG, false);
        if (urlElement != null) {
            String remoteURL = XMLTools.asString(urlElement);
            desc.id.setFullId(false);
            Remote script = new Javascript.Remote(desc.id, contextPath, remoteURL, 0);
            desc.modules.add(script);
        } else {
            for (Element localeElt : XMLTools.getChildren(element, "supported-locale")) {
                String localeValue = XMLTools.asString(localeElt);
                Locale locale = I18N.parseTagIdentifier(localeValue);
                desc.supportedLocales.add(locale);
            }
            for (Element scriptElt : XMLTools.getChildren(element, SCRIPT_TAG)) {
                String resourceBundle = parseOptString(scriptElt, "resource-bundle");

                List<Content> contents = new LinkedList<Content>();
                Element adapter = XMLTools.getUniqueChild(scriptElt, ADAPTER_TAG, false);
                String scriptPath = parseOptString(scriptElt, "path");
                if (scriptPath != null) {
                    contents.add(new Content(scriptPath));
                } else if (adapter != null) {
                    NodeList childs = adapter.getChildNodes();
                    for (int i = 0; i < childs.getLength(); i++) {
                        Node item = childs.item(i);
                        if (item instanceof Element) {
                            Element include = (Element) item;
                            if (INCLUDE_TAG.equals(include.getTagName())) {
                                contents.add(new Content(XMLTools.asString(include, true)));
                            }
                        } else if (item.getNodeType() == Node.TEXT_NODE) {
                            contents.add(new Content(item.getNodeValue().trim(), false));
                        }
                    }
                }
                Content[] tmp = contents.toArray(new Content[contents.size()]);

                //
                Javascript script = new Javascript.Local(desc.id, contextPath, tmp, resourceBundle, 0);
                desc.modules.add(script);
            }
        }
        for (Element moduleElt : XMLTools.getChildren(element, "depends")) {
            Element dependencyElt = XMLTools.getUniqueChild(moduleElt, "module", false);
            if (dependencyElt == null) {
                dependencyElt = XMLTools.getUniqueChild(moduleElt, "scripts", false);
            }
            ResourceId resourceId = new ResourceId(ResourceScope.SHARED, XMLTools.asString(dependencyElt));
            DependencyDescriptor dependency = new DependencyDescriptor(resourceId, parseOptString(moduleElt, AS_TAG),
                    parseOptString(moduleElt, RESOURCE_TAG));
            desc.dependencies.add(dependency);
        }
    }

    private String parseGroup(Element element) {
        Element group = XMLTools.getUniqueChild(element, GROUP_TAG, false);
        if (group != null) {
            String grpName = XMLTools.asString(group, true);
            if (grpName.isEmpty()) {
                grpName = null;
            }
            return grpName;
        } else {
            return null;
        }
    }

    private String parseOptString(Element element, String childTag) {
        Element childElt = XMLTools.getUniqueChild(element, childTag, false);
        return childElt == null ? null : XMLTools.asString(childElt, true);
    }

    private String[] parseCludes(Element filesetElement, String cludesTag, String cludeTag) {
        Element cludesElement = XMLTools.getUniqueChild(filesetElement, cludesTag, false);
        if (cludesElement != null) {
            List<Element> cludeElements = XMLTools.getChildren(cludesElement, cludeTag);
            List<String> result = new ArrayList<String>(cludeElements.size());
            for (Element cludeElement : cludeElements) {
                result.add(XMLTools.asString(cludeElement, true));
            }
            return result.toArray(new String[result.size()]);
        } else {
            return null;
        }

    }

    private void parseAmd(Element documentElement, List<ScriptResourceDescriptor> result) {
        Element amd = XMLTools.getUniqueChild(documentElement, AMD_TAG, false);
        if (amd != null) {
            for (Element fileset : XMLTools.getChildren(amd, FILESET_TAG)) {
                Element dirElement = XMLTools.getUniqueChild(fileset, DIRECTORY_TAG, true);
                String dir = XMLTools.asString(dirElement, true);
                if (dir.charAt(dir.length() - 1) != AmdResourceScanner.FILE_SEPARATOR) {
                    dir = dir + '/';
                }

                String[] includes = parseCludes(fileset, INCLUDES_TAG, INCLUDE_TAG);
                String[] excludes = parseCludes(fileset, EXCLUDES_TAG, EXCLUDE_TAG);
                List<String> amdFiles = new AmdResourceScanner(dir, includes, excludes, AMD_FILE_EXTENSIONS, servletContext).scan();
                for (String amdFile : amdFiles) {
                    /* if dir is somethig like /js/amd and amdFile is something like /js/amd/package/mymodule.js
                     * then fqModuleName will be package/mymodule */
                    String fqModuleName = amdFile.substring(dir.length() + 1, amdFile.length() - 3);

                    String alias = toModuleAlias(fqModuleName);
                    ScriptResourceDescriptor d = new ScriptResourceDescriptor(
                            new ResourceId(ResourceScope.SHARED, fqModuleName),
                            FetchMode.ON_LOAD, alias, null, true);
                    Javascript js = Javascript.create(
                            new ResourceId(ResourceScope.SHARED, LEGACY_JAVA_SCRIPT),
                            amdFile, contextPath, Integer.MAX_VALUE);
                    d.modules.add(js);

                    result.add(d);
                }

            }
        }
    }

    private static String toModuleAlias(String fqModulePath) {
        StringBuilder result = new StringBuilder(fqModulePath.length());
        boolean nextUpper = false;
        int len = fqModulePath.length();
        for (int i = 0; i < len; i++) {
            char ch = fqModulePath.charAt(i);
            while (result.length() == 0 ? !Character.isJavaIdentifierStart(ch) : !Character.isJavaIdentifierPart(ch)) {
                i++;
                if (i >= len) {
                    return result.toString();
                }
                ch = fqModulePath.charAt(i);
                nextUpper = true;
            }
            result.append(nextUpper ? Character.toUpperCase(ch) : ch);
            nextUpper = false;
        }
        String strResult = result.toString();
        if (TokenStream.isKeyword(strResult)) {
            return result.append('_').toString();
        } else {
            return strResult;
        }
    }


    private static class AmdResourceScanner {

        private static final char FILE_SEPARATOR = '/';
        private final String directory;
        private final ServletContext servletContext;
        private final IncludeExcludeFileSelector selector;
        private final String[] fileExtensions;

        /**
         * @param directory
         * @param includes
         * @param excludes
         * @param servletContext
         */
        public AmdResourceScanner(String directory, String[] includes, String[] excludes, String[] fileExtensions, ServletContext servletContext) {
            super();
            IncludeExcludeFileSelector sel = new IncludeExcludeFileSelector();
            sel.setIncludes(includes);
            sel.setExcludes(excludes);
            this.selector = sel;
            this.fileExtensions = fileExtensions;
            this.directory = directory;
            this.servletContext = servletContext;
        }

        public List<String> scan() {
            List<String> result = new ArrayList<String>();
            return scanDirectory(directory, result);
        }

        /**
         * @param directory
         * @return
         */
        private List<String> scanDirectory(String directory, List<String> result) {
            @SuppressWarnings("unchecked")
            Set<String> paths = servletContext.getResourcePaths(directory);
            if (paths != null && paths.size() > 0) {
                for (String path : paths) {
                    if (isDirectory(path)) {
                        scanDirectory(path, result);
                    } else if (matchesExtensions(path)) {
                        String relName = path.substring(this.directory.length());
                        FileInfo fileInfo = new SimpleFileInfo(relName, true);
                        try {
                            if (selector.isSelected(fileInfo)) {
                                result.add(path);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Could not filter path '"+ path +"'", e);
                        }
                    }
                }
            }
            return result;
        }

        /**
         * @param path
         * @return
         */
        private boolean matchesExtensions(String path) {
            for (String ext : fileExtensions) {
                if (path.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param path
         * @return
         */
        private boolean isDirectory(String path) {
            return path.charAt(path.length() - 1) == FILE_SEPARATOR;
        }

    }

    private static class SimpleFileInfo implements FileInfo {

        /**
         * @param name
         * @param isFile
         */
        public SimpleFileInfo(String name, boolean isFile) {
            super();
            this.name = name;
            this.isFile = isFile;
        }

        private final String name;
        private final boolean isFile;

        /**
         * @see org.codehaus.plexus.components.io.fileselectors.FileInfo#getName()
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Unsupported in this implementation. Always throws a {@link UnsupportedOperationException}.
         *
         * @see org.codehaus.plexus.components.io.fileselectors.FileInfo#getContents()
         */
        @Override
        public InputStream getContents() throws IOException {
            throw new UnsupportedOperationException("FileInfo.getContents() unsupported in "+ this.getClass().getName());
        }

        /**
         * @see org.codehaus.plexus.components.io.fileselectors.FileInfo#isFile()
         */
        @Override
        public boolean isFile() {
            return isFile;
        }

        /**
         * @see org.codehaus.plexus.components.io.fileselectors.FileInfo#isDirectory()
         */
        @Override
        public boolean isDirectory() {
            return !isFile;
        }

    }

}
