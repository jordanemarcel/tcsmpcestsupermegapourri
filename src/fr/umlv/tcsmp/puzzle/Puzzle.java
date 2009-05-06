package fr.umlv.tcsmp.puzzle;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Puzzle {
	public static void main(String[] args) {
		Puzzle p = new Puzzle(2,2,"aaaabbbaacccbddc");
		Puzzle p2 = new Puzzle(2,2,"baaabbbaacccbddc");
		Puzzle p3 = Puzzle.randomPuzzle(4, 4);
		Puzzle p4 = Puzzle.randomPuzzle(4, 4);
		Wheel w1 = new Wheel("popi");
		Wheel w2 = new Wheel("papi");
		System.out.println(w1.equals(w2));
		System.out.println(p3);
		System.out.println("-----------");
		System.out.println(p4);
		System.out.println("-----------");
		System.out.println(p);
		System.out.println("p equals p2="+p.equals(p2));
		System.out.println("p isResolved="+isResolved(p));
		System.out.println("p2 isResolved="+isResolved(p2));
		System.out.println("p3 equals p4="+p3.equals(p4));
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
			for(int i=0;i<4 && w.readSouth() != this.readSouth();i++){
				this.rotate();
			}
			boolean eq=true;
			for(int i=0; i<4;i++){
				w.rotate();
				this.rotate();
				if(w.readSouth() != this.readSouth()){
					eq=false;
				}
			}
			this.setOffset(bak);
			return eq;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<4;i++){
				sb.append(ll.get(i));
			}
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
	public Puzzle(int width,int height,Wheel[][] wheels){
		for(int i = 0;i<width;i++){
			for(int j = 0;j<height;j++){
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
					puzzle[i+1][j].setWest(puzzle[i][j].readSouth());
				}
				if(j-1>=0 && puzzle[i][j].readNorth() != puzzle[i][j-1].readSouth()){
					puzzle[i][j-1].setSouth(puzzle[i][j].readSouth());
				}
				if(i-1>=0 && puzzle[i][j].readWest() != puzzle[i-1][j].readEast()){
					puzzle[i-1][j].setEast(puzzle[i][j].readSouth());
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
		for(int j = 0;j<height;j++){
			StringBuilder sb[] = new StringBuilder[3];
			for(int k = 0;k<3;k++){
				sb[k] = new StringBuilder();
			}
			for(int i = 0;i<width;i++){
				Wheel w = wheels.get(i+j*height);
				sb[0].append("       ");
				sb[0].append(w.readNorth());
				sb[2].append("       ");
				sb[2].append(w.readSouth());

				sb[1].append("   ");
				if(i==0)
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
	public static boolean isResolved(Puzzle p){
		Wheel puzzle[][] = new Wheel[p.getWidth()][p.getHeight()];
		for(int i = 0;i<p.getWidth();i++){
			for(int j = 0;j<p.getHeight();j++){
				puzzle[i][j] = p.getWheels().get(i+j*p.getHeight());
			}
		}
		for(int i = 0;i<p.getWidth();i++){
			for(int j = 0;j<p.getHeight();j++){
				if(j+1<p.getHeight())
					continue;
				if(i+1<p.getWidth())
					continue;
				if(j-1>=0)
					continue;
				if(i-1>=0)
					continue;
				if( puzzle[i][j].readSouth() != puzzle[i][j+1].readNorth()){
					return false;
				}
				if( puzzle[i][j].readEast() != puzzle[i+1][j].readWest()){
					return false;
				}
				if( puzzle[i][j].readNorth() != puzzle[i][j-1].readSouth()){
					return false;
				}
				if(puzzle[i][j].readWest() != puzzle[i-1][j].readEast()){
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
	public static void resolve(){
		//TODO resolution :'(
	}
}
