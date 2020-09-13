package player;

import java.awt.Point;

import exceptions.InvalidMoveException;
import model.Data;
import model.Turn;

public abstract class Player {
	protected Data data;
	private final char figure;
	private Point myMove;

	public Player(final char figure, Data data) {
		this.figure = figure;
		this.data = data;
	}

	public char getFigure() {
		return this.figure;
	}

	public Point getMyMove() {
		return this.myMove;
	}

	public abstract void move();

	protected void setMoveInData() throws InvalidMoveException {
		Turn.setMove(this, this.data);
	}

	protected void setMyMove(final Point move) {
		this.myMove = move;
	}

	protected void updateData() {
		this.data.load(this.getFigure(), this.getMyMove());
	}
}
