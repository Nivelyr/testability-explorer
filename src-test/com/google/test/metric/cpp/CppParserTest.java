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

import java.io.CharArrayReader;
import java.io.Reader;

import junit.framework.TestCase;

import com.google.test.metric.cpp.dom.ClassDeclaration;
import com.google.test.metric.cpp.dom.ElseStatement;
import com.google.test.metric.cpp.dom.FunctionDeclaration;
import com.google.test.metric.cpp.dom.FunctionDefinition;
import com.google.test.metric.cpp.dom.IfStatement;
import com.google.test.metric.cpp.dom.LoopStatement;
import com.google.test.metric.cpp.dom.Namespace;

public class CppParserTest extends TestCase {

  private TranslationUnit parse(String source) throws Exception {
    RootBuilder builder = new RootBuilder();
    Reader reader = new CharArrayReader(source.toCharArray());
    CPPLexer lexer = new CPPLexer(reader);
    CPPParser parser = new CPPParser(lexer);
    parser.translation_unit(builder);
    return builder.getNode();
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
  }

  public void testForStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { for(;;); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
  }

  public void testWhileStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement whileStatement = functionFoo.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do {} while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
  }

  public void testWhileInForStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { for(;;) while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
    LoopStatement whileStatement = forStatement.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testForInDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do for(;;); while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
    LoopStatement forStatement = doStatement.getChild(0);
    assertNotNull(forStatement);
  }

  public void testForInCompoundDoStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { do { for(;;); } while(true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement doStatement = functionFoo.getChild(0);
    assertNotNull(doStatement);
    LoopStatement forStatement = doStatement.getChild(0);
    assertNotNull(forStatement);
  }

  public void testForInSeveralCompoundStatements() throws Exception {
    TranslationUnit unit = parse("void foo() { {{{ for(;;); }}} }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    LoopStatement forStatement = functionFoo.getChild(0);
    assertNotNull(forStatement);
  }

  public void testIfStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true); }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
  }

  public void testIfElseStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true); else; }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    assertNotNull(elseStatement);
  }

  public void testCompoundIfElseStatement() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true) {} else {} }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    IfStatement ifStatement = functionFoo.getChild(0);
    assertNotNull(ifStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    assertNotNull(elseStatement);
  }

  public void testIfElseAndLoopStatements() throws Exception {
    TranslationUnit unit = parse("void foo() { if (true) { for(;;); } else { while(true); } }");
    FunctionDefinition functionFoo = unit.getChild(0);
    assertEquals("foo", functionFoo.getName());
    IfStatement ifStatement = functionFoo.getChild(0);
    LoopStatement forStatement = ifStatement.getChild(0);
    assertNotNull(forStatement);
    ElseStatement elseStatement = functionFoo.getChild(1);
    LoopStatement whileStatement = elseStatement.getChild(0);
    assertNotNull(whileStatement);
  }

  public void testClassLoadCppVariables() throws Exception {
    assertEquals(64, CPPvariables.QI_TYPE.size());
  }
}
