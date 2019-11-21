package rpi.legup.puzzle.shorttruthtable;


import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.rules.ContradictionRule;

import edu.rpi.legup.puzzle.shorttruthtable.ShortTruthTableBoard;
import edu.rpi.legup.puzzle.shorttruthtable.ShortTruthTableCell;
import edu.rpi.legup.puzzle.shorttruthtable.ShortTruthTableCellType;
import edu.rpi.legup.puzzle.shorttruthtable.ShortTruthTableStatement;

import java.util.Set;
import java.util.Iterator;


public class ContradictionRuleAtomic extends ContradictionRule{


    ContradictionRuleAtomic(){
        super("Contradicting Variable",
                "A single variable can not be both True and False",
                "image path");
    }


    @Override
    public String checkContradictionAt(Board puzzleBoard, PuzzleElement puzzleElement) {

        //cast the board toa shortTruthTableBoard
        ShortTruthTableBoard board = (ShortTruthTableBoard) puzzleBoard;

        //get the cell that contradicts another cell in the board
        ShortTruthTableCell cell = (ShortTruthTableCell) board.getPuzzleElement(puzzleElement);

        if(!cell.isVariable()){
            return "Can not check for contradiction on a non-variable element";
        }

        ShortTruthTableCellType cellType = cell.getType();
        if(!cellType.isTrueOrFalse())
            return "Can only check for a contradiction against a cell that is assigned a value of True or False";

        //get all the cells with the same value
        Set<ShortTruthTableCell> varCells = board.getCellsWithSymbol(cell.getSymbol());

        Iterator<ShortTruthTableCell> itr = varCells.iterator();
        while(itr.hasNext()){
            ShortTruthTableCellType checkCellType = itr.next().getType();
            //if there is an assigned contradiction, return null
            if(checkCellType.isTrueOrFalse() && checkCellType!=cellType)
                return null;
        }

        //if it made it through the while loop, thene there is no contradiction
        return "There is no contradiction for the variable "+cell.getSymbol();

    }

}