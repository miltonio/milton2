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
package io.milton.http;

public class Range {

    private final Long start;
    private final Long finish;

    public static Range parse(String s) {
        String[] arr = s.trim().split("-");
        if (arr.length == 0 || arr.length > 2) {
            throw new RuntimeException("Invalid range. Use format start-finish, eg 1000-1500. Range:" + s + " parts=" + arr.length);
        }
        try {
            Long start = null;
            if (arr[0].length() > 0) {
                start = Long.parseLong(arr[0]);
            }
            Long finish = null;
            if (arr.length > 1) {
                if (arr[1].length() > 0) {
                    finish = Long.parseLong(arr[1]);
                }
            }
            return new Range(start, finish);
        } catch (Throwable e) {
            throw new RuntimeException("Invalid range. Use format start-finish, eg 1000-1500. Range:" + s, e);
        }
    }

    public Range(int start, int finish) {
        this.start = (long)start;
        this.finish = (long)finish;
    }
    
    public Range(Long start, Long finish) {
        this.start = start;
        this.finish = finish;
    }

    public Long getStart() {
        return start;
    }

    public Long getFinish() {
        return finish;
    }

    @Override
    public String toString() {
        return "bytes " + start + "-" + finish;
    }

    /**
     * Returns range in String format ("start-end"), ready to be put into HTTP
     * range request
     *
     * @return Range of data in stream
     */
    public String getRange() {
        String s = "";
        if (start != null) {
            s += start;
        }
        s += "-";
        if (finish != null) {
            s += "finish";
        }
        return s;
    }
}
