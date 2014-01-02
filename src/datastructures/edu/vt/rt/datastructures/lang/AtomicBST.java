package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;

public class AtomicBST<E extends Comparable<E> & Serializable> implements DistributedSet<E> {

	private final String treeID;

	public AtomicBST(String id) {
		this.treeID = id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		BST<E> tree = (BST<E>) locator.open(treeID, "w");
		return tree.add(value);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean contains(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		BST<E> tree = (BST<E>) locator.open(treeID, "r");
		return tree.contains(value);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new BST<E>(treeID);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		BST<E> tree = (BST<E>) locator.open(treeID, "w");
		return tree.remove(value);
	}

}
