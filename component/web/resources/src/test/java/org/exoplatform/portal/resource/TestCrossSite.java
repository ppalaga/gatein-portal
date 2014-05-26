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

package org.exoplatform.portal.resource;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestCrossSite extends TestCase {

    private static final int EXPERIMENT_COUNT = 10;
    private static final int ITERATTIONS_COUNT = 100000;

    public void testIsCrossSiteUrl() {
        assertFalse(SkinService.isCrossSiteUrl("bla"));
        assertFalse(SkinService.isCrossSiteUrlNaive("bla"));
        for (int j = 0; j < EXPERIMENT_COUNT; j++) {
            long start = System.currentTimeMillis();

            for (int i = 0; i < ITERATTIONS_COUNT; i++) {
                assertFalse(SkinService.isCrossSiteUrlNaive(null));
                assertFalse(SkinService.isCrossSiteUrlNaive(""));
                assertFalse(SkinService.isCrossSiteUrlNaive("/"));
                assertFalse(SkinService.isCrossSiteUrlNaive("/foo"));
                assertFalse(SkinService.isCrossSiteUrlNaive("/foo/bar/baz"));
                assertFalse(SkinService.isCrossSiteUrlNaive("foo/bar/baz"));
                assertFalse(SkinService.isCrossSiteUrlNaive("foo-bar-baz"));
                assertFalse(SkinService.isCrossSiteUrlNaive("http"));
                assertFalse(SkinService.isCrossSiteUrlNaive("https"));

                assertTrue(SkinService.isCrossSiteUrlNaive("//"));
                assertTrue(SkinService.isCrossSiteUrlNaive("//foo"));
                assertTrue(SkinService.isCrossSiteUrlNaive("//foo/bar/baz"));
                assertTrue(SkinService.isCrossSiteUrlNaive("http:"));
                assertTrue(SkinService.isCrossSiteUrlNaive("http://foo"));
                assertTrue(SkinService.isCrossSiteUrlNaive("http://foo/bar/baz"));
                assertTrue(SkinService.isCrossSiteUrlNaive("https:"));
                assertTrue(SkinService.isCrossSiteUrlNaive("https://foo"));
                assertTrue(SkinService.isCrossSiteUrlNaive("https://foo/bar/baz"));
            }

            long naiveDuration = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            for (int i = 0; i < ITERATTIONS_COUNT; i++) {
                assertFalse(SkinService.isCrossSiteUrl(null));
                assertFalse(SkinService.isCrossSiteUrl(""));
                assertFalse(SkinService.isCrossSiteUrl("/"));
                assertFalse(SkinService.isCrossSiteUrl("/foo"));
                assertFalse(SkinService.isCrossSiteUrl("/foo/bar/baz"));
                assertFalse(SkinService.isCrossSiteUrl("foo/bar/baz"));
                assertFalse(SkinService.isCrossSiteUrl("foo-bar-baz"));
                assertFalse(SkinService.isCrossSiteUrl("http"));
                assertFalse(SkinService.isCrossSiteUrl("https"));

                assertTrue(SkinService.isCrossSiteUrl("//"));
                assertTrue(SkinService.isCrossSiteUrl("//foo"));
                assertTrue(SkinService.isCrossSiteUrl("//foo/bar/baz"));
                assertTrue(SkinService.isCrossSiteUrl("http:"));
                assertTrue(SkinService.isCrossSiteUrl("http://foo"));
                assertTrue(SkinService.isCrossSiteUrl("http://foo/bar/baz"));
                assertTrue(SkinService.isCrossSiteUrl("https:"));
                assertTrue(SkinService.isCrossSiteUrl("https://foo"));
                assertTrue(SkinService.isCrossSiteUrl("https://foo/bar/baz"));
            }
            long duration = System.currentTimeMillis() - start;

            System.out.println(""+ naiveDuration +"/"+ duration +" = "+ ((double) naiveDuration / (double) duration));
        }

    }


}
