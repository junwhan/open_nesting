package org.deuce.transform.asm.code;

import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.asm.storage.GetterSetterDetails;

public class GetterSetterCode implements Opcodes{

	public static void addGttrCode(GetterSetterDetails gSD, MethodVisitor mv, String className) {
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		//Get the Field
		mv.visitVarInsn(Opcodes.ALOAD, 0);		
		mv.visitFieldInsn(Opcodes.GETFIELD, className, gSD.fD.VarName, gSD.fD.VarDesc);
		//Return the field value depending on Return type
		Type returnType = Type.getType(gSD.fD.VarDesc);
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

		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	public static void addSttrCode(GetterSetterDetails gSD, MethodVisitor mv, String className) {
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		//Load the Argument Depending upon variable type
		Type arg = Type.getType(gSD.fD.VarDesc);
		{
			if (arg.equals(Type.CHAR_TYPE) || arg.equals(Type.INT_TYPE)
					|| arg.equals(Type.SHORT_TYPE)
					|| arg.equals(Type.BYTE_TYPE)
					|| arg.equals(Type.BOOLEAN_TYPE))
				mv.visitVarInsn(ILOAD, 1);
			else if(arg.equals(Type.DOUBLE_TYPE))
				mv.visitVarInsn(DLOAD, 1);
			else if(arg.equals(Type.LONG_TYPE))
				mv.visitVarInsn(LLOAD	, 1);
			else if(arg.equals(Type.FLOAT_TYPE))
				mv.visitVarInsn(FLOAD, 1);
			else 
				mv.visitVarInsn(ALOAD, 1);				
		}
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, gSD.fD.VarName, gSD.fD.VarDesc);
		mv.visitInsn(Opcodes.RETURN);

		mv.visitMaxs(2, 2);
		mv.visitEnd();	
	}
}
