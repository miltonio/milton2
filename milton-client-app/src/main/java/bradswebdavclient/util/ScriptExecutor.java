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

package bradswebdavclient.util;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Use this to execute a native script file, such as a bash script on linux or a batch file on windows
 * 
 * The exec method blocks until the script has terminated. Success is determined by the script return code. Information
 * returned by the script is buffered and will be added to an exception if thrown to support logging
 * 
 * This class explicitly supports win32 batch files. If setWin32Batch(true) is called then the process argument is assumed to
 * be a batch file and cmd.exe /c is inserted into the called command.
 * 
 * @author mcevoyb
 *
 */
public class ScriptExecutor {
  
  final String process;
  final String[] args;
  final int successCode;
  boolean win32Batch;
  
  /**
   * 
   * @param process - name and/or path of the script file. May be a win32 batch file
   * @param args - array of string arguments to be set on the command line
   * @param successCode - numeric code which will indicate successful completion. This is normally 0
   */
  public ScriptExecutor(String process, String[] args, int successCode) {
    this.process = process;
    this.args = args;
    this.successCode = successCode;
    if( process.endsWith(".bat") ) setWin32Batch(true); // this is to help with running on windows workstations
  }
  
  public boolean isWin32Batch() {
    return win32Batch;
  }
  
  /**
   * Set this to true to force the class to format the command to execute cmd.exe /c process {args..}
   * 
   * @param win32Batch
   */
  public void setWin32Batch(boolean win32Batch) {
    this.win32Batch = win32Batch;
  }
  
  /**
   * Synchronously executes the script file. Once complete, the return code will be inspected for success
   * 
   * @throws ScriptException - 
   */
  public void exec() throws ScriptException {
    int cmdSize = args.length+1;
    if( isWin32Batch() ) cmdSize+=2;
    String[] cmd = new String[cmdSize];
    int param=0;
    if( isWin32Batch() ) {
      cmd[param++] = "cmd.exe";
      cmd[param++] = "/c";
    }
    cmd[param++] = process;
    for( int i=0; i<args.length; i++ ) {
      String s = args[i];
      if( s == null ) s = "";
      cmd[param++] = s;
    }
    for (int i = 0; i < cmd.length; i++) {
      System.out.println("cmd: " + i + " = " + cmd[i]);
    }
    Runtime rt = Runtime.getRuntime();
    try {
      Process proc = rt.exec( cmd );
      StreamDiscarder errorDiscarder = new StreamDiscarder( proc.getErrorStream() );
      ScriptOutputReader output = new ScriptOutputReader( proc.getInputStream() );
      errorDiscarder.start();
      output.start();
      int exitVal = proc.waitFor();
      if ( exitVal != successCode ) {
        throw new ScriptException(exitVal,output.toString());
      }
      output.join( 10000 ); // 10 sec at most!
      errorDiscarder.join( 1000 );  // 1 more sec at most!
      if ( output.isAlive() ) {
        output.interrupt();
      }
      if ( errorDiscarder.isAlive() ) {
        errorDiscarder.interrupt();
      }      
    } catch ( IOException ioe ) {
      throw new ScriptException(ioe);
    } catch ( InterruptedException ie ) {
      throw new ScriptException(ie);
    }    
  }
  
  private abstract static class StreamReader extends Thread {
    
    private InputStream is;
    
    protected abstract void processLine(String line);
    
    public StreamReader( InputStream is ) {
      this.is = is;
    }
    
    @Override
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader( isr );
        String line = null;
        while ( (line = br.readLine()) != null ) {
          if ( interrupted() ) {
            break;
          }
          processLine( line );
        }
        br.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }       
  }
  
  private static class StreamDiscarder extends StreamReader {    
    public StreamDiscarder( InputStream is ) {
      super( is );
    }    
    protected void processLine( String line ) {
    }    
  }
  
  private static class ScriptOutputReader extends StreamReader {
    
    private StringBuffer sb = new StringBuffer();
    private boolean found = false;     
    
    public ScriptOutputReader( InputStream is ) {
      super( is );
    }
        
    protected void processLine( String cmdOut ) {
      sb.append( cmdOut );
    }
    
    @Override
    public String toString() {
      return sb.toString();
    }
  }
  
  
}
