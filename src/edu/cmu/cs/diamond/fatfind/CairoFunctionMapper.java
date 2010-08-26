package edu.cmu.cs.diamond.fatfind;

import java.lang.reflect.Method;

import com.sun.jna.FunctionMapper;
import com.sun.jna.NativeLibrary;

class CairoFunctionMapper implements FunctionMapper {

    final static CairoFunctionMapper INSTANCE = new CairoFunctionMapper();

    public static FunctionMapper getInstance() {
        return INSTANCE;
    }

    private CairoFunctionMapper() {
    }

    @Override
    public String getFunctionName(NativeLibrary library, Method method) {
        StringBuilder sb = new StringBuilder("cairo_");
        String name = method.getName();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
