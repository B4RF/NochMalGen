package com.barf.nochmalgen.model;

import java.util.ArrayList;
import java.util.List;

public class ColorGroup {

	private Color color;
	private final List<Square> squares = new ArrayList<>();
	private boolean finished = true;

	public ColorGroup(final Color color) {
		this.color = color;
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void addSquare(final Square square) {
		this.squares.add(square);
		square.setColorGroup(this);
	}

	public boolean isFinished() {
		return this.finished;
	}

	public void setFinished(final boolean finished) {
		this.finished = finished;
	}

	public int getSize() {
		return this.squares.size();
	}

	public void inValidate() {
		for (final Square square : this.squares) {
			square.setColorGroup(null);
		}
	}
}