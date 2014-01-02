package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

/**
 * A distributed object that maps keys to values.
 * @author Peter DiMarco
 * @param <K> The type of key.
 * @param <V> The type of value.
 */
public interface DistributedMap<K extends Serializable, V extends Serializable> {
	
	/**
	 * Determines whether the key is in the map.
	 * @param key The key to search the map for.
	 * @return True if the key is in the map, else false.
	 */
	public boolean containsKey(K key);
	/**
	 * Creates the container needed for the Distributed, Persistent Map. Must be
	 * called before the map can be used.
	 */
	public void create();
	/**
	 * Adds the Key-Value pair to the map. Replacing the current value if the Key
	 * is already in the map.
	 * @param key The key to add to the map.
	 * @param value The value to associate with the key.
	 * @return The previous value mapped to key, null if key doesn't exist.
	 */
	public V put(K key, V value);
	/**
	 * Removes the Key and its mapped value from the map.
	 * @param key The key to remove from the map.
	 * @return The value associated with key when it was removed.
	 */
	public V remove(K key);

}
