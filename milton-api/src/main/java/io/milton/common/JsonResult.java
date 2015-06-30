/*
 *
 * Copyright 2014 McEvoy Software Ltd.
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

package io.milton.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Value object holding the result of some operation in a generic form
 * which is suitable for outputting as JSON.
 * 
 *
 * @author brad
 */
public class JsonResult {
    
    //public static String CONTENT_TYPE = "application/x-javascript; charset=utf-8";
    public static String CONTENT_TYPE = "application/json; charset=utf-8";

    public static JsonResult error(String description) {
        JsonResult r = new JsonResult(false, description);
        return r;        
    }
    
    public static JsonResult fieldError(String field, String fieldMessage) {
        JsonResult r = new JsonResult(false, "Validation error");
        r.addFieldMessage(field, fieldMessage);
        return r;
    }
    
    public static JsonResult returnData(String href, Object data) {
        JsonResult r = new JsonResult(true);
        r.setData(data);
        r.setNextHref(href);
        return r;        
    }
    
    public static JsonResult returnData(Object data) {
        JsonResult r = new JsonResult(true);
        r.setData(data);
        return r;
    }    
    
    private boolean status;
    private String nextHref;
    private List<String> messages;
    private List<FieldMessage> fieldMessages;
    private Object data;

    public JsonResult() {
    }

    public JsonResult(boolean status, String nextHref, List<String> messages, List<FieldMessage> fieldMessages) {
        this.status = status;
        this.nextHref = nextHref;
        this.messages = messages;
        this.fieldMessages = fieldMessages;
    }
    
    public JsonResult(boolean status) {
        this.status = status;
    }    
    
    public JsonResult(boolean status, String message) {
        this.status = status;
        this.messages = Arrays.asList(message);
    }        
    
    public JsonResult(boolean status, String message, String nextHref) {
        this.status = status;
        this.nextHref = nextHref;
        this.messages = Arrays.asList(message);
    }          
    
    public void addFieldMessage(String field, String message) {
        if( fieldMessages == null ) {
            fieldMessages = new ArrayList<FieldMessage>();
        }
        fieldMessages.add(new FieldMessage(field, message)); 
    }
    
    /**
     * Flag to indicate success or failure of the operation
     * 
     * @return the status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * If an object was created this should be the href of that object.
     * If the operation completed was part of a workflow then this should be the
     * href of the next step in the workflow
     * 
     * @return the nextHref
     */
    public String getNextHref() {
        return nextHref;
    }

    /**
     * @param nextHref the nextHref to set
     */
    public void setNextHref(String nextHref) {
        this.nextHref = nextHref;
    }

    /**
     * Any messages which are not specific to certain fields
     * 
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * @param messages the messages to set
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * List of messages relating to specific fields. The field name must
     * correspond to a POST variable name
     * 
     * @return the fieldMessages
     */
    public List<FieldMessage> getFieldMessages() {
        return fieldMessages;
    }

    /**
     * @param fieldMessages the fieldMessages to set
     */
    public void setFieldMessages(List<FieldMessage> fieldMessages) {
        this.fieldMessages = fieldMessages;
    }

    /**
     * Any JSON friendly object
     * @return 
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    
    
    
    /**
     * Represents a message (usually a validation error) pertaining to a field
     * 
     * The field name is the name of the POST variable which caused the error
     * 
     */
    public class FieldMessage {
        private String field;
        private String message;

        public FieldMessage() {
        }

        public FieldMessage(String field, String message) {
            this.field = field;
            this.message = message;
        }
       
        
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }                
    }
}
