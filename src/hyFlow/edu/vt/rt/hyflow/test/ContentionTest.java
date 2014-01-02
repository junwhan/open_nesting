package edu.vt.rt.hyflow.test;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.tm.bank.BankAccount;

public class ContentionTest {
	
	public static void main(String[] args) throws Throwable {
		HyFlow.start(5);
		System.out.println("Sleeping");
		Thread.sleep(1000);
		
		//	 Local accounts
		BankAccount account = new BankAccount("5-123");
		account .deposit(50);

		DirectoryManager locator = HyFlow.getLocator();
		
		System.out.println("Acquire1");
		BankAccount account1 = (BankAccount)locator.open("5-123");
		System.out.println("Release1");
//		locator.release(account1);
		
		System.out.println("Acquire2");
		BankAccount account2 = (BankAccount)locator.open("5-123");
		System.out.println("Release2");
		locator.release(account1);
		
		System.out.println("Complete Test");
		System.exit(0);
	}
}
