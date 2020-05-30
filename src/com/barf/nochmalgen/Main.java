package com.barf.nochmalgen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.barf.nochmalgen.model.Color;
import com.barf.nochmalgen.model.Grid;

public class Main {

	public static boolean DEBUG = false;

	public static void main(final String[] args) {

		boolean columnsFirst = false;
		if (args.length > 0) {
			columnsFirst = Boolean.parseBoolean(args[0]);
		}

		final Grid grid = new Grid();

		if (columnsFirst) {
			Main.generateColumns(grid);
		} else {
			Main.generateRows(grid);
		}

		System.out.println("done");
	}

	private static void generateRows(final Grid grid) {
		outer: for (int i = 0; i < Grid.ROW_COUNT; i++) {
			Color lastSquare = null;
			for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
				Color currentSquare = lastSquare;
				lastSquare = null;

				do {
					currentSquare = Main.nextSquare(currentSquare, i, j);

					if (currentSquare != null) {
						grid.setPos(currentSquare, i, j);

						if (Main.DEBUG) {
							grid.print();
						}
					} else {
						grid.setPos(null, i, j);

						if (j > 0) {
							j--;
							lastSquare = grid.getPos(i, j);
							j--;
						} else {
							if (i == 0) {
								System.err.println("no more possibilities");
								break outer;
							}
							i--;
							j = Grid.COLUMN_COUNT - 1;
							lastSquare = grid.getPos(i, j);
							j--;
						}
						break;
					}
				} while (!grid.isValid());

				if (i == Grid.ROW_COUNT - 1 && j == Grid.COLUMN_COUNT - 1) {
					grid.print();

					try (Scanner sc = new Scanner(System.in)) {
						System.out.println("Continue? [y/n]");

						if ("y".equalsIgnoreCase(sc.nextLine())) {
							lastSquare = grid.getPos(i, j);
							j--;
						}
					}
				}
			}
			grid.print();
		}
	}

	private static void generateColumns(final Grid grid) {
		outer: for (int j = 0; j < Grid.COLUMN_COUNT; j++) {
			Color lastSquare = null;
			for (int i = 0; i < Grid.ROW_COUNT; i++) {
				Color currentSquare = lastSquare;
				lastSquare = null;

				do {
					currentSquare = Main.nextSquare(currentSquare, i, j);

					if (currentSquare != null) {
						grid.setPos(currentSquare, i, j);

						if (Main.DEBUG) {
							grid.print();
						}
					} else {
						grid.setPos(null, i, j);

						if (i > 0) {
							i--;
							lastSquare = grid.getPos(i, j);
							i--;
						} else {
							if (j == 0) {
								System.err.println("no more possibilities");
								break outer;
							}
							j--;
							i = Grid.ROW_COUNT - 1;
							lastSquare = grid.getPos(i, j);
							i--;
						}
						break;
					}
				} while (!grid.isValid());

				if (i == Grid.ROW_COUNT - 1 && j == Grid.COLUMN_COUNT - 1) {
					grid.print();

					try (Scanner sc = new Scanner(System.in)) {
						System.out.println("Continue? [y/n]");

						if ("y".equalsIgnoreCase(sc.nextLine())) {
							lastSquare = grid.getPos(i, j);
							i--;
						}
					}
				}
			}
			grid.print();
		}
	}

	final static Color[][][] shuffles = new Color[Grid.ROW_COUNT][Grid.COLUMN_COUNT][];
	final static List<Color> squareIndex = Arrays.asList(null, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED,
			Color.ORANGE);

	public static Color nextSquare(final Color square, final int i, final int j) {
		Color[] shuffle = Main.shuffles[i][j];

		if (shuffle == null) {
			final List<Color> random = Stream.of(Color.values()).collect(Collectors.toList());
			Collections.shuffle(random);

			shuffle = new Color[6];
			int k = 0;
			for (final Color color : random) {
				shuffle[k] = color;
				k = Main.squareIndex.indexOf(color);
			}
			Main.shuffles[i][j] = shuffle;
		}

		final Integer index = Main.squareIndex.indexOf(square);
		return shuffle[index];
	}
}
