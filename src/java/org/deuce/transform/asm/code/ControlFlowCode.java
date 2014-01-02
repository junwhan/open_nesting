package org.deuce.transform.asm.code;

import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.Method;

public class ControlFlowCode implements Opcodes {

	public static void addCode(MethodVisitor mv, String className, Method method, int argumentsSize, boolean isStatic) {
		int pos = className.lastIndexOf('/')+1;
		String proxyIname = className.substring(0,pos)+"$HY$_I"+className.substring(pos);
		Type[] src = Type.getArgumentTypes(method.desc);
		Type returnType = method.getReturnType();
		Type[] arguements = new Type[src.length+1];
		
		arguements[0]= Type.getType(Object.class);
		System.arraycopy(src, 0, arguements, 1, src.length);
		String desc = Type.getMethodDescriptor(method.getReturnType(), arguements);
		
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/rmi/RemoteException");
		//If Statement
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "$HY$_proxy", "L"+proxyIname+";");	//
		Label l4 = new Label();
		mv.visitJumpInsn(IFNULL, l4);		//Go to start of method
		//Start try block
		mv.visitLabel(l0);		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "$HY$_proxy", "L"+proxyIname+";");
		
		//Get Id
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "$HY$_id", Type.getDescriptor(Object.class));
		//Load other arguments
		int i;
		if(isStatic)
			i=0;
		else
			i=1;
		for(Type arg : src)
		{
			if(i < argumentsSize-1){
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
		//Load Last Argument Context
		mv.visitVarInsn(ALOAD, argumentsSize-1);		//Load Context
		mv.visitTypeInsn(CHECKCAST, "edu/vt/rt/hyflow/core/tm/control/ControlContext");
		//Call the method
		mv.visitMethodInsn(INVOKEINTERFACE, proxyIname, method.name, desc);
		//End the try block
		mv.visitLabel(l1);
		//Provide specific Return type
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
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/rmi/RemoteException"});
		//Store it is last position
		mv.visitVarInsn(ASTORE, argumentsSize);
		mv.visitVarInsn(ALOAD, argumentsSize);
		//Call print stack
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/rmi/RemoteException", "printStackTrace", "()V");
		//Define the end of added code
		mv.visitLabel(l4);
//		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);		//Test if required
	}

}
