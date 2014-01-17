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

package org.gatein.api.navigation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.ImmutableAttributes;
import org.gatein.api.internal.Parameters;
import org.gatein.mop.api.Attributes;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ApiNodeAttributes extends org.gatein.api.common.Attributes {
    transient NodeContext<ApiNode> context;

    /**
     * @param attributes
     */
    public ApiNodeAttributes(NodeContext<ApiNode> context) {
        super();
        this.context = context;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return context.getState().getAttributes().getKeys().size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return context.getState().getAttributes().getKeys().isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return context.getState().getAttributes().getKeys().contains(key);
    }

    /**
     * Sequential search.
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        for (String key : context.getState().getAttributes().getKeys()) {
            if (context.getState().getAttributes().getObject(key) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        return context.getState().getAttributes().getString((String) key);
    }



    /**
     * @see org.gatein.api.common.Attributes#get(org.gatein.api.common.Attributes.Key)
     */
    @Override
    public <T> T get(Key<T> key) {
        Parameters.requireNonNull(key, "key");
        String name = key.getName();
        Object value = context.getState().getAttributes().getObject(name);
        if (value == null) {
            return null;
        } else {
            Class<T> type = key.getType();
            if (type == value.getClass()) {
                return type.cast(value);
            } else if (value instanceof String) {
                /* fallback to fromString transformation for entries that came as strings
                 * via org.exoplatform.portal.config.model.Properties */
                return fromString(type, (String) value);
            } else {
                throw new IllegalArgumentException("Cannot cast '"+ value.getClass().getName() +"' value to '" + type +"' for attribute key '"+ name +"'");
            }
        }
    }

    /**
     * @see org.gatein.api.common.Attributes#put(org.gatein.api.common.Attributes.Key, java.lang.Object)
     */
    @Override
    public <T> T put(Key<T> key, T value) {
        Parameters.requireNonNull(key, "key");
        if (!key.getType().equals(value.getClass())) {
            throw new IllegalArgumentException("Value class is not the same as key type");
        }

        T oldValue = get(key);

        NodeState oldState = context.getState();
        Attributes oldAttributes = oldState.getAttributes();
        ImmutableAttributes newAttributes = new ImmutableAttributes.Builder(oldAttributes).attribute(key.getName(), value).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);

        return oldValue;
    }

    /**
     * @see org.gatein.api.common.Attributes#remove(org.gatein.api.common.Attributes.Key)
     */
    @Override
    public <T> T remove(Key<T> key) {
        return put(key, null);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        NodeState oldState = context.getState();
        Attributes oldAttributes = oldState.getAttributes();
        ImmutableAttributes newAttributes = new ImmutableAttributes.Builder(oldAttributes).attribute(key, value).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);
        return oldAttributes != null ? oldAttributes.getString(key) : null;
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        return put((String) key, null) ;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        NodeState oldState = context.getState();
        Attributes oldAttributes = oldState.getAttributes();
        ImmutableAttributes newAttributes = new ImmutableAttributes.Builder(oldAttributes).attributes(m).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        NodeState oldState = context.getState();
        NodeState newState = new NodeState.Builder(oldState).attributes(ImmutableAttributes.EMPTY).build();
        context.setState(newState);
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return context.getState().getAttributes().getKeys();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
