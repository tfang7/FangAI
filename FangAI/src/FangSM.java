import java.util.ArrayList;

import bwapi.*;

public class FangSM {
	public enum Role{
		MELEE,
		RANGED,
		HARVESTER,
		BUILDER,
		PRODUCER,
		GAS
	}
	protected FangAi fang;
	protected BuildingUtil buildUtil = new BuildingUtil();
	protected unitEnum enumerator = new unitEnum();
	public FangSM(FangAi f){
		fang = f;
	}
	public boolean Action(Unit u, Game game, Role state, FangAi fangController){
		//Role state = determineState(u, enumerator.eval(u.getType()), builders);
		switch (state){
			case HARVESTER:
				mineClosest(u, game);
				return true;
			case GAS:
				//System.out.println("Gas" + fangController.getUnitList(enumerator.eval(UnitType.Terran_Refinery).toString()).size());
				//test(game, fangController)
				mineGas(u, game, fangController);
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
	public boolean Action(Unit u, Game game, Role state){
		//Role state = determineState(u, enumerator.eval(u.getType()), builders);
		switch (state){
			case HARVESTER:
				mineClosest(u, game);
				return true;
			case GAS:
			//	System.out.println("Gas");
			//	mineGas(u, game);
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
	public boolean Action(Unit u, Game game, Role state, PositionOrUnit target){
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
				if (!unit.isStimmed() && unit.canUseTech(TechType.Stim_Packs) && unit.getHitPoints() >= 30) unit.useTech(TechType.Stim_Packs);
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
			else if (cd > 0){
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
	//	ArrayList<Unit> cc = fang.getUnitList(unitEnum.Type.CC.toString());
	//	buildUtil.drawLine(game, u.getPosition(), cc.get(0).getPosition());
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
	private void mineGas(Unit u, Game game, FangAi fc){
		//ArrayList<Unit> cc = fc.getUnitList(unitEnum.Type.CC.toString());
		ArrayList<Unit> refineries = fc.getUnitList(unitEnum.Type.REFINERY.toString());
		//System.out.println("drawing gas" + refineries.size());
	//	System.out.println("gathering gas..." + mainBase.gassers.size());
		if (refineries.size() > 0){
		    if (!u.isGatheringGas()) {
				//game.drawLineMap(refineries.get(0).getPosition(), cc.get(0).getPosition(), Color.Cyan);
		//		System.out.println(refineries.get(0));
		  }
		}
		//buildUtil.drawLine(game, u.getPosition(), cc.get(0).getPosition());
		//System.out.println(cc.size());
		

	}

    public void buildStarport(Unit u,Game game, BuildingUtil builder){
    	Player self = game.self();
    	//boolean canBuild = false;
	    if (self.hasUnitTypeRequirement(UnitType.Terran_Factory))
	    {
	    	TilePosition toBuild = buildUtil.getBuildTile(u, UnitType.Terran_Starport, self.getStartLocation(), game);
	    	Produce(u, UnitType.Terran_Starport, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }
    
    public void buildFactory(Unit u, Game game, BuildingUtil builder ){
	    Player self = game.self();
    	if (   
	        self.hasUnitTypeRequirement(UnitType.Terran_Barracks) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Academy))
	    {
	    	//mainBase.barracks.toTilePosition()
	    	TilePosition toBuild = buildUtil.getBuildTile(u, UnitType.Terran_Factory, self.getStartLocation(), game);
	        Produce(u, UnitType.Terran_Factory, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }
    public void Starport(Unit starport, Game game, BuildingUtil builder, Base base){
    	Player self = game.self();
    	if (!self.hasUnitTypeRequirement(UnitType.Terran_Control_Tower) && starport.canBuildAddon()){
    		TilePosition toBuild = buildUtil.getBuildTile(starport, starport.getType(), base.barracks.toTilePosition(), game);
    		starport.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (!starport.isTraining()){
    		starport.train(UnitType.Terran_Science_Vessel);
    	}
    }
    public void Factory(Unit factory, Game game, BuildingUtil builder){
    	Player self = game.self();
    	if (!self.hasUnitTypeRequirement(UnitType.Terran_Machine_Shop) && factory.canBuildAddon()){
    		TilePosition toBuild = buildUtil.getBuildTile(factory, factory.getType(), factory.getTilePosition(), game);
    		factory.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (factory.isLifted()){
    		TilePosition toBuild = buildUtil.getBuildTile(factory, factory.getType(), factory.getTilePosition(), game);
    		factory.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (!factory.isTraining() && factory.isIdle()){
    		if (self.hasUnitTypeRequirement(UnitType.Terran_Machine_Shop)) factory.train(UnitType.Terran_Siege_Tank_Tank_Mode);
    		else {
    			factory.train(UnitType.Terran_Vulture);
    		}
    	}
    	else if (factory.canBuildAddon())
    		factory.buildAddon(UnitType.Terran_Machine_Shop);
    }
    public void Academy(Unit academy, Player self){
    	//System.out.println("Doing academy things...");
    	if (!academy.isResearching() && !academy.isBeingConstructed()){
    		if (!self.hasResearched(TechType.Stim_Packs)){
    			academy.research(TechType.Stim_Packs);
    		}
    		else {
    			academy.upgrade(UpgradeType.U_238_Shells);
    		/*	List<TechType> techList = academy.getType().researchesWhat();
    			UnitType.Terran_Marine.upgrades();
    			for (TechType t : techList){
    				if (!self.hasResearched(t)){
    					academy.research(t);
    				}
    			}*/
    		}
    	}
    }
}
