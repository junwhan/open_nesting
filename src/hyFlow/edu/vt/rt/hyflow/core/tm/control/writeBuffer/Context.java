package edu.vt.rt.hyflow.core.tm.control.writeBuffer;

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
		Object value;
		WriteEntry(AbstractLoggableObject reference, long field, Object value){
			this.reference = reference;
			this.field = field;
			this.value = value;
		}
		
		public void commit(){
			UnsafeHolder.getUnsafe().putObject(reference, field, value);
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
			Logger.info(reference.getId() + " validate " + reference.__getVersion() + " " + version);
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
			Object[] metadata = registery.get(txnId);
			synchronized(metadata){
				Logger.debug(loggable.getId() + ":added to readset of: " + this);
				readset.add(new ReadEntry(loggable));
			}
		}
		Logger.debug(this + ":Accessed R: " + obj);
	}
	
	@Override
	public boolean release(Long txnId){
		return release(txnId, true);
	}
	
	public boolean release(Long txnId, boolean clear) {
		Object[] metadata = registery.get(txnId);
		Logger.debug(txnId + ": Release ...");
		if(metadata==null){	// already released
			Logger.debug(txnId + ": Already Released!");
			return false;
		}
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

	public boolean kill() {
//		Object[] metadata = registery.get(txnId);
//		if(metadata==null)
//			return true;
//		AtomicInteger status = ((AtomicInteger)metadata[STATUS_INDEX]);
//		status.set(ABORTED);
//		return true;
		
		Object[] metadata = registery.get(txnId);
		if(metadata==null)
			return true;
		AtomicInteger status = ((AtomicInteger)metadata[STATUS_INDEX]);
		Logger.debug("Trying to Kill"+txnId);
		return status.get()==ABORTED || status.compareAndSet(ACTIVE, ABORTED);
	}

	@Override
	protected boolean tryCommit(Long txnId) {
		Logger.debug(txnId + ":TryCommit: start");
		Object[] metadata = registery.get(txnId);
		if(metadata==null)	// already aborted
			return false;

		for(Iterator<WriteEntry> itr=((List<WriteEntry>)metadata[WRITE_SET_INDEX]).iterator(); itr.hasNext(); )
			try {
				AbstractLoggableObject loggable = itr.next().reference; 
				while(!loggable.__own((Context)metadata[CONTEXT_INDEX])){	// try to set me as the owner
					int res = HyFlow.getConflictManager().resolve((Context)metadata[CONTEXT_INDEX], loggable.__getOwner());
					if(res==0)
						loggable.__release();
					if(res > 0)
						try {
							Thread.currentThread().sleep(res);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			} catch (TransactionException e) {
				Logger.debug(txnId + ":TryCommit: fail ownership");
				return false;
			}
		
		for(Iterator<ReadEntry> itr=((List<ReadEntry>)metadata[READ_SET_INDEX]).iterator(); itr.hasNext(); )
			if(!itr.next().validate()){
				Logger.debug(txnId + ":TryCommit: fail validation");
				return false;
			}
		
		Logger.debug(txnId + ":TryCommit: done");

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
			for(Iterator<WriteEntry> itr=writeset.iterator(); itr.hasNext(); itr.next().commit());
			commitCreational();
			release(txnId);
			complete();
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
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, byte value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, char value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, short value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, int value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, long value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, float value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

	@Override
	public void onWriteAccess(Object obj, double value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}
	
	@Override
	public void onWriteAccess(Object obj, Object value, long field) {
		writeset.add(new WriteEntry((AbstractLoggableObject)obj, field, (Object)value));
	}

}
