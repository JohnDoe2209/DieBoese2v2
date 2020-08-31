package player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import model.Board;

public class Minimax {
	private final int availibleThreads = 1;// Runtime.getRuntime().availableProcessors();
	private final int wantedDepth = 4; // could be increased during the game
	private final int squareSize = 2;
	private BoardEvaluation evaluation;
	private HashMap<Point, Integer> bestMoves;
	// private ArrayList<Point> sortedPoints;
	private final char myFigure;
	private final char enemyFigure;

	public Minimax(char myFigure, char enemyFigure) {
		evaluation = new BoardEvaluation(myFigure, enemyFigure);
		this.myFigure = myFigure;
		this.enemyFigure = enemyFigure;
	}

	/**
	 * takes the already split lists and starts different threads which are
	 * executing the bestMove method / minimax
	 * 
	 * @param board
	 * @param threadLists list which contains the lists with points for each thread
	 * @return returns best overall move generated by all threads
	 */
	private Point parallelizedSearch(Board realBoard, ArrayList<ArrayList<Point>> threadLists) {
		bestMoves = new HashMap<>();
		Thread threads[] = new Thread[availibleThreads];

		for (var i = 0; i < availibleThreads; i++) {
			var list = threadLists.get(i);
			threads[i] = new Thread(() -> {

				bestMove(realBoard.copyBoard(), list);
			});
			threads[i].start();
		}

		for (var thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Thread error");
			}
		}
		return sortPoints(bestMoves).get(0); // returns best evaluated point
	}

	private void bestMove(char[][] board, ArrayList<Point> allMoves) {
		var bestMove = new Point();
		var bestValue = Integer.MIN_VALUE;
		var worstValue = Integer.MAX_VALUE;
		var moves = new ArrayList<Point>();
		var clonedMoves = cloneList(allMoves);
		for (var move : clonedMoves) {
			moves.add(move);
			var value = setFigure(board, moves, true, clonedMoves, wantedDepth);
			worstValue = Math.min(value, bestValue);
			if (value > bestValue) {
				bestValue = value;
				bestMove = move;
			}
		}
		System.out.println(bestMove + ": " + bestValue);
		System.out.println("Worst value: " + worstValue);
		bestMoves.put(bestMove, bestValue);
	}

	/**
	 * places figure on board, executes minimax algorithm and deletes the figure
	 * afterwards
	 * 
	 * @param board
	 * @param move         the point where the figure should be placed
	 * @param isMaximizing
	 * @param depth        current depth
	 * @return value generated by the minimax algorithm
	 */
	private int setFigure(char[][] board, ArrayList<Point> previousMoves, boolean isMaximizing,
			ArrayList<Point> possibleMoves, int depth) {
		var x = previousMoves.get(previousMoves.size() - 1).x;
		var y = previousMoves.get(previousMoves.size() - 1).y;
		addSquare(board, previousMoves.get(previousMoves.size() - 1), possibleMoves);
		if (isMaximizing)
			board[x][y] = this.myFigure;
		else
			board[x][y] = this.enemyFigure;
		var value = minimax(board, previousMoves, !isMaximizing, possibleMoves, depth - 1);
		board[x][y] = ' ';
		previousMoves.remove(previousMoves.size() - 1);
		return value;
	}

	private int minimax(char[][] board, ArrayList<Point> lastMoves, boolean isMaximizing,
			ArrayList<Point> possibleMoves, int depth) {
		int bestValue = Integer.MAX_VALUE;
		ArrayList<Point> allMoves = possibleMoves;
		if (evaluation.hasWon(board, lastMoves.get(lastMoves.size() - 1))) {
			if (isMaximizing) {
				return Integer.MIN_VALUE;
			} else
				return Integer.MAX_VALUE;
		}
		if (depth == 0)
			return evaluation.evaluateBoard(board, lastMoves, isMaximizing);
		if (isMaximizing)
			bestValue = Integer.MIN_VALUE;
		for (var move : allMoves) {
			lastMoves.add(move);
			var value = setFigure(board, lastMoves, isMaximizing, possibleMoves, depth);
//			System.out.println(move + " Value: " + value);
			if (isMaximizing)
				bestValue = Math.max(value, bestValue);
			else
				bestValue = Math.min(value, bestValue);
		}

		return bestValue;
	}

	/**
	 * 
	 * @param board
	 * @return best point calculated by minimax
	 */
	public Point createMove(Board board) {
		var pointList = createPoints(board.getBoard());
		var evaluatedPoints = evaluation.evaluatePoints(board.getBoard(), pointList);
		var sortedPoints = sortPoints(evaluatedPoints);
		var threadList = createThreadList(sortedPoints);
		return parallelizedSearch(board, threadList);
	}

	/**
	 * sorts all points from evaluated best to worst move
	 * 
	 * @param pointList
	 */
	private ArrayList<Point> sortPoints(HashMap<Point, Integer> evaluatedPoints) {
		var sortedList = new ArrayList<Point>();
		var sorted = false;
		int highestValue;
		Point bestPoint = new Point(0, 0);
		while (!sorted) {
			highestValue = Integer.MIN_VALUE;
			sorted = true;
			for (var point : evaluatedPoints.keySet()) {
				if (evaluatedPoints.get(point) > highestValue) {
					bestPoint = point;
					highestValue = evaluatedPoints.get(point);
					sorted = false;
				}
			}
			sortedList.add(bestPoint);
			evaluatedPoints.remove(bestPoint);
		}
		return sortedList;
	}

	private ArrayList<Point> cloneList(ArrayList<Point> list) {
		var clonedList = new ArrayList<Point>();
		for (var element : list) {
			clonedList.add(element);
		}
		return clonedList;
	}

	/**
	 * creates list of points which might be good moves for minimax algorithm
	 * 
	 * @param board
	 * @return list of points
	 */
	private ArrayList<Point> createPoints(char[][] board) {
		var examinePoints = new ArrayList<Point>();
		for (var x = 0; x < board.length; x++) {
			for (var y = 0; y < board.length; y++) {
				if (board[x][y] == myFigure || board[x][y] == enemyFigure) {
					addSquare(board, new Point(x, y), examinePoints);
				}
			}
		}
		return examinePoints;
	}

	private void addSquare(char[][] board, Point center, ArrayList<Point> list) {
		var square = new ArrayList<Point>();
		for (int x = center.x - squareSize; x <= center.x + squareSize; x++) {
			for (int y = center.y - squareSize; y <= center.y + squareSize; y++) {
				if ((x >= 0 && x < board.length) && (y >= 0 && y < board.length)) {
					if (board[x][y] == ' ') {
						square.add(new Point(x, y));
					}
				}
			}
		}
		for (var point : square) {
			if (!list.contains(point))
				list.add(point);
		}
	}

	/**
	 * splits points to all threads starting with the best move
	 * 
	 * @param allMoves
	 * @return
	 */
	private ArrayList<ArrayList<Point>> createThreadList(ArrayList<Point> allMoves) {
		var threadList = new ArrayList<ArrayList<Point>>();
		for (var i = 0; i < availibleThreads; i++) {
			threadList.add(new ArrayList<>());
		}
		for (var point : allMoves) {
			var threadNumber = allMoves.indexOf(point) % availibleThreads;
			ArrayList<Point> currentList = threadList.get(threadNumber);
			currentList.add(point);
		}
		return threadList;
	}
}
