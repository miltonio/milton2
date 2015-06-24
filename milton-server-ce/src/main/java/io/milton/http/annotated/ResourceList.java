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

import io.milton.resource.CollectionResource;
import java.util.*;
import io.milton.resource.Resource;

/**
 *
 * @author brad
 */
public class ResourceList extends ArrayList<CommonResource> {

	private static final long serialVersionUID = 1L;
	private final Map<String, CommonResource> map = new HashMap<String, CommonResource>();

	public ResourceList() {
	}

	public ResourceList(AnnoResource[] array) {
		addAll(Arrays.asList(array));
	}

	public ResourceList(ResourceList copyFrom) {
		super(copyFrom);
	}

	public ResourceList getDirs() {
		ResourceList list = new ResourceList();
		for( CommonResource cr : this ) {
			if( cr instanceof CollectionResource) {
				list.add(cr);
			}
		}
		return list;		
	}
	
	public ResourceList getFiles() {
		ResourceList list = new ResourceList();
		for( CommonResource cr : this ) {
			if( !(cr instanceof CollectionResource)) {
				list.add(cr);
			}
		}
		return list;		
	}	
	
	@Override
	public boolean add(CommonResource e) {
		if (e == null) {
			throw new NullPointerException("Attempt to add null node");
		}
		if (e.getName() == null) {
			throw new NullPointerException("Attempt to add resource with null name: " + e.getClass().getName());
		}
		map.put(e.getName(), e);
		boolean b = super.add(e);
		return b;
	}

	/**
	 * Just adds the elements in the given list to this list and returns list to
	 * make it suitable for chaining and use from velocity
	 *
	 * @param otherList
	 * @return
	 */
	public ResourceList add(ResourceList otherList) {
		addAll(otherList);
		return this;
	}

	public CommonResource get(String name) {
		return map.get(name);
	}
	
	public Resource remove(String name ) {
		CommonResource r = map.remove(name);
		if( r != null ) {
			super.remove(r);
		}
		return r;
	}

	public boolean hasChild(String name) {
		return get(name) != null;
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Resource) {
			Resource e = (Resource) o;
			map.remove(e.getName());
		}
		return super.remove(o);
	}

	public Resource getFirst() {
		if (isEmpty()) {
			return null;
		}
		return this.get(0);
	}

	public Resource getLast() {
		if (this.size() > 0) {
			return this.get(this.size() - 1);
		} else {
			return null;
		}
	}

	public Resource getRandom() {
		int l = this.size();
		if (l == 0) {
			return null;
		}

		List<Resource> list = new ArrayList<Resource>();
		for (Resource res : this) {
			list.add(res);
		}
		if (list.isEmpty()) {
			return null;
		}

		Random rnd = new Random();
		int pos = rnd.nextInt(list.size());
		return list.get(pos);
	}

	public ResourceList getReverse() {
		ResourceList list = new ResourceList(this);
		Collections.reverse(list);
		return list;
	}

	public ResourceList getSortByModifiedDate() {
		ResourceList list = new ResourceList(this);
		Collections.sort(list, new Comparator<Resource>() {
			@Override
			public int compare(Resource o1, Resource o2) {
				Date dt1 = o1.getModifiedDate();
				Date dt2 = o2.getModifiedDate();
				if (dt1 == null) {
					return -1;
				}
				return -1 * dt1.compareTo(dt2);
			}
		});
		return list;
	}

	public ResourceList getSortByName() {
		ResourceList list = new ResourceList(this);
		Collections.sort(list, new Comparator<Resource>() {
			@Override
			public int compare(Resource o1, Resource o2) {
				String n1 = o1.getName();
				String n2 = o2.getName();
				return n1.compareTo(n2);
			}
		});
		return list;
	}

	public ResourceList getRandomSort() {
		AnnoResource[] array = new AnnoResource[this.size()];
		this.toArray(array);

		Random rng = new Random();   // i.e., java.util.Random.
		int n = array.length;        // The number of items left to shuffle (loop invariant).
		while (n > 1) {
			int k = rng.nextInt(n);  // 0 <= k < n.
			n--;                     // n is now the last pertinent index;
			AnnoResource temp = array[n];     // swap array[n] with array[k] (does nothing if k == n).
			array[n] = array[k];
			array[k] = temp;
		}
		ResourceList newList = new ResourceList(array);
		return newList;
	}

	public ResourceList exclude(String s) {
		return _exclude(s);
	}

	public ResourceList exclude(String s1, String s2) {
		return _exclude(s1, s2);
	}

	public ResourceList exclude(String s1, String s2, String s3) {
		return _exclude(s1, s2, s3);
	}

	public ResourceList exclude(String s1, String s2, String s3, String s4) {
		return _exclude(s1, s2, s3, s4);
	}

	public ResourceList _exclude(String... s) {
		ResourceList newList = new ResourceList(this);
		Iterator<CommonResource> it = newList.iterator();
		while (it.hasNext()) {
			Resource ct = it.next();
			if (contains(s, ct.getName())) {
				it.remove();
			}
		}
		return newList;
	}

	private boolean contains(String[] arr, String name) {
		for (String s : arr) {
			if (name.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a new list where elements satisfy is(s)
	 *
	 * @param s
	 * @return
	 */
	public ResourceList ofType(String s) {
		ResourceList newList = new ResourceList(this);
		Iterator<CommonResource> it = newList.iterator();
		while (it.hasNext()) {
			CommonResource ct = it.next();
			if (!ct.is(s)) {
				it.remove();
			}
		}
		return newList;
	}

	/**
	 * Return a new list with a size no greater then the given argument
	 *
	 * @param maxSize - the maximum number of elements in the new list
	 * @return
	 */
	public ResourceList truncate(int maxSize) {
		ResourceList list = new ResourceList();
		for (int i = 0; i < maxSize && i < size(); i++) {
			list.add(get(i));
		}
		return list;
	}

	public Map<String, ResourceList> getOfType() {
		return new ChildrenOfTypeMap(this);
	}

	/**
	 * Returns the next item after the one given. If the given argument is null,
	 * returns the first item in the list
	 *
	 * @param from
	 * @return
	 */
	public Resource next(Resource from) {
		if (from == null) {
			return getFirst();
		} else {
			boolean found = false;
			for (Resource r : this) {
				if (found) {
					return r;
				}
				if (r == from) {
					found = true;
				}
			}
			return null;
		}
	}

	public ResourceList closest(String type) {
		ResourceList l = new ResourceList();
		for( CommonResource r : this) {
			while( r != null ) {
				if( r.is(type)) {
					l.add(r);
					break;
				}
				r = r.getParent();
			}
		}
		return l;
	}
	
    
    public Map<String,CommonResource> getMap() {
        return map;
    }
    	
	
//	public ResourceList find(String path, String type) {
//		ResourceList l = new ResourceList();
//		for( CommonResource r : this) {
//			if( r.is(type)) {
//				l.add(r);
//			}
//		}
//		return l;
//	}
	
}
