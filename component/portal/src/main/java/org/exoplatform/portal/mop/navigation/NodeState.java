/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.core.util.AbstractAttributes;

/**
 * An immutable node state class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class NodeState implements Serializable {

    /** . */
    public static final NodeState INITIAL = new NodeState.Builder().build();

    /**
     * Builder class.
     */
    public static class Builder {

        /** . */
        private String label;

        /** . */
        private String icon;

        /** . */
        private long startPublicationTime;

        /** . */
        private long endPublicationTime;

        /** . */
        private Visibility visibility;

        /** . */
        private PageKey pageRef;

        /** . */
        private ImmutableAttributes attributes;

        public Builder() {
            this.icon = null;
            this.label = null;
            this.startPublicationTime = -1;
            this.endPublicationTime = -1;
            this.visibility = Visibility.DISPLAYED;
            this.pageRef = null;
            this.attributes = null;
        }

        /**
         * Creates a builder from a specified state.
         *
         * @param state the state to copy
         * @throws NullPointerException if the stateis null
         */
        public Builder(NodeState state) throws NullPointerException {
            if (state == null) {
                throw new NullPointerException();
            }
            this.label = state.label;
            this.icon = state.icon;
            this.startPublicationTime = state.startPublicationTime;
            this.endPublicationTime = state.endPublicationTime;
            this.visibility = state.visibility;
            this.pageRef = state.pageRef;
            this.attributes = state.attributes;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder startPublicationTime(long startPublicationTime) {
            this.startPublicationTime = startPublicationTime;
            return this;
        }

        public Builder endPublicationTime(long endPublicationTime) {
            this.endPublicationTime = endPublicationTime;
            return this;
        }

        public Builder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder pageRef(PageKey pageRef) {
            this.pageRef = pageRef;
            return this;
        }

        public Builder attributes(ImmutableAttributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public NodeState build() {
            return new NodeState(label, icon, startPublicationTime, endPublicationTime, visibility, pageRef, attributes);
        }
    }

    /**
     * TODO: A top level class in mop-core would probably be a better place for this. Needs to be
     * consulted with Julien.
     *
     * An immutable implementation of {@link org.gatein.mop.api.Attributes}. The underlying
     * {@link Map} is granted:
     * <ol>
     * <li>
     * Not to be changed once the given {@link ImmutableAttributes} is created.
     * <li>
     * Not to be exposed to any code able to change it outside this class.
     * </ol>
     * <p>
     * Note that no warranty can be given as for immutability of the attribute values.
     * <p>
     * How to create:
     * <pre>ImmutableAttributes myAttributes = new ImmutableAttributes.Builder()
     *         .attribute("key1", "value1")
     *         .attribute("key2", "value2")
     *         .build();</pre>
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    public static class ImmutableAttributes extends AbstractAttributes {

        public static final ImmutableAttributes EMPTY = new ImmutableAttributes(Collections.<String, Object> emptyMap());

        public static class Builder {

            public Builder() {
            }

            public Builder(Attributes attributes) {
                super();
                attributes(attributes);
            }

            public Builder(Map<? extends String, ? extends Object> attributes) {
                super();
                attributes(attributes);
            }

            private Map<String, Object> map;

            public Builder attribute(String key, Object value) {
                if (value == null) {
                    if (map != null) {
                        /* here we follow what is in SimpleAttributes */
                        map.remove(key);
                    }
                } else {
                    if (map == null) {
                        map = new HashMap<String, Object>();
                    }
                    map.put(key, value);
                }
                return this;
            }

            public Builder attributes(Map<? extends String, ? extends Object> attributes) {
                if (attributes != null) {
                    if (attributes.size() > 0) {
                        if (map == null) {
                            int attributesSize = attributes.size();
                            map = new HashMap<String, Object>(attributesSize + attributesSize / 2);
                        }
                        for (Map.Entry<? extends String, ? extends Object> en : attributes.entrySet()) {
                            if (en.getValue() == null) {
                                /* here we follow what is in SimpleAttributes */
                                map.remove(en.getKey());
                            } else {
                                map.put(en.getKey(), en.getValue());
                            }

                        }
                    }
                }
                return this;
            }

            public Builder attributes(Attributes attributes) {
                if (attributes != null) {
                    Set<String> keys = attributes.getKeys();
                    if (keys.size() > 0) {
                        if (map == null) {
                            int attributesSize = keys.size();
                            map = new HashMap<String, Object>(attributesSize + attributesSize / 2);
                        }
                        for (String key : attributes.getKeys()) {
                            Object value = attributes.getObject(key);
                            if (value == null) {
                                /* here we follow what is in SimpleAttributes */
                                map.remove(key);
                            } else {
                                map.put(key, value);
                            }

                        }
                    }
                }
                return this;
            }

            public ImmutableAttributes build() {
                return new ImmutableAttributes(map);
            }
        }

        private final Set<String> keys;
        private final Map<String, Object> map;

        /**
         * @param map
         */
        private ImmutableAttributes(Map<String, Object> map) {
            if (map == null) {
                this.map = Collections.<String, Object> emptyMap();
                this.keys = Collections.<String> emptySet();
            } else {
                this.map = map;
                this.keys = Collections.unmodifiableSet(map.keySet());
            }
        }

        @Override
        public final Set<String> getKeys() {
            return keys;
        }

        @Override
        protected final void set(String name, Object o) {
            throw new UnsupportedOperationException(this.getClass().getSimpleName()
                    + " cannot be changed by design.");
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (obj.getClass() == this.getClass()) {
                ImmutableAttributes other = (ImmutableAttributes) obj;
                return other.map == this.map || (other.map != null && other.map.equals(this.map));
            }
            return false;
        }

        @Override
        public String toString() {
            return map.toString();
        }

        protected Object get(String name) {
            return map.get(name);
        }
    }

    /** . */
    private final String label;

    /** . */
    private final String icon;

    /** . */
    private final long startPublicationTime;

    /** . */
    private final long endPublicationTime;

    /** . */
    private final Visibility visibility;

    /** . */
    private final PageKey pageRef;

    /** . */
    private final ImmutableAttributes attributes;

    public NodeState(String label, String icon, long startPublicationTime, long endPublicationTime, Visibility visibility,
            PageKey pageRef, ImmutableAttributes attributes) {
        this.label = label;
        this.icon = icon;
        this.startPublicationTime = startPublicationTime;
        this.endPublicationTime = endPublicationTime;
        this.visibility = visibility;
        this.pageRef = pageRef;
        this.attributes = attributes;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public long getStartPublicationTime() {
        return startPublicationTime;
    }

    Date getStartPublicationDate() {
        return startPublicationTime != -1 ? new Date(startPublicationTime) : null;
    }

    public long getEndPublicationTime() {
        return endPublicationTime;
    }

    Date getEndPublicationDate() {
        return endPublicationTime != -1 ? new Date(endPublicationTime) : null;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public PageKey getPageRef() {
        return pageRef;
    }

    public ImmutableAttributes getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NodeState) {
            NodeState that = (NodeState) o;
            return Safe.equals(label, that.label) && Safe.equals(icon, that.icon)
                    && Safe.equals(startPublicationTime, that.startPublicationTime)
                    && Safe.equals(endPublicationTime, that.endPublicationTime) && Safe.equals(visibility, that.visibility)
                    && Safe.equals(pageRef, that.pageRef) && Safe.equals(attributes, that.attributes);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeState[label=" + label + ",icon=" + icon + ",startPublicationTime=" + startPublicationTime
                + ",endPublicationTime=" + endPublicationTime + ",visibility=" + visibility + ",pageRef=" + pageRef
                + ",attributes=" + attributes + "]";
    }

    public Builder builder() {
        return new Builder(this);
    }
}
