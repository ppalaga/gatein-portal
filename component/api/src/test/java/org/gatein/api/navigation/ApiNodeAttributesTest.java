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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.gatein.api.AbstractApiTest;
import org.gatein.api.common.AttributesTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ApiNodeAttributesTest extends AbstractApiTest {

    private static final Logger log = Logger.getLogger(ApiNodeAttributesTest.class.getName());

    private Navigation navigation;
    private Node root;

    /**
     * Runs tests from {@link AttributesTest} on an {@link ApiNodeAttributes} instance returned
     * from {@link Node#getAttributes()}.
     */
    @Test
    public void delegateTests() {
        AttributesTest delegate = new AttributesTest() {
            private int i = 0;
            @Override
            public void before() {
                /* create fresh attributes and put them into AttributesTest to run a testcase on it */
                this.attributes = root.addChild("ch"+ i++).getAttributes();
            }
        };
        Class<? extends AttributesTest> delegateClass = delegate.getClass();

        for (Method method : delegateClass.getMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                try {
                    log.info("About to run delegated "+ AttributesTest.class.getName() +"."+ method.getName());
                    delegate.before();
                    method.invoke(delegate, new Object[0]);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(AttributesTest.class.getName() +"."+ method.getName() +"() failed", e);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(AttributesTest.class.getName() +"."+ method.getName() +"() failed", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(AttributesTest.class.getName() +"."+ method.getName() +"() failed", e);
                }
            }
        }
    }

    @Before
    public void before() throws Exception {
        super.before();

        createSite(defaultSiteId);

        navigation = portal.getNavigation(defaultSiteId);
        root = getRoot(true);
    }

    public Node getRoot(boolean expanded) {
        return navigation.getRootNode(expanded ? Nodes.visitChildren() : Nodes.visitNone());
    }

}
