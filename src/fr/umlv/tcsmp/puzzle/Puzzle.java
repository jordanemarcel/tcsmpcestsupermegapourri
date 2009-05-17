package fr.umlv.tcsmp.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class is an implementation of a eternity puzzle. With this class you can
 * generate puzzle for a string, or generate random puzzle with specified number of wheel
 * (in width and in height).
 * This class provide also a solver which can resolved eternity puzzle.
 * 
 * @author Clément Lebreton & Jordane Marcel & Rémy Masson & Clément Lecigne
 */
public class Puzzle {
	/**
	 * This inner class provide a implementation of a Wheel of an eternity puzzle
	 * This wheel provide some basic operation like rotate, un-rotate. 
	 * @author Clément Lebreton & Jordane Marcel & Rémy Masson & Clément Lecigne
	 */
	static class Wheel{
		private ArrayList<Character> ll = new ArrayList<Character>();
		private int offset=0;
		/**
		 * Constructs a new Wheel with the given String. The first letter is at the
		 * north, the second at the east, the third at the south, and the fourth at
		 * the west. 
		 * 
		 * @param values the given string. it have to contains 4 char only.
		 */
		public Wheel(String values) {
			if(values==null || values.length()!=4)
				throw new IllegalArgumentException("Should contains 4 char");
			for(Character c : values.toCharArray()){
				ll.add(c);
			}
		}
		/**
		 * Rotates the wheel in the anticlockwise. e.g. : north become west
		 */
		public void unrotate(){
			offset++;
			if(offset>3){
				offset=0;
			}
		}
		/**
		 * Rotates the wheel in the clockwise. e.g. : north become east
		 */
		public void rotate(){
			offset--;
			if(offset<0){
				offset=3;
			}
		}
		/**
		 * Returns the number of quarter of turn done.
		 * @return the number of quarter of turn done.Always between 0 and 3 include
		 */
		public int getOffset() {
			return offset;
		}
		/**
		 * Set the number of rotate to do.
		 * @param offset the number of rotate. Have to be between 0 and 3 include
		 */
		public void setOffset(int offset) {
			if(offset>3){
				throw new IllegalArgumentException("Offset should not be greater than 3");
			}
			if(offset<0)
				throw new IllegalArgumentException("Offset have to be positive");
			this.offset = offset;
		}
		/**
		 * Returns the value read on the north
		 * @return the value read on the north
		 */
		public char readNorth(){
			return ll.get(offset);
		}
		/**
		 * Returns the value read on the east
		 * @return the value read on the east
		 */
		public char readEast(){
			return ll.get((offset+1)%4);
		}
		/**
		 * Returns the value read on the south
		 * @return the value read on the south
		 */
		public char readSouth(){
			return ll.get((offset+2)%4);
		}
		/**
		 * Returns the value read on the west
		 * @return the value read on the west
		 */
		public char readWest(){
			return ll.get((offset+3)%4);
		}
		private void setNorth(Character c){
			if(c==null)
				throw new IllegalArgumentException("c should not be null");
			ll.set(offset,c);
		}
		private void setEast(Character c){
			ll.set((offset+1)%4,c);
		}
		private void setSouth(Character c){
			ll.set((offset+2)%4,c);
		}
		private void setWest(Character c){
			ll.set((offset+3)%4,c);
		}
		/**
		 * Returns if the wheel is equals to the given object. Two wheels are equals
		 * if they have the same sequence of char read in the clockwise. e.g. : abcd and dabc is equals
		 * @return true if obj and the wheels are equals, false otherwise
		 */
		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof Wheel) )
				return false;
			Wheel w = (Wheel) obj;
			int bak = this.getOffset();
			int obak = w.getOffset();

			for(int i=0;i<3;i++) {
				for(int j=0;j<3;j++) {
					if(this.readSouth()==w.readSouth()) {
						int tmpbak = this.getOffset();
						int otmpbak = w.getOffset();
						for(int k=0;k<3;k++) {
							this.rotate();
							w.rotate();
							if(this.readSouth()!=w.readSouth()) {
								break;
							}
							if(k==2) {
								this.setOffset(bak);
								w.setOffset(obak);
								return true;
							}
						}
						this.setOffset(tmpbak);
						w.setOffset(otmpbak);
					}
					w.rotate();
				}
				this.rotate();
			}


			this.setOffset(bak);
			w.setOffset(obak);
			return false;
		}
		/**
		 * Returns a string that represent a wheel.
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(readNorth());
			sb.append(readEast());
			sb.append(readSouth());
			sb.append(readWest());
			return sb.toString();
		}
	}
	private final ArrayList<Wheel> wheels = new ArrayList<Wheel>();
	private final static Random rand = new Random();
	private int width;
	private int height;
	/**
	 * Constructs a new Puzzle with the given width and height. Its wheels are build with values.
	 * @param width the number of wheel in width
	 * @param height the number of wheel in height
	 * @param values the char use to build all wheels. values have to contains width*height*4 char.
	 */
	public Puzzle(int width,int height,String values) {
		if(width<=0 || height<=0)
			throw new IllegalArgumentException("width and height have to be positive");
		this.width = width;
		this.height = height;
		if(values==null || values.length() != width*height*4)
			throw new IllegalArgumentException("Incorrect char number");
		for(int i = 0;i<width*height;i++){		
			CharSequence wheelVal = values.subSequence(i*4, i*4+4);
			wheels.add(new Wheel(wheelVal.toString()));
		}
	}
	/**
	 * Constructs a new Puzzle with the given width and height. Its wheels are in the given.
	 * @param width the number of wheel in width
	 * @param height the number of wheel in height
	 * @param wheelList the list of wheel use to build the puzzle. Have to contains
	 * width*height wheels
	 */
	public Puzzle(int width,int height,List<Wheel> wheelList){
		if(width<=0 || height<=0)
			throw new IllegalArgumentException("width and height have to be positive");
		if(wheels==null || wheelList.size()!=width*height)
			throw new IllegalArgumentException("wheels have to contains heigth*width wheels");
		wheels.addAll(wheelList);
		this.width = width;
		this.height = height;
	}
	/**
	 * Constructs a new Puzzle with the given width and height. Its wheels are in the given.
	 * @param width the number of wheel in width
	 * @param height the number of wheel in height
	 * @param wheelList the list of wheel use to build the puzzle. Have to contains
	 * width*height wheels
	 */
	public Puzzle(int width,int height,Wheel[][] wheels){
		if(width<=0 || height<=0)
			throw new IllegalArgumentException("width and height have to be positive");
		if(wheels==null || wheels.length!=width || wheels[0].length!=height)
			throw new IllegalArgumentException("wheels have to contains heigth*width wheels");
		for(int j = 0;j<height;j++){
			for(int i = 0;i<width;i++){
				this.wheels.add(wheels[i][j]);
			}
		}
		this.width = width;
		this.height = height;
	}
	/**
	 * Returns the number of wheel in height
	 * @return the number of wheel in height
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * Returns the number of wheel in width
	 * @return the number of wheel in width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * Returns the list of wheel of the puzzle. WARNING this list can be modify but you should
	 * not do that.
	 * @return
	 */
	public List<Wheel> getWheels() {
		return wheels;
	}
	/**
	 * Changes the wheels of this puzzle
	 * @param wheels
	 */
	private void setWheels(List<Wheel> wheels) {
		if(this.wheels.size()!=wheels.size())
			return;
		this.wheels.clear();
		this.wheels.addAll(wheels);
	}
	/**
	 * Shuffle a puzzle. Each wheels is rotate in random position, and 
	 * all wheels in the puzzle are shuffle too.
	 * @param puzzle the puzzle to shuffle.
	 */
	public static void shuffle(Puzzle puzzle){
		if(puzzle==null)
			throw new IllegalArgumentException("puzzle should not be null");
		List<Wheel> wheels = puzzle.getWheels();
		Random rand = new Random();
		for (Wheel w : wheels){
			int r = rand.nextInt(3);
			for(int i = 0;i<r;i++)
				w.rotate();
		}
		Collections.shuffle(wheels);
	}
	/**
	 * Generates a random puzzle of the given width and height. WARNING this puzzle is return SOLVED
	 * you have to shuffle it if you want a challenge 
	 * @param width the number of wheel in width that is queried
	 * @param height the number of wheel in height that is queried
	 * @return the generated puzzle. This puzzle is already solved! 
	 */
	public static Puzzle randomPuzzle(int width,int height){
		if(width<=0 || height<=0)
			throw new IllegalArgumentException("width and height have to be positive");
		Wheel puzzle[][] = new Wheel[width][height];
		//create random wheels
		for(int i = 0;i<width;i++){
			for(int j = 0;j<height;j++){
				puzzle[i][j]=new Wheel("aaaa");
				for(int k=0;k<4;k++){
					int val = rand.nextInt(36);
					puzzle[i][j].setNorth(convertIntToChar(val));
					puzzle[i][j].rotate();
				}

			}
		}
		//change wheel for make a resolvable puzzle
		for(int i = 0;i<width;i++){
			for(int j = 0;j<height;j++){
				if(j+1<height && puzzle[i][j].readSouth() != puzzle[i][j+1].readNorth()){					
					puzzle[i][j+1].setNorth(puzzle[i][j].readSouth());
				}
				if(i+1<width && puzzle[i][j].readEast() != puzzle[i+1][j].readWest()){
					puzzle[i+1][j].setWest(puzzle[i][j].readEast());
				}
				if(j-1>=0 && puzzle[i][j].readNorth() != puzzle[i][j-1].readSouth()){
					puzzle[i][j-1].setSouth(puzzle[i][j].readNorth());
				}
				if(i-1>=0 && puzzle[i][j].readWest() != puzzle[i-1][j].readEast()){
					puzzle[i-1][j].setEast(puzzle[i][j].readWest());
				}
			}
		}
		return new Puzzle(width,height,puzzle);
	}
	private static char convertIntToChar(int val){
		if(val>9)
			return (char)('a'+val-10);
		return (char)('0'+val);
	}
	@Override
	public String toString() {

		StringBuilder globalsb = new StringBuilder();
		for(int i = 0;i<height;i++){
			StringBuilder sb[] = new StringBuilder[3];
			for(int k = 0;k<3;k++){
				sb[k] = new StringBuilder();
			}
			for(int j = 0;j<width;j++){
				Wheel w = wheels.get(j+i*width);
				sb[0].append("       ");
				sb[0].append(w.readNorth());
				sb[2].append("       ");
				sb[2].append(w.readSouth());

				sb[1].append("   ");
				if(j==0)
					sb[1].append("  ");
				sb[1].append(w.readWest());
				sb[1].append("   ");
				sb[1].append(w.readEast());

			}
			for(int k = 0;k<3;k++){
				sb[k].append("\n");
				globalsb.append(sb[k].toString());
			}
			globalsb.append("\n");

		}
		return globalsb.toString();
	}
	/**
	 * Returns a string which contains char of all wheels. The puzzle is read from left to right and from top to bottom
	 * and each wheel is read in clockwise.
	 * @return
	 */
	public String lineString() {
		StringBuilder globalsb = new StringBuilder();
		for(int j = 0;j<height;j++){
			for(int i = 0;i<width;i++){
				Wheel w = wheels.get(i+j*width);
				globalsb.append(w.readNorth());
				globalsb.append(w.readEast());
				globalsb.append(w.readSouth());
				globalsb.append(w.readWest());
			}
		}
		return globalsb.toString();
	}
	/**
	 * Checks if a puzzle is solved. 
	 * @param p the puzzle to check
	 * @return true if p is solved, false otherwise
	 */
	public static boolean isSolved(Puzzle p){
		if(p==null)
			return false;
		Wheel puzzle[][] = new Wheel[p.getWidth()][p.getHeight()];
		for(int j = 0;j<p.getHeight();j++){
			for(int i = 0;i<p.getWidth();i++){
				puzzle[i][j] = p.getWheels().get(i+j*p.getWidth());
			}
		}

		for(int j = 0;j<p.getHeight();j++){
			for(int i = 0;i<p.getWidth();i++){
				if(j!=p.getHeight()-1 && puzzle[i][j].readSouth() != puzzle[i][j+1].readNorth()){
					return false;
				}
				if( i!=p.getWidth()-1 && puzzle[i][j].readEast() != puzzle[i+1][j].readWest()){
					return false;
				}
				if(j!=0 && puzzle[i][j].readNorth() != puzzle[i][j-1].readSouth()){
					return false;
				}
				if(i!=0 && puzzle[i][j].readWest() != puzzle[i-1][j].readEast()){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Returns if the puzzle is equals to the given object. Two puzzle are equals if
	 * they have the same width height and the contains the same wheels.
	 * @param obj the given object
	 * @return true if equals, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Puzzle) )
			return false;
		Puzzle p = (Puzzle) obj;
		if(p.getHeight()!=this.getHeight() || p.getWidth()!=this.getWidth())
			return false;
		LinkedList<Wheel> res = new LinkedList<Wheel>();
		List<Wheel> pWheel = p.getWheels();
		for(Wheel myW : wheels){
			for(Wheel pW : pWheel){
				if(myW.equals(pW)){
					res.add(pW);
					break;
				}
			}
		}
		if(res.size()==wheels.size())
			return true;
		return false;
	}
	/**
	 * Resolve the given puzzle. The puzzle HAVE to be resolvable.
	 * If the puzzle is not resolvable an IllegalArgumentExecption will be throw
	 * @param puzzle the puzzle to resolved
	 */
	public static void resolve(Puzzle puzzle){
		if(isSolved(puzzle))
			return;
		int width = puzzle.getWidth();
		HashSet<Integer> usedWheelSet = new HashSet<Integer>();
		ArrayList<Wheel> puzzleWheel = new ArrayList<Wheel>(puzzle.getWheels());
		ArrayList<Wheel> resolv = new ArrayList<Wheel>();
		int index = 0;
		int nbWheel = puzzle.getWheels().size();
		int wheelIndexArray[] = new int[nbWheel];
		int rotateWheelIndex[] = new int[nbWheel];
		Arrays.fill(wheelIndexArray, 0);
		Arrays.fill(rotateWheelIndex, 0);
		while(usedWheelSet.size()!=nbWheel){
			if(usedWheelSet.contains(wheelIndexArray[index])){
				wheelIndexArray[index]++;
				continue;
			}
			if(wheelIndexArray[index]>=nbWheel){
				wheelIndexArray[index]=0;
				index--;
				if(index<=0){
					index=0;
					usedWheelSet.remove(wheelIndexArray[index]);
					puzzleWheel.get(wheelIndexArray[index]).rotate();
					rotateWheelIndex[index]++;
					if(rotateWheelIndex[index]>3){
						rotateWheelIndex[index]=0;
						wheelIndexArray[index]++;
						if(wheelIndexArray[index]>=nbWheel)
							throw new IllegalArgumentException("Unresolvable puzzle");
					}
				}else{
					usedWheelSet.remove(wheelIndexArray[index]);
					wheelIndexArray[index]++;
				}
				continue;
			}
			usedWheelSet.add(wheelIndexArray[index]);
			resolv.clear();
			for(int i=0;i<=index;i++){
				Wheel w = puzzleWheel.get(wheelIndexArray[i]);
				resolv.add(w);
			}
			for(int i=0;i<4;i++){
				if(checkWheelAtPos(resolv, width, index)){
					index++;
					break;
				}else if(i==3){//Bad wheel
					usedWheelSet.remove(wheelIndexArray[index]);
					wheelIndexArray[index]++;
				}
				//Bad wheel
				resolv.get(index).rotate();
			}
		}
		puzzle.setWheels(resolv);
	}
	private static boolean checkWheelAtPos(List<Wheel> wheelList,int width,int pos){
		Wheel chechedWheel = wheelList.get(pos);
		if(pos-width>=0 && chechedWheel.readNorth() != wheelList.get(pos-width).readSouth()){
			return false;
		}
		if(pos%width!=0 && pos-1>=0  && chechedWheel.readWest() != wheelList.get(pos-1).readEast()){
			return false;
		}
		return true;
	}
}