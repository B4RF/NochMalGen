package com.barf.nochmalgen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
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

	public Square getPos(final int x, final int y) {
		return this.squares[x][y];
	}

	public void setPos(final Square square, final int x, final int y) {
		this.squares[x][y] = square;
	}

	public static <T extends Enum<T>> Collector<T, ?, Map<T, Integer>> counting(final Class<T> type) {
		return Collectors.toMap(Function.<T>identity(), x -> 1, Integer::sum, () -> new HashMap<>(
				Stream.of(type.getEnumConstants()).collect(Collectors.toMap(Function.<T>identity(), t -> 0))));
	}

	public boolean validate() {
		final Map<Square, Integer> remainingSquaresRow = new HashMap<>();
		final Map<Square, Boolean> usedMissingRow = new HashMap<>();

		for (final Square square : Square.values()) {
			remainingSquaresRow.put(square, Grid.COLOR_COUNT);
			usedMissingRow.put(square, false);
		}

		final Map<Square, Integer> remainingSquaresColumn = new HashMap<>(remainingSquaresRow);

		for (int i = 0; i < Grid.ROW_COUNT; i++) {
			final Map<Square, Integer> rowCounts = Stream.of(this.squares[i]).flatMap(s -> Stream.of(s))
					.collect(Grid.counting(Square.class));
			final int emptySquares = rowCounts.containsKey(null) ? rowCounts.get(null) : 0;
			final long differentColors = rowCounts.entrySet().stream()
					.filter(e -> e.getKey() != null && e.getValue() > 0).map(Map.Entry::getKey).distinct().count();

			if (emptySquares + differentColors < 4) {
				if (Main.DEBUG) {
					System.err.println("two colors missing in same row");
				}
				return false;
			}

			for (final Entry<Square, Integer> entry : rowCounts.entrySet()) {
				final Square key = entry.getKey();

				if (key != null) {
					final int count = entry.getValue();

					if (count == 0) {
						if (emptySquares == 0) {
							if (usedMissingRow.get(key)) {
								if (Main.DEBUG) {
									System.err.println(key.name() + " is missing in two rows");
								}
								return false;
							} else {
								usedMissingRow.put(key, true);
							}
						}
					} else if (count > 7) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has more then 7 entries in same row");
						}
						return false;
					}

					final int remaining = remainingSquaresRow.get(key) - (count == 0 ? 1 : count);

					if (remaining < 0) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has not enough squares left for the missing rows");
						}
						return false;
					} else {
						remainingSquaresRow.put(key, remaining);
					}
				}
			}
		}

		for (int i = 0; i < Grid.COLUMN_COUNT; i++) {
			final int columnIndex = i;
			final Map<Square, Integer> columnCounts = Stream.of(this.squares).map(s -> s[columnIndex])
					.flatMap(s -> Stream.of(s)).collect(Grid.counting(Square.class));
			final int emptySquares = columnCounts.containsKey(null) ? columnCounts.get(null) : 0;
			final long differentColors = columnCounts.entrySet().stream()
					.filter(e -> e.getKey() != null && e.getValue() > 0).map(Map.Entry::getKey).distinct().count();

			if (emptySquares + differentColors < 5) {
				if (Main.DEBUG) {
					System.err.println("not all colors in column " + i);
				}
				return false;
			}

			for (final Entry<Square, Integer> entry : columnCounts.entrySet()) {
				final Square key = entry.getKey();

				if (key != null) {
					final int count = entry.getValue();
					final int remaining = remainingSquaresColumn.get(key) - (count == 0 ? 1 : count);

					if (remaining < 0) {
						if (Main.DEBUG) {
							System.err.println(key.name() + " has not enough squares left for the missing columns");
						}
						return false;
					} else {
						remainingSquaresColumn.put(key, remaining);
					}
				}
			}
		}

		// check for each square type there are 1 to 6 combined squares once
		final boolean[][] validated = new boolean[Grid.ROW_COUNT][Grid.COLUMN_COUNT];
		final List<SquareGroup> groups = new ArrayList<>();

		for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				final Square square = this.squares[i][j];

				if (square == null) {
					continue;
				}

				if (!validated[i][j]) {
					groups.add(this.getSquareGroup(i, j, validated));
				}
			}
		}

		for (final Square color : Square.values()) {
			final List<SquareGroup> colorGroups = groups.stream().filter(g -> g.square == color)
					.collect(Collectors.toList());
			final TreeSet<Integer> possibleSizes = new TreeSet<>(
					Stream.of(1, 2, 3, 4, 5, 6).collect(Collectors.toSet()));
			final List<Integer> unfinishedSizes = new ArrayList<>();

			for (final SquareGroup group : colorGroups) {
				if (group.finished) {
					if (!possibleSizes.remove(Integer.valueOf(group.size))) {
						if (Main.DEBUG) {
							System.err.println("finished group sizes for " + color.name() + " not valid");
						}
						return false;
					}
				} else {
					unfinishedSizes.add(group.size);
				}
			}

			// unfinished groups will have to use the same size
			for (final Integer size : unfinishedSizes) {
				final Integer match = possibleSizes.ceiling(size);

				if (match == null) {
					if (Main.DEBUG) {
						System.err.println("unfinished group sizes for " + color.name() + " not valid");
					}
					return false;
				} else {
					possibleSizes.remove(match);
				}
			}
		}

		return true;
	}

	private SquareGroup getSquareGroup(final int x, final int y, final boolean[][] validated) {
		final Square groupColor = this.squares[x][y];
		int colorCount = 0;
		boolean finished = true;

		final Queue<Position> queue = new LinkedList<>();
		queue.add(new Position(x, y));

		while (!queue.isEmpty()) {
			final Position pos = queue.poll();

			if (!validated[pos.x][pos.y]) {
				final Square posColor = this.squares[pos.x][pos.y];

				if (posColor == groupColor) {
					validated[pos.x][pos.y] = true;
					colorCount++;
					queue.addAll(pos.getNeighbors());
				} else if (posColor == null) {
					finished = false;
				}
			}
		}

		return new SquareGroup(groupColor, colorCount, finished);
	}

	public void print() {
		outer: for (int i = 0; i < Grid.ROW_COUNT; i++) {
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				final Square square = this.squares[i][j];

				if (square == null) {
					System.out.println("");
					continue outer;
				}

				System.out.print(square.name().charAt(0));
			}
			System.out.println("");
		}
		System.out.println("");
	}

	private class Position {
		int x;
		int y;

		public Position(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public List<Position> getNeighbors() {
			final List<Position> neighbors = new ArrayList<>();

			if (this.x > 0) {
				neighbors.add(new Position(this.x - 1, this.y));
			}
			if (this.y > 0) {
				neighbors.add(new Position(this.x, this.y - 1));
			}
			if (this.x < Grid.ROW_COUNT - 1) {
				neighbors.add(new Position(this.x + 1, this.y));
			}
			if (this.y < Grid.COLUMN_COUNT - 1) {
				neighbors.add(new Position(this.x, this.y + 1));
			}

			return neighbors;
		}
	}

	private class SquareGroup {
		Square square;
		int size;
		boolean finished;

		public SquareGroup(final Square square, final int size, final boolean finished) {
			this.square = square;
			this.size = size;
			this.finished = finished;
		}
	}
}
