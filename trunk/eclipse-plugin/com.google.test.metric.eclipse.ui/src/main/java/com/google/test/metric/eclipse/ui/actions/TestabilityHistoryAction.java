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
package com.google.test.metric.eclipse.ui.actions;

import org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction;

/**
 * 
 * @author shyamseshadri@google.com (Shyam Seshadri)
 */
public class TestabilityHistoryAction extends AbstractLaunchHistoryAction {

  public TestabilityHistoryAction() {
    super("com.google.test.metric.eclipse.ui.launchGroup.testability");
  }

}
