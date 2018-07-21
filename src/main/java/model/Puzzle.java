package model;

import model.gameboard.Board;
import model.gameboard.ElementFactory;
import model.observer.IBoardListener;
import model.observer.IBoardSubject;
import model.observer.ITreeListener;
import model.observer.ITreeSubject;
import model.rules.*;
import model.tree.Tree;
import model.tree.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import save.InvalidFileFormatException;
import ui.boardview.BoardView;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Puzzle implements IBoardSubject, ITreeSubject
{
    private static final Logger LOGGER = Logger.getLogger(Puzzle.class.getName());

    protected String name;
    protected Board currentBoard;
    protected Tree tree;
    protected BoardView boardView;
    protected PuzzleImporter importer;
    protected PuzzleExporter exporter;
    protected ElementFactory factory;

    private List<IBoardListener> boardListeners;
    private List<ITreeListener> treeListeners;

    protected List<BasicRule> basicRules;
    protected List<ContradictionRule> contradictionRules;
    protected List<CaseRule> caseRules;

    /**
     * Puzzle Constructor - creates a new Puzzle
     */
    public Puzzle()
    {
        this.boardListeners = new ArrayList<>();
        this.treeListeners = new ArrayList<>();

        this.basicRules = new ArrayList<>();
        this.contradictionRules = new ArrayList<>();
        this.caseRules = new ArrayList<>();
    }

    /**
     * Initializes the view. Called by the invoker of the class
     */
    public abstract void initializeView();

    /**
     * Generates a random puzzle based on the difficulty
     *
     * @param difficulty level of difficulty (1-10)
     * @return board of the random puzzle
     */
    public abstract Board generatePuzzle(int difficulty);

    /**
     * Determines if the puzzle was solves correctly
     *
     * @return true if the board was solved correctly, false otherwise
     */
    public boolean isPuzzleComplete()
    {
        boolean isComplete = tree.isValid();
        if(isComplete)
        {
            for(TreeNode leaf : tree.getLeafNodes())
            {
                if(!leaf.isRoot())
                {
                    isComplete &= leaf.getParent().isContradictoryBranch() || isBoardComplete(leaf.getBoard());
                }
                else
                {
                    isComplete &= isBoardComplete(leaf.getBoard());
                }
            }
        }
        return isComplete;
    }

    /**
     * Determines if the current board is a valid state
     *
     * @param board board to check for validity
     * @return true if board is valid, false otherwise
     */
    public abstract boolean isBoardComplete(Board board);

    /**
     * Callback for when the board data changes
     *
     * @param board the board that has changed
     */
    public abstract void onBoardChange(Board board);

    /**
     * Imports the board using the file stream
     *
     * @param fileName
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void importPuzzle(String fileName) throws InvalidFileFormatException
    {
        try
        {
            importPuzzle(new FileInputStream(fileName));
        }
        catch(IOException e)
        {
            LOGGER.log(Level.SEVERE, "Invalid file");
            throw new InvalidFileFormatException("Could not find file");
        }
    }

    /**
     * Imports the board using the file stream
     *
     * @param inputStream
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void importPuzzle(InputStream inputStream) throws InvalidFileFormatException
    {
        Document document;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
        }
        catch(IOException | SAXException | ParserConfigurationException e)
        {
            LOGGER.log(Level.SEVERE, "Invalid file");
            throw new InvalidFileFormatException("Could not find file");
        }

        Element rootNode = document.getDocumentElement();
        if(rootNode.getTagName().equals("Legup"))
        {
            Node node = rootNode.getElementsByTagName("puzzle").item(0);
            if(importer == null)
            {
                throw new InvalidFileFormatException("Puzzle importer null");
            }
            importer.initializePuzzle(node);
        }
        else
        {
            LOGGER.log(Level.ALL, "Invalid file");
            throw new InvalidFileFormatException("Invalid file: must be a Legup file");
        }
    }

    /**
     * Imports a proof file
     *
     * @param fileName
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void importProof(String fileName) throws InvalidFileFormatException
    {
        importPuzzle(fileName);
    }

    /**
     * Gets the puzzle importer for importing puzzle files
     *
     * @return puzzle importer
     */
    public PuzzleImporter getImporter()
    {
        return importer;
    }

    /**
     * Gets the puzzle exporter for exporting puzzle files
     *
     * @return puzzle exporter
     */
    public PuzzleExporter getExporter()
    {
        return exporter;
    }

    /**
     * Gets the name of the puzzle
     *
     * @return name of the puzzle
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the list of basic rules
     *
     * @return list of basic rules
     */
    public List<BasicRule> getBasicRules()
    {
        return basicRules;
    }

    /**
     * Sets the list of basic rules
     *
     * @param basicRules list of basic rules
     */
    public void setBasicRules(List<BasicRule> basicRules)
    {
        this.basicRules = basicRules;
    }

    /**
     * Adds a basic rule to this Puzzle
     *
     * @param rule basic rule to add
     */
    public void addBasicRule(BasicRule rule)
    {
        basicRules.add(rule);
    }

    /**
     * Remove a basic rule from this Puzzle
     *
     * @param rule basic rule to remove
     */
    public void removeBasicRule(BasicRule rule)
    {
        basicRules.remove(rule);
    }

    /**
     * Gets the list of contradiction rules
     *
     * @return list of contradiction rules
     */
    public List<ContradictionRule> getContradictionRules()
    {
        return contradictionRules;
    }

    /**
     * Sets the list of contradiction rules
     *
     * @param contradictionRules list of contradiction rules
     */
    public void setContradictionRules(List<ContradictionRule> contradictionRules)
    {
        this.contradictionRules = contradictionRules;
    }

    /**
     * Adds a contradiction rule to this Puzzle
     *
     * @param rule contradiction rule to add
     */
    public void addContradictionRule(ContradictionRule rule)
    {
        contradictionRules.add(rule);
    }

    /**
     * Remove a contradiction rule from this Puzzle
     *
     * @param rule contradiction rule to remove
     */
    public void removeContradictionRule(ContradictionRule rule)
    {
        contradictionRules.remove(rule);
    }

    /**
     * Gets the list of case rules
     *
     * @return list of case rules
     */
    public List<CaseRule> getCaseRules()
    {
        return caseRules;
    }

    /**
     * Sets the list of case rules
     *
     * @param caseRules list of case rules
     */
    public void setCaseRules(List<CaseRule> caseRules)
    {
        this.caseRules = caseRules;
    }

    /**
     * Adds a case rule to this Puzzle
     *
     * @param rule case rule to add
     */
    public void addCaseRule(CaseRule rule)
    {
        caseRules.add(rule);
    }

    /**
     * Removes a case rule from this Puzzle
     *
     * @param rule case rule to remove
     */
    public void removeCaseRule(CaseRule rule)
    {
        caseRules.remove(rule);
    }

    /**
     * Gets the rule using the specified name
     *
     * @param name name of the rule
     * @return Rule
     */
    public Rule getRuleByName(String name)
    {
        for(Rule rule : basicRules)
        {
            if(rule.getRuleName().equals(name))
            {
                return rule;
            }
        }
        for(Rule rule : contradictionRules)
        {
            if(rule.getRuleName().equals(name))
            {
                return rule;
            }
        }
        for(Rule rule : caseRules)
        {
            if(rule.getRuleName().equals(name))
            {
                return rule;
            }
        }
        Rule mergeRule = new MergeRule();
        if(mergeRule.getRuleName().equals(name))
        {
            return mergeRule;
        }
        return null;
    }

    /**
     * Gets the current board
     *
     * @return current board
     */
    public Board getCurrentBoard()
    {
        return currentBoard;
    }

    /**
     * Sets the current board
     *
     * @param currentBoard the current board
     */
    public void setCurrentBoard(Board currentBoard)
    {
        this.currentBoard = currentBoard;
    }

    /**
     * Gets the Tree for keeping the board states
     *
     * @return Tree
     */
    public Tree getTree()
    {
        return tree;
    }

    /**
     * Sets the Tree for keeping the board states
     *
     * @param tree tree of board states
     */
    public void setTree(Tree tree)
    {
        this.tree = tree;
    }

    /**
     * Gets the board view that displays the board
     *
     * @return board view
     */
    public BoardView getBoardView()
    {
        return boardView;
    }

    /**
     * Sets the board view that displays the board
     *
     * @param boardView board view
     */
    public void setBoardView(BoardView boardView)
    {
        this.boardView = boardView;
    }

    /**
     * Gets the ElementFactory associated with this puzzle
     *
     * @return ElementFactory associated with this puzzle
     */
    public ElementFactory getFactory()
    {
        return factory;
    }

    /**
     * Sets the ElementFactory associated with this puzzle
     *
     * @param factory ElementFactory associated with this puzzle
     */
    public void setFactory(ElementFactory factory)
    {
        this.factory = factory;
    }

    /**
     * Adds a board listener
     *
     * @param listener listener to add
     */
    @Override
    public void addBoardListener(IBoardListener listener)
    {
        boardListeners.add(listener);
    }

    /**
     * Removes a board listener
     *
     * @param listener listener to remove
     */
    @Override
    public void removeBoardListener(IBoardListener listener)
    {
        boardListeners.remove(listener);
    }

    /**
     * Notifies listeners
     *
     * @param algorithm algorithm to notify the listeners with
     */
    @Override
    public void notifyBoardListeners(Consumer<? super IBoardListener> algorithm)
    {
        boardListeners.forEach(algorithm);
    }

    /**
     * Adds a board listener
     *
     * @param listener listener to add
     */
    @Override
    public void addTreeListener(ITreeListener listener)
    {
        treeListeners.add(listener);
    }

    /**
     * Removes a tree listener
     *
     * @param listener listener to remove
     */
    @Override
    public void removeTreeListener(ITreeListener listener)
    {
        treeListeners.remove(listener);
    }

    /**
     * Notifies listeners
     *
     * @param algorithm algorithm to notify the listeners with
     */
    @Override
    public void notifyTreeListeners(Consumer<? super ITreeListener> algorithm)
    {
        treeListeners.forEach(algorithm);
    }
}