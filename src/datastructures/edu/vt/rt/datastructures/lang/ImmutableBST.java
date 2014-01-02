package edu.vt.rt.datastructures.lang;

import java.io.Serializable;
import java.util.Stack;

public class ImmutableBST<E extends Comparable<E>> implements Serializable {

	private static final long serialVersionUID = -3630917489040014354L;
	private final E data;
	private final ImmutableBST<E> left;
	private final ImmutableBST<E> right;

	public ImmutableBST(E data) {
		this(data, null, null);
	}

	private ImmutableBST(E data, ImmutableBST<E> left, ImmutableBST<E> right) {
		this.data = data;
		this.left = left;
		this.right = right;
	}

	public ImmutableBST<E> add(E value) {
		int comparison = this.data.compareTo(value);
		if (comparison < 0) {
			if (this.right == null) {
				return new ImmutableBST<E>(this.data, this.left, new ImmutableBST<E>(value));
			}
			final ImmutableBST<E> newRight = this.right.add(value);
			if (this.right.equals(newRight)) {
				return this;
			}
			return new ImmutableBST<E>(this.data, this.left, newRight);
		}
		else if (comparison > 0) {
			if (this.left == null) {
				return new ImmutableBST<E>(this.data, new ImmutableBST<E>(value), this.right);
			}
			final ImmutableBST<E> newLeft = this.left.add(value);
			if (this.left.equals(newLeft)) {
				return this;
			}
			return new ImmutableBST<E>(this.data, newLeft, this.right);
		}
		else {
			return this;	//Already in tree
		}
	}

	public boolean contains(E value) {
		int comparison = this.data.compareTo(value);
		if (comparison < 0) {
			if (this.right == null) {
				return false;
			}
			return this.right.contains(value);
		}
		else if (comparison > 0) {
			if (this.left == null) {
				return false;
			}
			return this.left.contains(value);
		}
		else {
			return true;
		}
	}

	public ImmutableBST<E> remove(E value) {
		int comparison = this.data.compareTo(value);
		if (comparison < 0) {
			if (this.right == null) {
				return this;
			}
			final ImmutableBST<E> newRight = this.right.remove(value);
			if (this.right.equals(newRight)) {
				return this;
			}
			return new ImmutableBST<E>(this.data, this.left, newRight);
		}
		else if (comparison > 0) {
			if (this.left == null) {
				return this;
			}
			final ImmutableBST<E> newLeft = this.left.remove(value);
			if (this.left.equals(newLeft)) {
				return this;
			}
			return new ImmutableBST<E>(this.data, newLeft, this.right);
		}
		else {
			if (this.left == null || this.right == null) {	//1 or 0 Children
				boolean left = (this.left != null) ? true : false;
				if (left) {
					return this.left;
				}
				else {
					return this.right;
				}
			}
			else {	//2 Children
				//Find Right Child's left most node
				Stack<ImmutableBST<E>> stack = new Stack<ImmutableBST<E>>();
				ImmutableBST<E> iter = this.right;
				while (iter.left != null) {
					stack.push(iter);
					iter = iter.left;
				}
				E newValue = iter.data;
				iter = iter.right;	//Don't lose left most child's right nodes
				while (!stack.empty()) {
					ImmutableBST<E> node = stack.pop();
					iter = new ImmutableBST<E>(node.data, iter, node.right);
				}
				//Remove node by replacing it's value
				return new ImmutableBST<E>(newValue, this.left, iter);
			}
		}
	}

}
