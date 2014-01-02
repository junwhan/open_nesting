package org.deuce.transform.asm.code;

import java.util.ArrayList;

import org.deuce.objectweb.asm.ClassVisitor;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.asm.storage.MethodDetails;

import edu.vt.rt.hyflow.core.tm.control.ControlContext;

public class ProxyInterface implements Opcodes{

	public static void addCode(ArrayList<Object>rmdtl, ClassVisitor cw) {
		MethodVisitor mv;
		if(rmdtl == null){
			System.err.println("Trying to Write the Proxy Remote Method without any Details, Not Supported Currently");
		}
		else{
			for(Object md : rmdtl){
				MethodDetails mD = (MethodDetails) md;
				//Get new Arguments
				Type[] src = Type.getArgumentTypes(mD.desc);
				Type rType = Type.getReturnType(mD.desc);
				Type[] mdesc = new Type[src.length + 2];
				mdesc[0] = Type.getType(Object.class);
				mdesc[1] = Type.getType(ControlContext.class);
				System.arraycopy(src, 0, mdesc, 2, src.length);
				
				//Get new Exceptions
				String[] exceptions;
				if(mD.exceptions != null){
					exceptions = new String[mD.exceptions.length + 1];
					exceptions[0] = "java/rmi/RemoteException";
					System.arraycopy(mD.exceptions, 0, exceptions, 1, mD.exceptions.length);				
				}else{
					String[] exceptions1 = {"java/rmi/RemoteException"};
					exceptions = exceptions1;
				}
				
				String desc = Type.getMethodDescriptor(rType, mdesc);
				mv = cw.visitMethod(ACC_PUBLIC+ACC_ABSTRACT, mD.name, desc, null, exceptions);
				mv.visitEnd();
			}				
		}		
	}
}
