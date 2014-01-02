package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;

/**
 * An Distributed, Atomic list. Where the entire list is moved as one piece.
 * @author Peter DiMarco
 * @param <E> The type of value
 */
public class AtomicList<E extends Serializable> implements DistributedSet<E> {

	private final String listID;

	public AtomicList(String id) {
		this.listID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		List<E> list = (List<E>) locator.open(listID, "w");
		return list.add(value);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean contains(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		List<E> list = (List<E>) locator.open(listID, "r");
		return list.contains(value);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new List<E>(listID);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		List<E> list = (List<E>) locator.open(listID, "w");
		return list.remove(value);
	}

}
