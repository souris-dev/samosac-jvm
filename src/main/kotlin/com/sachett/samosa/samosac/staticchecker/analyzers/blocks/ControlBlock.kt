package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.samosac.symbol.FunctionSymbol

class ControlBlock(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock?,
    val type: ControlBlockType = ControlBlockType.IF
) :
    IFunctionInnerBlock {
    private var doesReturnComputed = false

    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()

    init {
        // Every ControlBlock has at least one child block that is initially considered an empty block
        children.add(StrayBlock(parentFnSymbol, doesReturnProperly = false, parent = this))
    }

    override var doesReturnProperly: Boolean = false
        get() {
            if (doesReturnComputed) {
                return field
            }

            calculateReturn()
            doesReturnComputed = true
            return field
        }
        set(value) {
            doesReturnComputed = true
            field = value
        }

    private fun calculateReturn() {
        if (children.size == 0) {
            doesReturnProperly = false
        }

        if (children.size == 1) {
            doesReturnProperly = children[0].doesReturnProperly
            return
        }

        // Compute doesReturnProperly for this node recursively by visiting the nodes
        var tempDoesReturnProperly = false
        for (child in children) {
            if (((child is IfControlNode) && !child.hasElseBlock) || (child is WhileControlNode)) {
                continue
            }

            tempDoesReturnProperly = tempDoesReturnProperly || child.doesReturnProperly
        }

        doesReturnProperly = tempDoesReturnProperly
    }
}