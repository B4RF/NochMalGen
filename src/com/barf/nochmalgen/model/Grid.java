package com.barf.nochmalgen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.barf.nochmalgen.Main;

public class Grid {

	public static int ROW_COUNT = 7;
	public static int COLUMN_COUNT = 15;
	public static int COLOR_COUNT = 21;

	private final Square[][] squares = new Square[Grid.ROW_COUNT][Grid.COLUMN_COUNT];

	private final List<Color> changedColors = new ArrayList<>();

	private int validations = 0;
	private long startTime = 0l;

	public Grid() {
		for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				this.squares[i][j] = new Square();
			}
		}

		for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				final Square square = this.squares[i][j];

				if (i > 0) {
					square.addNeighbor(this.squares[i - 1][j]);
				}
				if (j > 0) {
					square.addNeighbor(this.squares[i][j - 1]);
				}
				if (i < Grid.ROW_COUNT - 1) {
					square.addNeighbor(this.squares[i + 1][j]);
				}
				if (j < Grid.COLUMN_COUNT - 1) {
					square.addNeighbor(this.squares[i][j + 1]);
				}
			}
		}
		this.startTime = System.currentTimeMillis();
	}

	public Color getPos(final int x, final int y) {
		return this.squares[x][y].getColor();
	}

	public void setPos(final Color color, final int x, final int y) {
		final Square square = this.squares[x][y];
		this.changedColors.add(square.getColor());

		square.setColor(color);
		this.changedColors.add(color);
	}

	public static <T extends Enum<T>> Collector<T, ?, Map<T, Integer>> counting(final Class<T> type) {
		return Collectors.toMap(Function.<T>identity(), x -> 1, Integer::sum, () -> new HashMap<>(
				Stream.of(type.getEnumConstants()).collect(Collectors.toMap(Function.<T>identity(), t -> 0))));
	}

	public boolean isValid() {
		this.validations++;
		boolean isValid = true;

		isValid = this.columnsValid();

		if (isValid) {
			isValid = this.rowsValid();
		}

		if (isValid) {
			isValid = this.groupsValid();
		}

		if (isValid) {
			this.changedColors.clear();
		}

		return isValid;
	}

	private boolean rowsValid() {
		boolean isValid = true;

		final Map<Color, Integer> remainingSquaresRow = new HashMap<>();
		final Map<Color, Boolean> usedMissingRow = new HashMap<>();

		for (final Color square : Color.values()) {
			remainingSquaresRow.put(square, Grid.COLOR_COUNT);
			usedMissingRow.put(square, false);
		}

		outer: for (int i = 0; i < Grid.ROW_COUNT; i++) {
			final Map<Color, Integer> rowCounts = Stream.of(this.squares[i]).flatMap(s -> Stream.of(s.getColor()))
					.collect(Grid.counting(Color.class));
			final int emptySquares = rowCounts.containsKey(null) ? rowCounts.get(null) : 0;
			final long differentColors = rowCounts.entrySet().stream()
					.filter(e -> e.getKey() != null && e.getValue() > 0).map(Map.Entry::getKey).distinct().count();

			if (emptySquares + differentColors < 4) {
				if (Main.DEBUG) {
					System.err.println("two colors missing in same row");
				}
				isValid = false;
				break outer;
			}

			for (final Entry<Color, Integer> entry : rowCounts.entrySet()) {
				final Color key = entry.getKey();

				if (key != null && this.changedColors.contains(key)) {
					final int count = entry.getValue();

					if (count == 0) {
						if (emptySquares == 0) {
							if (usedMissingRow.get(key)) {
								if (Main.DEBUG) {
									System.err.println(key.name() + " is missing in two rows");
								}
								isValid = false;
								break outer;
							} else {
								usedMissingRow.put(key, true);
							}
						}
					} else if (count > 7) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has more then 7 entries in same row");
						}
						isValid = false;
						break outer;
					}

					final int remaining = remainingSquaresRow.get(key) - (count == 0 ? 1 : count);

					if (remaining < 0) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has not enough squares left for the missing rows");
						}
						isValid = false;
						break outer;
					} else {
						remainingSquaresRow.put(key, remaining);
					}
				}
			}
		}
		return isValid;
	}

	private boolean columnsValid() {
		boolean isValid = true;

		final Map<Color, Integer> remainingSquaresColumn = new HashMap<>();
		for (final Color square : Color.values()) {
			remainingSquaresColumn.put(square, Grid.COLOR_COUNT);
		}

		outer: for (int i = 0; i < Grid.COLUMN_COUNT; i++) {
			final int columnIndex = i;
			final Map<Color, Integer> columnCounts = Stream.of(this.squares).map(s -> s[columnIndex].getColor())
					.flatMap(s -> Stream.of(s)).collect(Grid.counting(Color.class));
			final int emptySquares = columnCounts.containsKey(null) ? columnCounts.get(null) : 0;
			final long differentColors = columnCounts.entrySet().stream()
					.filter(e -> e.getKey() != null && e.getValue() > 0).map(Map.Entry::getKey).distinct().count();

			if (emptySquares + differentColors < 5) {
				if (Main.DEBUG) {
					System.err.println("not all colors in column " + i);
				}
				isValid = false;
				break outer;
			}
			if (emptySquares == 0) {
				Color last = null;
				int repeats = 0;
				for (final Square[] square : this.squares) {
					final Color color = square[i].getColor();
					if (color == last) {
						repeats++;
					}
					last = color;
				}

				if (repeats < 2) {
					if (Main.DEBUG) {
						System.err.println("More then 5 groups in same column");
					}
					isValid = false;
					break outer;
				}
			}

			for (final Entry<Color, Integer> entry : columnCounts.entrySet()) {
				final Color key = entry.getKey();

				if (key != null && this.changedColors.contains(key)) {
					final int count = entry.getValue();
					final int remaining = remainingSquaresColumn.get(key) - (count == 0 ? 1 : count);

					if (remaining < 0) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has not enough squares left for the missing columns");
						}
						isValid = false;
						break outer;
					} else {
						remainingSquaresColumn.put(key, remaining);
					}
				}
			}
		}
		return isValid;
	}

	private boolean groupsValid() {
		boolean isValid = true;
		// check for each square type there are 1 to 6 combined squares once
		final Set<ColorGroup> groups = new HashSet<>();

		for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				final Square square = this.squares[i][j];

				if (square == null) {
					continue;
				}

				groups.add(this.getSquareGroup(i, j));
			}
		}

		outer: for (final Color color : Color.values()) {
			final List<ColorGroup> colorGroups = groups.stream().filter(g -> g.getColor() == color)
					.collect(Collectors.toList());
			final TreeSet<Integer> possibleSizes = new TreeSet<>(
					Stream.of(1, 2, 3, 4, 5, 6).collect(Collectors.toSet()));
			final List<Integer> unfinishedSizes = new ArrayList<>();

			for (final ColorGroup group : colorGroups) {
				if (group.isFinished()) {
					if (!possibleSizes.remove(Integer.valueOf(group.getSize()))) {
						if (Main.DEBUG) {
							System.err.println("finished group sizes for " + color.name() + " not valid");
						}
						isValid = false;
						break outer;
					}
				} else {
					unfinishedSizes.add(group.getSize());
				}
			}

			// unfinished groups will have to use the same size
			for (final Integer size : unfinishedSizes) {
				final Integer match = possibleSizes.ceiling(size);

				if (match == null) {
					if (Main.DEBUG) {
						System.err.println("unfinished group sizes for " + color.name() + " not valid");
					}
					isValid = false;
					break outer;
				} else {
					possibleSizes.remove(match);
				}
			}
		}
		return isValid;
	}

	private ColorGroup getSquareGroup(final int x, final int y) {
		ColorGroup group = this.squares[x][y].getColorGroup();

		if (group == null) {
			group = new ColorGroup(this.squares[x][y].getColor());

			final Queue<Square> queue = new LinkedList<>();
			queue.add(this.squares[x][y]);

			while (!queue.isEmpty()) {
				final Square square = queue.poll();

				if (square.getColorGroup() == null) {
					group.addSquare(square);

					for (final Square neighbor : square.getNeighbors()) {
						if (neighbor.getColor() == group.getColor()) {
							queue.add(neighbor);
						} else if (neighbor.getColor() == null) {
							group.setFinished(false);
						}
					}
				}
			}
		}

		return group;
	}

	public void print() {
		if (Main.DEBUG) {
			final long seconds = (System.currentTimeMillis() - this.startTime) / 1000l;
			final long validationsPerSecond = seconds == 0 ? 0 : this.validations / seconds;
			System.out.println(this.validations + " " + validationsPerSecond);
		}
		outer: for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				final Color color = this.squares[i][j].getColor();

				if (color == null) {
					System.out.println("");
					continue outer;
				}

				System.out.print(color.name().charAt(0));
			}
			System.out.println("");
		}
		System.out.println("");
	}
}
