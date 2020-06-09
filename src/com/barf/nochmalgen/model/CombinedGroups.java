package com.barf.nochmalgen.model;

import java.util.HashSet;
import java.util.Set;

public class CombinedGroups implements Comparable<CombinedGroups> {

	private int minSize = 0;
	private int maxSize = 0;
	private Set<ColorGroup> groups = new HashSet<>();

	public CombinedGroups(final Set<ColorGroup> groups) {
		this.groups = groups;

		if (groups.size() > 1) {
			this.minSize++;
		}

		for (final ColorGroup colorGroup : groups) {
			this.minSize += colorGroup.getSize();
		}

		this.maxSize = ColorGroup.getMaxSize(groups);
	}

	public int getMinSize() {
		return this.minSize;
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public Set<ColorGroup> getColorGroups() {
		return this.groups;
	}

	@Override
	public int compareTo(final CombinedGroups o) {
		if (this.groups.size() == 1 && o.groups.size() > 1) {
			return -1;
		} else if (this.groups.size() > 1 && o.groups.size() == 1) {
			return 1;
		} else {
			return Integer.compare(o.minSize, this.minSize);
		}
	}
}
