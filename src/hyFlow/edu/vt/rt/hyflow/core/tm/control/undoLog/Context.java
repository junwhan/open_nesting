package edu.vt.rt.hyflow.core.tm.control.undoLog;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.TransactionException;
import org.deuce.transform.Exclude;

import aleph.comm.Address;
import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.core.tm.undoLog.AbstractLoggableObject;
import edu.vt.rt.hyflow.util.io.Logger;

/**
 * Control flow Context implementation
 *
 * @author Mohamed M. Saad
 * @since	1.0
 */
@Exclude
final public class Context extends ControlContext{

	class WriteEntry{
		AbstractLoggableObject reference;
		long field;
		Object oldValue;
		WriteEntry(AbstractLoggableObject reference,  long field){
			this.reference = reference;
			this.field = field;
			if(field>0)
				this.oldValue = UnsafeHolder.getUnsafe().getObject(reference, field);
		}
	}
	private List<WriteEntry> writeset;

	class ReadEntry{
		AbstractLoggableObject reference;
		int version;
		ReadEntry(AbstractLoggableObject reference){
			this.reference = reference;
			this.version = reference.__getVersion();
		}
		public boolean validate() {
			return reference.__getVersion()==version;
		}
	}
	private List<ReadEntry> readset;

	@Override
	public void init(int atomicBlockId) {
		super.init(atomicBlockId);
		Logger.info("########### " + txnId + " ##############");
		initSets(this);
	}
	
	@Override
	public void delete(AbstractDistinguishable deleted) {
		super.delete(deleted);
//		beforeWriteAccess(deleted, 0);
	}
	
	private void initSets(Context context){
		Logger.debug(txnId + ": init");
		if(handler==null)
			handler = new Context();
		Object[] metadata = new Object[METADATA_SIZE];
		if(writeset==null || neighbors==null){
			writeset = new LinkedList<WriteEntry>();
			readset = new LinkedList<ReadEntry>();
			neighbors = new HashSet<Address>();
		}else{
			writeset.clear();
			readset.clear();
			neighbors.clear();
		}
		metadata[WRITE_SET_INDEX]	 	= writeset;
		metadata[READ_SET_INDEX]	 	= readset;
		metadata[NEIGHBOR_INDEX] 		= neighbors;
		metadata[FINISHED_INDEX] 		= false;
		metadata[CONTEXT_INDEX] 		= this;
		metadata[NEIGHBOR_TREE_INDEX] 	= null;
		metadata[VOTE_ACTIVE_INDEX] 	= false;
		metadata[VOTE_DECISION_INDEX] 	= null;
		metadata[ORIGINATOR] 			= context==null ? null : false;
		metadata[STATUS_INDEX] 			= new AtomicInteger(ACTIVE);
		registery.put(txnId, metadata);
		Logger.debug(txnId + " : " + String.valueOf(registery.get(txnId)));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		Object[] objects = registery.get(txnId);
		if(objects!=null){
			writeset = (List<WriteEntry>)objects[WRITE_SET_INDEX];
			readset = (List<ReadEntry>)objects[READ_SET_INDEX];
			neighbors = (Set<Address>)objects[NEIGHBOR_INDEX];
		}else
			initSets(null);
	}
	
	@Override
	public void beforeReadAccess(Object obj, long field) {
		// self-check validation
		if(isAborted())
			throw new TransactionException();
		// add to read-set
		if(obj instanceof AbstractLoggableObject){
			AbstractLoggableObject loggable = (AbstractLoggableObject)obj;
			while(!loggable.__isFree(getContextId())){
				Logger.debug(this + ":" + loggable.getId() + ":is not free : " + loggable.__getOwner());
				
				// resolve contention	R/W
				int res = HyFlow.getConflictManager().resolve(this, loggable.__getOwner());
				if(res==0)
					loggable.__release();
				if(res > 0)
					try {
						Thread.sleep(res);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
			
			Object[] metadata = registery.get(txnId);
			synchronized(metadata){
				Logger.debug(loggable.getId() + ":added to readset of: " + this);
				readset.add(new ReadEntry(loggable));
			}
		}
		
		Logger.debug(this + ":Accessed R: " + obj);
	}
	
	public void beforeWriteAccess(Object obj, long field) {
		// self-check validation
		if(isAborted())
			throw new TransactionException();
		Logger.debug(this + ":Try access: " + obj);
		// add to write-set
		if(obj instanceof AbstractLoggableObject){
			AbstractLoggableObject loggable = (AbstractLoggableObject)obj;
			while(!loggable.__own(this)){	// try to set me as the owner
				// resolve contention	W/W
				int res = HyFlow.getConflictManager().resolve(this, loggable.__getOwner());
				if(res==0)
					loggable.__release();
				if(res > 0)
					try {
						Thread.sleep(res);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

			Object[] metadata = registery.get(txnId);
			if(metadata==null){	// aborted
				loggable.__release();
				HyFlow.getLocator().release(loggable);	//release owned object
			}
			synchronized(metadata){
				if(!isAborted()){
					Logger.debug(loggable.getId() + ":added to writeset of: " + this);
					writeset.add(new WriteEntry(loggable, field));
				}else{
					loggable.__release();
					HyFlow.getLocator().release(loggable);	//release owned object
				}
			}
			
		}
		Logger.debug(this + ":Accessed W: " + obj);
	}

	@Override
	public boolean release(Long txnId){
		return release(txnId, true);
	}
	
	public boolean release(Long txnId, boolean clear) {
		Object[] metadata = registery.get(txnId);
		if(metadata==null){	// already released
			Logger.debug(txnId + ": Already Released!");
			return false;
		}
		Logger.debug(txnId + ": Release ...");
		AtomicInteger status = (AtomicInteger)metadata[STATUS_INDEX];
		boolean incrementVersion = status.get()!=ABORTED; 
		Object neighbors = metadata[NEIGHBOR_INDEX];
		synchronized(metadata){
			try{
				metadata = registery.get(txnId);
				if(metadata==null){	// double check, for concurrent release
					Logger.debug(txnId + ": Concurrent Release!");
					return false;
				}
				Set<AbstractDistinguishable> acquiredObjects = new HashSet<AbstractDistinguishable>();
				List<WriteEntry> writeSet = ((List<WriteEntry>)metadata[WRITE_SET_INDEX]);
				Logger.debug(txnId + ": Writeset: " + writeSet.size());
				while(true)
					try {
						for(Iterator<WriteEntry> itr=writeSet.iterator(); itr.hasNext(); ){
							AbstractLoggableObject reference = itr.next().reference;
							if(reference instanceof AbstractDistinguishable){
								boolean exists = acquiredObjects.add((AbstractDistinguishable)reference);
								Logger.debug("Adding object:" + reference + " " + exists);
							}
							else
								Logger.debug("Nondistingshiable object:" + reference);
						}
						break;
					} catch (ConcurrentModificationException e) {
						Logger.debug("Retry iterating read/write set");
						acquiredObjects.clear();
					}
				if(!clear){
					Logger.debug(txnId + ": No Clear.");
					writeSet.clear();
				}
				if(acquiredObjects.isEmpty()){
					Logger.debug(txnId + ": Empty Writeset.");
					return true;
				}
				Logger.debug("Backoff acquired objects :" + acquiredObjects.size());
				DirectoryManager locator = HyFlow.getLocator();
				for(AbstractDistinguishable distinguishable:acquiredObjects){
					if(incrementVersion)
						((AbstractLoggableObject)distinguishable).__incVersion();
					((AbstractLoggableObject)distinguishable).__release();
					locator.release(distinguishable);
					Logger.debug("Release :" + distinguishable.getId());
				}
			} finally {
				if(clear){
					Logger.debug(txnId + ": Remove from registery");
					registery.remove(txnId);
					if(neighbors!=null)
						synchronized (neighbors) {
							neighbors.notifyAll();
						}
				}
			}
		}
		return true;
	}

	@Override
	protected boolean tryCommit(Long txnId) {
		Object[] metadata = registery.get(txnId);
		if(metadata==null)	// already aborted
			return false;
		Logger.debug(txnId + ":TryCommit: done");
		for(Iterator<ReadEntry> itr=((List<ReadEntry>)metadata[READ_SET_INDEX]).iterator(); itr.hasNext(); )
			if(!itr.next().validate())
				return false;
		return true;
	}

	@Override
	public boolean rollback(Long txnId) {
		Logger.debug(txnId + ":Rollback...");
		Object[] metadata = registery.get(txnId);
		if(metadata==null){	// already aborted
			aborts++;
			return true;
		}
		AtomicInteger status = (AtomicInteger)metadata[STATUS_INDEX];
		status.set(ABORTED);
		aborts++;
		return true;
	}
	
	@Override
	public boolean commit() {
		boolean commit = false;
		try {
			ControlContext.vote(txnId, null);
			Logger.debug(txnId +": COMMIT LOCAL");
			commit = true;
		} catch (TransactionException e) {
			aborts++;
			Object[] metadata = registery.get(txnId);
			AtomicInteger status = (AtomicInteger)metadata[STATUS_INDEX];
			status.set(ABORTED);
			release(txnId, true);
		}
		if(commit){
			commitCreational();
			release(txnId);
			complete();
			Logger.debug(txnId + ":&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		}
		return commit;
	}
	
	public boolean rollback(boolean clear){
		Logger.debug(txnId +": ABORT LOCAL");
		boolean done = rollback(txnId);
		if(done){
			Object[] metadata = registery.get(txnId);
			AtomicInteger status = (AtomicInteger)metadata[STATUS_INDEX];
			status.set(ABORTED);
			release(txnId, clear);
		}else
			Logger.debug(txnId +": ABORT FAIL !!!");
		return done;
	}
	
	@Override
	public boolean rollback() {
		return rollback(true);
	}
	
	@Override
	public Object onReadAccess(Object obj, Object value, long field) {
		return value;
	}

	@Override
	public boolean onReadAccess(Object obj, boolean value, long field) {
		return value;
	}

	@Override
	public byte onReadAccess(Object obj, byte value, long field) {
		return value;
	}

	@Override
	public char onReadAccess(Object obj, char value, long field) {
		return value;
	}

	@Override
	public short onReadAccess(Object obj, short value, long field) {
		return value;
	}

	@Override
	public int onReadAccess(Object obj, int value, long field) {
		return value;
	}

	@Override
	public long onReadAccess(Object obj, long value, long field) {
		return value;
	}

	@Override
	public float onReadAccess(Object obj, float value, long field) {
		return value;
	}

	@Override
	public double onReadAccess(Object obj, double value, long field) {
		return value;
	}

	@Override
	public void onWriteAccess(Object obj, boolean value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putBoolean(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, byte value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putByte(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, char value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putChar(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, short value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putShort(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, int value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putInt(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, long value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putLong(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, float value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putFloat(obj, field, value);
	}

	@Override
	public void onWriteAccess(Object obj, double value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putDouble(obj, field, value);
	}
	
	@Override
	public void onWriteAccess(Object obj, Object value, long field) {
		beforeWriteAccess(obj, field);
		UnsafeHolder.getUnsafe().putObject(obj, field, value);
	}

}
