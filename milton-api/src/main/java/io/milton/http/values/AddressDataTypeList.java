/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.http.values;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Holds a list of Pair<String, String>, i.e. actually address data Type, where 
 * object1 represents ContentType and object2 represents the version.
 * 
 * @author nabil.shams
 */
public class AddressDataTypeList extends ArrayList<Pair<String, String>>{
    public static AddressDataTypeList asList(Pair<String, String>... items) {
        AddressDataTypeList list = new AddressDataTypeList();
		list.addAll(Arrays.asList(items));
        return list;
    }
}
