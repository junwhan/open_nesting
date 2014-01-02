package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

public class PersistentBST<E extends Comparable<E> & Serializable> implements DistributedSet<E> {

	private final String containerID;
	
	public PersistentBST(String id) {
		this.containerID = id;
	}
	
	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableBST<E>> container = (IDContainer<ImmutableBST<E>>) locator.open(containerID, "r");
		final ImmutableBST<E> tree = container.getItem();
		if (tree == null) {	//List is empty
			container.setItem(new ImmutableBST<E>(value));
			return true;
		}
		final ImmutableBST<E> newTree = tree.add(value);
		if (newTree == tree) {
			return false;
		}
		container = (IDContainer<ImmutableBST<E>>) locator.open(containerID, "w");
		container.setItem(newTree);
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(E value) {
		final ImmutableBST<E> currTree = getHead();
		if (currTree == null) {	//List is empty
			return false;
		}
		return currTree.contains(value);
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new IDContainer<ImmutableBST<E>>(containerID);
	}
	
	@Atomic
	private ImmutableBST<E> getHead() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableBST<E>> container = (IDContainer<ImmutableBST<E>>) locator.open(containerID, "r");
		return container.getItem();
	}

	/* (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<ImmutableBST<E>> container = (IDContainer<ImmutableBST<E>>) locator.open(containerID, "r");
		final ImmutableBST<E> tree = container.getItem();
		if (tree == null) {	//List is empty
			return false;
		}
		final ImmutableBST<E> newTree = tree.remove(value);
		if (newTree == tree) {
			return false;
		}
		container = (IDContainer<ImmutableBST<E>>) locator.open(containerID, "w");
		container.setItem(newTree);
		return true;
	}

}
