package player;

import java.awt.Point;

import io.Input;
import io.localization.ConsoleOutput;

public class HumanPlayer extends Player {
	private final boolean DEBUG = false;

	public HumanPlayer(final char figure) {
		super(figure);
	}

	@Override
	public void move(final int boardSize) {
		ConsoleOutput.printCoordinateInput();
		try {
			this.move(boardSize, Input.readInput().toLowerCase());
		} catch (final InvalidStringException e) {
			System.out.println(e.getMessage());
			this.move(boardSize);
		}
	}

	private Point convertCoordinates(final int boardSize, final String coordinates) {
		return new Point(this.getLetter(coordinates), this.getNumber(boardSize, coordinates));
	}

	/**
	 * gets letter in string as int
	 */
	private int getLetter(final String coordinates) {
		var letter = 0;
		for (var i = 0; i < coordinates.length(); i++)
			if (((coordinates.charAt(i) >= 'a') && (coordinates.charAt(i) <= 'z')))
				letter = coordinates.charAt(i) - 'a';
		return letter;
	}

	/**
	 * gets number in string as int
	 */
	private int getNumber(final int boardSize, final String coordinates) {
		var number = "";
		for (var i = 0; i < coordinates.length(); i++)
			if (((coordinates.charAt(i) >= '0') && (coordinates.charAt(i) <= '9')))
				number += coordinates.charAt(i);
		return (boardSize - Integer.valueOf(number));
	}

	/**
	 * checks string and sets move
	 */
	private void move(final int boardSize, final String coordinates) throws InvalidStringException {
		this.validateString(boardSize, coordinates);
		this.setMyMove(this.convertCoordinates(boardSize, coordinates));
	}

	/**
	 * checks if character is a letter or a number
	 *
	 * @throws InvalidStringException
	 */
	private void validateCharacters(final String coordinates) throws InvalidStringException {
		for (var i = 0; i < coordinates.length(); i++)
			if ((coordinates.charAt(i) < '0')
					|| ((coordinates.charAt(i) > '9') && (coordinates.charAt(i) < 'a'))
					|| (coordinates.charAt(i) > 'z'))
				throw new InvalidStringException(coordinates.charAt(i) + " is not a valid character!");
	}

	/**
	 * checks if string length is 2 or 3
	 *
	 * @throws InvalidStringException
	 */
	private void validateLength(final String coordinates) throws InvalidStringException {
		if (coordinates.length() < 2)
			throw new InvalidStringException("Please enter more than one character!");
		if (coordinates.length() > 3)
			throw new InvalidStringException("Please don't enter more than three characters!");
	}

	/**
	 * checks if letter is valid
	 *
	 * @throws InvalidStringException
	 */
	private void validateLetter(final int boardSize, final String coordinates) throws InvalidStringException {
		for (var i = 0; i < coordinates.length(); i++)
			if (coordinates.charAt(i) >= 'a')
				if ((coordinates.charAt(i) - 'a') > (boardSize - 1))
					throw new InvalidStringException(
							"Please enter a letter between a and " + (char) ('a' + (boardSize - 1)) + "!");
	}

	/**
	 * checks if there is only one letter
	 *
	 * @throws InvalidStringException
	 */
	private void validateLetterCount(final String coordinates) throws InvalidStringException {
		var letterCount = 0;
		for (var i = 0; i < coordinates.length(); i++)
			if (coordinates.charAt(i) >= 'a')
				if (++letterCount > 1)
					throw new InvalidStringException("Please don't enter more than one letter!");
		if (letterCount == 0)
			throw new InvalidStringException("You need to enter a letter as well!");
	}

	/**
	 * checks if number is valid
	 *
	 * @throws InvalidStringException
	 */
	private void validateNumber(final int boardSize, final String coordinates) throws InvalidStringException {
		var number = "";
		for (var i = 0; i < coordinates.length(); i++)
			if (((coordinates.charAt(i) >= '0') && (coordinates.charAt(i) <= '9')))
				number += coordinates.charAt(i);
		if ((Integer.valueOf(number) <= 0) || (Integer.valueOf(number) > (boardSize)))
			throw new InvalidStringException("Please enter a number between 1 and " + boardSize + "!");
	}

	/**
	 * checks if second character is a letter in case string has a length of 3
	 *
	 * @throws InvalidStringException
	 */
	private void validateOrder(final String coordinates) throws InvalidStringException {
		if ((coordinates.length() == 3) && ((coordinates.charAt(1) <= '0') || (coordinates.charAt(1) >= '9')))
			throw new InvalidStringException("Please don't enter more than one number!");
	}

	private void validateString(final int boardSize, final String coordinates) throws InvalidStringException {
		this.validateLength(coordinates);
		this.validateCharacters(coordinates);
		this.validateLetterCount(coordinates);
		this.validateOrder(coordinates);
		this.validateLetter(boardSize, coordinates);
		this.validateNumber(boardSize, coordinates);
	}
}
