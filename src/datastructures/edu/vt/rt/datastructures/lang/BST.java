package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import edu.vt.rt.hyflow.core.IDistinguishable;

public class BST<E extends Comparable<E>> implements IDistinguishable {

	private static final long serialVersionUID = -4212510042431314939L;
	public final String id;
	public Node root;
	
	private class Node implements Serializable {

		private static final long serialVersionUID = 201263424529152106L;
		private E value;
		private Node left;
		private Node right;
		
		public Node(E value) {
			this.value = value;
		}
		
	}
	
	public BST(String id) {
		this.id = id;
	}
	
	public boolean add(E value) {
		if (root == null) {
			root = new Node(value);
			return true;
		}
		Node pred = null;
		Node iter = root;
		boolean lastLeft = false;
		while (iter != null) {	//while not at leaf
			pred = iter;
			int comparison = root.value.compareTo(value);
			if (comparison < 0) {
				iter = iter.right;
				lastLeft = false;
			}
			else if (comparison > 0) {
				iter = iter.left;
				lastLeft = true;
			}
			else
				return true;	//Already in tree
		}
		if (lastLeft)
			pred.left = new Node(value);
		else
			pred.right = new Node(value);
		root = root;	//Needed to have change be recognized by TM system
		return true;
	}
	
	public boolean contains(E value) {
		Node iter = root;
		while (iter != null) {
			int comparison = iter.value.compareTo(value);
			if (comparison < 0)
				iter = iter.right;
			else if (comparison > 0)
				iter = iter.left;
			else
				return true;
		}
		return false;
	}
	
	@Override
	public Object getId() {
		return id;
	}
	
	public boolean remove(E value) {
		Node pred = null;
		Node curr = root;
		boolean lastLeft = false;
		while (curr != null) {
			int comparison = curr.value.compareTo(value);
			if (comparison < 0) {
				pred = curr;
				curr = curr.right;
				lastLeft = false;
			}
			else if (comparison > 0) {
				pred = curr;
				curr = curr.left;
				lastLeft = true;
			}
			else {
				if (pred == null)	//Root node
					root = null;
				else if (curr.left != null && curr.right != null) {	//2 Children
					//Get right child's left most leaf
					Node parent = curr;
					Node iter = parent.right;
					while (iter.left != null) {
						parent = iter;
						iter = iter.left;
					}
					//Copy value to curr
					curr.value = iter.value;
					//Remove old node
					if (iter == parent.right)
						parent.right = curr.right;
					else
						parent.left = curr.right;
				}
				else {	//1 or 0 Children
					boolean leftChild = (curr.left != null) ? true : false;
					if (lastLeft)
						pred.left = (leftChild) ? curr.left : curr.right;
					else
						pred.right = (leftChild) ? curr.left : curr.right;
				}
				root = root;	//Needed to have change be recognized by TM system
				return true;
			}
		}
		return false;
	}

}
