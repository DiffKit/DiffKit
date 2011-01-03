/**
 * Copyright 2010 Joseph Panico
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.diffkit.util.tst;

import groovy.util.GroovyTestCase;
import junit.framework.Assert;

import org.diffkit.util.DKArrayUtil;

/**
 * @author jpanico
 */
public class JTestArrayUtil extends GroovyTestCase {

   public void testRemoveMe() {
      char var = 'A' + 1;
      System.out.println("var->" + var);
      System.out.println("A+B->" + ("" + 'A' + 'B' + '\0' + 'C'));
   }

   public void testIndicesOfIntersection() {
      String[] source = new String[] { "beware", "the", "jabberwocky" };
      String[] target = null;
      Assert.assertNull(DKArrayUtil.getIndicesOfIntersection(source, target));
      Assert.assertNull(DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[0];
      GroovyTestCase.assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(source, target));
      GroovyTestCase.assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "ack", "awk", "hack" };
      GroovyTestCase.assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(source, target));
      GroovyTestCase.assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "beware", "the", "jabberwocky" };
      GroovyTestCase.assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));
      GroovyTestCase.assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "jabberwocky", "the", "beware" };
      GroovyTestCase.assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));
      GroovyTestCase.assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "jabberwocky" };
      GroovyTestCase.assertEquals(new int[] { 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));

      target = new String[] { "jabberwocky", "ack" };
      GroovyTestCase.assertEquals(new int[] { 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));

   }

   public void testRetain() {
      String[] target = new String[] { "beware", "the", "jabberwocky" };

      Assert.assertNull(DKArrayUtil.retainElementsAtIndices(null, null));
      Assert.assertNull(DKArrayUtil.retainElementsAtIndices(target, null));
      Assert.assertNull(DKArrayUtil.retainElementsAtIndices(null, new int[] { 2 }));
      Assert.assertNull(DKArrayUtil.retainElementsAtIndices(null, new int[0]));
      Assert.assertNull(DKArrayUtil.retainElementsAtIndices(target, new int[0]));
      GroovyTestCase.assertEquals(target,
         DKArrayUtil.retainElementsAtIndices(target, new int[] { 0, 1, 2 }));
      GroovyTestCase.assertEquals(new String[] { "the" },
         DKArrayUtil.retainElementsAtIndices(target, new int[] { 1 }));

   }

   public void testIntersection() {
      String[] source = new String[] { "beware", "the", "jabberwocky" };
      String[] target = null;
      Assert.assertNull(DKArrayUtil.getIntersection(source, target));
      Assert.assertNull(DKArrayUtil.getIntersection(target, source));

      target = new String[0];
      GroovyTestCase.assertEquals(new String[0],
         DKArrayUtil.getIntersection(source, target));
      GroovyTestCase.assertEquals(new String[0],
         DKArrayUtil.getIntersection(target, source));

      target = new String[] { "ack", "awk", "hack" };
      GroovyTestCase.assertEquals(new String[0],
         DKArrayUtil.getIntersection(source, target));
      GroovyTestCase.assertEquals(new String[0],
         DKArrayUtil.getIntersection(target, source));

      target = new String[] { "beware", "the", "jabberwocky" };
      GroovyTestCase.assertEquals(source, DKArrayUtil.getIntersection(source, target));
      GroovyTestCase.assertEquals(source, DKArrayUtil.getIntersection(target, source));

      target = new String[] { "jabberwocky", "the", "beware" };
      GroovyTestCase.assertEquals(source, DKArrayUtil.getIntersection(source, target));
      GroovyTestCase.assertEquals(target, DKArrayUtil.getIntersection(target, source));

      target = new String[] { "jabberwocky" };
      GroovyTestCase.assertEquals(target, DKArrayUtil.getIntersection(source, target));

      target = new String[] { "jabberwocky", "ack" };
      GroovyTestCase.assertEquals(new String[] { "jabberwocky" },
         DKArrayUtil.getIntersection(source, target));

   }

   public void testSubarray() {
      String[] array = new String[] { "beware", "the", "jabberwocky" };
      this.assertArrayEquals(array, DKArrayUtil.subarray(array, 0, array.length));
      this.assertArrayEquals(new String[] { "beware", "the" },
         DKArrayUtil.subarray(array, 0, 2));
      this.assertArrayEquals(new String[] { "the", "jabberwocky" },
         DKArrayUtil.subarray(array, 1, 3));
   }

   public void testCreateArray() {
      String[] array = DKArrayUtil.createArray(String.class, 5);
      Assert.assertNotNull(array);
      try {
         String[] oArray = (String[]) DKArrayUtil.createArray(Object.class, 5);
         fail("Should raise an ClassCastException");
      }
      catch (ClassCastException e) {
      }
   }
}
