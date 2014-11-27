package org.apache.lucene.facet.collections;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.apache.lucene.facet.FacetTestCase;
import org.apache.lucene.facet.collections.IntIterator;
import org.apache.lucene.facet.collections.ObjectToIntMap;
import org.junit.Test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ObjectToIntMapTest extends FacetTestCase {

  @Test
  public void test0() {
    ObjectToIntMap<Integer> map = new ObjectToIntMap<Integer>();

    assertIntegerMaxValue(map.get(0));
    
    for (int i = 0; i < 100; ++i) {
      int value = 100 + i;
      assertFalse(map.containsValue(value));
      map.put(i, value);
      assertTrue(map.containsValue(value));
      assertNotIntegerMaxValue(map.get(i));
    }

    assertEquals(100, map.size());
    for (int i = 0; i < 100; ++i) {
      assertTrue(map.containsKey(i));
      assertEquals(100 + i, map.get(i), 1E-5);

    }

    for (int i = 10; i < 90; ++i) {
      map.remove(i);
      assertIntegerMaxValue(map.get(i));
    }

    assertEquals(20, map.size());
    for (int i = 0; i < 100; ++i) {
      assertEquals(map.containsKey(i), !(i >= 10 && i < 90));
    }

    for (int i = 5; i < 85; ++i) {
      map.put(i, Integer.valueOf(5 + i));
    }
    assertEquals(95, map.size());
    for (int i = 0; i < 100; ++i) {
      assertEquals(map.containsKey(i), !(i >= 85 && i < 90));
    }
    for (int i = 0; i < 5; ++i) {
      assertEquals(map.get(i), (100 + i), 1E-5);
    }
    for (int i = 5; i < 85; ++i) {
      assertEquals(map.get(i), (5 + i), 1E-5);
    }
    for (int i = 90; i < 100; ++i) {
      assertEquals(map.get(i), (100 + i), 1E-5);
    }
  }

  private static void assertIntegerMaxValue(int i) {
    assertTrue(i == Integer.MAX_VALUE);
  }
  
  private static void assertNotIntegerMaxValue(int i) {
    assertFalse(i == Integer.MAX_VALUE);
  }

  @Test
  public void test1() {
    ObjectToIntMap<Integer> map = new ObjectToIntMap<Integer>();

    for (int i = 0; i < 100; ++i) {
      map.put(i, Integer.valueOf(100 + i));
    }
    
    HashSet<Integer> set = new HashSet<Integer>();
    
    for (IntIterator iterator = map.iterator(); iterator.hasNext();) {
      set.add(iterator.next());
    }

    assertEquals(set.size(), map.size());
    for (int i = 0; i < 100; ++i) {
      assertTrue(set.contains(Integer.valueOf(100+i)));
    }

    set.clear();
    for (IntIterator iterator = map.iterator(); iterator.hasNext();) {
      Integer value = iterator.next();
      if (value % 2 == 1) {
        iterator.remove();
        continue;
      }
      set.add(value);
    }
    assertEquals(set.size(), map.size());
    for (int i = 0; i < 100; i+=2) {
      assertTrue(set.contains(Integer.valueOf(100+i)));
    }
  }
  
  @Test
  public void test2() {
    ObjectToIntMap<Integer> map = new ObjectToIntMap<Integer>();

    assertTrue(map.isEmpty());
    assertIntegerMaxValue(map.get(0));
    for (int i = 0; i < 128; ++i) {
      int value = i * 4096;
      assertFalse(map.containsValue(value));
      map.put(i, value);
      assertTrue(map.containsValue(value));
      assertNotIntegerMaxValue(map.get(i));
      assertFalse(map.isEmpty());
    }

    assertEquals(128, map.size());
    for (int i = 0; i < 128; ++i) {
      assertTrue(map.containsKey(i));
      assertEquals(i * 4096, map.get(i), 1E-5);
    }
    
    for (int i = 0 ; i < 200; i+=2) {
      map.remove(i);
    }
    assertEquals(64, map.size());
    for (int i = 1; i < 128; i+=2) {
      assertTrue(map.containsKey(i));
      assertEquals(i * 4096, map.get(i), 1E-5);
      map.remove(i);
    }
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void test3() {
    ObjectToIntMap<Integer> map = new ObjectToIntMap<Integer>();
    int length = 100;
    for (int i = 0; i < length; ++i) {
      map.put(i*64, 100 + i);
    }
    HashSet<Integer> keySet = new HashSet<Integer>();
    for (Iterator<Integer> iit = map.keyIterator(); iit.hasNext(); ) {
      keySet.add(iit.next());
    }
    assertEquals(length, keySet.size());
    for (int i = 0; i < length; ++i) {
      assertTrue(keySet.contains(i * 64));
    }
    
    HashSet<Integer> valueSet = new HashSet<Integer>();
    for (IntIterator iit = map.iterator(); iit.hasNext(); ) {
      valueSet.add(iit.next());
    }
    assertEquals(length, valueSet.size());
    int[] array = map.toArray();
    assertEquals(length, array.length);
    for (int value: array) {
      assertTrue(valueSet.contains(value));
    }
    
    int[] array2 = new int[80];
    array2 = map.toArray(array2);
    assertEquals(80, array2.length);
    for (int value: array2) {
      assertTrue(valueSet.contains(value));
    }
    
    int[] array3 = new int[120];
    array3 = map.toArray(array3);
    for (int i = 0 ;i < length; ++i) {
      assertTrue(valueSet.contains(array3[i]));
    }
    assertIntegerMaxValue(array3[length]);
    
    for (int i = 0; i < length; ++i) {
      assertTrue(map.containsValue(i + 100));
      assertTrue(map.containsKey(i*64));
    }
    
    for (Iterator<Integer> iit = map.keyIterator(); iit.hasNext(); ) {
      iit.next();
      iit.remove();
    }
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    
  }

  // now with random data.. and lots of it
  @Test
  public void test4() {
    ObjectToIntMap<Integer> map = new ObjectToIntMap<Integer>();
    int length = ArrayHashMapTest.RANDOM_TEST_NUM_ITERATIONS;
    
    // for a repeatable random sequence
    long seed = random().nextLong();
    Random random = new Random(seed);
    
    for (int i = 0; i < length; ++i) {
      int value = random.nextInt(Integer.MAX_VALUE);
      map.put(i*128, value);
    }

    assertEquals(length, map.size());
    
    // now repeat
    random.setSeed(seed);

    for (int i = 0; i < length; ++i) {
      int value = random.nextInt(Integer.MAX_VALUE);
      assertTrue(map.containsValue(value));
      assertTrue(map.containsKey(i*128));
      assertEquals(value, map.remove(i*128));
    }
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
  }
  
  @Test
  public void testEquals() {
    ObjectToIntMap<Float> map1 = new ObjectToIntMap<Float>(100);
    ObjectToIntMap<Float> map2 = new ObjectToIntMap<Float>(100);
    assertEquals("Empty maps should be equal", map1, map2);
    assertEquals("hashCode() for empty maps should be equal", 
        map1.hashCode(), map2.hashCode());
    
    for (int i = 0; i < 100; ++i) {
      map1.put(i * 1.1f, 100 + i);
      map2.put(i * 1.1f, 100 + i);
    }
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());

    for (int i = 10; i < 20; i++) {
      map1.remove(i * 1.1f);
    }
    assertFalse("Different maps should not be equal", map1.equals(map2));
    
    for (int i = 19; i >=10; --i) {
      map2.remove(i * 1.1f);
    }
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());
    
    map1.put(-1.1f,-1);
    map2.put(-1.1f,-2);
    assertFalse("Different maps should not be equal", map1.equals(map2));
    
    map2.put(-1.1f,-1);
    assertEquals("Identical maps should be equal", map1, map2);
    assertEquals("hashCode() for identical maps should be equal", 
        map1.hashCode(), map2.hashCode());
  }
}
