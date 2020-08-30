package player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public class Minimax {
	private final int availibleThreads = 1;//Runtime.getRuntime().availableProcessors();
	private final int wantedDepth = 3; // could be increased during the game
	private BoardEvaluation evaluation;
	private HashMap<Point, Integer> bestMoves;
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
	private Point parallelizedSearch(char[][] board, ArrayList<ArrayList<Point>> threadLists) {
		bestMoves = new HashMap<>();
		Thread threads[] = new Thread[availibleThreads];
		for (var i = 0; i < availibleThreads; i++) {
			var list = threadLists.get(i);
			threads[i] = new Thread(() -> {
				bestMove(board, list);
			});
			threads[i].start();
		}
		for (var thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("Thread error");
			}
		}
		return sortPoints(bestMoves).get(0); // returns best evaluated point
	}

	private void bestMove(char[][] board, ArrayList<Point> allMoves) {
		var bestMove = new Point();
		var bestValue = Integer.MIN_VALUE;
		Point[] moves = new Point[wantedDepth];
		for (var move : allMoves) {
			moves[0] = move;
			var value = setFigure(board, moves, true, wantedDepth);
			if (value > bestValue) {
				bestValue = value;
				bestMove = move;
			}
		}
		System.out.println(bestMove+": "+ bestValue);
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
	private int setFigure(char[][] board, Point[] moves, boolean isMaximizing, int depth) {
		var x = moves[wantedDepth - depth].x;
		var y = moves[wantedDepth - depth].y;
		if (isMaximizing)
			board[x][y] = this.myFigure;
		else
			board[x][y] = this.enemyFigure;
		var value = minimax(board, moves, !isMaximizing, depth - 1);
		board[x][y] = ' ';
		return value;
	}

	private int minimax(char[][] board, Point[] lastMoves, boolean isMaximizing, int depth) {
		int bestValue = Integer.MAX_VALUE;
		var allMoves = createPoints(board);
		if (evaluation.hasWon(board, lastMoves[wantedDepth - (depth)])) {
			if (isMaximizing)
				return Integer.MIN_VALUE;
			else
				return Integer.MAX_VALUE;
		}
		if (depth == 0)
			return evaluation.evaluateBoard(board, lastMoves, isMaximizing);
		if (isMaximizing)
			bestValue = Integer.MIN_VALUE;
		for (var move : allMoves) {
			lastMoves[wantedDepth-depth] = move;
			var value = setFigure(board, lastMoves, isMaximizing, depth);
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
	public Point createMove(char[][] board) {
		var pointList = createPoints(board);
		var evaluatedPoints = evaluation.evaluatePoints(board, pointList);
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

	/**
	 * creates list of points which might be good moves for minimax algorithm
	 * 
	 * @param board
	 * @return list of points
	 */
	private ArrayList<Point> createPoints(char[][] board) {
		var examinePoints = new ArrayList<Point>();
		// small board / move evaluation needed

		// just for testing:
		// all empty positions
		for (var x = 0; x < board.length; x++) {
			for (var y = 0; y < board.length; y++) {
				if (board[x][y] == ' ')
					examinePoints.add(new Point(x, y));
			}
		}
		return examinePoints;
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
