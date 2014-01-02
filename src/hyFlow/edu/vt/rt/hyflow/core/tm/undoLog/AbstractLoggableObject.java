package edu.vt.rt.hyflow.core.tm.undoLog;

import java.util.concurrent.atomic.AtomicReference;

import org.deuce.transaction.AbstractContext;

import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.util.io.Logger;

abstract public class AbstractLoggableObject extends AbstractDistinguishable{
	
	private AtomicReference<AbstractContext> writer = new AtomicReference<AbstractContext>();
	private int version = 0;
	
	public boolean __own(AbstractContext context){
		Logger.debug(context + ": try own:" + getId() + " owned-by " + writer.get());
		if(writer.compareAndSet(null, context))
			return true;
		return isMeOwn(context);
	}
	
	public boolean isMeOwn(AbstractContext context){
		return __isMeOwn(context.getContextId());
	}
	
	public boolean __isMeOwn(Long id){
		AbstractContext w = writer.get();
		return w!=null && w.getContextId().equals(id);
	}
	
	public AbstractContext __getOwner(){
		return writer.get();
	}
	
	public void __release(){
		Logger.debug(getId() + " released from " + writer.get() );
		writer.set(null);
	}

	public boolean __isFree(Long id) {
		return writer.get()==null || __isMeOwn(id);
	}
	
	public void __incVersion(){
		version++;
		Logger.debug(getId() + " @ " + version);
	}
	
	public int __getVersion(){
		return version;
	}
}