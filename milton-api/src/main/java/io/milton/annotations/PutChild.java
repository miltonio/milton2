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
package io.milton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which creates a child resource containing the given content,
 * or replaces an existing child's content with new data<br/><br/>
 * 
 * <p>Return type - Must return the created POJO object, or the updated child
 * 
 * <p>Parameters:
 * <ol>
 *  <li>first arg must be the object to update OR..</li>
 *  <li>the parent object, followed by the name of the child resource</li> 
 *  <li>items following that may include (in preferential order)
 *      <ol>
 *      <li>inputStream or byte[]</li>
 *      <li>contentType</li>
 *      <li>content length as Long</li>
 *     </ol>
 * </li>
 * </ol>
 * 
 * <p>
 * Example: Creating a new child resource
 * 
 * <pre>
 *    {@literal @}PutChild
 *    public MyDatabase.FileContentItem createFile(MyDatabase.FolderContentItem parent, String name, byte[] bytes) {
 *        FileContentItem file = parent.addFile(name);
 *        file.setBytes(bytes);
 *        return file;
 *    }
 * </pre>
 *
 *
 * <p>
 * Example: updating an existing resource
 * <pre>
 *    {@literal @}PutChild
 *    public Image uploadImage(Image image, byte[] bytes) throws IOException {
 *        File fRoot = getContentRoot();
 *        File content = new File(fRoot, image.getFileName());
 *        FileUtils.writeByteArrayToFile(content, bytes);
 *        return image;
 *    }
 * </pre>
 * 
 * <p>
 * Example: Creating a new child resource from an inputstream. This uses the contentLength and contentType
 * headers, but please note these are not always sent by client apps.
 * 
 * <pre>
 *    {@literal @}PutChild
 *    public MyDatabase.FileContentItem createFile(MyDatabase.FolderContentItem parent, String name, InputStream in, Long contentLength, String contentType) {
 *      ...
 *    }
 * </pre>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PutChild {
    
}
