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
import org.gatein.mop.api.Attributes;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ApiNodeAttributes implements NodeAttributes {
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
    public Object get(Object key) {
        return context.getState().getAttributes().getObject((String) key);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        NodeState oldState = context.getState();
        Attributes oldAttributes = oldState.getAttributes();
        ImmutableAttributes newAttributes = new ImmutableAttributes.Builder(oldAttributes).attribute(key, value).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);
        return oldAttributes != null ? oldAttributes.getObject(key) : null;
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return put((String) key, null) ;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
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
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
