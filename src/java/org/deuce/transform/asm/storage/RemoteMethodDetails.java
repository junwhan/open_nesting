package org.deuce.transform.asm.storage;

public class RemoteMethodDetails {
	public MethodDetails rmD;		//Remote Method Details
	public MethodDetails dmD;		//Deuce created Method details

	public RemoteMethodDetails(MethodDetails nrmD, MethodDetails ndmD) {
		rmD = nrmD;
		dmD = ndmD;
	}
}
