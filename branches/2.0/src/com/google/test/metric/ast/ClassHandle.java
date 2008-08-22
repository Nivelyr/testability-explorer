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


/**
 * Handle to classes. This interface (and its subinterfaces) should be the
 * one and only reference that the parser ever holds to a class in the AST.
 *
 * The ClassHandle and each subclasses shouldn't have any more methods than
 * the 'getHandle' method.
 */
public interface ClassHandle {

  /**
   * Returns a handle. Classes that implement the ClassHandle
   * interface need to provide at least a method for creating a
   * handle.
   */
  ClassHandle getHandle();

}
