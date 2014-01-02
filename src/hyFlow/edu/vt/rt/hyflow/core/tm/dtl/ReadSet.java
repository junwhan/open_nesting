package edu.vt.rt.hyflow.core.tm.dtl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deuce.transaction.TransactionException;
import org.deuce.transform.Exclude;

import aleph.Message;
import aleph.comm.CommunicationManager;
import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.core.dir.dtl2.TrackerDirectory;
import edu.vt.rt.hyflow.core.tm.dtl2.field.ReadObjectAccess;
import edu.vt.rt.hyflow.util.io.Logger;

/**
 * Represents the transaction read set.
 * And acts as a recycle pool of the {@link ReadObjectAccess}.
 *  
 * @author Guy Korland
 * @author Mohamed M. Saad
 * @since 0.7
 */
@Exclude
public class ReadSet{
	
	private static final int DEFAULT_CAPACITY = 1024;
	private ReadObjectAccess[] readSet = new ReadObjectAccess[DEFAULT_CAPACITY];
	private int nextAvaliable = 0;
	private ReadObjectAccess currentReadFieldAccess = null;
	
	int remoteValidateResult;
	
	public static Map<Integer, ReadSet> pendingValidates = new ConcurrentHashMap<Integer, ReadSet>();  
	
	public ReadSet(){
		fillArray( 0);
	}
	
	public void clear(){
		nextAvaliable = 0;
	}

	private void fillArray( int offset){
		for( int i=offset ; i < readSet.length ; ++i){
			readSet[i] = new ReadObjectAccess();
		}
	}

	public ReadObjectAccess getNext(){
		if( nextAvaliable >= readSet.length){
			int orignLength = readSet.length;
			ReadObjectAccess[] tmpReadSet = new ReadObjectAccess[ 2*orignLength];
			System.arraycopy(readSet, 0, tmpReadSet, 0, orignLength);
			readSet = tmpReadSet;
			fillArray( orignLength);
		}
		currentReadFieldAccess = readSet[ nextAvaliable++];
		return currentReadFieldAccess;
	}
	
	public ReadObjectAccess getCurrent(){
		Logger.debug("get current " + currentReadFieldAccess);
		return currentReadFieldAccess;
	}
	
    public synchronized void checkClock(int clock, boolean clear) {
    	Logger.debug("READSET: Readset size:" + nextAvaliable);
    	Integer hashCode = hashCode();
    	pendingValidates.put(hashCode, this);
    	try {
    		Logger.debug("Constructing Read list");
    		Map<Object, AbstractDistinguishable> set = new HashMap<Object, AbstractDistinguishable>();
    		for(ReadObjectAccess field: readSet){
    			Object obj = field.getObject();
    			if(obj!=null){
        			if(obj instanceof AbstractDistinguishable){
        				AbstractDistinguishable dist = (AbstractDistinguishable) obj;
        				if(dist.isShared()){
            				Logger.debug("Skip " + obj);
        					continue;
        				}
        				set.put(dist.getId(), dist);
        			}
    				Logger.debug("Add " + obj);
    			}
    			else
    				break;
    		}
    		Logger.debug("Readlist " + set.size());
    		
	        for (AbstractDistinguishable obj: set.values()) {
	        	if(LockTable.checkLock( obj, clock, false)<0){
	        		AbstractDistinguishable object = (AbstractDistinguishable)obj;
	        		try {
	        			try {
							CommunicationManager.getManager().send(obj.getOwnerNode(), new ValidateRequest(obj.getId(), hashCode));
						} catch (IOException e) {
							e.printStackTrace();
							throw new TransactionException();
						}
						Logger.debug("READSET: Wait Remote " + obj + " Validation ...");
						wait();
						Logger.debug(obj + " [" + clock + " < " + (remoteValidateResult & LockTable.UNLOCK) + "]");
						if(clock<(remoteValidateResult & LockTable.UNLOCK)){
							Logger.debug("READSET: Remote Validation failed, remote version:" + (remoteValidateResult & LockTable.UNLOCK));
							throw new TransactionException();
						}
						Logger.debug("READSET: Remote Validation " + obj.getId() + " successeded.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}
	        }
		} finally{
			if(clear)
				for(ReadObjectAccess field: readSet)
					field.clear();
			pendingValidates.remove(hashCode);
		}
    }
    
    public interface ReadSetListener{
    	void execute( ReadObjectAccess read);
    }
}

class ValidateRequest extends Message{

	private Object key;
	private Integer readsetHashcode;
	private int senderClock;
	ValidateRequest(Object key, Integer readsetHashcode){
		this.key = key;
		this.readsetHashcode = readsetHashcode;
		this.senderClock = LocalClock.get();
	}
	
	@Override
	public void run() {
		try {
			int localClock = LocalClock.get();
			if(senderClock>localClock)
				LocalClock.advance(senderClock);
			AbstractDistinguishable object = ((TrackerDirectory)DirectoryManager.getManager()).getLocalObject(key);
			int currentLockVersion = object == null ? Integer.MAX_VALUE : LockTable.getLockVersion(object);
			CommunicationManager.getManager().send(from, new ValidateResponse(currentLockVersion, readsetHashcode));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ValidateResponse extends Message{

	private int lockVersion;
	private int senderClock;
	private int readsetHashcode;
	ValidateResponse(int lockVersion, int readsetHashcode){
		this.readsetHashcode = readsetHashcode;
		this.lockVersion = lockVersion;
		this.senderClock = LocalClock.get();
	}
	
	@Override
	public void run() {
		if(senderClock>LocalClock.get())
			LocalClock.advance(senderClock);
		ReadSet readSet = ReadSet.pendingValidates.get(readsetHashcode);
		if(readSet==null){
			Logger.debug("Null context: " + readSet);
			return;
		}
		synchronized (readSet) {
			readSet.remoteValidateResult = lockVersion;
			readSet.notifyAll();
		}
	}
}
