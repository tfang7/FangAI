import java.util.ArrayList;

import bwapi.*;

public class FangSM {
	public enum Role{
		MELEE,
		RANGED,
		HARVESTER,
		BUILDER,
		PRODUCER
	}
	protected FangAi fang;
	protected BuildingUtil buildUtil = new BuildingUtil();
	protected unitEnum enumerator = new unitEnum();
	public FangSM(FangAi f){
		fang = f;
	}
	public boolean Action(Unit u, Game game, Role state){
		//Role state = determineState(u, enumerator.eval(u.getType()), builders);
		switch (state){
			case HARVESTER:
				mineClosest(u, game);
				return true;
			case MELEE:
				System.out.println("Melee");
				return true;
			case RANGED:
				System.out.println("Ranged");
				return true;
			case PRODUCER:
				return true;
			default: 
				return false;
		}
	}
	public boolean Action(Unit u, Game game, Role state, Unit target){
		//Role state = determineState(u, enumerator.eval(u.getType()), builders);
		switch (state){
			case HARVESTER:
				mineClosest(u, game);
				return true;
			case MELEE:
				System.out.println("Melee");
				return true;
			case RANGED:
				Kite(u, target);
				return true;
			case PRODUCER:
				return true;
			default: 
				return false;
		}
	}
	public Role determineState(Unit u, unitEnum.Type type, ArrayList<Unit> b){
		switch(type){
			case SCV:
				return Role.HARVESTER;
			default:
				return null;
		}

	}
	public void Produce(Unit builder, UnitType uType, TilePosition buildLoc){
		if (uType.isBuilding()){
			createBuilding(builder, uType, buildLoc);
		}
		else {
			createUnit(builder, uType);
		}
		/*switch (uType.isBuilding()){
			case False:
				createUnit(builder, uType);
				return true;
			case MARINE:
				createUnit(builder, uType);
			case :
				TilePosition buildLoc = buildingUtil.getBuildTile(builder, Terran, aroundTile, game)
				return true;
			default:
				return false;*/
		

	}
	//working
	private void Kite(Unit unit, Unit target){
		if (target != null){
			int cd = unit.getGroundWeaponCooldown();
			if (cd == 0){
				PositionOrUnit attackPos = new PositionOrUnit(target);
				unit.issueCommand(UnitCommand.attack(unit, attackPos));;
			}
			else if (cd > 5 && unit.isAttacking()){
				unit.stop();
				Position dir = buildUtil.getDir(target.getPosition(), unit.getPosition());			
				dir = buildUtil.normalize(dir);
				Position away = new Position(unit.getX() + ((dir.getX()) * -100), unit.getY() +  ((dir.getY()) * -100));
				unit.issueCommand(UnitCommand.move(unit, away ));
			}
		}
	}
	//not working
	private void Kite(Unit unit, PositionOrUnit target){
		if (target != null){
			int cd = unit.getGroundWeaponCooldown();
			if (cd == 0){
				PositionOrUnit attackPos = target;
				unit.issueCommand(UnitCommand.attack(unit, attackPos));;
			}
			else if (cd > 5 && unit.isAttacking()){
				unit.stop();
				Position dir = buildUtil.getDir(target.getPosition(), unit.getPosition());			
				dir = buildUtil.normalize(dir);
				Position away = new Position(unit.getX() + ((dir.getX()) * -100), unit.getY() +  ((dir.getY()) * -100));
				unit.issueCommand(UnitCommand.move(unit, away ));
			}
		}
	}
	public void Produce(Unit builder, UnitType uType){
		createUnit(builder, uType);	
	}
	private void createBuilding(Unit b, UnitType uType, TilePosition buildLoc){
		b.build(uType, buildLoc);
		
	}
	private void createUnit(Unit b, UnitType uType){
		if (b.isIdle()){
			b.train(uType);
		}
	}
	private void mineClosest(Unit u, Game game){
		ArrayList<Unit> cc = fang.getUnitList(unitEnum.Type.CC.toString());
		buildUtil.drawLine(game, u.getPosition(), cc.get(0).getPosition());
		//System.out.println(cc.size());
		
	    if (u.isIdle()) {
	        Unit closestMineral = null;

	        //find the closest mineral
	        for (Unit neutralUnit : game.neutral().getUnits()) {
	            if (neutralUnit.getType().isMineralField()) {
	                if (closestMineral == null || u.getDistance(neutralUnit) < u.getDistance(closestMineral)) {
	                    closestMineral = neutralUnit;
	                }
	            }
	        }

	        //if a mineral patch was found, send the worker to gather it
	        if (closestMineral != null) {
	            u.gather(closestMineral, false);
	        }
	    }
	}
}
