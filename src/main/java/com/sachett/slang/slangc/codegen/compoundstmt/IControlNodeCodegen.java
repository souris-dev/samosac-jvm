package com.sachett.slang.slangc.codegen.compoundstmt;

import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for Codegens of compound statements that serve as Control Nodes, like if, while, etc.
 */
public interface IControlNodeCodegen {
    public ControlNodeCodegenType getControlNodeCodegenType();
}
