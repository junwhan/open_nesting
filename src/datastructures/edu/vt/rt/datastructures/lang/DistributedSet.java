package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

/**
 * A distributed collection that contains no duplicate elements.
 * @author Peter DiMarco
 * @param <E> The type of element in the collection.
 */
public interface DistributedSet<E extends Serializable> {
	
	/**
	 * Adds the item to the set if not already in it.
	 * @param item The item to add to the set.
	 * @return True if item was added, else false.
	 */
	public boolean add(E value);
	/**
	 * Determines whether the item is in the set.
	 * @param item The item to look for in the set.
	 * @return True if item is in the set, else false.
	 */
	public boolean contains(E value);
	/**
	 * Creates the container needed for the Distributed, Persistent Set. Must be
	 * called before the set can be used.
	 */
	public void create();
	/**
	 * Removes the item from the set.
	 * @param item The item to remove from the set.
	 * @return True if item was removed, else false.
	 */
	public boolean remove(E value);

}
