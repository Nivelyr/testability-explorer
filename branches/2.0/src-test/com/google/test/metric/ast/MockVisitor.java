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
package com.google.test.metric.ast;

import java.util.ArrayList;
import java.util.List;

public class MockVisitor implements Visitor {

  public final List<ClassInfo> classes = new ArrayList<ClassInfo>();
  public final List<MethodInfo> methods = new ArrayList<MethodInfo>();
  public final List<ModuleInfo> modules = new ArrayList<ModuleInfo>();

  public void visitClass(ClassInfo classInfo) {
    classes.add(classInfo);
  }

  public void visitMethod(MethodInfo methodInfo) {
    methods.add(methodInfo);
  }

  public void visitModule(ModuleInfo moduleInfo) {
    modules.add(moduleInfo);
  }
}
