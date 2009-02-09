/*
 * Copyright 2009 Google Inc.
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
package com.google.test.metric;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;
import static java.util.Arrays.asList;

import java.io.PrintStream;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.classpath.ClassPath;
import com.google.classpath.RegExpResourceFilter;
import com.google.test.metric.report.Report;

/**
 * Has the responsibility of kicking off the analysis. A programmatic interface into using 
 * Testability Explorer.
 * 
 * @author Jonathan Andrew Wolter <jaw@jawspeak.com>
 */
public class TestabilityRunner {

  private final List<String> entryList;
  private final ClassPath classPath;
  private final RegExpWhiteList whitelist;
  private final Report report;
  private final PrintStream err;
  private final int printDepth;

  public TestabilityRunner(TestabilityConfig config) {
    this.entryList = config.getEntryList();
    this.classPath = config.getClassPath();
    this.whitelist = config.getWhitelist();
    this.report = config.getReport();
    this.err = config.getErr();
    this.printDepth = config.getPrintDepth();
  }

  public Report run(){
    ClassRepository classRepository = new JavaClassRepository(classPath);
    
    MetricComputer computer = new MetricComputer(classRepository, err, whitelist, printDepth);
    report.printHeader();
    
    SortedSet<String> classNames = new TreeSet<String>();
    RegExpResourceFilter resourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
    for (String entry : entryList) {
      if (entry.equals(".")) {
        entry = "";
      }
      // TODO(jonathan) seems too complicated, replacing "." with "/" using the resource filter, then right below replace all "/" with "."
      classNames.addAll(asList(classPath.findResources(entry.replace(".", "/"), resourceFilter)));
    }
    for (String resource : classNames) {
      String className = resource.replace(".class", "").replace("/", ".");
      try {
        if (!whitelist.isClassWhiteListed(className)) {
          ClassInfo clazz = classRepository.getClass(className);
          ClassCost classCost = computer.compute(clazz);
          report.addClassCost(classCost);
        }
      } catch (ClassNotFoundException e) {
        err.println("WARNING: can not analyze class '" + className
            + "' since class '" + e.getClassName() + "' was not found.");
      }
    }
    report.printFooter();
    return report;
  }  

}
