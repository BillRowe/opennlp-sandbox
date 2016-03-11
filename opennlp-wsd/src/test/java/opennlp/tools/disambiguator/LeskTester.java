/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package opennlp.tools.disambiguator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.disambiguator.lesk.Lesk;
import opennlp.tools.disambiguator.lesk.LeskParameters;
import opennlp.tools.disambiguator.lesk.LeskParameters.LESK_TYPE;
import opennlp.tools.util.Span;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is the test class for {@link Lesk}.
 * 
 * The scope of this test is to make sure that the Lesk disambiguator code can be
 * executed. This test can not detect mistakes which lead to incorrect feature
 * generation or other mistakes which decrease the disambiguation performance of the
 * disambiguator.
 */
public class LeskTester {
  // TODO write more tests

  static String modelsDir = "src\\test\\resources\\models\\";

  static Lesk lesk;

  static String test1 = "We need to discuss an important topic, please write to me soon.";
  static String test2 = "The component was highly radioactive to the point that"
      + " it has been activated the second it touched water";
  static String test3 = "The summer is almost over and I did not go to the beach even once";

  static String[] sentence1;
  static String[] sentence2;
  static String[] sentence3;

  static String[] tags1;
  static String[] tags2;
  static String[] tags3;

  static String[] lemmas1;
  static String[] lemmas2;
  static String[] lemmas3;

  /*
   * Setup the testing variables
   */
  @BeforeClass
  public static void setUp() {

    WSDHelper.loadTokenizer(modelsDir + "en-token.bin");
    WSDHelper.loadLemmatizer(modelsDir + "en-lemmatizer.dict");
    WSDHelper.loadTagger(modelsDir + "en-pos-maxent.bin");

    sentence1 = WSDHelper.getTokenizer().tokenize(test1);
    sentence2 = WSDHelper.getTokenizer().tokenize(test2);
    sentence3 = WSDHelper.getTokenizer().tokenize(test3);

    tags1 = WSDHelper.getTagger().tag(sentence1);
    tags2 = WSDHelper.getTagger().tag(sentence2);
    tags3 = WSDHelper.getTagger().tag(sentence3);

    List<String> tempLemmas1 = new ArrayList<String>();
    for (int i = 0; i < sentence1.length; i++) {
      tempLemmas1
          .add(WSDHelper.getLemmatizer().lemmatize(sentence1[i], tags1[i]));
    }
    lemmas1 = tempLemmas1.toArray(new String[tempLemmas1.size()]);

    List<String> tempLemmas2 = new ArrayList<String>();
    for (int i = 0; i < sentence2.length; i++) {
      tempLemmas2
          .add(WSDHelper.getLemmatizer().lemmatize(sentence2[i], tags2[i]));
    }
    lemmas2 = tempLemmas2.toArray(new String[tempLemmas2.size()]);

    List<String> tempLemmas3 = new ArrayList<String>();
    for (int i = 0; i < sentence3.length; i++) {
      tempLemmas3
          .add(WSDHelper.getLemmatizer().lemmatize(sentence3[i], tags3[i]));
    }
    lemmas3 = tempLemmas3.toArray(new String[tempLemmas3.size()]);

    lesk = new Lesk();

    LeskParameters params = new LeskParameters();
    params.setLeskType(LESK_TYPE.LESK_EXT);
    boolean a[] = { true, true, true, true, true, true, true, true, true,
        true };
    params.setFeatures(a);
    lesk.setParams(params);
  }

  /*
   * Tests disambiguating only one word : The ambiguous word "please"
   */
  @Test
  public void testOneWordDisambiguation() {
    String[] senses = lesk.disambiguate(sentence1, tags1, lemmas1, 8);

    assertEquals("Check number of senses", 1, senses.length);
  }

  /*
   * Tests disambiguating a word Span In this case we test a mix of monosemous
   * and polysemous words as well as words that do not need disambiguation such
   * as determiners
   */
  @Test
  public void testWordSpanDisambiguation() {
    Span span = new Span(3, 7);
    List<String[]> senses = lesk.disambiguate(sentence2, tags2, lemmas2, span);

    assertEquals("Check number of returned words", 5, senses.size());
    assertEquals("Check number of senses", 3, senses.get(0).length);
    assertEquals("Check monosemous word", 1, senses.get(1).length);
    assertEquals("Check preposition", "WSDHELPER to", senses.get(2)[0]);
    assertEquals("Check determiner", "WSDHELPER determiner", senses.get(3)[0]);
  }

  /*
   * Tests disambiguating all the words
   */
  @Test
  public void testAllWordsDisambiguation() {
    List<String[]> senses = lesk.disambiguate(sentence3, tags3, lemmas3);

    assertEquals("Check number of returned words", 15, senses.size());
    assertEquals("Check preposition", "WSDHELPER personal pronoun",
        senses.get(6)[0]);
  }

}