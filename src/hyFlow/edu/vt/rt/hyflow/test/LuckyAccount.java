package edu.vt.rt.hyflow.test;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.tm.bank.BankAccount;

public class LuckyAccount {
	
	public static void main(final String[] args) throws Throwable {
		if(args.length<2){
			System.err.println("Missing argumets\nTransform <node-id> <object-1-id> [<object-2-id> <object-3-id> ...]");
			System.exit(1);
		}
		Integer nodeId = Integer.parseInt(args[0]);
		HyFlow.start(nodeId);
		
		System.out.println("Creating Local Objects");
		// Local accounts
		for(int i=1; i<args.length; i++)
			if(args[i].startsWith(args[0]))
				new BankAccount(args[i]).deposit(50);
		
		System.out.println("Sleeping to objects populated");
		Thread.sleep(5000);

		// Making money transfer
		final String luckyAccount = args[nodeId+1];	// pick one of the accounts
		for(int i=nodeId+2; i<args.length; i++){
			final int index = i;
			if(!args[i].equals(luckyAccount))
				new Thread(){
					public void run() {
					System.out.println("Transaction<" + args[index] + "," + luckyAccount + ">:");
					try {
						BankAccount.transfer(args[index], luckyAccount, 10);
					} catch (Throwable e) {
						e.printStackTrace();
					}
//					transaction.cleanup();
				}
			}.start();
		}
		Thread.sleep(10000);

		// View final output
		DirectoryManager locator = HyFlow.getLocator();
		if(nodeId==1){ // just one node view output
			System.out.println("Sleep till transactions complete");
			Thread.sleep(10000);
			for(int i=1; i<args.length; i++){
				
				BankAccount account = (BankAccount)locator.open(args[i], "r");
				System.out.println(account.checkBalance());
				locator.release(account);

				BankAccount account2 = (BankAccount)locator.open(args[i], "r");
				System.out.println(account2.checkBalance());
				locator.release(account2);
			}
		}
		
		System.out.println("Test complete");
	}
}
