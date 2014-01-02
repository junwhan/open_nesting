package edu.vt.rt.hyflow.core.tm.dtl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deuce.transaction.AbstractContext;
import org.deuce.transaction.TransactionException;
import org.deuce.transform.Exclude;

import aleph.comm.Address;
import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.core.tm.dtl2.field.ReadObjectAccess;
import edu.vt.rt.hyflow.core.tm.dtl2.field.WriteObjectAccess;
import edu.vt.rt.hyflow.util.io.Logger;

/**
 * DTL2 implementation
 *
 * @author Mohamed M. Saad
 * @since	1.0
 */
@Exclude
final public class Context extends AbstractContext{
	
	final private ReadSet readSet = new ReadSet();
	final private WriteSet writeSet = new WriteSet();
	final private List<AbstractDistinguishable> lazyPublish = new LinkedList<AbstractDistinguishable>(); 
	final private List<AbstractDistinguishable> lazyDelete = new LinkedList<AbstractDistinguishable>();
	
	public static int forwardings;
		
	//Used by the thread to mark locks it holds.
	final private Set<Object> locksMarker = new HashSet<Object>();
	
	//Marked on beforeRead, used for the double lock check
	private int localClock;
//	private int lastReadLock;
	
	public boolean pendingLock;
	
	public Context(){
		this.localClock = LocalClock.get();
	}
	
	public void init(int atomicBlockId){
		Logger.debug("Init");
		super.init(atomicBlockId);
		this.locksMarker.clear();
		this.readSet.clear(); 
		this.writeSet.clear();
		this.lazyPublish.clear();
		this.lazyDelete.clear();
		this.localClock = LocalClock.get();
		Logger.debug("Init done");
	}

	@Override
	public void newObject(AbstractDistinguishable object) {
		lazyPublish.add(object);
	}
	
	@Override
	public void delete(AbstractDistinguishable deleted) {
		lazyDelete.add(deleted);
		writeSet.get(deleted);	// add to write set
	}
	
	public void forward(int senderClock) {
		forwardings++;
		Logger.debug("Forwarding time from <" + localClock + "> to <" + senderClock + ">");
		try {
			readSet.checkClock(localClock, false);
			Logger.debug("Validating readset against <" + localClock + ">");
			localClock = senderClock;
			Logger.debug("Forwarding successded.");
		} catch (TransactionException e) {
			Logger.debug("Early Validation fail !!");
			status = STATUS.ABORTED;
		}
	}

	public boolean commit(){
		Logger.debug("Try Commit");
        		
        Logger.debug("Try Lock Write-Set");
		int lockedCounter = 0;//used to count how many fields where locked if unlock is needed 
		AbstractDistinguishable[] writeSet = this.writeSet.sortedItems();
		try
		{
			for(AbstractDistinguishable obj : writeSet){
				Object key = obj.getId();
				Address owner = obj.getOwnerNode();
				Logger.debug("Validating: " + key);
				
				if(!LockTable.lock(obj, locksMarker)){
					LockTable.remoteLockRequest(this, key, owner);
					if(!pendingLock){
						Logger.debug("Remote lock refused");
						throw new TransactionException();
					}
					Logger.debug("Remote lock granted");
				}
				++lockedCounter;
			}
	        Logger.debug("Validate Read-Set");
			readSet.checkClock( localClock, true);
		}
		catch(TransactionException exception){
			Logger.debug("Invalid Read-Set");
			for(AbstractDistinguishable obj : writeSet){
				if( lockedCounter-- == 0)
					break;
				Logger.debug("Releasing " + obj);
				if(!LockTable.unLock(obj, locksMarker))
					LockTable.remoteUnlockRequest(obj.getId(), obj.getOwnerNode());
			}
			return false;
		}

		final int newClock = LocalClock.increment();

		Logger.debug("Commit values to objects");
		for( WriteObjectAccess writeField : this.writeSet)
			writeField.put(); // commit value to field
			
		Logger.debug("Publish newly created objects");
		DirectoryManager manager = HyFlow.getLocator();
		for(AbstractDistinguishable object : lazyPublish)
			manager.register(object); // populate me as this object owner

		DirectoryManager locator = HyFlow.getLocator();
		Logger.debug("Unregister deleted objects");
		for(AbstractDistinguishable object : lazyDelete){
			locator.unregister(object); // unregister this object
		}

		Logger.debug("Release Write-Set");
		for(AbstractDistinguishable obj: writeSet){
			if(LockTable.setAndReleaseLock( obj, newClock, locksMarker)){	// is it remote
				AbstractDistinguishable object = (AbstractDistinguishable)obj;
				Object key = obj.getId();
				Logger.debug("I'm new owner of " + key);
				locator.register(object);	// register at the directory manager
				Logger.debug("Registered as owner of " + key);
			}
		}
		
		Logger.debug("Commited ===================================");
		complete();
		return true;
	}
	
	public boolean rollback(){
		Logger.debug("Rollback !!!");
		aborts++;
		return true;
	}

	private WriteObjectAccess onReadAccess0( Object obj, long field){
		if(status.equals(STATUS.ABORTED)){
			System.out.println("Aborted transaction!!");
			throw new TransactionException();
		}
		
		// Check if it is already included in the write set
		System.out.println("check");
		return writeSet.contains( readSet.getCurrent() );
	}

	
	public void beforeReadAccess(Object obj, long field) {
		if(obj instanceof AbstractDistinguishable)
			Logger.debug("Try access " + ((AbstractDistinguishable)obj).getId());
		ReadObjectAccess next = readSet.getNext();
		next.init(obj);

		LockTable.checkLock(obj, localClock, true);
	}
	
	public Object onReadAccess( Object obj, Object value, long field){
		Logger.debug("On read access " + obj);
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null){
			Logger.debug("On read access 2 " + writeAccess);
			return value;
		}
		Logger.debug("read new value " + obj);
		Object val = writeAccess.getValue(field);
		return val==null ? value : val;
	}
		
	public boolean onReadAccess(Object obj, boolean value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Boolean val = (Boolean)writeAccess.getValue(field);
		return val==null ? value : val;    
	}
	
	public byte onReadAccess(Object obj, byte value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Byte val = (Byte)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public char onReadAccess(Object obj, char value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Character val = (Character)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public short onReadAccess(Object obj, short value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Short val = (Short)writeAccess.getValue(field);    
		return val==null ? value : val;
	}
	
	public int onReadAccess(Object obj, int value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Integer val = (Integer)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public long onReadAccess(Object obj, long value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Long val = (Long)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public float onReadAccess(Object obj, float value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Float val = (Float)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public double onReadAccess(Object obj, double value, long field) {
		WriteObjectAccess writeAccess = onReadAccess0(obj, field);
		if( writeAccess == null)
			return value;
		
		Double val = (Double)writeAccess.getValue(field);
		return val==null ? value : val;
	}
	
	public void onWriteAccess( Object obj, Object value, long field){
		if(status.equals(STATUS.ABORTED))
			throw new TransactionException();

		WriteObjectAccess fieldAccess = writeSet.get(obj);
		fieldAccess.set(field, value);
	}
	
	public void onWriteAccess(Object obj, boolean value, long field) {
		this.onWriteAccess(obj, (Object)value, field);
	}
	
	public void onWriteAccess(Object obj, byte value, long field) {
		this.onWriteAccess(obj, (Object)value, field);
	}
	
	public void onWriteAccess(Object obj, char value, long field) {
		this.onWriteAccess(obj, (Object)value, field);
	}
	
	public void onWriteAccess(Object obj, short value, long field) {
		this.onWriteAccess(obj, (Object)value, field);	
	}
	
	public void onWriteAccess(Object obj, int value, long field) {
		this.onWriteAccess(obj, (Object)value, field);		
	}
	
	public void onWriteAccess(Object obj, long value, long field) {
		this.onWriteAccess(obj, (Object)value, field);		
	}

	public void onWriteAccess(Object obj, float value, long field) {
		this.onWriteAccess(obj, (Object)value, field);		
	}
	
	public void onWriteAccess(Object obj, double value, long field) {
		this.onWriteAccess(obj, (Object)value, field);		
	}
}
