package edu.vt.rt.hyflow.test;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.tm.bank.BankAccount;

public class BankTest {
	
	public static void main(String[] args) throws Throwable {
		if(args.length<2){
			System.err.println("Missing argumets\nTransform <node-id> <object-1-id> [<object-2-id> <object-3-id> ...]");
			System.exit(1);
		}
		HyFlow.start(Integer.parseInt(args[0]));
		System.out.println("Sleeping");
		Thread.sleep(1000);
		
		//	 Local accounts
		for(int i=1; i<args.length; i++)
			if(args[i].startsWith(args[0]))
				new BankAccount(args[i]).deposit(50);
		
		Thread.sleep(1000);
		
		// Remote accounts
		DirectoryManager locator = HyFlow.getLocator();
		for(int i=1; i<args.length; i++){
				BankAccount account = (BankAccount)locator.open(args[i]);
				if(account!=null){
					account.deposit(50);
					System.out.println(account.withdraw(10*Integer.parseInt(args[0])));
					locator.release(account);
				}
				else
					System.out.println("Account " + args[i] + " is not found!");
			}

		// Check results
		Thread.sleep(1000);
		for(int i=1; i<args.length; i++){
			BankAccount account = (BankAccount)HyFlow.getLocator().open(args[i], "r");
			System.out.println(account.checkBalance());
			locator.release(account);
		}
		
		System.out.println("Complete Test");
	}
}
