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
package com.google.test.metric;



import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestabilityContext {

  private final Set<Variable> injectables = new HashSet<Variable>();
  private final Set<Variable> statics = new HashSet<Variable>();
  private final ClassRepository classRepository;
  private final Map<MethodInfo, MethodCost> methodCosts = new HashMap<MethodInfo, MethodCost>();
  private final PrintStream err;
  private final WhiteList whitelist;
  private final CostModel costModel;
  private Variable returnValue;

  // TODO(jwolter): As all "Context" objects we need to remove this one and break it into smaller,
  // single-responsibility objects.
  public TestabilityContext(ClassRepository classRepository, PrintStream err,
    WhiteList whitelist, CostModel costModel) {
    this.classRepository = classRepository;
    this.err = err;
    this.costModel = costModel;
    this.whitelist = whitelist;
  }

  public MethodInfo getMethod(String clazzName, String methodName) {
    return classRepository.getClass(clazzName).getMethod(methodName);
  }

  public boolean methodAlreadyVisited(MethodInfo method) {
    return methodCosts.containsKey(method);
  }

  /**
   * Records that there is a call to {@code toMethod} from within {@code fromMethod}, on the
   * {@code fromLineNumber}. Recurses into {@code toMethod} and records all of the
   * operations there, computing for all the methods in the transitive closure (avoiding
   * whitelisted method invocations).
   *
   * @param fromMethod the method making the call of {@code toMethod}
   * @param fromLineNumber source code line number in the {@code fromMethod}
   * @param toMethod method that is getting called from within {@code fromMethod}
   */
  // TODO(jwolter): I don't think this needs to be on TestabilityContext, can we break it off?
  // Does it belong to live on a MethodInfoBuilder? (I think so) or on the MethodInfo itself?
  // Or maybe TestabilityContext should be pruned off of the extra baggage, and renamed to
  // MethodCostBuilder? CostAccumulator?
  public void recordMethodCall(MethodInfo fromMethod, int fromLineNumber,
      MethodInfo toMethod) {
    MethodCost from = getMethodCost(fromMethod);
    MethodCost to = getMethodCost(toMethod);
    if (from != to) {
      from.addMethodCost(fromLineNumber, to);
      toMethod.computeMetric(this);
    }
  }

  /**
   * Looks up the MethodCost and returns the cached one, or a new one is created for
   * this method. Then link() is called. Note: this returns the linked method cost only
   * because some tests require linking (and don't go through the usual route of
   * ClassCost#link().
   */
  public MethodCost getLinkedMethodCost(MethodInfo method) {
    MethodCost cost = getMethodCost(method);
    cost.link(costModel);
    return cost;
  }

  MethodCost getMethodCost(MethodInfo method) {
    MethodCost methodCost = methodCosts.get(method);
    if (methodCost == null) {
      methodCost = new MethodCost(method.getFullName(), method.getStartingLineNumber(), method.getTestCost());
      methodCosts.put(method, methodCost);
    }
    return methodCost;
  }

  /**
   * Implicit costs are added to the {@code from} method's costs when
   * it is assumed that the costs must be incurred in order for the {@code from}
   * method to execute. Example:
   * <pre>
   * void fromMethod() {
   *   this.someObject.toMethod();
   * }
   * </pre>
   * <p>We would add the implicit cost of the toMethod() to the fromMethod().
   * Implicit Costs consist of:
   * <ul>
   * <li>Cost of construction for the someObject field referenced in fromMethod()</li>
   * <li>Static initialization blocks in someObject</ul>
   * <li>The cost of calling all the methods starting with "set" on someObject.</ul>
   * <li>Note that the same implicit costs apply for the class that has the fromMethod.
   * (Meaning a method will always have the implicit costs of the containing class and
   * super-classes at a minimum).</li>
   * </ul>
   *
   * @param from the method that we are adding the implicit cost upon.
   * @param to the method that is getting called by {@code from} and contributes cost transitively.
   */
  public void implicitCost(MethodInfo from, MethodInfo to) {
    int line = to.getStartingLineNumber();
    getMethodCost(from).addMethodCost(line, getMethodCost(to));
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("MethodCost:");
    for (MethodCost cost : methodCosts.values()) {
      buf.append("  ");
      buf.append(cost);
      buf.append("\n");
    }
    buf.append("\nInjectables:");
    for (Variable var : injectables) {
      buf.append("\n   ");
      buf.append(var);
    }
    buf.append("\nGlobals:");
    for (Variable var : statics) {
      buf.append("\n   ");
      buf.append(var);
    }
    return buf.toString();
  }

  public void setInjectable(List<? extends Variable> parameters) {
    for (Variable variable : parameters) {
      setInjectable(variable);
    }
  }

  public void setInjectable(MethodInfo method) {
    if (!method.isStatic()) {
      setInjectable(method.getMethodThis());
    }
    setInjectable(method.getParameters());
  }

  public void localAssignment(MethodInfo inMethod, int lineNumber,
      Variable destination, Variable source) {
    if (isInjectable(source)) {
      setInjectable(destination);
    }
    if (destination.isGlobal() || isGlobal(source)) {
      setGlobal(destination);
      if (source instanceof LocalField && !source.isFinal()) {
        getMethodCost(inMethod).addGlobalCost(lineNumber, source);
      }
    }
  }

  public boolean isGlobal(Variable var) {
    if (var instanceof LocalField) {
      LocalField field = (LocalField) var;
      return isGlobal(field.getInstance()) || isGlobal(field.getField());
    }
    return var != null && (var.isGlobal() || statics.contains(var));
  }

  /**
   * The method propagates the global property of a field onto any field it is assigned to.
   * The globality is propagated because global state is transitive (static cling) So any
   * modification on class which is transitively global should also be penalized.
   *
   * <p>Note: <em>final</em> static fields are not added, because they are assumed to be
   * constants, thus this will miss some actual global state. (The justification
   * is that if costs were included for constants it would penalize people for
   * a good practice -- removing magic values from code).
   */
  public void fieldAssignment(Variable fieldInstance, FieldInfo field,
      Variable value, MethodInfo inMethod, int lineNumber) {
    localAssignment(inMethod, lineNumber, field, value);
    if (fieldInstance == null || statics.contains(fieldInstance)) {
      if (!field.isFinal()) {
        getMethodCost(inMethod).addGlobalCost(lineNumber, fieldInstance);
      }
      statics.add(field);
    }
  }

  /** If and only if the array is a static, then add it as a Global State Cost for the
   * {@code inMethod}. */
  public void arrayAssignment(Variable array, Variable index, Variable value,
      MethodInfo inMethod, int lineNumber) {
    if (statics.contains(array)) {
      getMethodCost(inMethod).addGlobalCost(lineNumber, array);
    }
  }

  public void setGlobal(Variable var) {
    statics.add(var);
  }

  public boolean isInjectable(Variable var) {
    if (var instanceof LocalField) {
      return isInjectable(((LocalField)var).getField());
    }
    return injectables.contains(var);
  }

  public void setInjectable(Variable var) {
    injectables.add(var);
  }

  // TODO(jwolter): This should not be on this object, it only clutters the single responsibility
  // we would like to have within it. It makes this object double as a service locator.
  public void reportError(String errorMessage) {
    err.println(errorMessage);
  }

  //TODO(jwolter): This should be removed from this class, because it is only acting as a
  // service locator, cluttering its responsibilities.
  public WhiteList getWhitelist() {
    return whitelist;
  }

  // TODO(jwolter): This should be removed from this class, because it is only acting as a
  // service locator, cluttering its responsibilities.
  public boolean isClassWhiteListed(String className) {
    return whitelist.isClassWhiteListed(className);
  }

  // TODO(jwolter): This class is too tightly coupled to the MethodInvokation class, can we pull off
  // this method and put it somewhere else?
  public void setReturnValue(Variable value) {
    this.returnValue = value;
  }

  // TODO(jwolter): This class is too tightly coupled to the MethodInvokation class, can we pull off
  // this method and put it somewhere else?
  public Variable getReturnValue() {
    return returnValue;
  }
}
