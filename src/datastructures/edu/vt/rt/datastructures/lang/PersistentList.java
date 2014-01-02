package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

/**
 * A Distributed, Persistent list. Where the entire list is moved at once.
 * @author Peter DiMarco
 * @param <E> The type of the value.
 */
public class PersistentList<E extends Serializable> implements DistributedSet<E> {

	private final String containerID;

	public PersistentList(String id) {
		this.containerID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableList<E>> container = (IDContainer<ImmutableList<E>>) locator.open(containerID, "r");
		final ImmutableList<E> list = container.getItem();
		if (list == null) {	//List is empty
			container.setItem(new ImmutableList<E>(value));
			return true;
		}
		final ImmutableList<E> newList = list.add(value);
		if (newList == list) {
			return false;
		}
		container = (IDContainer<ImmutableList<E>>) locator.open(containerID, "w");
		container.setItem(newList);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(E value) {
		final ImmutableList<E> currList = getHead();
		if (currList == null) {	//List is empty
			return false;
		}
		return currList.contains(value);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new IDContainer<ImmutableList<E>>(containerID);
	}

	@Atomic
	private ImmutableList<E> getHead() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableList<E>> container = (IDContainer<ImmutableList<E>>) locator.open(containerID, "r");
		return container.getItem();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableList<E>> container = (IDContainer<ImmutableList<E>>) locator.open(containerID, "r");
		final ImmutableList<E> list = container.getItem();
		if (list == null) {	//List is empty
			return false;
		}
		final ImmutableList<E> newList = list.remove(value);
		if (newList == list) {
			return false;
		}
		container = (IDContainer<ImmutableList<E>>) locator.open(containerID, "w");
		container.setItem(newList);
		return true;
	}

}
