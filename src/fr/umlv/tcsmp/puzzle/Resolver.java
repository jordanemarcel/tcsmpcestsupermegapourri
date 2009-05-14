package fr.umlv.tcsmp.puzzle;

import fr.umlv.tcsmp.utils.TCSMPParser;

public class Resolver {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("usage: java Resolver dims desc");
			System.exit(1);
		}
		
		Puzzle puzzle = TCSMPParser.parsePuzzleDesc(args[0], args[1]);
		Puzzle.resolve(puzzle);
		System.out.println(puzzle.lineString());
	}
}
