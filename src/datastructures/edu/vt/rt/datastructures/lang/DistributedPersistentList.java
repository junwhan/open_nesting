package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

/**
 * A Distributed, Persistent list implementation. Where the list is moved around
 * in many pieces.
 * @author Peter DiMarco
 * @param <E> The type of value for the list.
 */
public class DistributedPersistentList<E extends Serializable> implements DistributedSet<E> {

	private final String containerID;

	public DistributedPersistentList(String id) {
		this.containerID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E item) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableList<E>> container = (IDContainer<DistributedImmutableList<E>>) locator.open(containerID, "r");
		final DistributedImmutableList<E> list = container.getItem();
		if (list == null) {	//List is empty
			container.setItem(new DistributedImmutableList<E>(item));
			return true;
		}
		final DistributedImmutableList<E> newList = list.add(item);
		if (newList == list) {
			return false;
		}
		container = (IDContainer<DistributedImmutableList<E>>) locator.open(containerID, "w");
		container.setItem(newList);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(E item) {
		while (true) {
			DistributedImmutableList<E> list = getHead();
			if (list == null) {	//List is empty
				return false;
			}
			try {
				return list.contains(item);
			} catch (TransactionException e) {
				//Do nothing, retry operation
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new IDContainer<DistributedImmutableList<E>>(containerID);
	}

	@Atomic
	private DistributedImmutableList<E> getHead() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableList<E>> container = (IDContainer<DistributedImmutableList<E>>) locator.open(containerID, "r");
		return container.getItem();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E item) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableList<E>> container = (IDContainer<DistributedImmutableList<E>>) locator.open(containerID, "r");
		final DistributedImmutableList<E> list = container.getItem();
		if (list == null) {	//List is empty
			return false;
		}
		final DistributedImmutableList<E> newList = list.remove(item);
		if (newList == list) {
			return false;
		}
		container = (IDContainer<DistributedImmutableList<E>>) locator.open(containerID, "w");
		container.setItem(newList);
		return true;
	}

}
