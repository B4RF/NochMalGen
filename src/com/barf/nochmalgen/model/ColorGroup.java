package com.barf.nochmalgen.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColorGroup implements Comparable<ColorGroup> {

	private Color color;
	private final List<Square> squares = new ArrayList<>();
	private final Set<ColorGroup> connectedNoColorGroups = new HashSet<>();

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

	public Set<ColorGroup> getConnectedNCGroups() {
		return this.connectedNoColorGroups;
	}

	public void addNoColorGroup(final ColorGroup group) {
		this.connectedNoColorGroups.add(group);
	}

	public int getSize() {
		return this.squares.size();
	}

	public void inValidate() {
		for (final Square square : this.squares) {
			square.setColorGroup(null);
		}
	}

	@Override
	public int compareTo(final ColorGroup group) {
		if (this.getSize() == group.getSize()) {
			Integer.compare(this.connectedNoColorGroups.size(), group.connectedNoColorGroups.size());
		}

		return Integer.compare(this.getSize(), group.getSize());
	}

	public static int getMaxSize(final Set<ColorGroup> groups) {
		int size = 0;
		final Set<ColorGroup> noColorGroups = new HashSet<>();

		for (final ColorGroup colorGroup : groups) {
			size += colorGroup.getSize();
			noColorGroups.addAll(colorGroup.getConnectedNCGroups());
		}

		for (final ColorGroup colorGroup : noColorGroups) {
			size += colorGroup.getSize();
		}

		return size;
	}
}