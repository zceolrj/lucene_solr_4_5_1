package org.apache.lucene.analysis.miscellaneous;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.BaseTokenStreamFactoryTestCase;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.util.Version;

public class TestLengthFilterFactory extends BaseTokenStreamFactoryTestCase {

  public void test() throws Exception {
    Reader reader = new StringReader("foo foobar super-duper-trooper");
    TokenStream stream = new MockTokenizer(reader, MockTokenizer.WHITESPACE, false);
    stream = tokenFilterFactory("Length",
        Version.LUCENE_43, new ClasspathResourceLoader(getClass()),
        "min", "4",
        "max", "10",
        "enablePositionIncrements", "false").create(stream);
    assertTokenStreamContents(stream, new String[] { "foobar" }, new int[] { 1 });
  }

  public void testPositionIncrements() throws Exception {
    Reader reader = new StringReader("foo foobar super-duper-trooper");
    TokenStream stream = new MockTokenizer(reader, MockTokenizer.WHITESPACE, false);
    stream = tokenFilterFactory("Length",
        "min", "4",
        "max", "10",
        "enablePositionIncrements", "true").create(stream);
    assertTokenStreamContents(stream, new String[] { "foobar" }, new int[] { 2 });
  }
  
  /** Test that bogus arguments result in exception */
  public void testBogusArguments() throws Exception {
    try {
      tokenFilterFactory("Length", 
          "min", "4", 
          "max", "5", 
          "bogusArg", "bogusValue");
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Unknown parameters"));
    }
  }
}