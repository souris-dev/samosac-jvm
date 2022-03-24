package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol

class IfControlNode(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
    ifCtx: SamosaParser.IfStmtContext
) :
    ControlNode(parentFnSymbol, parent) {
    private var doesReturnComputed = false
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()
    var hasElseBlock: Boolean = false

    init {
        // Partially build the IfControlNode using the ifCtx.
        // This part builds the children of this node (if, else if and else control blocks)
        // Note that a stray block is added to each of these control blocks
        // by default (in init method of ControlBlock).

        val nIfs = ifCtx.IF().size
        val nElses = ifCtx.ELSE().size

        if (nIfs > 0) {
            val ifCtrlBlock = ControlBlock(parentFnSymbol, parent = this, type = ControlBlockType.IF)
            children.add(ifCtrlBlock)
        }

        for (elseIfBlockParse in ifCtx.elseifblocks) {
            val elseIfCtrlBlock = ControlBlock(parentFnSymbol, parent = this, type = ControlBlockType.ELSEIF)
            children.add(elseIfCtrlBlock)
        }

        // Note that number of IF tokens == number of ELSE tokens implies that there's an ELSE block
        if (nIfs == nElses) {
            hasElseBlock = true
            val elseCtrlBlock = ControlBlock(parentFnSymbol, parent = this, type = ControlBlockType.ELSE)
            children.add(elseCtrlBlock)
        }
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