package fr.umlv.tcsmp.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Puzzle {
	public static void main(String[] args) {
		Puzzle p = Puzzle.randomPuzzle(6, 6);
		Puzzle p2 = new Puzzle(6, 6, p.lineString());
		shuffle(p);
		System.out.println(p);
		System.out.println(p.equals(p2));
		resolve(p);
		System.out.println(p);
		System.out.println(p.equals(p2));
		
		
		// My test
		Puzzle puz = new Puzzle(4, 4, "ntct41thdjhmiom6t6v7157ojqolocl866vr5triq0iuceut67chtphk0fk2e92f");
		System.out.println("ntct41thdjhmiom6t6v7157ojqolocl866vr5triq0iuceut67chtphk0fk2e92f");
		System.out.println("vs");
		System.out.println(puz.lineString());
	}
	static class Wheel{
		private LinkedList<Character> ll = new LinkedList<Character>();
		private int offset=0;
		public Wheel(String values) {
			if(values.length()!=4)
				throw new IllegalArgumentException("Should contains 4 char");
			for(Character c : values.toCharArray()){
				ll.add(c);
			}
		}
		public void rotate(){
			offset++;
			if(offset>3){
				offset=0;
			}
		}
		public void unrotate(){
			offset--;
			if(offset<0){
				offset=3;
			}
		}
		public int getOffset() {
			return offset;
		}
		public void setOffset(int offset) {
			if(offset>3){
				throw new IllegalArgumentException("Offset should not be greater than 3");
			}
			this.offset = offset;
		}
		public char readNorth(){
			return ll.get(offset);
		}
		public char readEast(){
			return ll.get((offset+1)%4);
		}
		public char readSouth(){
			return ll.get((offset+2)%4);
		}
		public char readWest(){
			return ll.get((offset+3)%4);
		}
		private void setNorth(Character c){
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
			
//			for(int i=0;i<4 && w.readSouth() != this.readSouth();i++){
//				this.rotate();
//			}
//			boolean eq=true;
//			for(int i=0; i<4;i++){
//				w.rotate();
//				this.rotate();
//				if(w.readSouth() != this.readSouth()){
//					eq=false;
//					break;
//				}
//			}
			
			this.setOffset(bak);
			w.setOffset(obak);
			return false;
		}
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
	private LinkedList<Wheel> wheels = new LinkedList<Wheel>();
	private int width;
	private int height;
	public Puzzle(int width,int height,String values) {
		this.width = width;
		this.height = height;
		if(values.length() != width*height*4)
			throw new IllegalArgumentException("Incorrect char number");
		for(int i = 0;i<width*height;i++){		
			CharSequence wheelVal = values.subSequence(i*4, i*4+4);
			wheels.add(new Wheel(wheelVal.toString()));
		}
	}
	public Puzzle(int width,int height,List<Wheel> wheelList){
		wheels.addAll(wheelList);
		this.width = width;
		this.height = height;
	}
	public Puzzle(int width,int height,Wheel[][] wheels){
		for(int j = 0;j<height;j++){
			for(int i = 0;i<width;i++){
				this.wheels.add(wheels[i][j]);
			}
		}
		this.width = width;
		this.height = height;
	}

	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public List<Wheel> getWheels() {
		return wheels;
	}
	public void setWheels(LinkedList<Wheel> wheels) {
		if(this.wheels.size()!=wheels.size())
			return;
		this.wheels = wheels;
	}
	public static void shuffle(Puzzle puzzle){
		List<Wheel> wheels = puzzle.getWheels();
		Random rand = new Random();
		for (Wheel w : wheels){
			int r = rand.nextInt(3);
			for(int i = 0;i<r;i++)
				w.rotate();
		}
		Collections.shuffle(wheels);
	}
	public static Puzzle randomPuzzle(int width,int height){
		Wheel puzzle[][] = new Wheel[width][height];
		Random rand = new Random();
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
				Wheel w = wheels.get(j+i*height);
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
	
	public String lineString() {
		StringBuilder globalsb = new StringBuilder();
		for(int j = 0;j<height;j++){
			for(int i = 0;i<width;i++){
				Wheel w = wheels.get(i+j*height);
				globalsb.append(w.readNorth());
				globalsb.append(w.readEast());
				globalsb.append(w.readSouth());
				globalsb.append(w.readWest());
			}
		}
		return globalsb.toString();
	}
	
	public static boolean isResolved(Puzzle p){
		Wheel puzzle[][] = new Wheel[p.getWidth()][p.getHeight()];
		for(int j = 0;j<p.getHeight();j++){
			for(int i = 0;i<p.getWidth();i++){
				puzzle[i][j] = p.getWheels().get(i+j*p.getHeight());
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
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Puzzle) )
			return false;
		Puzzle p = (Puzzle) obj;
		LinkedList<Wheel> res = new LinkedList<Wheel>();
		List<Wheel> pWheel = p.getWheels();
		for(Wheel pW : pWheel){
			for(Wheel myW : wheels){
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
	public static void resolve(Puzzle puzzle){
		if(isResolved(puzzle))
			return;
		int width = puzzle.getWidth();
		HashSet<Integer> usedWheelSet = new HashSet<Integer>();
		ArrayList<Wheel> puzzleWheel = new ArrayList<Wheel>(puzzle.getWheels());
		LinkedList<Wheel> resolv = new LinkedList<Wheel>();
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
				resolv.getLast().rotate();
			}
		}
		puzzle.setWheels(resolv);
	}
	private static boolean checkWheelAtPos(LinkedList<Wheel> wheelList,int width,int pos){
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