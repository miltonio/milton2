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
package io.milton.http.annotated;

import io.milton.annotations.Users;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author brad
 */
public class UsersAnnotationHandler extends AbstractAnnotationHandler {

	public static final String NOT_ATTEMPTED = "NotAttempted";

	public UsersAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Users.class);
	}

	public AnnoPrincipalResource findUser(AnnoCollectionResource root, String name) throws NotAuthorizedException, BadRequestException {

		for (AnnoCollectionResource userHome : findUsersCollections(root)) {
			List<ControllerMethod> availMethods = getMethods(userHome.getSource().getClass());
			if (!availMethods.isEmpty()) {
				Resource r = userHome.child(name);
				if (r instanceof AnnoPrincipalResource) {
					return (AnnoPrincipalResource) r;
				}
			}
		}

		return null;
	}

	public List<AnnoCollectionResource> findUsersCollections(AnnoCollectionResource root) {
		try {
			// iterate over each root collection, looking for objects which have
			// a @Authenticate annotation on their ChildOf or ChildrenOf methods
			List<AnnoCollectionResource> list = new ArrayList<>();
			for (Resource col : Optional.ofNullable(root.getChildren()).orElse(List.of())) {
				if (col instanceof AnnoCollectionResource) {
					AnnoCollectionResource acr = (AnnoCollectionResource) col;
					List<ControllerMethod> availMethods = getMethods(acr.getSource().getClass());
					if (!availMethods.isEmpty()) {
						list.add(acr);
					}
				}
			}
			return list;
		} catch (NotAuthorizedException | BadRequestException e) {
			throw new RuntimeException(e);
		}
	}
}
