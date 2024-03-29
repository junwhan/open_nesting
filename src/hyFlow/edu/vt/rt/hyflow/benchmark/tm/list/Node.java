package edu.vt.rt.hyflow.benchmark.tm.list;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;

import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.AbstractContext;
import org.deuce.transaction.Context;
import org.deuce.transaction.ContextDelegator;

import aleph.GlobalObject;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.core.dir.IHyFlow;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.core.tm.undoLog.AbstractLoggableObject;
import edu.vt.rt.hyflow.transaction.Remote;
import edu.vt.rt.hyflow.util.io.Logger;

public class Node 
	extends AbstractLoggableObject				// Implementation specific code for UndoLog context
	implements IHyFlow{							// Implementation specific code for ControlFlowDirecotry

	private Integer value = 0;
  	public static long value__ADDRESS__;
  	private String nextId;
  	public static long nextId__ADDRESS__;
  	private String id;
    public static long id__ADDRESS__;
    private $HY$_INode $HY$_proxy;
    public static long $HY$_proxy__ADDRESS__;
    private Object $HY$_id;
    public static long $HY$_id__ADDRESS__;
    public static Object __CLASS_BASE__;
    
    {
	  	try{
	  		value__ADDRESS__ = AddressUtil.getAddress(Node.class.getDeclaredField("value"));
	  		nextId__ADDRESS__ = AddressUtil.getAddress(Node.class.getDeclaredField("nextId"));
	  		id__ADDRESS__ = AddressUtil.getAddress(Node.class.getDeclaredField("id"));
	  		$HY$_proxy__ADDRESS__ = AddressUtil.getAddress(Node.class.getDeclaredField("$HY$_proxy"));
	  		$HY$_id__ADDRESS__ = AddressUtil.getAddress(Node.class.getDeclaredField("$HY$_id"));
	  	}catch (Exception e) {
	  		e.printStackTrace();
		}
  	}
	
	
	public Node(){}	// 	required for control flow model
	
	public Node(String id, Integer value) {
		this.id = id;
		this.value = value;
		
		AbstractContext context = ContextDelegator.getInstance();
		if(context.getContextId()==null)
			HyFlow.getLocator().register(this); // publish it now	
		else
			context.newObject(this);	// add it to context publish-set 
	}
	
	public void setNext(String nextId){
		this.nextId = nextId;
	}
	public void setNext(String nextId, Context __transactionContext__){
		if($HY$_proxy!=null){
			try {
				$HY$_proxy.setNext($HY$_id, (ControlContext) __transactionContext__, nextId);
				return;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.onWriteAccess(this, nextId, nextId__ADDRESS__, __transactionContext__);
	}

	public String getNext(){
		return nextId;
	}
	public String getNext(Context __transactionContext__){
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.getNext($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, nextId__ADDRESS__, __transactionContext__);
		return (String)ContextDelegator.onReadAccess(this, nextId, nextId__ADDRESS__, __transactionContext__);
	}
	
	public Integer getValue(){
		return value;
	}
	public Integer getValue(Context __transactionContext__){
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.getValue($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, value__ADDRESS__, __transactionContext__);
		return (Integer)ContextDelegator.onReadAccess(this, value, value__ADDRESS__, __transactionContext__);
	}
	
	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setRemote(Object id, String ownerIP, int ownerPort) {
		$HY$_id = id;
		try {
			$HY$_proxy = (($HY$_INode)LocateRegistry.getRegistry(ownerIP, ownerPort).lookup(getClass().getName()));
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			try {
				Logger.debug(Arrays.toString(LocateRegistry.getRegistry(ownerIP, ownerPort).list()));
			} catch (AccessException e1) {
				e1.printStackTrace();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

}
