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

package bradswebdavclient.util;

public class ScriptException extends Exception {
  private int returnCode;
  private String output;
  
  public ScriptException(int returnCode, String output) {
    super("return code indicates failure:" + returnCode);
    this.returnCode = returnCode;
    this.output = output;
  }
  
  public ScriptException(String message, Throwable cause) {
    super(message,cause);
  }
  
  public ScriptException( Throwable cause) {
    super(cause);
  }

  public ScriptException(String message) {
    super(message);
  }
  
  public int getReturnCode() {
    return returnCode;
  }
  
  public String getOutput() {
    return output;
  }
}
