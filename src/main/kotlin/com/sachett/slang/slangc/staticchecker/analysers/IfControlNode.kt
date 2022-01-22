package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.FunctionSymbol

class IfControlNode(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
    ifCtx: SlangParser.IfStmtContext
) :
    IFunctionInnerBlock, SlangBaseListener() {
    private var doesReturnComputed = false
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()
    var hasElseBlock: Boolean = false

    init {
        // Partially build the IfControlNode using the ifCtx.
        // This part builds the children of this node (if, else if and else control blocks)
        // Note that a stray block is added to each of these control blocks
        // by default (in init method of ControlBlock).

        if (ifCtx.IF() != null) {
            val ifCtrlBlock = ControlBlock(parentFnSymbol, parent = this, type = ControlBlockType.IF)
            children.add(ifCtrlBlock)
        }

        for (elseIfBlockParse in ifCtx.elseifblocks) {
            val elseIfCtrlBlock = ControlBlock(parentFnSymbol, parent = this, type = ControlBlockType.ELSEIF)
            children.add(elseIfCtrlBlock)
        }

        if (ifCtx.ELSE() != null) {
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