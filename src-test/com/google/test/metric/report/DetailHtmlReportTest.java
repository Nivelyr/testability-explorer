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

import static com.google.test.metric.report.Constants.NEW_LINE;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.google.test.metric.ClassCost;
import com.google.test.metric.CostModel;
import com.google.test.metric.LineNumberCost;
import com.google.test.metric.MethodCost;

public class DetailHtmlReportTest extends TestCase {

  ByteArrayOutputStream out = new ByteArrayOutputStream();
  PrintStream stream = new PrintStream(out, true);

  String emptyLineTemplate = "";
  String emptyClassTemplate = "";

  String lineTemplate = "http://code.google.com/p/testability-explorer/source/browse/trunk/src/{path}#{line}";
  String classTemplate = "http://code.google.com/p/testability-explorer/source/browse/trunk/src/{path}";

  public void testWriteLineCost() throws Exception {
    LineNumberCost lineCost = new LineNumberCost(123,
        createMethodCallWithOverallCost("a.methodName()V", 64));

    DetailHtmlReport report = new DetailHtmlReport(stream, new SourceLinker(
        emptyLineTemplate, emptyClassTemplate), 10, 10);
    report.write(lineCost, "");
    String text = out.toString();

    assertTrue(text, text.contains("<div class=\"Line\""));
    assertTrue(text, text.contains("123"));
    assertTrue(text, text.contains("methodName"));
    assertTrue(text, text.contains("64"));
    assertTrue(text, text.endsWith("</div>" + NEW_LINE));
  }

  public void testLinkedLineCost() throws Exception {
    LineNumberCost lineCost = new LineNumberCost(123,
        createMethodCallWithOverallCost("a.methodName()V", 64));

    DetailHtmlReport report = new DetailHtmlReport(stream, new SourceLinker(
        lineTemplate, classTemplate), 10, 10);
    report.write(lineCost, "com/google/ant/TaskModel.java");
    String text = out.toString();

    assertTrue(text,
        text.contains("<a href=\"http://code.google.com/p/testability-explorer/source/browse/trunk/src/com/google/ant/TaskModel.java#123"));
  }

  private MethodCost createMethodCallWithOverallCost(String methodName,
      int overallCost) {
    MethodCost cost = new MethodCost(methodName, -1, overallCost);
    cost.link(new CostModel(1, 1));
    assertEquals(overallCost, cost.getOverallCost());
    return cost;
  }

  public void testWriteMethodCost() throws Exception {
    DetailHtmlReport report = new DetailHtmlReport(stream, new SourceLinker(
        emptyLineTemplate, emptyClassTemplate), 10, 10) {
      @Override
      public void write(LineNumberCost lineNumberCost, String classFilePath) {
        write(" MARKER:" + lineNumberCost.getLineNumber());
      }
    };

    MethodCost method = createMethodCallWithOverallCost("a.methodX()V",
        567 + 789);
    method.addMethodCost(123, createMethodCallWithOverallCost("cost1", 567));
    method.addMethodCost(543, createMethodCallWithOverallCost("cost2", 789));
    report.write(method, "");
    String text = out.toString();
    assertTrue(text, text.contains("<div class=\"Method\""));
    assertTrue(text, text.contains("<span class='expand'>[+]</span>"));
    assertTrue(text, text.contains("methodX"));
    assertTrue(text, text.contains("[&nbsp;" + (567 + 789) + "&nbsp;]"));
    assertTrue(text, text.contains("MARKER:123"));
    assertTrue(text, text.contains("MARKER:543"));
    assertTrue(text, text.endsWith("</div>" + NEW_LINE));
  }

  public void testWriteClassCost() throws Exception {
    DetailHtmlReport report = new DetailHtmlReport(stream, new SourceLinker(
        emptyLineTemplate, emptyClassTemplate), 10, 10) {
      @Override
      public void write(MethodCost methodCost, String classFilePath) {
        write(" MARKER:" + methodCost.getMethodName());
      }
    };

    List<MethodCost> methods = new ArrayList<MethodCost>();
    methods.add(createMethodCallWithOverallCost("methodX", 233));
    methods.add(createMethodCallWithOverallCost("methodY", 544));
    ClassCost classCost = new ClassCost("classFoo", methods);
    classCost.link(new CostModel(1, 1));
    report.write(classCost);
    String text = out.toString();

    assertTrue(text, text.contains("<div class=\"Class\""));
    assertTrue(text, text.contains("<span class='expand'>[+]</span>"));
    assertTrue(text, text.contains("classFoo"));
    assertTrue(text, text.contains("[&nbsp;" + 475 + "&nbsp;]"));
    assertTrue(text, text.contains("MARKER:methodX"));
    assertTrue(text, text.contains("MARKER:methodY"));
    assertTrue(text, text.endsWith("</div>" + NEW_LINE));
  }

  public void testLinkedClassCost() throws Exception {
    DetailHtmlReport report = new DetailHtmlReport(stream, new SourceLinker(
       lineTemplate , classTemplate), 10, 10) ;

    List<MethodCost> methods = new ArrayList<MethodCost>();
    ClassCost classCost = new ClassCost("com.google.ant.TaskModel", methods);
    classCost.link(new CostModel(1, 1));
    report.write(classCost);
    String text = out.toString();

    assertTrue(
        text,
        text.contains("(<a href=\"http://code.google.com/p/testability-explorer/source/browse/trunk/src/com/google/ant/TaskModel.java\">source</a>)"));

  }
}
