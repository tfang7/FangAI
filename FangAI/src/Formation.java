import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bwapi.*;
import bwta.BWTA;
import bwta.Chokepoint;
public class Formation {
	public HashMap<Integer, Position> slots = new HashMap<Integer, Position>();
	public boolean check;
	public ArrayList<Unit> members;
	public State currentState;
	public PositionOrUnit target;
	public Position center;
	public int memberSize;
	public BuildingUtil builder = new BuildingUtil();
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
    public Unit getClosestEnemy(Unit u, List<Unit> enemies)
    {
    	Unit closest = enemies.get(0);
    	int min = closest.getDistance(u);
    	for (Unit e : enemies){
    		if (e.getDistance(u) < min){
    			closest = e;
    			min = closest.getDistance(u);
    		}
    	}
    	return closest;
    }
    public void FSM(Unit myUnit, Formation formation, Formation.State state, FangSM fangState, FangAi fangMind, Game game, HashSet<Position> enemyBuildings){
    	
		Formation form = formation;
		form.currentState = state;
		int numEnemies = game.enemy().getUnits().size();
		Unit attackEnemy = getClosestEnemy(myUnit, game.enemy().getUnits());
		int numMarines = fangMind.getUnits(UnitType.Terran_Marine).size();
		int separation = 1200;
		
		Position move = builder.getCenter(form.members);
		int dist = myUnit.getDistance(move);
		//game.drawTextMap(myUnit.getPosition(), "+:" + dist);
		Position target = findEnemyBuilding(numEnemies, enemyBuildings);
		//Chokepoint chokePointPos = BWTA.getNearestChokepoint(myUnit.getTilePosition());
		game.drawLineMap(move, myUnit.getPosition(), Color.Blue);
		int enemyDist = myUnit.getDistance(attackEnemy);
	//	if (enemyDist > myUnit.getDistance(target) && numMarines < 20){
		//	target = chokePointPos.getPoint();
	//		game.drawCircleMap(chokePointPos.getCenter(),(int)chokePointPos.getWidth(), Color.Cyan);
		//	form.currentState = Formation.State.ATTACKING;
	//	}
		if ( (numMarines > 20 && dist <= separation/2) || (attackEnemy != null && enemyDist < separation/2)){
			form.currentState = Formation.State.ATTACKING;
		}
		else if (form.members.contains(myUnit) && (enemyDist >= separation/2)){
			form.currentState = Formation.State.MOVING;
		}
		else {
			form.currentState = Formation.State.MOVING;
		}
		game.drawTextMap(myUnit.getPosition(), form.currentState.toString());
		switch(form.currentState){
    		case MOVING:
    			myUnit.issueCommand(UnitCommand.attack(myUnit, new PositionOrUnit(move)));
    			 if (myUnit.isSieged()) myUnit.unsiege();
    			 //attackEnemy.getDistance(myUnit) < separation/2 || 
				 if (numMarines > 20){
					 form.currentState = Formation.State.ATTACKING;
				 }
				 else {
					 myUnit.issueCommand(UnitCommand.attack(myUnit, new PositionOrUnit(move)));
				 }
    			break;
			
    		case FLEEING:
    			break;
			case ATTACKING:
				PositionOrUnit attackE = new PositionOrUnit(attackEnemy);
				/*for (Chokepoint c : BWTA.getChokepoints()){
					if (c.getPoint())
				}*/
			//	System.out.println("Enemy distance: " + );

				if (myUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || 
						 myUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					 if (!attackEnemy.isFlying() 
						 && (enemyDist < WeaponType.Arclite_Shock_Cannon.maxRange()) 
						 && enemyDist > WeaponType.Arclite_Shock_Cannon.minRange()){
						 if (myUnit.canUseTech(TechType.Tank_Siege_Mode))
						 myUnit.useTech(TechType.Tank_Siege_Mode);
					 }
					 else{
						  if (myUnit.isSieged())
						  {
								myUnit.unsiege(); 
							 }
						 fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());// form.currentState = Formation.State.MOVING;
					 }
				}
				else if (game.isVisible(attackEnemy.getTilePosition())){
					  if (enemyDist > separation/2) {
						  form.currentState = Formation.State.MOVING;
					 }
					 else {
						 fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());
					 }
				}
				else{
					/*if (myUnit.getType() == UnitType.Terran_Firebat && target != null){
						myUnit.issueCommand(UnitCommand.attack(myUnit, attackE));
					}*/
					if (target != null)
					{
						//System.out.println("Enemy distance: " + enemyDist);
						fangState.Action(myUnit, game,FangSM.Role.RANGED, new PositionOrUnit(target));
					}
					else {
						fangState.Action(myUnit, game,FangSM.Role.RANGED, new PositionOrUnit(move));//form.currentState = Formation.State.MOVING;
					}
				}
				
				//Attacking State
				
				//if (attackEnemy.getDistance(myUnit) < 1000){
					//form.target = attackE;
				//	fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());
					//break; 
			//	} 
			//	else if (numUnits(UnitType.Terran_Marine) + numUnits(UnitType.Terran_Medic) > 32 && target != null ) myUnit.issueCommand(UnitCommand.attack(myUnit, new PositionOrUnit(target)));
				break;
			default:
				break;
			
				
			}
	

}

    public Position findEnemyBuilding(int numEnemies, HashSet<Position> enemyBuildingMemory){
		if (numEnemies > 0) {
			for (Position p : enemyBuildingMemory){
				if (p != null){
					return p;
				}
			}
		}
		return null;
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
