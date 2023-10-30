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
import java.util.Collections;
import java.util.List;

/**
 * Value object holding the result of some operation in a generic form
 * which is suitable for outputting as JSON.
 *
 * @author brad
 */
public class JsonResult {

    //public static String CONTENT_TYPE = "application/x-javascript; charset=utf-8";
    public static String CONTENT_TYPE = "application/json; charset=utf-8";

    /**
     * Method which creates an instance of {@link JsonResult} class representing an error.
     * @param description Description of an error.
     * @return {@link JsonResult} instance.
     */
    public static JsonResult error(String description) {
        return new JsonResult(false, description);
    }

    /**
     * Method which creates an instance of {@link JsonResult} class representing a field validation error.
     * @param field Field under error.
     * @param fieldMessage Message describing the validation issues.
     * @return {@link JsonResult} instance.
     */
    public static JsonResult fieldError(String field, String fieldMessage) {
        JsonResult r = new JsonResult(false, "Validation error");
        r.addFieldMessage(field, fieldMessage);
        return r;
    }

    /**
     * Method which creates an instance of {@link JsonResult} class representing an object and its href.
     * @param href Href of the object.
     * @param data Object.
     * @return {@link JsonResult} instance.
     */
    public static JsonResult returnData(String href, Object data) {
        JsonResult r = new JsonResult(true);
        r.setData(data);
        r.setNextHref(href);
        return r;
    }

    /**
     * Method which creates an instance of {@link JsonResult} class representing an object.
     * @param data Object.
     * @return {@link JsonResult} instance.
     */
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

    /**
     * Creates an instance of {@link JsonResult} class.
     * @param status boolean status.
     * @param nextHref next href of the object.
     * @param messages collection of the messages.
     * @param fieldMessages collection of the field messages if exists.
     */
    public JsonResult(boolean status, String nextHref, List<String> messages, List<FieldMessage> fieldMessages) {
        this.status = status;
        this.nextHref = nextHref;
        this.messages = messages;
        this.fieldMessages = fieldMessages;
    }

    /**
     * Creates an instance of {@link JsonResult} class.
     * @param status boolean status.
     */
    public JsonResult(boolean status) {
        this.status = status;
    }

    /**
     * Creates an instance of {@link JsonResult} class.
     * @param status boolean status.
     * @param message message.
     */
    public JsonResult(boolean status, String message) {
        this.status = status;
        this.messages = Collections.singletonList(message);
    }

    /**
     * Creates an instance of {@link JsonResult} class.
     * @param status boolean status.
     * @param message message.
     * @param nextHref next href of the object.
     */
    public JsonResult(boolean status, String message, String nextHref) {
        this.status = status;
        this.nextHref = nextHref;
        this.messages = Collections.singletonList(message);
    }

    /**
     * Adds a validation message to a field.
     * @param field Field to add a validation message.
     * @param message Validation message.
     */
    public void addFieldMessage(String field, String message) {
        if (fieldMessages == null) {
            fieldMessages = new ArrayList<>();
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
     *
     * @return
     */
    public Object getData() {
        return data;
    }

    /**
     * Set any JSON friendly object.
     * @param data any JSON friendly object.
     */
    public void setData(Object data) {
        this.data = data;
    }


    /**
     * Represents a message (usually a validation error) pertaining to a field
     * <p>
     * The field name is the name of the POST variable which caused the error
     */
    public static class FieldMessage {
        private String field;
        private String message;

        public FieldMessage() {
        }

        /**
         * Field/message mapping.
         * @param field Field name.
         * @param message Field validation message.
         */
        public FieldMessage(String field, String message) {
            this.field = field;
            this.message = message;
        }


        /**
         * Returns a field name;
         * @return field name.
         */
        public String getField() {
            return field;
        }

        /**
         * Sets a field name.
         * @param field Field name.
         */
        public void setField(String field) {
            this.field = field;
        }

        /**
         * Retuens a validation message for a field.
         * @return Validation message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets validation message.
         * @param message Validation message.
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
