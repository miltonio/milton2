/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http;

public class Range {

    private final Long start;
    private final Long finish;

    public static Range parse(String s) {
        String[] arr = s.split("-");
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
