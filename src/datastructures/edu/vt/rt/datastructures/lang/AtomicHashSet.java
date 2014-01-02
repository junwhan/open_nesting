package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;

public class AtomicHashSet<E extends Serializable> implements DistributedSet<E> {

	private final String setID;

	public AtomicHashSet(String id) {
		this.setID = id;
	}
	
	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		HashSet<E> set = (HashSet<E>) locator.open(setID, "w");
		return set.add(value);
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean contains(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		HashSet<E> set = (HashSet<E>) locator.open(setID, "r");
		return set.contains(value);
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new HashSet<E>(setID);
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		HashSet<E> set = (HashSet<E>) locator.open(setID, "w");
		return set.remove(value);
	}

}
