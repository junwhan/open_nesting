package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.IDContainer;
import edu.vt.rt.hyflow.HyFlow;

/**
 * A Distributed, Persistent BST implementation.
 * @author Peter DiMarco
 * @param <E> The type of value for the list. Extends Comparable to determine
 * tree position.
 */
public class DistributedPersistentBST<E extends Comparable<E> & Serializable> implements DistributedSet<E> {

	private final String containerID;

	public DistributedPersistentBST(String id) {
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
		IDContainer<DistributedImmutableBST<E>> container = (IDContainer<DistributedImmutableBST<E>>) locator.open(containerID, "r");
		final DistributedImmutableBST<E> tree = container.getItem();
		if (tree == null) {
			container.setItem(new DistributedImmutableBST(item));
			return true;
		}
		final DistributedImmutableBST<E> newTree = tree.add(item);
		if (newTree == tree) {
			return false;
		}
		container = (IDContainer<DistributedImmutableBST<E>>) locator.open(containerID, "w");
		container.setItem(newTree);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(E item) {
		while (true) {
			DistributedImmutableBST<E> tree = getRoot();
			if (tree == null) {
				return false;
			}
			try {
				return tree.contains(item);
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
		new IDContainer<DistributedImmutableBST<E>>(containerID);
	}

	@Atomic
	private DistributedImmutableBST<E> getRoot() {
		DirectoryManager locator = HyFlow.getLocator();
		IDContainer<DistributedImmutableBST<E>> container = (IDContainer<DistributedImmutableBST<E>>) locator.open(containerID, "r");
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
		IDContainer<DistributedImmutableBST<E>> container = (IDContainer<DistributedImmutableBST<E>>) locator.open(containerID, "r");
		final DistributedImmutableBST<E> tree = container.getItem();
		if (tree == null) {
			return false;
		}
		final DistributedImmutableBST<E> newTree = tree.remove(item);
		if (newTree == tree) {
			return false;
		}
		container = (IDContainer<DistributedImmutableBST<E>>) locator.open(containerID, "w");
		container.setItem(newTree);
		return true;
	}

}
