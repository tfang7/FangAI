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
	protected BuildingUtil builder = new BuildingUtil();
	protected unitEnum enumerator = new unitEnum();
	public FangSM(FangAi f){
		fang = f;
	}
	public boolean Action(Unit u, Game game){
		Role state = determineState(enumerator.eval(u.getType()));
		switch (state){
			case HARVESTER:
				mineClosest(u, game);
				return true;
			case BUILDER:
				System.out.println("BUILDER");
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
	public Role determineState(unitEnum.Type type){
		switch(type){
			case SCV:
				return Role.HARVESTER;
			case MARINE:
				return Role.RANGED;
			default:
				return null;
		}
	}
	public boolean Produce(Unit builder, UnitType uType, Game game){
		switch (enumerator.eval(uType)){
			case SCV:
				createUnit(builder, uType);
				return true;
			case MARINE:
				createUnit(builder, uType);
				return true;
			default:
				return false;
		
		}
	}
	private void createUnit(Unit b, UnitType uType){
		if (b.isIdle()){
			b.train(uType);
		}
	}
	private void mineClosest(Unit u, Game game){
		ArrayList<Unit> cc = fang.getUnitList(unitEnum.Type.CC.toString());
		builder.drawLine(game, u.getPosition(), cc.get(0).getPosition());
		System.out.println(cc.size());
		
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
