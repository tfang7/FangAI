import java.util.ArrayList;
import java.util.HashMap;

import bwapi.*;
public class Formation {
	public HashMap<Integer, Position> slots = new HashMap<Integer, Position>();
	public boolean check;
	public ArrayList<Unit> members;
	public State currentState;
	public PositionOrUnit target;
	public Position center;
	public int memberSize;
	public Formation(int size) {
		check = false;
		memberSize = size;
		currentState = State.IDLE;
		members = new ArrayList<Unit>();
		// TODO Auto-generated constructor stub
	}
	
	public enum State {
		MOVING,
		FLEEING,
		ATTACKING,
		IDLE,
		PATROLLING
	}
	
	public void FSM(State state, ArrayList<Unit> units) {
		switch(state) {
			case ATTACKING:
				generateFormationSlots(units);
			default:
				break;
			
		}
	}
	public void add(Unit u){
		if (!members.contains(u))members.add(u);
	}
	public void generateFormationSlots(ArrayList<Unit> allUnits){
		
		int index = 0;
		int xOffset = 2;
		int yOffset = 2;
		int r = 4;
	//	System.out.println("Unit Length: " + allUnits.size());
		int xPos, yPos;
		if (!check)
		{
			for (int i = 0; i < allUnits.size()-1; i++){
				xPos = i * xOffset;
				yPos = (i % r) + yOffset;
				
				System.out.println("Index: " + i + " (x, y)" + "( " + xPos + "," + yPos + ")");
				slots.put(i, new Position(xPos, yPos));
				
				//index++;

			}
		}
		check = true;
	}
}
