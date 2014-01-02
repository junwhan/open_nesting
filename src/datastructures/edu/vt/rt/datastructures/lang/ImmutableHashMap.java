package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import edu.vt.rt.datastructures.util.Box;

public class ImmutableHashMap<K, V> implements Serializable {

	private static final long serialVersionUID = -3445429659072126695L;
	private final static Object NOT_FOUND = new Object();
	private final INode root;

	public ImmutableHashMap(INode tree) {
		root = tree;
	}

	public boolean contains(K key) {
		return (root == null) ? false : (root.find(0, key.hashCode(), key) != NOT_FOUND);
	}

	public ImmutableHashMap<K, V> put(K key, V value, Box found) {
		INode newMap = ((root == null) ? BitmapIndexedNode.EMPTY : root).put(0, key.hashCode(), key, value, found);
		if (newMap == root) {
			found.value = value;
			return this;
		}
		return new ImmutableHashMap<K, V>(newMap);
	}

	public ImmutableHashMap<K, V> remove(K key, Box found) {
		if (root == null) {	//Removing from empty map gives empty map
			return this;
		}
		final INode newMap = root.remove(0, key.hashCode(), key, found);
		if (newMap == root) {
			return this;
		}
		return new ImmutableHashMap<K, V>(newMap);
	}

	private static abstract class INode implements Serializable {

		private static final long serialVersionUID = 6702613516340964413L;

		public abstract Object find(int shift, int hash, Object key);
		public abstract INode put(int shift, int hash, Object key, Object value, Box addedLeaf);
		public abstract INode remove(int shift, int hash, Object key, Box removedLeaf);

		protected static int bitpos(int hash, int shift) {
			return 1 << mask(hash, shift);
		}

		protected static INode[] cloneAndSet(INode[] array, int i, INode a) {
			//INode[] clone = array.clone();
			INode[] clone = new INode[array.length];
			System.arraycopy(array, 0, clone, 0, array.length);
			clone[i] = a;
			return clone;
		}

		protected static Object[] cloneAndSet(Object[] array, int i, Object a) {
			//Object[] clone = array.clone();
			Object[] clone = new Object[array.length];
			System.arraycopy(array, 0, clone, 0, array.length);
			clone[i] = a;
			return clone;
		}

		protected static Object[] cloneAndSet(Object[] array, int i, Object a, int j, Object b) {
			//Object[] clone = array.clone();
			Object[] clone = new Object[array.length];
			System.arraycopy(array, 0, clone, 0, array.length);
			clone[i] = a;
			clone[j] = b;
			return clone;
		}

		protected static INode createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
			int key1hash = key1.hashCode();
			if(key1hash == key2hash) {
				return new HashCollisionNode(key1hash, 2, new Object[] {key1, val1, key2, val2});
			}
			
			Object[] newArray = new Object[4];
			//First set bitmap
			int bit1 = bitpos(key1hash, shift);
			int bit2 = bitpos(key2hash, shift);
			int bitmap = bit1 | bit2;
			//Set item 1
			int index = Integer.bitCount(bitmap & (bit1 - 1));
			newArray[2*index] = key1;
			newArray[2*index+1] = val1;
			//Set item 2
			index = Integer.bitCount(bitmap & (bit2 - 1));
			newArray[2*index] = key2;
			newArray[2*index+1] = val2;
			return new BitmapIndexedNode(bitmap, newArray);

//			Box _ = new Box(null);
//			INode newRoot = BitmapIndexedNode.EMPTY.put(shift, key1hash, key1, val1, _);
//			newRoot = newRoot.put(shift, key2hash, key2, val2, _);
//			return newRoot;

			//return BitmapIndexedNode.EMPTY.put(shift, key1hash, key1, val1, _).put(shift, key2hash, key2, val2, _);
		}

		protected static int mask(int hash, int shift) {
			return (hash >>> shift) & 0x01f;
		}

		protected static Object[] removePair(Object[] array, int i) {
			Object[] newArray = new Object[array.length - 2];
			System.arraycopy(array, 0, newArray, 0, 2*i);
			System.arraycopy(array, 2*(i+1), newArray, 2*i, newArray.length - 2*i);
			return newArray;
		}
	}

	private final static class ArrayNode extends INode {
	
		private static final long serialVersionUID = 159729147075196586L;
		private final INode[] array;
		private final Integer count;

		public ArrayNode(int count, INode[] array) {
			this.array = array;
			this.count = count;
		}

		@Override
		public Object find(int shift, int hash, Object key) {
			int index = mask(hash, shift);
			INode node = array[index];
			if (node == null) {
				return NOT_FOUND;
			}
			return node.find(shift + 5, hash, key);
		}

		@Override
		public INode put(int shift, int hash, Object key, Object value, Box addedLeaf) {
			int index = mask(hash, shift);
			INode node = array[index];
			if (node == null) {	//If the index is currently empty
				return new ArrayNode(count+1, cloneAndSet(array, index, BitmapIndexedNode.EMPTY.put(shift+5, hash, key, value, addedLeaf)));
			}
			//Else add it later on in the tree
			INode newNode = node.put(shift + 5, hash, key, value, addedLeaf);
			if (newNode == null)
				return this;
			return new ArrayNode(count, cloneAndSet(array, index, newNode));
		}

		@Override
		public INode remove(int shift, int hash, Object key, Box removedLeaf) {
			int index = mask(hash, shift);
			INode node = array[index];
			if (node == null) {
				return this;
			}
			INode n = node.remove(shift + 5, hash, key, removedLeaf);
			if (n == node) {
				return this;
			}
			if (n == null) {
				if (count <= 8) { //Shrink back to BitmapIndexedNode
					return pack(index);
				}
				return new ArrayNode(count-1, cloneAndSet(array, index, n));
			}
			return new ArrayNode(count, cloneAndSet(array, index, n));
		}

		private INode pack(int index) {
			Object[] newArray = new Object[2*(count - 1)];	//Allocate space needed
			int j = 1;
			int bitmap = 0;
			for(int i = 0; i < index; i++) {
				if (array[i] != null) {
					newArray[j] = array[i];
					bitmap |= 1 << i;
					j += 2;
				}
			}
			//Nothing goes to index because we know it's null
			for(int i = index + 1; i < array.length; i++) {
				if (array[i] != null) {
					newArray[j] = array[i];
					bitmap |= 1 << i;
					j += 2;
				}
			}
			return new BitmapIndexedNode(bitmap, newArray);
		}

	}

	private final static class BitmapIndexedNode extends INode {

		private static final long serialVersionUID = -3938084200063855652L;
		private final Object[] array;
		private final Integer bitmap;
		private static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(0, new Object[0]);

		public BitmapIndexedNode(int bitmap, Object[] array) {
			this.bitmap = bitmap;
			this.array = array;
		}

		@Override
		public Object find(int shift, int hash, Object key) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0) {
				return NOT_FOUND;
			}
			int index = index(bit);
			Object keyOrNull = array[2*index];
			Object valOrNode = array[2*index+1];
			if (keyOrNull == null) {	//index is another INode
				return ((INode) valOrNode).find(shift + 5, hash, key);
			}
			if (key.equals(keyOrNull)) {
				return valOrNode;
			}
			return NOT_FOUND;
		}

		@Override
		public INode put(int shift, int hash, Object key, Object value, Box addedLeaf) {
			int bit = bitpos(hash, shift);
			int index = index(bit);
			if ((bitmap & bit) != 0) {	//We check to see if the index of the hashCode in the bitmap is set
				Object keyOrNull = array[2*index];
				Object valOrNode = array[2*index+1];
				if (keyOrNull == null) {	//If null, then valOrNode is a INode
					INode newNode = ((INode) valOrNode).put(shift + 5, hash, key, value, addedLeaf);
					if(newNode == valOrNode)
						return this;
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, newNode));
				}
				if (keyOrNull.equals(key)) {
					if (valOrNode.equals(value)) {	//If we the K-V pair is already in table
						return this;
					}
					//Else update value for key
					addedLeaf.value = valOrNode;	//Update value to traverse back to recursive call
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, value));
				}
				//trying to insert a key where a key is already at that index. Need to make a new node and check to see if HashCollisionNode is needed
				return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index, null, 2*index+1, createNode(shift + 5, keyOrNull, valOrNode, hash, key, value))); 
			}
			else {
				int bitCount = Integer.bitCount(bitmap);
				if (bitCount >= 16) {	//16 bits set means 16 K-V pairs, need to upgrade to ArrayNode
					INode[] nodes = new INode[32];
					//Find where in the new ArrayNode the BitmapIndexedNode will go
					int jdex = mask(hash, shift);
					nodes[jdex] = BitmapIndexedNode.EMPTY.put(shift + 5, hash, key, value, addedLeaf);
					int j = 0;
					for (int i = 0; i < 32; i++) {
						if (((bitmap >>> i) & 1) != 0) {	//Shift to next entry, and check to see if it's set
							if (array[j] == null)	//Copy over INode
								nodes[i] = (INode) array[j+1];
							else {	//Copy over K-V in new BitmapIndexNode
								nodes[i] = BitmapIndexedNode.EMPTY.put(shift + 5, array[j].hashCode(), array[j], array[j+1], addedLeaf);
							}
							j += 2;
						}
					}
					return new ArrayNode(bitCount+1, nodes);
				}
				else {
					//Allocate more room in the array for the new K-V pair
					Object[] newArray = new Object[2*(bitCount+1)];
					System.arraycopy(array, 0, newArray, 0, 2*index);
					newArray[2*index] = key;
					newArray[2*index+1] = value;
					System.arraycopy(array, 2*index, newArray, 2*(index+1), 2*(bitCount-index));
					return new BitmapIndexedNode(bitmap | bit, newArray);	//Update that bitmapindexednode
				}
			}
		}

		@Override
		public INode remove(int shift, int hash, Object key, Box removedLeaf) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0) {	//If not in map
				return this;
			}
			int index = index(bit);
			Object keyOrNull = array[2*index];
			Object valOrNode = array[2*index+1];
			if (keyOrNull == null) {	//Removing INode
				INode n = ((INode) valOrNode).remove(shift + 5, hash, key, removedLeaf);
				if (n == valOrNode) {
					return this;
				}
				if (n != null) {
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, n));
				}
				if (bitmap == bit) {	//If removing last node
					return null;
				}
				//If index is null but not the last element, shrink the array
				return new BitmapIndexedNode(bitmap ^ bit, removePair(array, index));
			}
			if (keyOrNull.equals(key)) {
				removedLeaf.value = valOrNode;
				if (bitmap == bit) {	//If removing last node
					return null;
				}
				return new BitmapIndexedNode(bitmap ^ bit, removePair(array, index));
			}
			return this;
		}

		private final int index(int bit) {
			return Integer.bitCount(bitmap & (bit - 1));
		}
	}

	private final static class HashCollisionNode extends INode {

		private static final long serialVersionUID = -4627487315054773684L;
		private final Integer count;
		private final Integer hash;
		private final Object[] array;

		public HashCollisionNode(int hash, int count, Object[] array) {
			this.hash = hash;
			this.count = count;
			this.array = array;
		}

		@Override
		public Object find(int shift, int hash, Object key) {
			int index = findIndex(key);
			if (index < 0) {
				return NOT_FOUND;
			}
			if (key.equals(array[index])) {
				return array[index + 1];
			}
			return NOT_FOUND;
		}

		@Override
		public INode put(int shift, int hash, Object key, Object value, Box addedLeaf) {
			if(hash == this.hash) {
				int index = findIndex(key);
				if (index != -1) {	//If it is in the list
					if(array[index + 1] == value)
						return this;
					return new HashCollisionNode(hash, count, cloneAndSet(array, index + 1, value));
				}
				//Allocate room for new K-V pair
				Object[] newArray = new Object[array.length + 2];
				System.arraycopy(array, 0, newArray, 0, array.length);
				newArray[array.length] = key;
				newArray[array.length + 1] = value;
				return new HashCollisionNode(hash, count+1, newArray);
			}
			//Put this HashCollisionNode inside a BitmapIndexedNode
			return new BitmapIndexedNode(bitpos(this.hash, shift), new Object[] {null, this}).put(shift, hash, key, value, addedLeaf);
		}

		@Override
		public INode remove(int shift, int hash, Object key, Box removedLeaf) {
			int index = findIndex(key);
			if (index == -1) {
				return this;
			}
			if (count == 1) {
				return null;
			}
			return new HashCollisionNode(hash, count - 1, removePair(array, index/2));
		}

		private int findIndex(Object key) {
			for (int i = 0; i < array.length; i+=2) {
				if(array[i].equals(key)) {
					return i;
				}
			}
			return -1;
		}
	}

}

