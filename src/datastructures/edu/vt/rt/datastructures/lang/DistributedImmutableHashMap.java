package edu.vt.rt.datastructures.lang;

import aleph.dir.DirectoryManager;
import edu.vt.rt.datastructures.util.Box;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A Distributed, Immutable BST for which can be encapsulated by a Distributed,
 * Persistent data structure. This was originally by Phil Bagwell, then made
 * persistant by Rich Hickey, then made distributed. This class is not intended
 * for direct use.
 * @author Peter DiMarco
 * @param <K> The type of key for the HashMap.
 * @param <V> The type of value for the HashMap.
 */
public abstract class DistributedImmutableHashMap<K, V> implements IDistinguishable {
	
	private static final long serialVersionUID = -2268281041063924858L;
	public static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(0, new Object[0]);
	private final String id;
	
	public DistributedImmutableHashMap() {
		this.id = Long.toString(System.currentTimeMillis() + this.hashCode());
	}

	public abstract Object find(int shift, int hash, Object key, Object notFound);
	public abstract DistributedImmutableHashMap<K, V> put(int shift, int hash, Object key, Object value, Box addedLeaf);
	public abstract DistributedImmutableHashMap<K, V> remove(int shift, int hash, Object key, Box removedLeaf);

	protected static int bitpos(int hash, int shift) {
		return 1 << mask(hash, shift);
	}
	
	protected static String[] cloneAndSet(String[] array, int i, String a) {
		String[] clone = new String[array.length];
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

	protected static DistributedImmutableHashMap createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
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
				
//		Box _ = new Box(null);
//		DIHashMap newRoot = BitmapIndexedNode.EMPTY.put(shift, key1hash, key1, val1, _);
//		newRoot = newRoot.put(shift, key2hash, key2, val2, _);
//		return newRoot;

//		return BitmapIndexedNode.EMPTY.put(shift, key1hash, key1, val1, _).put(shift, key2hash, key2, val2, _);
	}
	
	@Override
	public Object getId() {
		return id;
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

	/**
	 * This class holds pointers to other nodes only.
	 */
	final static class ArrayNode extends DistributedImmutableHashMap {
		
		private static final long serialVersionUID = 2165701493692989114L;
		private final String[] array;
		private final Integer count;

		public ArrayNode(int count, String[] array) {
			super();
			this.array = array;
			this.count = count;
		}

		@Override
		public Object find(int shift, int hash, Object key, Object notFound) {
			int index = mask(hash, shift);
			String node = array[index];
			if (node == null) {
				return notFound;
			}
			DirectoryManager locator = HyFlow.getLocator();
			return ((DistributedImmutableHashMap) locator.open(node, "r")).find(shift+5, hash, key, notFound);
		}

		@Override
		public DistributedImmutableHashMap put(int shift, int hash, Object key, Object value, Box addedLeaf) {
			DirectoryManager locator = HyFlow.getLocator();
			int index = mask(hash, shift);
			String node = array[index];
			if (node == null) {	//If the index is currently empty
				locator.delete(this);
				return new ArrayNode(count+1, cloneAndSet(array, index, (String) (BitmapIndexedNode.EMPTY.put(shift+5, hash, key, value, addedLeaf)).getId()));
			}
			//Else add it later on in the tree
			final DistributedImmutableHashMap newNode = ((DistributedImmutableHashMap) locator.open(node, "r")).put(shift+5, hash, key, value, addedLeaf);
			if (newNode == null) {
				return this;
			}
			locator.delete(this);
			return new ArrayNode(count, cloneAndSet(array, index, (String) newNode.getId()));
		}

		@Override
		public DistributedImmutableHashMap remove(int shift, int hash, Object key, Box removedLeaf) {
			int index = mask(hash, shift);
			String node = array[index];
			if (node == null) {
				return this;
			}
			DirectoryManager locator = HyFlow.getLocator();
			final DistributedImmutableHashMap n = ((DistributedImmutableHashMap) locator.open(node, "r")).remove(shift+5, hash, key, removedLeaf);
			if (n == null) {
				locator.delete(this);
				if (count <= 8) { //Shrink back to BitmapIndexedNode
					return pack(index);
				}
				return new ArrayNode(count-1, cloneAndSet(array, index, null));
			}
			final String nID = (String) n.getId();
			if (node.equals(nID)) {
				return this;
			}
			locator.delete(this);
			return new ArrayNode(count, cloneAndSet(array, index, nID));
		}

		/**
		 * Pack all the Node pointers into a smaller BitmapIndexedNode
		 * @param index The index to ignore.
		 * @return A BitmapIndexedNode with pointers to other nodes.
		 */
		private DistributedImmutableHashMap pack(int index) {
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

	/**
	 * This class holds pointers and K-V pairings.
	 */
	final static class BitmapIndexedNode extends DistributedImmutableHashMap {
		
		private static final long serialVersionUID = 3367046823540899013L;
		private final Object[] array;
		private final Integer bitmap;

		public BitmapIndexedNode(int bitmap, Object[] array) {
			super();
			this.bitmap = bitmap;
			this.array = array;
		}

		@Override
		public Object find(int shift, int hash, Object key, Object notFound) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0) {
				return notFound;
			}
			int index = index(bit);
			Object keyOrNull = array[2*index];
			Object valOrNode = array[2*index+1];
			if (keyOrNull == null) {	//index is another INode
				DirectoryManager locator = HyFlow.getLocator();
				return ((DistributedImmutableHashMap) locator.open((String)valOrNode, "r")).find(shift+5, hash, key, notFound);
			}
			if (key.equals(keyOrNull)) {
				return valOrNode;
			}
			return notFound;
		}

		@Override
		public DistributedImmutableHashMap put(int shift, int hash, Object key, Object value, Box addedLeaf) {
			int bit = bitpos(hash, shift);
			int index = index(bit);
			DirectoryManager locator = HyFlow.getLocator();
			if ((bitmap & bit) != 0) {	//We check to see if the index of the hashCode in the bitmap is set
				Object keyOrNull = array[2*index];
				Object valOrNode = array[2*index+1];
				if (keyOrNull == null) {	//If null, then valOrNode is a INode
					final DistributedImmutableHashMap newNode = ((DistributedImmutableHashMap) locator.open((String)valOrNode, "r")).put(shift+5, hash, key, value, addedLeaf);
					final String newNodeID = (String) newNode.getId();
					if(valOrNode.equals(newNodeID)) {
						return this;
					}
					locator.delete(this);
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, newNodeID));
				}
				if (keyOrNull.equals(key)) {
					if (valOrNode.equals(value)) {	//If we the K-V pair is already in table
						return this;
					}
					//Else update value for key
					addedLeaf.value = valOrNode;	//Update value to traverse back to recursive call
					locator.delete(this);
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, value));
				}
				//trying to insert a key where a key is already at that index. Need to make a new node and check to see if HashCollisionNode is needed
				locator.delete(this);
				return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index, null, 2*index+1, (String)(createNode(shift + 5, keyOrNull, valOrNode, hash, key, value)).getId())); 
			}
			else {
				int bitCount = Integer.bitCount(bitmap);
				if (bitCount >= 16) {	//16 bits set means 16 K-V pairs, need to upgrade to ArrayNode
					String[] nodes = new String[32];
					//Find where in the new ArrayNode the BitmapIndexedNode will go
					int jdex = mask(hash, shift);
					nodes[jdex] = (String) (BitmapIndexedNode.EMPTY.put(shift + 5, hash, key, value, addedLeaf)).getId();
					int j = 0;
					for (int i = 0; i < 32; i++) {
						if (((bitmap >>> i) & 1) != 0) {	//Shift to next entry, and check to see if it's set
							if (array[j] == null)	//Copy over INode ID
								nodes[i] = (String) array[j+1];
							else {	//Copy over K-V in new BitmapIndexNode
								nodes[i] = (String) (BitmapIndexedNode.EMPTY.put(shift + 5, array[j].hashCode(), array[j], array[j+1], addedLeaf)).getId();
							}
							j += 2;
						}
					}
					locator.delete(this);
					return new ArrayNode(bitCount+1, nodes);
				}
				else {
					//Allocate more room in the array for the new K-V pair
					Object[] newArray = new Object[2*(bitCount+1)];
					System.arraycopy(array, 0, newArray, 0, 2*index);
					newArray[2*index] = key;
					newArray[2*index+1] = value;
					System.arraycopy(array, 2*index, newArray, 2*(index+1), 2*(bitCount-index));
					locator.delete(this);
					return new BitmapIndexedNode(bitmap | bit, newArray);	//Update that bitmapindexednode
				}
			}
		}

		@Override
		public DistributedImmutableHashMap remove(int shift, int hash, Object key, Box removedLeaf) {
			int bit = bitpos(hash, shift);
			DirectoryManager locator = HyFlow.getLocator();
			if ((bitmap & bit) == 0) {	//If not in map
				return this;
			}
			int index = index(bit);
			Object keyOrNull = array[2*index];
			Object valOrNode = array[2*index+1];
			if (keyOrNull == null) {	//Removing from another INode
				final DistributedImmutableHashMap n = ((DistributedImmutableHashMap) locator.open((String)valOrNode, "r")).remove(shift+5, hash, key, removedLeaf);
				if (n != null) {
					final String nID = (String) n.getId();
					if (valOrNode.equals(nID)) {
						return this;
					}
					locator.delete(this);
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2*index+1, nID));
				}
				locator.delete(this);
				if (bitmap == bit) {	//If removing last node
					return null;
				}
				//If index is null but not the last element, shrink the array
				return new BitmapIndexedNode(bitmap ^ bit, removePair(array, index));
			}
			if (keyOrNull.equals(key)) {
				removedLeaf.value = valOrNode;
				locator.delete(this);
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

	/**
	 * This class holds K-V pairings only.
	 */
	final static class HashCollisionNode extends DistributedImmutableHashMap {

		private static final long serialVersionUID = 9067812888301343777L;
		private final Integer count;
		private final Integer hash;
		private final Object[] array;

		public HashCollisionNode(int hash, int count, Object[] array) {
			super();
			this.hash = hash;
			this.count = count;
			this.array = array;
		}

		@Override
		public Object find(int shift, int hash, Object key, Object notFound) {
			int index = findIndex(key);
			if (index < 0) {
				return notFound;
			}
			if (key.equals(array[index])) {
				return array[index + 1];
			}
			return notFound;
		}

		@Override
		public DistributedImmutableHashMap put(int shift, int hash, Object key, Object value, Box addedLeaf) {
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
		public DistributedImmutableHashMap remove(int shift, int hash, Object key, Box removedLeaf) {
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
