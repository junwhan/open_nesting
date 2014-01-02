package org.deuce.transform.asm.loader;



public class LoaderTest {
	public static void main(String[] args) throws ClassNotFoundException {
		CustomClassLoader ccl = new CustomClassLoader();
		ccl.loadClass("$HY$_IBankAccount");				//"edu/vt/rt/hyflow/benchmark/tm/bankagent/BankAccount");
//		ccl.loadClass("$HY$_Proxy_BankAccount");
	}
}
