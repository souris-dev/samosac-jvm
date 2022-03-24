package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol

class WhileControlNode(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
    whileCtx: SamosaParser.WhileStmtContext
) :
    ControlNode(parentFnSymbol, parent) {
    private var doesReturnComputed = false
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()

    init {
        // Partially build the WhileControlNode using the whileCtx.
        // (add a WhileControlBlock and a stray block to it)

        // The ControlBlock initializes and adds a stray block to itself
        val whileControlBlock = ControlBlock(parentFnSymbol, this, ControlBlockType.WHILE)
        children.add(whileControlBlock)
    }

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
        set(value) {
            doesReturnComputed = true
            field = value
        }
}
