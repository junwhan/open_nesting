package org.deuce.transform.asm.loader;

import java.util.ArrayList;
import java.util.Hashtable;

import org.deuce.transform.asm.ClassTransformer;
import org.deuce.transform.asm.ExcludeIncludeStore;
import org.deuce.transform.asm.code.ProxyImplementation;
import org.deuce.transform.asm.code.ProxyInterfaceCode;
import org.deuce.transform.asm.storage.MethodDetails;


public class CustomClassLoader extends ClassLoader {
	ArrayList<Object> remoteDetails;
	Hashtable<String, Class> classes;
	
	public CustomClassLoader(){
		super(CustomClassLoader.class.getClassLoader());
		remoteDetails = new ArrayList<Object>();
		classes = new Hashtable<String, Class>();
	}
	
	public synchronized Class loadClass(String className) throws ClassNotFoundException{
		return findClass(className);
	}
	
	public Class findClass(String className)throws ClassNotFoundException{
		Class result = null;		
		result = (Class)classes.get(className);
		if(result !=null){
			return result;
		}
		/*
		 * Check for the class type if to be not instrumented
		 * Return using super class method
		 */
		if (!ExcludeIncludeStore.exclude(className)){
			return super.findClass(className);
		}else if(className.contains("$HY$_")){	
			remoteDetails = ClassTransformer.rmdtl;
			if(className.contains("$HY$_I")){
				byte[] bytes = ProxyInterfaceCode.getCode(remoteDetails, className);
				Class $HY$_I = super.defineClass(className, bytes, 0, bytes.length);
				classes.put(className, $HY$_I);
				return $HY$_I;		
			}else if(className.contains("$HY$_Proxy")){
				byte[] bytes = ProxyImplementation.getCode(remoteDetails, className);
				Class $HY$_P = super.defineClass(className, bytes, 0, bytes.length);
				classes.put(className, $HY$_P);
				return $HY$_P;
			}			
		}
//		try{
//			return findSystemClass(className);
//		}catch(Exception e){}
		
		return super.findClass(className);
	}
}
