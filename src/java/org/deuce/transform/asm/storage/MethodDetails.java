package org.deuce.transform.asm.storage;

public class MethodDetails {
	public int access;
	public String name;
	public String desc;
	public String signature;
	public String[] exceptions;
	public String accessType; 			//Require for RMI access 
	
	public MethodDetails(int access2, String name2, String desc2,
			String signature2, String[] exceptions2, String aT) {
		access = access2;
		name = name2;
		desc = desc2;
		signature = signature2;
		exceptions = exceptions2;
		accessType = aT;		
	}
	
	public boolean same(MethodDetails md1){
		if((access == md1.access) && (name.equals(md1.name)) && (desc.equals(md1.desc)) && (signature.equals(md1.signature)))
			return true;
		return false;			
	}
}
