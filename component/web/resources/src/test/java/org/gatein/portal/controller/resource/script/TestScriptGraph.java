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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.search.Similarity;
import org.exoplatform.component.test.BaseGateInTest;
import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.DuplicateResourceKeyException;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.common.util.Tools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestScriptGraph extends BaseGateInTest {
    private static final String CONTEXT_PATH_1 = "/my-app-1";
    private static final String CONTEXT_PATH_2 = "/my-app-2";

    /** . */
    private static final ResourceId A = new ResourceId(ResourceScope.SHARED, "A");

    /** . */
    private static final ResourceId B = new ResourceId(ResourceScope.SHARED, "B");

    /** . */
    private static final ResourceId C = new ResourceId(ResourceScope.SHARED, "C");

    /** . */
    private static final ResourceId D = new ResourceId(ResourceScope.PORTAL, "D");

    private static ResourceId id(ResourceScope scope) {
        return new ResourceId(scope, "test_"+ scope.name());
    }

    private static ScriptResourceDescriptor immediate(ResourceId rid) {
        return new ScriptResourceDescriptor(rid, FetchMode.IMMEDIATE);
    }
    private static ScriptResourceDescriptor onLoad(ResourceId rid) {
        return new ScriptResourceDescriptor(rid, FetchMode.ON_LOAD);
    }

    private static ScriptResourceDescriptor addDep(ScriptResourceDescriptor desc, ResourceId depId) {
        DependencyDescriptor dependency = new DependencyDescriptor(depId, null, null);
        desc.getDependencies().add(dependency);
        return desc;
    }

    public void testAddRemoveEmpty() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty();

        for (ResourceScope scope : ResourceScope.values()) {
            Collection<ScriptResource> scopeValues = initial.getResources(scope);
            assertEquals(0, scopeValues.size());
        }

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            ScriptGraph afterAdd = initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id)));

            Collection<ScriptResource> scopeValues = afterAdd.getResources(scope);
            assertEquals(1, scopeValues.size());

            assertEquals(id, afterAdd.getResource(id).getId());

            ScriptGraph afterRemoveEmpty = afterAdd.remove(CONTEXT_PATH_1, Collections.<ScriptResourceDescriptor> emptyList());
            assertSame(afterAdd, afterRemoveEmpty);
            ScriptGraph afterRemoveNull = afterAdd.remove(CONTEXT_PATH_1, null);
            assertSame(afterAdd, afterRemoveNull);

            ScriptGraph afterRemoveNonExistent = afterAdd.remove(CONTEXT_PATH_2, Arrays.asList(immediate(new ResourceId(scope, "non_exsistent_"+ scope.name()))));
            scopeValues = afterRemoveNonExistent.getResources(scope);
            assertEquals(1, scopeValues.size());
            assertEquals(id, afterAdd.getResource(id).getId());

            ScriptGraph afterRemove = afterAdd.remove(CONTEXT_PATH_1, Arrays.asList(immediate(id)));
            scopeValues = afterRemove.getResources(scope);
            assertEquals(0, scopeValues.size());
            assertNull(afterRemove.getResource(id));
        }
    }

    public void testSelfDependency() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty();
        ScriptGraph afterAdd = initial.add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), A)
        ));
        /* assert that self-dep has no effect */
        assertEquals(0, afterAdd.getResource(A).getDependencies().size());
        assertEquals(0, afterAdd.getResource(A).getClosure().size());
    }

    public void testDetectTwoNodeCycle() {
        ScriptGraph initial = ScriptGraph.empty();
        try {
            initial.add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    addDep(immediate(B), A)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
            expected.printStackTrace();
        }
    }

    public void testDetectThreeNodeCycle() {
        ScriptGraph initial = ScriptGraph.empty();
        try {
            initial.add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    addDep(immediate(B), C),
                    addDep(immediate(C), A)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }
    }

    public void testClosure() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(B), C),
                immediate(C)
        ));
        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        assertEquals(Tools.toSet(B, C), a.getClosure());
        assertEquals(Tools.toSet(C), b.getClosure());
        assertEquals(Collections.emptySet(), c.getClosure());
    }

    /**
     * Closure of any node depends on node relationships in graph but does not depend on the order of building graph nodes
     * @throws InvalidResourceException
     */
    public void testBuildingOrder() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(C), D),
                addDep(immediate(B), C),
                immediate(D)
        ));
        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);
        ScriptResource d = graph.getResource(D);

        assertEquals(Tools.toSet(D), c.getClosure());

        assertEquals(Tools.toSet(C, D), b.getClosure());

        assertEquals(Tools.toSet(B, C, D), a.getClosure());

        assertEquals(Collections.emptySet(), d.getClosure());
    }

    public void testFetchMode() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), C),
                    immediate(B),
                    immediate(C)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                onLoad(A),
                addDep(immediate(B), C),
                immediate(C)
        ));

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        Map<ScriptResource, FetchMode> resolution = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(resolution.keySet());
        assertEquals(1, resolution.size());
        assertEquals(Tools.toSet(a), resolution.keySet());

        //
        resolution = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(B, null));
        assertResultOrder(resolution.keySet());
        assertEquals(2, resolution.size());
        assertEquals(Tools.toSet(b, c), resolution.keySet());
        assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(c));

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        resolution = graph.resolve(pairs);
        assertResultOrder(resolution.keySet());
        assertEquals(3, resolution.size());
        assertEquals(Tools.toSet(a, b, c), resolution.keySet());
        assertEquals(FetchMode.ON_LOAD, resolution.get(a));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(c));
    }

    // ********

    public void testResolveDefaultOnLoadFetchMode() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(onLoad(A)));
        ScriptResource a = graph.getResource(A);

        // Use default fetch mode
        Map<ScriptResource, FetchMode> test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        // Get resource with with same fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.ON_LOAD));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        // Don't get resource with other fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.IMMEDIATE));
        assertEquals(0, test.size());
    }

    public void testResolveDefaultImmediateFetchMode() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(immediate(A)));
        ScriptResource a = graph.getResource(A);

        // Use default fetch mode
        Map<ScriptResource, FetchMode> test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));

        // Dont' get resource with other fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.ON_LOAD));
        assertEquals(0, test.keySet().size());

        // Get resource with the same fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.IMMEDIATE));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
    }

    public void testResolveDependency1() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    onLoad(B)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(A),
                onLoad(B)
        ));

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
        assertEquals(FetchMode.ON_LOAD, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        pairs.put(A, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
        assertEquals(FetchMode.ON_LOAD, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(b));
    }

    public void testResolveDependency2() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), B),
                    immediate(B)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                onLoad(A),
                immediate(B)
        ));

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));
        assertEquals(FetchMode.IMMEDIATE, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        pairs.put(A, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));
        assertEquals(FetchMode.IMMEDIATE, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(b));
    }

    public void testResolveDisjointDependencies() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), C),
                immediate(B),
                immediate(C)
        ));

        // Yes all permutations
        ResourceId[][] samples = { { A }, { A, B }, { B, A }, { A, B, C }, { A, C, B }, { B, A, C }, { B, C, A }, { C, A, B },
                { C, B, A }, };

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        for (ResourceId[] sample : samples) {
            pairs.clear();
            for (ResourceId id : sample) {
                pairs.put(id, null);
            }
            Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
            assertResultOrder(test.keySet());
        }
    }

    public void testCrossDependency() {
        // Scripts and Module can't depend on each other
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    onLoad(B)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), B),
                    immediate(B)
            ));
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

    }

    public void testDuplicateResource() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty();
        ResourceId shared = new ResourceId(ResourceScope.SHARED, "foo");

        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(shared)));
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(shared)));
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }

        ResourceId portlet = new ResourceId(ResourceScope.PORTLET, "foo");
        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portlet)));
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portlet)));
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }

        ResourceId portal = new ResourceId(ResourceScope.PORTAL, "foo");
        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portal)));
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portal)));
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
    }

    /**
     * Similar to {@link #testDuplicateResource()}.
     */
    public void testAddDuplicate() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty();

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            initial = initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id)));
        }

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            try {
                initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id)));
                fail("DuplicateResourceKeyException expected");
            } catch (DuplicateResourceKeyException expected) {
            }

            /* no change in initial */
            Collection<ScriptResource> scopeValues = initial.getResources(scope);
            assertEquals(1, scopeValues.size());

            ScriptResource found = initial.getResource(id);
            assertEquals(id, found.getId());
        }
    }

    /**
     * Test that each script of the test collection has no following script that belongs to its closure.
     *
     * @param test the test
     */
    private void assertResultOrder(Collection<ScriptResource> test) {
        ScriptResource[] array = test.toArray(new ScriptResource[test.size()]);
        for (int i = 0; i < array.length; i++) {
            ScriptResource resource = array[i];
            for (int j = i + 1; j < array.length; j++) {
                if (resource.getClosure().contains(array[j].getId()) && resource.getFetchMode().equals(array[j].getFetchMode())) {
                    failure("Was not expecting result order " + test, new Exception());
                }
            }
        }
    }
}
