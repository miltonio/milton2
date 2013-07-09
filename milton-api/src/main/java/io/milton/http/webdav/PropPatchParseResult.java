/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.webdav;

import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class PropPatchParseResult {
	private final Map<QName, String> fieldsToSet;
	private final Set<QName> fieldsToRemove;

	public PropPatchParseResult(Map<QName, String> fieldsToSet, Set<QName> fieldsToRemove) {
		this.fieldsToSet = fieldsToSet;
		this.fieldsToRemove = fieldsToRemove;
	}

	public Set<QName> getFieldsToRemove() {
		return fieldsToRemove;
	}

	public Map<QName, String> getFieldsToSet() {
		return fieldsToSet;
	}
    
}
