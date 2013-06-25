/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.annotated;

import io.milton.annotations.Users;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class UsersAnnotationHandler extends AbstractAnnotationHandler {

	public static final String NOT_ATTEMPTED = "NotAttempted";

	public UsersAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Users.class);
	}

	public AnnoPrincipalResource findUser(AnnoCollectionResource root, String name) {
		try {
			List<ControllerMethod> availMethods = getMethods(root.getSource().getClass());
			if (!availMethods.isEmpty()) {
				Resource r = root.child(name);
				if (r instanceof AnnoPrincipalResource) {
					AnnoPrincipalResource apr = (AnnoPrincipalResource) r;
					return apr;
				}
			}

			// iterate over each root collection, looking for objects which have
			// a @Users annotation on their ChildOf or ChildrenOf methods
			for (CommonResource col : root.getChildren()) {
				if (col instanceof AnnoCollectionResource) {
					AnnoCollectionResource acr = (AnnoCollectionResource) col;
					availMethods = getMethods(acr.getSource().getClass());
					if (!availMethods.isEmpty()) {
						Resource r = acr.child(name);
						if (r instanceof AnnoPrincipalResource) {
							AnnoPrincipalResource apr = (AnnoPrincipalResource) r;
							return apr;
						}
					}
				}
			}
		} catch (NotAuthorizedException e) {
			throw new RuntimeException(e);
		} catch (BadRequestException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public List<AnnoCollectionResource> findUsersCollections(AnnoCollectionResource root) {
		try {
			// iterate over each root collection, looking for objects which have
			// a @Authenticate annotation on their ChildOf or ChildrenOf methods
			List<AnnoCollectionResource> list = new ArrayList<AnnoCollectionResource>();
			for (CommonResource col : root.getChildren()) {
				if (col instanceof AnnoCollectionResource) {
					AnnoCollectionResource acr = (AnnoCollectionResource) col;
					List<ControllerMethod> availMethods = getMethods(acr.getSource().getClass());
					if (!availMethods.isEmpty()) {
						list.add(acr);
					}
				}
			}
			return list;
		} catch (NotAuthorizedException e) {
			throw new RuntimeException(e);
		} catch (BadRequestException e) {
			throw new RuntimeException(e);
		}
	}
}
