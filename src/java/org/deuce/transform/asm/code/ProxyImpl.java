package org.deuce.transform.asm.code;

import java.util.ArrayList;

import org.deuce.objectweb.asm.ClassVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.asm.storage.ClassDetails;
import org.deuce.transform.asm.storage.MethodDetails;
import org.deuce.transform.asm.storage.Names;
import org.deuce.transform.asm.storage.RemoteMethodDetails;

import aleph.dir.DirectoryManager;

import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.util.network.Network;

public class ProxyImpl implements Opcodes{
	public static void addCode(ArrayList<Object> rmdtl, ClassVisitor cw, ClassDetails cD) {
 		String className = cD.pkg+cD.name.substring(Names.ProxyClassPrefix.length());		//Original Class Name
		String proxyName = cD.pkg+cD.name;
		String proxyIName = cD.pkg + Names.ProxyInterfacePrefix + cD.name.substring(Names.ProxyClassPrefix.length());
		ArrayList<RemoteMethodDetails> urmdtl = getRUpdatedDetails(rmdtl);
		
		if(urmdtl == null){
			return;
		}
		
		FieldVisitor fv;
		MethodVisitor mv;
		
		//Add Fields
		fv = cw.visitField(ACC_PRIVATE + Opcodes.ACC_FINAL + ACC_STATIC, Names.SerialVersionUID, "J", null, new Long(1L));
		fv.visitEnd();
				
		fv = cw.visitField(0, "locator", Type.getObjectType(Names.AlephDirectoryManager).getDescriptor(), null, null);
		fv.visitEnd();
		
		//Add Constructor
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, new String[] { Names.RMIException });
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, Names.RMIException);
			Label l3 = new Label();
			Label l4 = new Label();
			Label l5 = new Label();
			mv.visitTryCatchBlock(l3, l4, l5, Names.Exception);
			Label l6 = new Label();
			mv.visitLabel(l6);
			//Call Super Class
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, Names.UniCastObject, "<init>", "()V");
			Label l7 = new Label();
			mv.visitLabel(l7);
	
			mv.visitMethodInsn(INVOKESTATIC, Names.Hyflow, "getLocator", Type.getMethodDescriptor(Type.getType(DirectoryManager.class), new Type [] {})); //"()Laleph/dir/DirectoryManager;");
			mv.visitTypeInsn(CHECKCAST, Names.ControlFlowDirectory);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, Names.ControlFlowDirectory, "addProxy", Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {Type.getType(Object.class)})); //"(Ljava/lang/Object;)V");
			Label l8 = new Label();
			mv.visitLabel(l8);
	
			mv.visitMethodInsn(INVOKESTATIC, Names.System, "getSecurityManager", Type.getMethodDescriptor(Type.getType(SecurityManager.class), new Type[]{}));//"()Ljava/lang/SecurityManager;");
			Label l9 = new Label();
			mv.visitJumpInsn(IFNONNULL, l9);
			Label l10 = new Label();
			mv.visitLabel(l10);
	
			mv.visitTypeInsn(NEW, Names.RMISecurityManager);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, Names.RMISecurityManager, "<init>", "()V");
			mv.visitMethodInsn(INVOKESTATIC, Names.System, "setSecurityManager", Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {Type.getType(SecurityManager.class)}));//"(Ljava/lang/SecurityManager;)V");
			mv.visitLabel(l9);
		
			mv.visitFrame(Opcodes.F_FULL, 1, new Object[] {proxyName}, 0, new Object[] {});
			mv.visitMethodInsn(INVOKESTATIC, Names.HyflowNetwork, "getInstance", Type.getMethodDescriptor(Type.getType(Network.class), new Type[]{}));//"()Ledu/vt/rt/hyflow/util/network/Network;");
			mv.visitMethodInsn(INVOKEVIRTUAL, Names.HyflowNetwork, "getPort", "()I");
			mv.visitIntInsn(SIPUSH, 1000);
			mv.visitInsn(IADD);
			mv.visitVarInsn(ISTORE, 1);
			Label l11 = new Label();
			mv.visitLabel(l11);
	
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLabel(l0);
	
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/rmi/registry/LocateRegistry", "createRegistry", "(I)Ljava/rmi/registry/Registry;");
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLabel(l1);
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l2);

			mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {proxyName, Opcodes.INTEGER, "java/rmi/registry/Registry"}, 1, new Object[] {Names.RMIException});
			mv.visitVarInsn(ASTORE, 3);
			Label l12 = new Label();
			mv.visitLabel(l12);

			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, Names.RMIException, "printStackTrace", "()V");
			mv.visitLabel(l3);

			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKESTATIC, Names.UniCastObject, "unexportObject", "(Ljava/rmi/Remote;Z)Z");
			mv.visitInsn(POP);
			mv.visitLabel(l4);
			Label l13 = new Label();
			mv.visitJumpInsn(GOTO, l13);
			mv.visitLabel(l5);

			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
			mv.visitVarInsn(ASTORE, 3);
			Label l14 = new Label();
			mv.visitLabel(l14);

			mv.visitLdcInsn("RMI unexporting");
			mv.visitMethodInsn(INVOKESTATIC, "edu/vt/rt/hyflow/util/io/Logger", "error", "(Ljava/lang/String;)V");
			mv.visitLabel(l13);

			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKESTATIC, "java/rmi/server/UnicastRemoteObject", "exportObject", "(Ljava/rmi/Remote;I)Ljava/rmi/Remote;");
			mv.visitTypeInsn(CHECKCAST, proxyIName);
			mv.visitVarInsn(ASTORE, 3);
			Label l15 = new Label();
			mv.visitLabel(l15);

			mv.visitVarInsn(ALOAD, 2);
			mv.visitLdcInsn(Type.getType("L"+className+";"));
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/rmi/registry/Registry", "rebind", "(Ljava/lang/String;Ljava/rmi/Remote;)V");
			Label l16 = new Label();
			mv.visitLabel(l16);

			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "edu/vt/rt/hyflow/HyFlow", "getLocator", "()Laleph/dir/DirectoryManager;");
			mv.visitFieldInsn(PUTFIELD, proxyName, "locator", "Laleph/dir/DirectoryManager;");
			Label l17 = new Label();
			mv.visitLabel(l17);

			mv.visitInsn(RETURN);

			mv.visitMaxs(0, 0);
			mv.visitEnd();	
		}
		
		//Add Methods
		for(RemoteMethodDetails mD: urmdtl){
			mv = cw.visitMethod(ACC_PUBLIC, mD.rmD.name, mD.rmD.desc, null, null);
			Type[] src = Type.getArgumentTypes(mD.rmD.desc);
			Type returnType = Type.getReturnType(mD.rmD.desc);
			int callerIndex = src.length + 1;	//1 for this
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
//			mv.visitLineNumber(56, l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, Names.ControlflowInterface, "getLastExecuter", "()Laleph/comm/Address;");
			mv.visitVarInsn(ASTORE, callerIndex);
			Label l1 = new Label();
			mv.visitLabel(l1);

			mv.visitInsn(ICONST_1);
			mv.visitVarInsn(ALOAD, callerIndex);
			mv.visitMethodInsn(INVOKESTATIC, "edu/vt/rt/hyflow/util/network/Network", "linkDelay", "(ZLaleph/comm/Address;)V");
			Label l2 = new Label();
			mv.visitLabel(l2);

			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "edu/vt/rt/hyflow/core/tm/control/ControlContext", "getContextId", "()Ljava/lang/Long;");
			mv.visitMethodInsn(INVOKESTATIC, "edu/vt/rt/hyflow/core/tm/control/ControlContext", "getNeighbors", "(Ljava/lang/Long;)Ljava/util/Set;");
			mv.visitVarInsn(ALOAD, callerIndex);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z");
			mv.visitInsn(POP);
			Label l3 = new Label();
			mv.visitLabel(l3);

			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, proxyName, "locator", "Laleph/dir/DirectoryManager;");
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(mD.rmD.accessType);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "aleph/dir/DirectoryManager", "open", "(Lorg/deuce/transaction/AbstractContext;Ljava/lang/Object;Ljava/lang/String;Z)Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, className);
			//Load All the remaining variables
			int i=1;		
			for(Type arg : src)
			{
				if(i == 1) ; 							//No need to load Id
				else if(i == 2);						//Load Context at last
				else if(i <= src.length){
					if (arg.equals(Type.CHAR_TYPE) || arg.equals(Type.INT_TYPE)
							|| arg.equals(Type.SHORT_TYPE)
							|| arg.equals(Type.BYTE_TYPE)
							|| arg.equals(Type.BOOLEAN_TYPE))
						mv.visitVarInsn(ILOAD, i);
					else if(arg.equals(Type.DOUBLE_TYPE))
						mv.visitVarInsn(DLOAD, i);
					else if(arg.equals(Type.LONG_TYPE))
						mv.visitVarInsn(LLOAD	, i);
					else if(arg.equals(Type.FLOAT_TYPE))
						mv.visitVarInsn(FLOAD, i);
					else 
						mv.visitVarInsn(ALOAD, i);						
				}
				i++;
			}
			//Load Context Now
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, className, mD.dmD.name, mD.dmD.desc);
			//Return Value as Argument
			{
				if (returnType.equals(Type.CHAR_TYPE)
						|| returnType.equals(Type.INT_TYPE)
						|| returnType.equals(Type.SHORT_TYPE)
						|| returnType.equals(Type.BYTE_TYPE)
						|| returnType.equals(Type.BOOLEAN_TYPE))
					mv.visitInsn(IRETURN);
				else if(returnType.equals(Type.DOUBLE_TYPE))
					mv.visitInsn(DRETURN);
				else if(returnType.equals(Type.FLOAT_TYPE))
					mv.visitInsn(FRETURN);
				else if(returnType.equals(Type.LONG_TYPE))
					mv.visitInsn(LRETURN);
				else if(returnType.equals(Type.VOID_TYPE))
					mv.visitInsn(RETURN);
				else 
					mv.visitInsn(ARETURN);
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
	}

	private static ArrayList<RemoteMethodDetails> getRUpdatedDetails(
			ArrayList<Object> rmdtl) {
		ArrayList<RemoteMethodDetails> urmdtl = new ArrayList<RemoteMethodDetails>();
		if(rmdtl == null){
			System.err.println("Trying to Write the Proxy Remote Method without any Details, Not Supported Currently");
			return null;
		}
		for(Object md: rmdtl){
			MethodDetails mD = (MethodDetails) md;
			Type[] src = Type.getArgumentTypes(mD.desc);
			Type rType = Type.getReturnType(mD.desc);
			//For Remote Method
			Type[] rmdesc = new Type[src.length + 2];
			rmdesc[0] = Type.getType(Object.class);
			rmdesc[1] = Type.getType(ControlContext.class);
			System.arraycopy(src, 0, rmdesc, 2, src.length);
			//For Deuce Method
			Type[] drmdesc = new Type[src.length + 1];
			System.arraycopy(src, 0, drmdesc, 0, src.length);
			drmdesc[src.length] = Type.getType(ControlContext.class);
			
			//Get new Exceptions
			String[] exceptions;
			if(mD.exceptions != null){
				exceptions = new String[mD.exceptions.length + 1];
				exceptions[0] = Names.RMIException;
				System.arraycopy(mD.exceptions, 0, exceptions, 1, mD.exceptions.length);	
			}else{
				String[] exceptions2 = {Names.RMIException};
				exceptions = exceptions2;
			}
			String rdesc = Type.getMethodDescriptor(rType, rmdesc);
			String ddesc = Type.getMethodDescriptor(rType, drmdesc);
			MethodDetails nrmD = new MethodDetails(mD.access, mD.name, rdesc, mD.signature, exceptions,mD.accessType);
			MethodDetails ndmD = new MethodDetails(mD.access, mD.name, ddesc, mD.signature, exceptions,mD.accessType);
			
			RemoteMethodDetails nmD = new RemoteMethodDetails(nrmD, ndmD);
			urmdtl.add(nmD);
		}		
		return urmdtl;
	}	
}
