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

package io.milton.httpclient;

/**
 *
 * @author mcevoyb
 */
public abstract class HttpException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int result;
    private final String href;

    public HttpException(int result, String href) {
        super("http error: " + result + " - " + href);
        this.result = result;
        this.href = href;
    }

    public HttpException(String href, Throwable cause) {
        super(href, cause);
        this.href = href;
        this.result = 0;
    }
    
    

    public int getResult() {
        return result;
    }

    public String getHref() {
        return href;
    }
}
