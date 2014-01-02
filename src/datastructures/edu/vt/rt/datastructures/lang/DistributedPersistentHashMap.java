package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.Box;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

/**
 * A Distributed, Persistent HashMap implementation. Where the map is moved around
 * in many pieces.
 * @author Peter DiMarco
 * @param <K> Type of key.
 * @param <V> Type of value.
 */
public class DistributedPersistentHashMap<K extends Serializable, V extends Serializable> implements DistributedMap<K, V> {

	private final String containerID;
	private final static Object NOT_FOUND = new Object();

	public DistributedPersistentHashMap(String id) {
		this.containerID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedMap#containsKey(java.io.Serializable)
	 */
	@Override
	public boolean containsKey(K key) {
		if (key == null) {
			throw new NullPointerException();
		}
		while (true) {
			final DistributedImmutableHashMap<K, V> map = getRoot();
			if (map == null) {
				return false;
			}
			try {
				return map.find(0, key.hashCode(), key, NOT_FOUND) != NOT_FOUND;
			} catch (TransactionException e) {
				//Do nothing, retry operation
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedMap#create()
	 */
	@Override
	public void create() {
		new IDContainer<DistributedImmutableHashMap<K, V>>(containerID);
	}

	@Atomic
	public DistributedImmutableHashMap<K, V> getRoot() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableHashMap<K, V>> container = (IDContainer<DistributedImmutableHashMap<K, V>>) locator.open(containerID, "r");
		return container.getItem();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedMap#put(java.io.Serializable, java.io.Serializable)
	 */
	@Atomic
	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableHashMap<K,V>> container = (IDContainer<DistributedImmutableHashMap<K,V>>) locator.open(containerID, "r");
		final DistributedImmutableHashMap<K,V> map = container.getItem();
		Box found = new Box(null);
		if (map == null) {
			container.setItem(DistributedImmutableHashMap.EMPTY.put(0, key.hashCode(), key, value, found));
			return null;
		}
		final DistributedImmutableHashMap<K,V> newMap = map.put(0, key.hashCode(), key, value, found);
		if (map.equals(newMap)) {
			return value;	//Map didnt change, so same K-V pairing already existed
		}
		container = (IDContainer<DistributedImmutableHashMap<K,V>>) locator.open(containerID, "w");
		container.setItem(newMap);
		return (V) found.value;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedMap#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public V remove(K key) {
		if (key == null) {
			throw new NullPointerException();
		}
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableHashMap<K,V>> container = (IDContainer<DistributedImmutableHashMap<K,V>>) locator.open(containerID, "r");
		final DistributedImmutableHashMap<K,V> map = container.getItem();
		if (map == null) {
			return null;
		}
		Box found = new Box(null);
		final DistributedImmutableHashMap<K,V> newMap = map.remove(0, key.hashCode(), key, found);
		if (map.equals(newMap)) {
			return null;
		}
		container = (IDContainer<DistributedImmutableHashMap<K,V>>) locator.open(containerID, "w");
		container.setItem(newMap);
		return (V) found.value;
	}

}
