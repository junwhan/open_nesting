package org.deuce.transform.asm.storage;

public class ClassDetails {
	public String pkg;				// File package Name
	public String name;				//*.java file Name
	public String className;		//Class Full Name
	
	public ClassDetails(){}
	public ClassDetails(String p, String n){
		pkg = p;
		name = n;
		className = p+"/"+n;
	}
	
	public void update(String c){
		className = c;
		pkg = c.substring(0,c.lastIndexOf('/')+1);
		name = c.substring(c.lastIndexOf('/')+1);
	}
}
