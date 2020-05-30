package com.barf.nochmalgen.model;

import java.util.ArrayList;
import java.util.List;

public class Square {

	private Color color;
	private ColorGroup colorGroup;
	private final List<Square> neighbors = new ArrayList<>();

	public Color getColor() {
		return this.color;
	}

	public void setColor(final Color color) {
		this.color = color;

		if (this.colorGroup != null) {
			this.colorGroup.inValidate();
		}

		for (final Square square : this.neighbors) {
			if (square.getColorGroup() != null) {
				square.getColorGroup().inValidate();
			}
		}
	}

	public ColorGroup getColorGroup() {
		return this.colorGroup;
	}

	public void setColorGroup(final ColorGroup colorGroup) {
		this.colorGroup = colorGroup;
	}

	public List<Square> getNeighbors() {
		return this.neighbors;
	}

	public void addNeighbor(final Square square) {
		this.neighbors.add(square);
	}
}
