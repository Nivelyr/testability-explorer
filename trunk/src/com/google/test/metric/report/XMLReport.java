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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.google.test.metric.ClassCost;
import com.google.test.metric.MethodCost;
import com.google.test.metric.ViolationCost;

public class XMLReport extends SummaryReport {

  private final ContentHandler out;

  public XMLReport(ContentHandler out, int maxExcellentCost,
      int maxAcceptableCost, int worstOffenderCount) {
    super(maxExcellentCost, maxAcceptableCost, worstOffenderCount);
    this.out = out;
  }

  public void printHeader() {
  }

  public void printFooter() {
    try {
      out.startDocument();
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("overall", getOverall());
      values.put("excellent", excellentCount);
      values.put("good", goodCount);
      values.put("needsWork", needsWorkCount);
      startElement("testability", values);
      for (ClassCost classCost : worstOffenders) {
        writeCost(classCost);
      }
      endElement("testability");
      out.endDocument();
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeElement(String elementName, Map<String, Object> values)
      throws SAXException {
    startElement(elementName, values);
    endElement(elementName);
  }

  private void endElement(String elementName) throws SAXException {
    out.endElement(null, elementName, elementName);
  }

  private void startElement(String elementName, Map<String, Object> values)
      throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    for (String key : new TreeSet<String>(values.keySet())) {
      atts.addAttribute(null, key, key, null, values.get(key).toString());
    }
    out.startElement(null, elementName, elementName, atts);
  }

  void writeCost(ViolationCost violation) throws SAXException {
    writeElement("cost", violation.getAttributes());
  }

  void writeCost(MethodCost methodCost) throws SAXException {
    startElement("method", methodCost.getAttributes());
    for (ViolationCost violation : methodCost.getViolationCosts()) {
      writeCost(violation);
    }
    endElement("method");
  }

  void writeCost(ClassCost classCost) throws SAXException {
    startElement("class", classCost.getAttributes());
    for (MethodCost cost : classCost.getMethods()) {
      writeCost(cost);
    }
    endElement("class");
  }

}
