package com.sachett.samosa.samosac.codegen.compoundstmt;

/**
 * Interface for Codegens of compound statements that serve as Control Nodes, like if, while, etc.
 */
public interface IControlNodeCodegen {
    public ControlNodeCodegenType getControlNodeCodegenType();
}
