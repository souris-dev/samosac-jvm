package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.slangc.symbol.FunctionSymbol

class IfControlNode(override val parentFnSymbol: FunctionSymbol) : IFunctionInnerBlock {
    private var doesReturnComputed = false
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()

    private var hasElseBlockComputed = false
    var hasElseBlock: Boolean = false
        get() {
            if (hasElseBlockComputed) {
                return field
            }

            for (child in children) {
                if (child is ControlBlock && child.type == ControlBlockType.ELSE) {
                    field = true
                }
            }

            hasElseBlockComputed = true
            return field
        }
    private set

    /**
     * For an if-control-node, all the children blocks must return a value for the node to return a value.
     */
    override var doesReturnProperly: Boolean = false
        get() {
            if (doesReturnComputed) {
                return field
            }

            var returns = true

            for (child in children) {
                returns = returns && child.doesReturnProperly
            }

            field = returns
            doesReturnComputed = true
            return field
        }
        private set
}