package com.sachett.slang.slangc.codegen.compoundstmt;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for Codegens of compound statements that serve as Control Nodes, like if, while, etc.
 */
public interface IControlNodeCodegen {
    @NotNull
    ControlNodeCodegenType controlNodeCodegenType = ControlNodeCodegenType.IF;

    default public ControlNodeCodegenType getControlNodeCodegenType() {
        return controlNodeCodegenType;
    }
}
