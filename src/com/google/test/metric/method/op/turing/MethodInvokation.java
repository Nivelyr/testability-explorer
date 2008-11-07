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
package com.google.test.metric.method.op.turing;

import java.util.List;

import com.google.test.metric.ClassNotFoundException;
import com.google.test.metric.MethodInfo;
import com.google.test.metric.MethodNotFoundException;
import com.google.test.metric.TestabilityVisitor;
import com.google.test.metric.Variable;

public class MethodInvokation extends Operation {

  private final String name;
  private final String clazzName;
  private final String signature;
  private final Variable methodThis;
  private final List<Variable> parameters;
  private final Variable returnVariable;

  public MethodInvokation(int lineNumber, String clazz, String name,
      String signature, Variable methodThis, List<Variable> parameters,
      Variable returnVariable) {
    super(lineNumber);
    this.clazzName = clazz;
    this.name = name;
    this.signature = signature;
    this.methodThis = methodThis;
    this.parameters = parameters;
    this.returnVariable = returnVariable;
  }

  public List<Variable> getParameters() {
    return parameters;
  }

  public String getMethodName() {
    return clazzName + "." + name;
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return clazzName;
  }

  @Override
  public String toString() {
    return getMethodName() + signature;
  }

  @Override
  public void visit(TestabilityVisitor.Frame visitor) {
    if (visitor.isClassWhiteListed(clazzName)) {
      return;
    }
    try {
      MethodInfo toMethod = visitor.getMethod(clazzName, name + signature);
      if (visitor.wasMethodAlreadyVisited(toMethod)) {
        // Method already counted, skip (to prevent recursion)
        if (returnVariable != null) {
          int thisCount = visitor.getLoDCount(methodThis);
          visitor.recordLoDDispatch(getLineNumber(), toMethod, returnVariable, thisCount + 1);
        }
        return;
      } else if (toMethod.canOverride() && visitor.isInjectable(methodThis)) {
        // Method can be overridden / injectable
        visitor.recordOverridableMethodCall(getLineNumber(), toMethod, methodThis, returnVariable);
      } else {
        // Method can not be intercepted we have to add the cost
        // recursively
        visitor.recordNonOverridableMethodCall(getLineNumber(), toMethod, methodThis, parameters, returnVariable);
      }
    } catch (ClassNotFoundException e) {
      visitor.reportError("WARNING: class not found: " + clazzName);
    } catch (MethodNotFoundException e) {
      visitor.reportError("WARNING: method not found: " + e.getMethodName()
          + " in " + e.getClassInfo().getName());
    }
  }

  public Variable getMethodThis() {
    return methodThis;
  }

}
