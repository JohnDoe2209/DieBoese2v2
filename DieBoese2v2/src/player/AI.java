package player;

import java.awt.Point;

import exceptions.InvalidMoveException;
import model.Data;

public class AI extends Player {
	private Minimax minimax;

	public AI(final char figure, Data data) {
		super(figure, data);
	}

	@Override
	public void move() {
		do {
			this.chooseMove();
		} while (!this.isValidMove());
		this.updateData();
	}

	private void chooseMove() {
		if (this.data.getTurnCounter() < 9)
			this.performRandomMove();
		else if (this.data.getTurnCounter() == 9)
			this.performSecondMove();
		else
			this.performMinimax();
	}

	private int createCoordinate() {
		return (int) (Math.random() * this.data.getBoardSize());
	}

	private Point createPointBottom() {
		return new Point(this.createCoordinate(), this.data.getBoardSize() - 1);
	}

	private Point createPointLeft() {
		return new Point(0, this.createCoordinate());
	}

	private Point createPointRight() {
		return new Point(this.data.getBoardSize() - 1, this.createCoordinate());
	}

	private Point createPointTop() {
		return new Point(this.createCoordinate(), 0);
	}

	private boolean isValidMove() {
		try {
			this.setMoveInData();
		} catch (final InvalidMoveException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

	private void performMinimax() {
		this.minimax = new Minimax(this.getFigure(), this.data.getEnemyFigure());
		this.setMyMove(this.minimax.createMove(this.data.getBoard()));
	}

	private void performRandomMove() {
		this.setMyMove(this.randomMove());
	}

	private void performSecondMove() {
		this.setMyMove(this.secondMove());
	}

	private Point randomMove() {
		return new Point(this.createCoordinate(), this.createCoordinate());
	}

	private Point secondMove() {
		switch ((int) (Math.random() * 4)) {
			case 0:
				return this.createPointLeft();
			case 1:
				return this.createPointRight();
			case 2:
				return this.createPointTop();
			case 3:
				return this.createPointBottom();
		}
		return null;
	}
}
