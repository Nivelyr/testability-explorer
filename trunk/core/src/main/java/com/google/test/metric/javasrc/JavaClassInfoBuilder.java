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
package com.google.test.metric.javasrc;

import com.google.test.metric.ClassInfo;

public class JavaClassInfoBuilder {

	private String pakageName = "";
	private String type;

	public ClassInfo build() {
		return new ClassInfo(pakageName + type, false, null, null);
	}

	public void setPackage(int line, String packageName) {
		if (!packageName.endsWith(".")) {
			packageName += ".";
		}
		this.pakageName = packageName;
	}

	public void startType(int line, String type) {
		this.type = type;
	}

}
