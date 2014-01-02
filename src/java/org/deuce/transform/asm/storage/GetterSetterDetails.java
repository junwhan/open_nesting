package org.deuce.transform.asm.storage;

public class GetterSetterDetails {
	public FieldDetails fD;
	public boolean sttrXst;
	public boolean gttrXst;	
	
	public GetterSetterDetails(FieldDetails fieldDetails, boolean b, boolean c) {
		fD = fieldDetails;
		sttrXst = b;
		gttrXst = c;
	}
}
