package edu.vt.rt.hyflow.benchmark.tm.bank;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;

import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.AbstractContext;
import org.deuce.transaction.Context;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.TransactionException;
import org.deuce.transform.Exclude;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.Benchmark;
import edu.vt.rt.hyflow.core.dir.IHyFlow;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.core.tm.undoLog.AbstractLoggableObject;
import edu.vt.rt.hyflow.util.io.Logger;

@Exclude
public class BankAccount
	extends AbstractLoggableObject				// Implementation specific code for UndoLog context
	implements 	IHyFlow							// Implementation specific code for ControlFlowDirecotry
{

	private Integer amount = 0;
  	public static long amount__ADDRESS__=0;
  	private String id;
    public static long id__ADDRESS__=0;
    private $HY$_IBankAccount $HY$_proxy;
    public static long $HY$_proxy__ADDRESS__=0;
    private Object $HY$_id;
    public static long $HY$_id__ADDRESS__=0;
    public static Object __CLASS_BASE__;
    
    
    {
	  	try{
	  		amount__ADDRESS__ = AddressUtil.getAddress(BankAccount.class.getDeclaredField("amount"));
	  		id__ADDRESS__ = AddressUtil.getAddress(BankAccount.class.getDeclaredField("id"));
	  		$HY$_proxy__ADDRESS__ = AddressUtil.getAddress(BankAccount.class.getDeclaredField("$HY$_proxy"));
	  		$HY$_id__ADDRESS__ = AddressUtil.getAddress(BankAccount.class.getDeclaredField("$HY$_id"));
	  	}catch (Exception e) {
	  		e.printStackTrace();
		}
  	}
    
	public BankAccount(){}	// 	required for control flow model
	
	public BankAccount(String id) {
		this.id = id;

		AbstractContext context = ContextDelegator.getInstance();
		if(context.getContextId()==null)
			HyFlow.getLocator().register(this);
		else
			context.newObject(this);	// add it to context publish-set 
	}

	
	public Object getId() {
		return id;
	}
	

	public void deposit(int dollars){
		amount = amount + dollars;
	}
	public void deposit(int dollars, Context __transactionContext__){
		if($HY$_proxy!=null){
			try {
				$HY$_proxy.deposit($HY$_id, (ControlContext) __transactionContext__, dollars);
				return;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, amount__ADDRESS__, __transactionContext__);
		Integer temp = (Integer)ContextDelegator.onReadAccess(this, amount, amount__ADDRESS__, __transactionContext__) + dollars;
		ContextDelegator.onWriteAccess(this, temp, amount__ADDRESS__, __transactionContext__);
	}
	
	
	public boolean withdraw(int dollars) {
		amount = amount - dollars;
		return amount >= 0;
	}
	public boolean withdraw(int dollars, Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.withdraw($HY$_id, (ControlContext) __transactionContext__, dollars);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, amount__ADDRESS__, __transactionContext__);
		Integer temp = (Integer)ContextDelegator.onReadAccess(this, amount, amount__ADDRESS__, __transactionContext__) - dollars;
		ContextDelegator.onWriteAccess(this, temp, amount__ADDRESS__, __transactionContext__);
		ContextDelegator.beforeReadAccess(this, amount__ADDRESS__, __transactionContext__);
		return (Integer)ContextDelegator.onReadAccess(this, amount, amount__ADDRESS__, __transactionContext__) >= 0;
	}

	public Integer checkBalance() throws Throwable{
		return amount;
	}
	public Integer checkBalance(Context __transactionContext__) {
		if($HY$_proxy!=null){
			try {
				return $HY$_proxy.checkBalance($HY$_id, (ControlContext) __transactionContext__);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ContextDelegator.beforeReadAccess(this, amount__ADDRESS__, __transactionContext__);
		return (Integer)ContextDelegator.onReadAccess(this, amount, amount__ADDRESS__, __transactionContext__);
	}
	
	
	
	public static long totalBalance(String accountNum1, String accountNum2, Context context){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			BankAccount account1 = (BankAccount) locator.open(accountNum1);
			ContextDelegator.beforeReadAccess(account1, 0, context);
			BankAccount account2 = (BankAccount) locator.open(accountNum2);
			
			long balance = 0;
			for(int i=0; i<Benchmark.calls; i++)
				balance += account1.checkBalance(context);
			
			try {
				for(int i=0; i<Benchmark.calls; i++)
					balance += account2.checkBalance(context);
			}catch(TransactionException e){
				throw e;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return balance;
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}
	public static long totalBalance(String accountNum1, String accountNum2) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		long result = 0;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				result = totalBalance(accountNum1, accountNum2, context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
					if (throwable != null) {
						throw throwable;
					} else {
						return result;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	
	
	public static void transfer(String accountNum1, String accountNum2, int amount, Context context){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			BankAccount account1 = (BankAccount) locator.open(accountNum1);
			ContextDelegator.beforeReadAccess(account1, 0, context);
			BankAccount account2 = (BankAccount) locator.open(accountNum2);
			
//			try {
//				if(account1.checkBalance()+account2.checkBalance()!=20000)
//					wrong = true;
//			} catch (Throwable e1) {
//				e1.printStackTrace();
//			}
			
			for(int i=0; i<Benchmark.calls; i++)
				account1.withdraw(amount, context);
			
			try {
				for(int i=0; i<Benchmark.calls; i++)
					account2.deposit(amount, context);
			}catch(TransactionException e){
				throw e;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}
	public static void transfer(String accountNum1, String accountNum2, int amount) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
//			long start = System.currentTimeMillis();
			context.init(4);
//			boolean wrong = false;
			try {
				transfer(accountNum1, accountNum2, amount, context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
//					if(wrong)
//						Logger.fetal("ZZZZZZZZZZZZZZZZZZZZZZZ");
//					System.err.println(start + "-" + System.currentTimeMillis());
					if (throwable != null) {
						throw throwable;
					} else {
						return;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	
	@Override
	public void setRemote(Object id, String ownerIP, int ownerPort) {
		$HY$_id = id;
		try {
			$HY$_proxy = (($HY$_IBankAccount)LocateRegistry.getRegistry(ownerIP, ownerPort).lookup(getClass().getName()));
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
	
	@Override
	public String toString() {
		return getId() + "---" + hashCode();
	}
}
