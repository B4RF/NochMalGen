package com.barf.nochmalgen;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.barf.nochmalgen.model.Color;
import com.barf.nochmalgen.model.Grid;
import com.barf.nochmalgen.model.Square;

public class Main {

	public static boolean DEBUG = false;

	final static Map<Square, Color[]> shuffles = new HashMap<>();
	final static List<Color> squareIndex = Arrays.asList(null, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED,
			Color.ORANGE);

	public static void main(final String[] args) {

		final Grid grid = new Grid();

		for (final Square square : grid.getSquares()) {
			final List<Color> random = Stream.of(Color.values()).collect(Collectors.toList());
			Collections.shuffle(random);

			final Color[] shuffle = new Color[6];
			int k = 0;
			for (final Color color : random) {
				shuffle[k] = color;
				k = Main.squareIndex.indexOf(color);
			}
			Main.shuffles.put(square, shuffle);
		}

		Main.generateRandom(grid);

		System.out.println("done");
	}

	@SuppressWarnings("resource")
	private static void generateRandom(final Grid grid) {
		long lastPrint = System.currentTimeMillis();

		final List<Square> squares = grid.getSquares();
		Collections.shuffle(squares);

		Color lastColor = null;
		outer: for (int i = 0; i < squares.size(); i++) {
			final Square square = squares.get(i);
			Color currentColor = lastColor;
			lastColor = null;

			do {
				// next shuffled color
				final Color[] shuffle = Main.shuffles.get(square);
				final Integer index = Main.squareIndex.indexOf(currentColor);
				currentColor = shuffle[index];

				if (currentColor != null) {
					grid.setSquare(square, currentColor);

					if (Main.DEBUG) {
						grid.print();
					}
				} else {
					if (i == 0) {
						System.err.println("no more possibilities");
						break outer;
					}

					// go back one square
					grid.setSquare(square, null);

					lastColor = squares.get(i - 1).getColor();
					i -= 2;
					continue outer;
				}
			} while (!grid.isValid());

			final long now = System.currentTimeMillis();
			if (now - lastPrint > 1000l) {
				lastPrint = now;
				grid.print();
			}

			// all squares generated
			if (i == squares.size() - 1) {
				System.out.println("board found!\n");
				grid.print();

				final Scanner sc = new Scanner(System.in);
				System.out.println("Continue? [y/n]");

				if ("y".equalsIgnoreCase(sc.nextLine())) {
					lastColor = currentColor;
					i--;
				}
			}
		}
	}
}
