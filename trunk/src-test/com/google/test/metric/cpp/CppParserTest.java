/*
 * Copyright 2008 Google Inc.
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
package com.google.test.metric.cpp;

import junit.framework.TestCase;

import com.google.test.metric.cpp.dom.BreakStatement;
import com.google.test.metric.cpp.dom.CaseStatement;
import com.google.test.metric.cpp.dom.ClassDeclaration;
import com.google.test.metric.cpp.dom.DefaultStatement;
import com.google.test.metric.cpp.dom.ElseStatement;
import com.google.test.metric.cpp.dom.FunctionDeclaration;
import com.google.test.metric.cpp.dom.FunctionDefinition;
import com.google.test.metric.cpp.dom.FunctionInvocation;
import com.google.test.metric.cpp.dom.IfStatement;
import com.google.test.metric.cpp.dom.LoopStatement;
import com.google.test.metric.cpp.dom.Namespace;
import com.google.test.metric.cpp.dom.NodeList;
import com.google.test.metric.cpp.dom.ReturnStatement;
import com.google.test.metric.cpp.dom.SwitchStatement;
import com.google.test.metric.cpp.dom.TernaryOperation;
import com.google.test.metric.cpp.dom.TranslationUnit;

public class CppParserTest extends TestCase {

  private TranslationUnit parse(String source) throws Exception {
    return new Parser().parse(source);
  }

  public void testEmptyClass() throws Exception {
    TranslationUnit unit = parse("class A{};");
    ClassDeclaration classA = unit.getChild(0);
    assertEquals("A", classA.getName());
  }

  public void testTwoClasses() throws Exception {
    TranslationUnit unit = parse("class A{}; class B{};");
    ClassDeclaration classA = unit.getChild(0);
    assertEquals("A", classA.getName());
    ClassDeclaration classB = unit.getChild(1);
    assertEquals("B", classB.getName());
  }

  public void testNestedClass() throws Exception {
    TranslationUnit unit = parse("class A{ class B{}; };");
    ClassDeclaration classA = unit.getChild(0);
    assertEquals("A", classA.getName());
    ClassDeclaration classB = classA.getChild(0);
    assertEquals("B", classB.getName());
  }

  public void testEmptyNamespace() throws Exception {
    TranslationUnit unit = parse("namespace A{}");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
  }

  public void testTwoNamespaces() throws Exception {
    TranslationUnit unit = parse("namespace A{} namespace B{}");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
    Namespace namespaceB = unit.getChild(1);
    assertEquals("B", namespaceB.getName());
  }

  public void testNestedNamespace() throws Exception {
    TranslationUnit unit = parse("namespace A{ namespace B{} }");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
    Namespace namespaceB = namespaceA.getChild(0);
    assertEquals("B", namespaceB.getName());
  }

  public void testClassInNamespace() throws Exception {
    TranslationUnit unit = parse("namespace A{ class B{}; }");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
    ClassDeclaration classB = namespaceA.getChild(0);
    assertEquals("B", classB.getName());
  }

  public void testGlobalFunctionDeclaration() throws Exception {
    TranslationUnit unit = parse("void foo();");
    FunctionDeclaration functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
  }

  public void testFunctionDeclarationInNamespace() throws Exception {
    TranslationUnit unit = parse("namespace A { void foo(); };");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
    FunctionDeclaration functionFoo = namespaceA.getChild(0);
    assertEquals("foo", functionFoo.getName());
  }

  public void testMemberFunctionDeclaration() throws Exception {
    TranslationUnit unit = parse("class A { void foo(); };");
    ClassDeclaration classA = unit.getChild(0);
    assertEquals("A", classA.getName());
    FunctionDeclaration functionFoo = classA.getChild(0);
    assertEquals("foo", functionFoo.getName());
  }

  public void testEmptyGlobalFunction() throws Exception {
    TranslationUnit unit = parse("void foo() {}");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
  }

  public void testEmptyFunctionInNamespace() throws Exception {
    TranslationUnit unit = parse("namespace A { void foo() {} }");
    Namespace namespaceA = unit.getChild(0);
    assertEquals("A", namespaceA.getName());
    FunctionDefinition functionFoo = namespaceA.getChild(0);
    assertEquals("foo", functionFoo.getName());
  }

  public void testEmptyMemberFunction() throws Exception {
    TranslationUnit unit = parse("class A { void foo() {} };");
    ClassDeclaration classA = unit.getChild(0);
    assertEquals("A", classA.getName());
    FunctionDefinition functionFoo = classA.getChild(0);
    assertEquals("foo", functionFoo.getName());
    assertEquals(1, functionFoo.getLine());
  }

  public void testFunctionLineNumbers() throws Exception {
    TranslationUnit unit = parse("class A { void foo() {}\n void\n bar() {} };");
    ClassDeclaration classA = unit.getChild(0);
    FunctionDefinition functionFoo = classA.getChild(0);
    assertEquals(1, functionFoo.getLine());
    FunctionDefinition functionBar = classA.getChild(1);
    assertEquals(3, functionBar.getLine());
  }

  public void testSimpleFunction() throws Exception {
    TranslationUnit unit = parse("int foo() { int a = 0; a = a + 1; return a; }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    ReturnStatement returnStatement = functionFoo.getChild(0);
    assertNotNull(returnStatement);
  }

  public void testFunctionWithParameters() throws Exception {
    TranslationUnit unit = parse("int foo(int a, int b) { return a + b; }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    ReturnStatement returnStatement = functionFoo.getChild(0);
    assertNotNull(returnStatement);
  }

  public void testForStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { for(;;); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
  }

  public void testWhileStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement whileStatement = functionFoo.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do {} while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
  }

  public void testWhileInForStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { for(;;) while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
    LoopStatement whileStatement = forStatement.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testForInDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do for(;;); while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
    LoopStatement forStatement = doStatement.getChild(0);
    assertNotNull(forStatement);
  }

  public void testForInCompoundDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do { for(;;); } while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
    LoopStatement forStatement = doStatement.getChild(0);
    assertNotNull(forStatement);
  }

  public void testForInSeveralCompoundStatements() throws Exception {
    TranslationUnit unit = parse("void foo() { {{{ for(;;); }}} }");
    FunctionDefinition functionFoo = unit.getChild(0);
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
  }

  public void testIfStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
  }

  public void testIfElseStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true); else; }");
    FunctionDefinition functionFoo = unit.getChild(0);
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    assertNotNull(elseStatement);
  }

  public void testCompoundIfElseStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true) {} else {} }");
    FunctionDefinition functionFoo = unit.getChild(0);
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    assertNotNull(elseStatement);
  }

  public void testIfElseAndLoopStatements() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true) { for(;;); } else { while(true); } }");
    FunctionDefinition functionFoo = unit.getChild(0);
    IfStatement ifStatement = functionFoo.getChild(0);
    LoopStatement forStatement = ifStatement.getChild(0);
    assertNotNull(forStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    LoopStatement whileStatement = elseStatement.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testSwitchStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { switch (a) { case 1: break; } }");
    FunctionDefinition functionFoo = unit.getChild(0);
    SwitchStatement switchStatement = functionFoo.getChild(0);
    CaseStatement caseStatement = switchStatement.getChild(0);
    assertNotNull(caseStatement);
  }

  public void testSwitchStatementWithDefault() throws Exception {
    TranslationUnit unit = parse("void foo() { switch (a) { case 1: break; default: break; } }");
    FunctionDefinition functionFoo = unit.getChild(0);
    SwitchStatement switchStatement = functionFoo.getChild(0);
    CaseStatement caseStatement = switchStatement.getChild(0);
    BreakStatement breakStatement = caseStatement.getChild(0);
    assertNotNull(breakStatement);
    DefaultStatement defaultStatement = switchStatement.getChild(1);
    breakStatement = defaultStatement.getChild(0);
    assertNotNull(breakStatement);
  }

  public void testTernaryOperator() throws Exception {
    TranslationUnit unit = parse("int foo(int a, int b) { return a ? 0 : b; }");
    FunctionDefinition functionFoo = unit.getChild(0);
    ReturnStatement returnStatement = functionFoo.getChild(0);
    TernaryOperation ternaryOperation = returnStatement.getChild(0);
    assertNotNull(ternaryOperation);
  }

  public void testNestedTernaryOperator() throws Exception {
    TranslationUnit unit = parse("int foo(int a, int b) { int c = a ? 0 : (b ? 1 : 2); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    TernaryOperation ternaryOperation = functionFoo.getChild(0);
    TernaryOperation nestedTernaryOperation = ternaryOperation.getChild(0);
    assertNotNull(nestedTernaryOperation);
  }

  public void testFunctionCall() throws Exception {
    TranslationUnit unit = parse("void foo(int) {} void bar(int) { foo(5); }");
    FunctionDefinition functionBar = unit.getChild(1);
    FunctionInvocation callFoo = functionBar.getChild(0);
    assertEquals("foo", callFoo.getName());
  }

  public void testNestedFunctionCall() throws Exception {
    TranslationUnit unit = parse(
        "int foo(int a) { return a; }             " +
        "int bar(int b) { return foo(foo(b)); }   ");
    FunctionDefinition functionBar = unit.getChild(1);
    assertEquals("bar", functionBar.getName());
    ReturnStatement returnStatement = functionBar.getChild(0);
    FunctionInvocation callFoo = returnStatement.getChild(0);
    assertEquals("foo", callFoo.getName());
    NodeList parameters = callFoo.getParameters();
    FunctionInvocation callFooAgain = parameters.get(0);
    assertEquals("foo", callFooAgain.getName());
    assertEquals(0, callFooAgain.getChildren().size());
  }

  public void testSequentialFunctionCalls() throws Exception {
    TranslationUnit unit = parse(
        "class A { public: void foo() {} };     " +
        "A bar() { A a; return a; }             " +
        "void main() { bar().foo(); }           ");
    FunctionDefinition functionMain = unit.getChild(2);
    assertEquals("main", functionMain.getName());
    FunctionInvocation callBar = functionMain.getChild(0);
    assertEquals("bar", callBar.getName());
    FunctionInvocation callFoo = callBar.getChild(0);
    assertEquals("foo", callFoo.getName());
    assertEquals(0, callFoo.getChildren().size());
  }

  public void testClassLoadCppVariables() throws Exception {
    assertEquals(64, CPPvariables.QI_TYPE.size());
  }
}
