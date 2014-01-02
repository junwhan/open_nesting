package edu.vt.rt.hyflow.benchmark.tm.bankagent;

import org.deuce.Atomic;
import org.deuce.transaction.TransactionException;
import org.deuce.transform.Mobile;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.Benchmark;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.transaction.Remote;

@Mobile
public class BankAccount extends AbstractDistinguishable{
	
	Integer amount=0;
	String id;
//	static int dummy =0;
	
	public BankAccount(String id) {
		this.id = id;
	}
	
	@Override
	public Object getId() {
		return id;
	}
	
	@Remote
	public void withdraw(int amount){
		this.amount-= amount;
	}
	
	@Remote
	public void deposit(int amount){
		this.amount+= amount;
	}
	
	@Remote
	public Integer checkBalance(){
		return amount;
	}
	
	@Atomic
	public static long totalBalance(String accountNum1, String accountNum2){
		DirectoryManager locator = HyFlow.getLocator();
		BankAccount account1 = (BankAccount) locator.open(accountNum1);
		BankAccount account2 = (BankAccount) locator.open(accountNum2);
		
		long balance = 0;
		for(int i=0; i<Benchmark.calls; i++)
			balance += account1.checkBalance();
		
		try {
			for(int i=0; i<Benchmark.calls; i++)
				balance += account2.checkBalance();
		}catch(TransactionException e){
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return balance;
	}
	
	public static void transfer(String accountNum1, String accountNum2, int amount){
		DirectoryManager locator = HyFlow.getLocator();
		BankAccount account1 = (BankAccount) locator.open(accountNum1);
		BankAccount account2 = (BankAccount) locator.open(accountNum2);
		
		for(int i=0; i<Benchmark.calls; i++)
			account1.withdraw(amount);
		
		try {
			for(int i=0; i<Benchmark.calls; i++)
				account2.deposit(amount);
		}catch(TransactionException e){
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
