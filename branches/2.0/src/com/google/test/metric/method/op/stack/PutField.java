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
package com.google.test.metric.method.op.stack;

import com.google.test.metric.Variable;
import com.google.test.metric.ast.FieldHandle;
import com.google.test.metric.method.op.turing.FieldAssignment;
import com.google.test.metric.method.op.turing.Operation;

import java.util.List;

public class PutField extends StackOperation {

  private final FieldHandle fieldHandle;

  public PutField(int lineNumber, FieldHandle fieldHandle) {
    super(lineNumber);
    this.fieldHandle = fieldHandle;
  }

  @Override
  public String toString() {
    return "put " + (fieldHandle.isGlobal() ? "static " : "") + fieldHandle;
  }

  @Override
  public int getOperatorCount() {
    int valueCount = fieldHandle.getType().isDoubleSlot() ? 2 : 1;
    int fieldThis = fieldHandle.isGlobal() ? 0 : 1;
    return valueCount + fieldThis;
  }

  @Override
  public Operation toOperation(List<Variable> input) {
    if (fieldHandle.isGlobal()) {
      Variable value = input.get(0);
      return new FieldAssignment(lineNumber, null, fieldHandle, value);
    } else {
      Variable instance = input.get(0);
      Variable value = input.get(1);
      return new FieldAssignment(lineNumber, instance, fieldHandle, value);
    }
  }

}
