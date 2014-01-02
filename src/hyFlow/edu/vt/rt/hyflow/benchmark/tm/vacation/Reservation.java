package edu.vt.rt.hyflow.benchmark.tm.vacation;

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
import edu.vt.rt.hyflow.util.io.Logger;

public class Reservation extends AbstractLoggableObject
	implements 	IHyFlow{							// Implementation specific code for ControlFlowDirecotry


	private Integer total = INITIAL;
  	public static long total__ADDRESS__;
  	private Integer used = 0;
  	public static long used__ADDRESS__;
  	private Integer price;
  	public static long price__ADDRESS__;
  	private String id;
    public static long id__ADDRESS__;
    private $HY$_IReservation $HY$_proxy;
    public static long $HY$_proxy__ADDRESS__;
    private Object $HY$_id;
    public static long $HY$_id__ADDRESS__;
    public static Object __CLASS_BASE__;
	
	private static final int INITIAL = 10; 
	
	 {
		  	try{
		  		total__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("total"));
		  		used__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("used"));
		  		price__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("price"));
		  		id__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("id"));
		  		$HY$_proxy__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("$HY$_proxy"));
		  		$HY$_id__ADDRESS__ = AddressUtil.getAddress(Reservation.class.getDeclaredField("$HY$_id"));
		  	}catch (Exception e) {
		  		e.printStackTrace();
			}
	 }
	
	
	public Reservation(){}	// 	required for control flow model
	
	public Reservation(String id, int price) {
		this.id = id;
		this.price = price;
		this.total = INITIAL;
		this.used = 0;
		
		AbstractContext context = ContextDelegator.getInstance();
		if(context.getContextId()==null)
			HyFlow.getLocator().register(this); // publish it now	
		else
			context.newObject(this);	// add it to context publish-set 
	}

	@Override
	public Object getId() {
		return id;
	}

	public boolean isAvailable() {
		return total-used>0;
	}
	public boolean isAvailable(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.isAvaliable($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, total__ADDRESS__, __transactionContext__);
		ContextDelegator.beforeReadAccess(this, used__ADDRESS__, __transactionContext__);
		return ((Integer)ContextDelegator.onReadAccess(this, total, total__ADDRESS__, __transactionContext__)
		 		- (Integer)ContextDelegator.onReadAccess(this, used, used__ADDRESS__, __transactionContext__)) > 0;		
	}
	
	public boolean reserve() {
		if(used==total)
			return false;
		used++;
		return true;
	}
	public boolean reserve(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.reserve($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, used__ADDRESS__, __transactionContext__);
		Integer old = (Integer)ContextDelegator.onReadAccess(this, used, used__ADDRESS__, __transactionContext__);
		if(old==0)
			return false;
		ContextDelegator.onWriteAccess(this, old+1, used__ADDRESS__, __transactionContext__);
		return true;
	}

	public void release() {
		used--;
	}
	public void release(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				$HY$_proxy.release($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, used__ADDRESS__, __transactionContext__);
		Integer old = (Integer)ContextDelegator.onReadAccess(this, used, used__ADDRESS__, __transactionContext__);
		ContextDelegator.onWriteAccess(this, old-1, used__ADDRESS__, __transactionContext__);
	}

	public int getPrice() {
		return price;
	}
	public int getPrice(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.getPrice($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, price__ADDRESS__, __transactionContext__);
		return (Integer)ContextDelegator.onReadAccess(this, price, price__ADDRESS__, __transactionContext__);
	}

	public void setPrice(int price) {
		this.price = price;
	}
	public void setPrice(int price, Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				$HY$_proxy.setPrice($HY$_id, (ControlContext) __transactionContext__, price);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.onWriteAccess(this, price, price__ADDRESS__, __transactionContext__);
	}

	public void retrieItem() {
		total--;
	}
	public void retrieItem(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				$HY$_proxy.retrieItem($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, total__ADDRESS__, __transactionContext__);
		Integer old = (Integer)ContextDelegator.onReadAccess(this, total, total__ADDRESS__, __transactionContext__);;
		ContextDelegator.onWriteAccess(this, old-1, total__ADDRESS__, __transactionContext__);
	}

	

	@Override
	public void setRemote(Object id, String ownerIP, int ownerPort) {
		$HY$_id = id;
		try {
			$HY$_proxy = (($HY$_IReservation)LocateRegistry.getRegistry(ownerIP, ownerPort).lookup(getClass().getName()));
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
