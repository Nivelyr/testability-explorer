/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.test.metric.report;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.tools.ant.filters.StringInputStream;

import com.google.test.metric.ClassCost;
import com.google.test.metric.CostModel;
import com.google.test.metric.MethodCost;

public class PropertiesReportTest extends TestCase {

  ByteArrayOutputStream out = new ByteArrayOutputStream();
  PropertiesReport report = new PropertiesReport(out, 0, 0, 0);
  CostModel costModel = new CostModel(1, 1);

  private static final String CLASS_NAME = "com.google.foo.Bar";
  public void testReport() throws Exception {

    MethodCost methodCost = new MethodCost("doThing", 3);
    methodCost.addCyclomaticCost(0);
    methodCost.link(new CostModel(1.0, 1.0));
    final ClassCost classCost = new ClassCost(CLASS_NAME, Arrays.asList(methodCost), costModel);
    report.addClassCost(classCost);
    report.printFooter();

    String output = out.toString();
    assertTrue(output.contains("Bar"));
    Properties props = new Properties();
    props.load(new StringInputStream(output));
    assertEquals(1, Integer.parseInt(props.getProperty(CLASS_NAME)));
  }
}