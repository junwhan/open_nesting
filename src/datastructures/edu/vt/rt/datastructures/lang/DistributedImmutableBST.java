package edu.vt.rt.datastructures.lang;

import java.util.Stack;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A Distributed, Immutable BST for which can be encapsulated by the Distributed,
 * Persistent BST. This class is not intended for direct use.
 * @author Peter DiMarco
 * @param <E> The type of value for the BST. Extends Comparable to determine
 * tree position.
 */
public class DistributedImmutableBST<E extends Comparable<E>> implements IDistinguishable {
	
	private static final long serialVersionUID = 2327348466220205802L;
	private final E data;
	private final String id;
	private final String leftID;
	private final String rightID;

	public DistributedImmutableBST(E data) {
		this(data, null, null);
	}

	private DistributedImmutableBST(E data, String left, String right) {
		this.data = data;
		this.id = Long.toString(System.currentTimeMillis() + this.hashCode());
		this.leftID = left;
		this.rightID = right;
	}

	public DistributedImmutableBST<E> add(E item) {
		int comparison = this.data.compareTo(item);
		if (comparison < 0) {	//Go right
			if (this.rightID == null) {			
				return new DistributedImmutableBST<E>(this.data, this.leftID, (String) new DistributedImmutableBST<E>(item).getId());
			}
			DirectoryManager locator = HyFlow.getLocator();
			final DistributedImmutableBST<E> right = (DistributedImmutableBST<E>) locator.open(this.rightID, "r");
			final DistributedImmutableBST<E> newRight = right.add(item);
			if (right.equals(newRight)) {
				return this;
			}
			return new DistributedImmutableBST<E>(this.data, this.leftID, (String) newRight.getId());
		}
		else if (comparison > 0) {	//Go left
			if (this.leftID == null) {
				return new DistributedImmutableBST<E>(this.data, (String) new DistributedImmutableBST<E>(item).getId(), this.rightID);
			}
			DirectoryManager locator = HyFlow.getLocator();
			final DistributedImmutableBST<E> left = (DistributedImmutableBST<E>) locator.open(this.leftID, "r");
			final DistributedImmutableBST<E> newLeft = left.add(item);
			if (left.equals(newLeft)) {
				return this;
			}
			return new DistributedImmutableBST<E>(this.data, (String) newLeft.getId(), this.rightID);
		}
		else {
			return this;	//Already in tree
		}
	}

	public boolean contains(E value) {
		int comparison = this.data.compareTo(value);
		if (comparison < 0) {
			if (this.rightID == null) {
				return false;
			}
			DirectoryManager locator = HyFlow.getLocator();
			return ((DistributedImmutableBST<E>) locator.open(this.rightID, "r")).contains(value);
		}
		else if (comparison > 0) {
			if (this.leftID == null) {
				return false;
			}
			DirectoryManager locator = HyFlow.getLocator();
			return ((DistributedImmutableBST<E>) locator.open(this.leftID, "r")).contains(value);
		}
		else {
			return true;
		}
	}

	@Override
	public Object getId() {
		return id;
	}

	public DistributedImmutableBST<E> remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		int comparison = this.data.compareTo(value);
		if (comparison < 0) {
			if (this.rightID == null) {
				return this;
			}
			final DistributedImmutableBST<E> right = (DistributedImmutableBST<E>) locator.open(this.rightID, "r");			
			final DistributedImmutableBST<E> newRight = right.remove(value);
			if (right.equals(newRight)) {
				return this;
			}
			return new DistributedImmutableBST<E>(this.data, this.leftID, (newRight == null) ? null : (String) newRight.getId());
		}
		else if (comparison > 0) {
			if (this.leftID == null) {
				return this;
			}
			final DistributedImmutableBST<E> left = (DistributedImmutableBST<E>) locator.open(this.leftID, "r");			
			final DistributedImmutableBST<E> newLeft = left.remove(value);
			if (left.equals(newLeft)) {
				return this;
			}
			return new DistributedImmutableBST<E>(this.data, (newLeft == null) ? null : (String) newLeft.getId(), this.rightID);
		}
		else {
			locator.delete(this);
			if (this.leftID == null && this.rightID == null) {	//0 Children
				return null;
			}
			if (this.leftID == null || this.rightID == null) {	//1 Child
				return (DistributedImmutableBST<E>)((this.leftID != null) ? locator.open(this.leftID, "r") : locator.open(this.rightID, "r"));
			}
			else {	//2 Children
				//Find Right Child's left most node
				Stack<DistributedImmutableBST<E>> stack = new Stack<DistributedImmutableBST<E>>();
				DistributedImmutableBST<E> iter = (DistributedImmutableBST<E>) locator.open(this.rightID, "r");
				while (iter.leftID != null) {
					stack.push(iter);
					iter = (DistributedImmutableBST<E>) locator.open(iter.leftID, "r");
				}
				E newValue = iter.data;
				iter = (DistributedImmutableBST<E>) locator.open(iter.rightID, "r");	//Don't lose left most child's right nodes
				while (!stack.empty()) {
					DistributedImmutableBST<E> node = stack.pop();
					iter = new DistributedImmutableBST<E>(node.data, (String) iter.getId(), node.rightID);
					locator.delete(node);
				}
				//Remove node by replacing it's value
				return new DistributedImmutableBST<E>(newValue, this.leftID, (String) iter.getId());
			}
		}
	}

}
