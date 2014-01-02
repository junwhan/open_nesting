package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;

import edu.vt.rt.datastructures.util.Box;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

public class PersistentHashSet<E extends Serializable> implements DistributedSet<E> {
	
	private final String containerID;

	public PersistentHashSet(String id) {
		this.containerID = id;
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableHashMap<E, E>> container = (IDContainer<ImmutableHashMap<E, E>>) locator.open(containerID, "r");
		final ImmutableHashMap<E, E> map = container.getItem();
		Box found = new Box(null);
		final ImmutableHashMap<E, E> newMap = map.put(value, value, found);
		if (found.value != null) {
			return false;
		}
		container = (IDContainer<ImmutableHashMap<E, E>>) locator.open(containerID, "w");
		container.setItem(newMap);
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean contains(E value) {
		final ImmutableHashMap<E, E> currMap = getHead();
		return currMap.contains(value);
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		IDContainer<ImmutableHashMap<E, E>> container = new IDContainer<ImmutableHashMap<E, E>>(containerID);
		container.setItem(new ImmutableHashMap<E, E>(null));
	}
	
	@Atomic
	private ImmutableHashMap<E, E> getHead() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableHashMap<E, E>> container = (IDContainer<ImmutableHashMap<E, E>>) locator.open(containerID, "r");
		return container.getItem();
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableHashMap<E, E>> container = (IDContainer<ImmutableHashMap<E, E>>) locator.open(containerID, "r");
		final ImmutableHashMap<E, E> map = container.getItem();
		Box found = new Box(null);
		final ImmutableHashMap<E, E> newMap = map.remove(value, found);
		if (found.value == null) {
			return false;
		}
		container = (IDContainer<ImmutableHashMap<E, E>>) locator.open(containerID, "w");
		container.setItem(newMap);
		return true;
	}

}
