	import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;

public class FangBot extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    
    private Game game;
    private Base mainBase;
    private Player self;
    private unitEnum uType = new unitEnum();
    private FangAi fang = new FangAi();
    private FangSM fangState = new FangSM(fang);
    private BuildingUtil builder = new BuildingUtil();
    private ArrayList<Base> allBases = new ArrayList<Base>();
    private ArrayList<UnitType> buildOrder = new ArrayList<UnitType>();
    private BaseLocation target = null;
    private Position enemyLastSeen;
    private ArrayList<BaseLocation> checkedBases = new ArrayList<BaseLocation>();
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private ArrayList<Formation> formations;
    private int formationIndex = 0;
    private Formation fangForm = new Formation(16);
    private ArrayList<TilePosition> expansions;
    ArrayList<Unit> marines;
    ArrayList<Unit> medics;
    boolean expanding = false;
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    @Override
    public void onUnitDestroy(Unit unit){
    	String ftype = uType.eval(unit.getType()).toString();
		fang.removeUnit(ftype, unit);
		 if(fangForm.members.contains(unit)) fangForm.members.remove(unit);
    	
    }
    @Override
    public void onUnitCreate(Unit unit) {
    	String ftype = uType.eval(unit.getType()).toString();
    	fang.addUnit(ftype, unit);
    	if (unit.getType() == UnitType.Terran_Marine || unit.getType() == UnitType.Terran_Medic ){
    		//fangForm.members.add(unit);

    	}
    	else if (unit.getType() == UnitType.Terran_Refinery){
    		mainBase.refinery = unit;
    	}
    	else if (unit.getType() == UnitType.Terran_Command_Center){
    		if (checkResources(UnitType.Terran_Comsat_Station)){
    			unit.buildAddon(UnitType.Terran_Comsat_Station);
    		}
    		expansions.add(unit.getTilePosition());
    	}
    	else if (unit.getType() == UnitType.Terran_SCV){
    		if (mainBase.builders != null && mainBase.builders.size() < 3)
    		{
    			mainBase.addBuilder(unit);
    		}
    		
    		
    		else if (	
    				self.hasUnitTypeRequirement(UnitType.Terran_Refinery) 
    				&& mainBase.gassers != null 
    				&& mainBase.gassers.size() < 3 )
    		{
    			mainBase.addGasWorker(unit);
    		//	System.out.println("adding gas worker" + unit.getID() + " " + mainBase.gassers.size());
    		}
    	//	fang.addUnit(ftype, unit);
    		//fang.addWorker(unit);
    	}
        //System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        game.setLocalSpeed(5);
        self = game.self();
       // game.sendText("show me the money");
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
      //  System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        expansions = new ArrayList<TilePosition>();
        //System.out.println("Map data ready");
        for (Unit u : self.getUnits()){
        	if (u.getType() == UnitType.Terran_Command_Center){
        		mainBase = new Base(u, game);
        		
        		allBases.add(mainBase);
        	}
        }

  /*      int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        }*/

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
    	//game.drawTextScreen(10, 10, fang.workers.toString());
        StringBuilder units = new StringBuilder("My units:\n");
        
        checkEnemyMemory();
        //iterate through my units
        int supplyDiff = game.self().supplyTotal() - game.self().supplyUsed() - checkProductionRate();
      //  System.out.println("Supply difference: " + supplyDiff);
       
       /* if (enemyLastSeen != null){
        	game.drawLineMap(enemyLastSeen, allBases.get(0).CC.getPosition(), Color.Red);
        }*/
        
      //  if (numUnits(UnitType.Terran_Marine) <= 0) marines = getUnits(UnitType.Terran_Marine);
       // ArrayList<Unit> medics = getUnits(UnitType.Terran_Marine);
      //  Position move = builder.getCenter(marines);
       // BaseLocation expand = getNextExpansion(self);
      //  game.drawLineMap(expand.getPosition(), self.getStartLocation().toPosition(), Color.Cyan);
        for (Unit myUnit : self.getUnits()) 
        {
        	units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
    		UnitType cType = myUnit.getType();
        	//getNextExpansion(mainBase.CC);
            SCV(myUnit, cType, supplyDiff);
            
    		if (myUnit.getType() == UnitType.Terran_Command_Center && numUnits(UnitType.Terran_SCV) < mainBase.maxWorkers)
    		{
    			 
    			//game.drawLineMap(getNextExpansion().getPosition(), mainBase.CC.getPosition(), Color.Blue);
    			//if (self.hasUnitTypeRequirement(UnitType.Terran_Comsat_Station)&& checkResources(UnitType.Terran_Comsat_Station))myUnit.build(UnitType.Terran_Comsat_Station);
    			fangState.Produce(myUnit, UnitType.Terran_SCV);
    		}
    		else if (myUnit.getType() == UnitType.Terran_Academy) Academy(myUnit);
    		else if (myUnit.getType() == UnitType.Terran_Factory) Factory(myUnit);
    		else if (myUnit.getType() == UnitType.Terran_Machine_Shop){
    			if (!self.hasResearched(TechType.Tank_Siege_Mode) && checkResources(TechType.Tank_Siege_Mode)) 
    				myUnit.research(TechType.Tank_Siege_Mode);
    			
    		}
    		else if (myUnit.getType() == UnitType.Terran_Starport){
    			if (myUnit.canBuildAddon() && checkResources(UnitType.Terran_Control_Tower)){
    				TilePosition tp = builder.getBuildTile(myUnit,UnitType.Terran_Control_Tower, myUnit.getTilePosition(), game);
    				myUnit.build(UnitType.Terran_Control_Tower, tp);
    			}
    			if (numUnits(UnitType.Terran_Science_Vessel)  <= 0 && self.hasUnitTypeRequirement(UnitType.Terran_Control_Tower)){
    				fangState.Produce(myUnit, UnitType.Terran_Science_Vessel);
    			}

    			
    		}
    		else if (myUnit.getType() == UnitType.Terran_Barracks && !myUnit.isTraining())
    		{
    			//System.out.println("There are +" + getUnits()
    			if (self.hasUnitTypeRequirement(UnitType.Terran_Academy)){
    				int m = numUnits(UnitType.Terran_Marine);
    				int medic = numUnits(UnitType.Terran_Medic);
    				int f;
    				if ( m / 3 >= medic ) fangState.Produce(myUnit, UnitType.Terran_Medic);
    				else {
    					fangState.Produce(myUnit, UnitType.Terran_Marine);
    				}
    			}
    			else fangState.Produce(myUnit, UnitType.Terran_Marine);
    			
    				
    			
    			/*else{
    				fangState.Produce(myUnit, UnitType.Terran_Marine);
    			}*/
    			
    		}
    		
    		//	ArrayList<Unit> marines = (fang.getUnitList(uType.eval(UnitType.Terran_Marine).toString()));
        	/*		System.out.println(marines.size());
        			if (!formation.contains(myUnit)) formation.add(myUnit);
        			fangForm.FSM(Formation.State.ATTACKING, marines);*/
    		if (   myUnit.isCompleted() && (
    				 myUnit.getType() == UnitType.Terran_Medic 
    				|| myUnit.getType() == UnitType.Terran_Marine
    				|| myUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
    				|| myUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
    				|| myUnit.getType() == UnitType.Terran_Vulture
    				
    				)
    			)
    		{
    			

        				fangForm.add(myUnit);
        				FSM(myUnit, fangForm, Formation.State.ATTACKING);
    				
    			
    		}
    		
        }

        //draw my units on screen
        mainBase.drawLayout(game);
        game.drawTextScreen(10, 25, fangForm.currentState.toString());
       // game.drawTextScreen(10, 40, "formation: current: " +  formations.get(0).members.size() + "max" + fangForm.memberSize);
    }

    public void FSM(Unit myUnit, Formation formation, Formation.State state){
    		Formation form = formation;
    		form.currentState = state;
			//builder.getCenter()
    		int formationDist = 20;
			int numEnemies = game.enemy().getUnits().size();
			//PositionL (myUnit.getLastCommand().getTargetTilePosition()))
			Position move = builder.getCenter(form.members);
			int dist = myUnit.getDistance(move);
			//game.drawLineMap(myUnit.getPosition(), move, Color.Blue);
			Position target = findEnemyBuilding(numEnemies);

			if (contains(myUnit, form.members) && dist > 500){
				form.currentState = Formation.State.MOVING;
			}
			else{
				form.currentState = Formation.State.ATTACKING;
			}
			//System.out.println(form.currentState);
    		switch(form.currentState){
	    		case MOVING:
	    			if (myUnit.isSieged()) myUnit.unsiege();
					 myUnit.issueCommand(UnitCommand.attack(myUnit, new PositionOrUnit(move)));
	    			break;
    			
	    		case FLEEING:
	    			break;
    			case ATTACKING:
					Unit attackEnemy = getClosestEnemy(myUnit, game.enemy().getUnits());
					PositionOrUnit attackE = new PositionOrUnit(attackEnemy);
					if (numUnits(UnitType.Terran_Marine) > 10 && dist >  1000){
						fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());
					}
					if (dist < 1000){
						game.drawLineMap(myUnit.getPosition(), move, Color.Red);
					}
					 if (myUnit.canUseTech(TechType.Tank_Siege_Mode)){
						 int enemyDist = myUnit.getDistance(attackEnemy);
						 if (enemyDist < 600 && attackEnemy.getType().isBuilding())
						 {
							 myUnit.useTech(TechType.Tank_Siege_Mode);
						 }
						 else if (myUnit.isSieged()) {
							 myUnit.unsiege();
						 }
						 
						}
					 
					//Attacking State
					
					if (attackEnemy.getDistance(myUnit) < 1000){

						
						form.target = attackE;
						fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());
						//break; 
					} 
					else if (numUnits(UnitType.Terran_Marine) + numUnits(UnitType.Terran_Medic) > 32 && target != null ) myUnit.issueCommand(UnitCommand.attack(myUnit, new PositionOrUnit(target)));
    				break;
    			default:
    				break;
    			
    				
    			}
		

    }
    public void marineMicro(Unit unit, PositionOrUnit target){
    	Unit attackEnemy = getClosestEnemy(unit, game.enemy().getUnits());
		 if (attackEnemy.getDistance(unit) < 500){
			PositionOrUnit attackE = new PositionOrUnit(attackEnemy);
			fangForm.target = attackE;
			fangState.Action(unit, game, FangSM.Role.RANGED, attackE.getUnit());
			//break; 
		}
    }
    public Position findEnemyBuilding(int numEnemies){
		if (numEnemies > 0) {
			for (Position p : enemyBuildingMemory){
				if (p != null){
					return p;
				}
			}
		}
		return null;
    }
    public void createFormation(ArrayList<Unit> units){
    	Formation form = new Formation(16);
    	form.generateFormationSlots(units);
    }
    public void setFormationMembers(Formation form){
    	ArrayList<Unit> allUnits = new ArrayList<Unit>();
    	allUnits.addAll(getUnits(UnitType.Terran_Marine));
    	allUnits.addAll(getUnits(UnitType.Terran_Medic));
    	form.members = allUnits;
    	//System.out.println(form.members.size());
    	
    }
    public void Starport(Unit starport){
    	if (!self.hasUnitTypeRequirement(UnitType.Terran_Control_Tower) && checkResources(UnitType.Terran_Control_Tower) && starport.canBuildAddon()){
    		TilePosition toBuild = builder.getBuildTile(starport, starport.getType(), mainBase.barracks.toTilePosition(), game);
    		starport.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (!starport.isTraining()){
    		starport.train(UnitType.Terran_Science_Vessel);
    	}
    }
    public void Factory(Unit factory){
    	if (!self.hasUnitTypeRequirement(UnitType.Terran_Machine_Shop) && checkResources(UnitType.Terran_Machine_Shop) && factory.canBuildAddon()){
    		TilePosition toBuild = builder.getBuildTile(factory, factory.getType(), mainBase.barracks.toTilePosition(), game);
    		factory.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	
    	if (factory.isLifted()){
    		TilePosition toBuild = builder.getBuildTile(factory, factory.getType(), factory.getTilePosition(), game);
    		factory.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (!factory.isTraining()){
    		if (self.hasUnitTypeRequirement(UnitType.Terran_Machine_Shop)) factory.train(UnitType.Terran_Siege_Tank_Tank_Mode);
    		else {
    			factory.train(UnitType.Terran_Vulture);
    		}
    	}
    }
    public void Academy(Unit academy){
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
    public void SCV(Unit myUnit, UnitType cType, Integer supplyDiff)
    {
    	
    	if (cType == UnitType.Terran_SCV){

    		boolean buildPylon = supplyDiff <= (checkProductionRate() + 5) 
    		&& self.minerals() >= 100 
    		&& checkResources(UnitType.Terran_Supply_Depot)
    		&& !checkConstructing(UnitType.Terran_Supply_Depot);
    		scoutEnemy(myUnit);
			if (contains(myUnit, mainBase.builders))
			{
				
				/*if (checkResources(UnitType.Terran_Command_Center)){
					
				}*/
				//if (checkResources(UnitType.Terran_Command_Center)){

				//	myUnit.move(getNextExpansion(self).getPosition());
				/*	BaseLocation bl = getNextExpansion();
						game.drawLineMap(bl.getPosition(), myUnit.getPosition(), Color.Cyan);
						if (game.isVisible(bl.getTilePosition())){
							if (!expansions.contains(bl)) expansions.add(bl);
							TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Command_Center, bl.getTilePosition(), game);
					    	fangState.Produce(myUnit, UnitType.Terran_Command_Center, toBuild);
					    	expanding = true;
							
						}
						else{
							myUnit.move(bl.getPosition());
						}*/
						
						
					
			//	}
				
			//	if (!checkConstructing(UnitType.Terran_Starport) && self.hasUnitTypeRequirement(UnitType.Terran_Factory) && !self.hasUnitTypeRequirement(UnitType.Terran_Starport)) 
				//	buildStarport(myUnit);
				 if (!self.hasUnitTypeRequirement(UnitType.Terran_Science_Facility) 
						&& self.hasUnitTypeRequirement(UnitType.Terran_Starport)
						&& !checkConstructing(UnitType.Terran_Science_Facility)){
			    	TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Science_Facility, mainBase.supplies.toTilePosition(), game);
			    	fangState.Produce(myUnit, UnitType.Terran_Science_Facility, toBuild);
				}
				
				buildFactory(myUnit);
			    if (!self.hasUnitTypeRequirement(UnitType.Terran_Academy)
				    	&& checkResources(UnitType.Terran_Academy)
				    	&& self.hasUnitTypeRequirement(UnitType.Terran_Barracks)
				    	&& !checkConstructing(UnitType.Terran_Academy))
				    {
				    	TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Academy, mainBase.supplies.toTilePosition(), game);
				    	fangState.Produce(myUnit, UnitType.Terran_Academy, toBuild);
				    }
			    
//        		System.out.println("there are " + mainBase.builders.size() + "workers");
			    else if (	   !checkConstructing(UnitType.Terran_Barracks)
			    		&& self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) 
			    		&& checkResources(UnitType.Terran_Barracks)
			    		&& numUnits(UnitType.Terran_Barracks) < allBases.size() + 2
			    	    && supplyDiff >= 5   )
			    {
			    	
					TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Barracks, mainBase.supplies.toTilePosition(), game);
					fangState.Produce(myUnit, UnitType.Terran_Barracks, toBuild);
				}
			  //  buildEngineeringBay(myUnit);
			    if (numUnits(UnitType.Terran_Refinery) <= 0 
			    		&& checkResources(UnitType.Terran_Refinery) 
			    		&& self.hasUnitTypeRequirement(UnitType.Terran_Barracks)
			    		&& !checkConstructing(UnitType.Terran_Refinery))
			    {
			    	List<Unit> geysers = game.getGeysers();
			    	int dist;
			    	for (Unit g : geysers)
			    	{
			    		dist = g.getDistance(mainBase.CC.getPosition());
			    		if (dist < 200){
			    			TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Refinery, g.getTilePosition(), game);
			    			fangState.Produce(myUnit, UnitType.Terran_Refinery, toBuild);
			    			break;
			    		}
			    		game.drawLineMap(g.getPosition(), mainBase.CC.getPosition(), Color.Red);
			    	}
			    }
			    if (buildPylon)
			    {
					TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Supply_Depot, mainBase.supplies.toTilePosition(), game);
					fangState.Produce(myUnit, UnitType.Terran_Supply_Depot, toBuild);
				}
				else 
				{
    				fangState.Action(myUnit, game, FangSM.Role.HARVESTER);
    			}
    		}
			//System.out.println(contains(myUnit, mainBase.gassers));
			else if (contains(myUnit, mainBase.gassers) && !myUnit.isBeingConstructed())
		    {
				if (!myUnit.isGatheringGas()){
					for (Unit u : self.getUnits()){
						if (u.getType().isRefinery()){
							myUnit.issueCommand(UnitCommand.gather(myUnit, u));
						}
					}
				}
				//System.out.println(getUnits(UnitType.Terran_Refinery).size());

			//	System.out.println("Mining gas...");
			//	PositionOrUnit refine = new PositionOrUnit(mainBase.refinery);
			//System.out.println(refine.getPosition());
				//	myUnit.issueCommand(UnitCommand.rightClick(myUnit, refine));
			//	game.drawLineMap(refine.getPosition(), mainBase.CC.getPosition(), Color.Blue);
			//	builder.drawBox(game, myUnit.getPosition(), 32, 32, UnitType.Terran_SCV);
			//	break;
		    }
			else 
			{
				fangState.Action(myUnit, game, FangSM.Role.HARVESTER);
			}
    	}
    }
    public boolean checkResources(UnitType type){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public boolean checkResources(TechType type){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public void scoutEnemy(Unit u)
    {
		if (mainBase.scout == null){
			if (!contains(u, mainBase.builders)){
				mainBase.scout = u;
			}
		}
		if (enemyBuildingMemory.size() == 0 && self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot)){
			
			if (target == null)
			{
				target = getDest(BWTA.getStartLocations());
			}

			if (u == mainBase.scout)
			{
				if (target != null && u.getDistance(target.getPosition()) < 10) 
				{
					checkedBases.add(target);
    				target = null;
				}
				u.move(target.getPosition());
			}
		}
		/*else if (self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot)){
			
			if (self.minerals() > 400){
				BaseLocation next = getNextExpansion(self);
				game.drawLineMap(next.getPosition(), mainBase.CC.getPoint(), Color.Orange);
				if (!game.isVisible(next.getTilePosition())){
					System.out.println("Moving to expansion");
					u.move(next.getPosition());
				}
				else {
					
					System.out.println("Building to expansion");
					TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Command_Center, next.getTilePosition(), game);
					u.build(UnitType.Terran_Command_Center, toBuild);
					
				}
			}
		}*/
    }
    public void buildStarport(Unit u){
	    if (checkResources(UnitType.Terran_Starport) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Factory))
	    {
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Starport, self.getStartLocation(), game);
	    	fangState.Produce(u, UnitType.Terran_Starport, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }
    
    public void buildFactory(Unit u){
	    if (checkResources(UnitType.Terran_Factory) && numUnits(UnitType.Terran_Factory) < allBases.size() 
	        && !checkConstructing(UnitType.Terran_Factory)  
	        && self.hasUnitTypeRequirement(UnitType.Terran_Barracks) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Academy))
	    {
	    	//mainBase.barracks.toTilePosition()
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Factory, self.getStartLocation(), game);
	    	
	    	fangState.Produce(u, UnitType.Terran_Factory, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }
    public void buildEngineeringBay(Unit u){
	    if (checkResources(UnitType.Terran_Engineering_Bay) && numUnits(UnitType.Terran_Engineering_Bay) <= 0 
	        && !checkConstructing(UnitType.Terran_Engineering_Bay)  
	        && self.hasUnitTypeRequirement(UnitType.Terran_Barracks) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Academy))
	    {
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Engineering_Bay, mainBase.supplies.toTilePosition(), game);
	    	fangState.Produce(u, UnitType.Terran_Engineering_Bay, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }

    public boolean contains(Unit u, ArrayList<Unit> list){
    	for (Unit unit : list){
    		if (unit.getID() == u.getID()){
    			return true;
    		}
    	}
    	return false;
    }
    ArrayList<Unit> getUnits(UnitType unit){
    	return fang.getUnitList(uType.eval(unit).toString());
    }
    public boolean checkConstructing(UnitType building){
    	ArrayList<Unit> scvs = mainBase.builders;//fang.getUnitList(uType.eval(UnitType.Terran_SCV).toString());
    	Unit b;
    	for (Unit u : scvs){
    		if (u.isConstructing()){
    			b = u.getBuildUnit();
    			if (b.getType() == building){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    public int numUnits(UnitType unitType)
    {
    	int num = fang.getUnitsLength(uType.eval(unitType).toString());
    	return num;
    }
    public BaseLocation getNextExpansion(Player player)
    {
    	TilePosition start = self.getStartLocation();
    	
    	BaseLocation result = null;
    	double dist = 0;
    	double closestDist = 1000000;
    	for (BaseLocation baseLocation : BWTA.getStartLocations()){
    		if (!expansions.contains(baseLocation)){
   //(start != baseLocation.getTilePosition() || baseLocation != BWTA.getStartLocation(player)){
    			dist = BWTA.getGroundDistance(baseLocation.getTilePosition(), start);

    			if (dist > 0 && dist < closestDist ){
    				result = baseLocation;
    				closestDist = dist;
    			}
    		}

    		//	game.drawLineMap(baseLocation.getPosition(), start.toPosition(),Color.Blue);
    			
    		
    	//	if (baseLocation.getDistance(toCheck.getPosition()) > closest.getDistance(toCheck.getPosition())){
    		//	closest = baseLocation;
    			
    		}
    	if (closestDist > 10000 || result == null){
    		return null;
    	}
    	System.out.println(closestDist);
    	return result;
    	//return result;
    	}
  //  	game.drawLineMap(closest.getPosition(), toCheck.getPosition(),Color.Blue);
    public BaseLocation getDest(List<BaseLocation> loc)
    {
    	for (BaseLocation baseLocation : loc){
			if (!checkedBases.contains(baseLocation) && !game.isVisible(baseLocation.getTilePosition()) )
			{
				return baseLocation;
			}
    	}
    	return null;
    }
    public int checkProductionRate() 
    {
    	int productionRate = 0;
    	for (Unit u : self.getUnits()){
    		if (u.isTraining()){
    			productionRate++;
    		}
    	}
    	return productionRate;
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
    public void checkEnemyMemory(){
    	for (Unit u : game.enemy().getUnits()){
    		if (u.getType().isBuilding()){
    			if (!enemyBuildingMemory.contains(u.getPosition())) enemyBuildingMemory.add(u.getPosition());
    		}
    	}
    	for (Position p : enemyBuildingMemory){
    		TilePosition tileMemory = new TilePosition(p.getX()/32, p.getY()/32);
    		if (game.isVisible(tileMemory)){
    			boolean buildingStillThere = false;
    			for (Unit u: game.enemy().getUnits()){
    				if (u.getType().isBuilding() && (u.getPosition() == p)){
    					buildingStillThere = true;
    					break;
    				}
    			}
    			if (!buildingStillThere){
    				enemyBuildingMemory.remove(p);
    				break;
    			}
    		}
    	}
    }
    public static void main(String[] args) {
        new FangBot().run();
    }
}
/*
//if there's enough minerals, train an SCV
boolean isWorker = myUnit.getType().isWorker();
boolean isBuilding = myUnit.getType().isBuilding();
if (isBuilding){
	if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.isCompleted() && myUnit.isIdle()){
    	myUnit.train(UnitType.Terran_Marine);   
    	break;
	}
	else if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50 && myUnit.isIdle()) {
       myUnit.train(UnitType.Terran_SCV);
       break;
    }

}

if (isWorker){

    if (self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) && self.minerals() >= 150 && !fang.barracksBuilt){
    	System.out.println("building barracks");
    	TilePosition check = new TilePosition(self.getStartLocation().getX() + (8 * dir), self.getStartLocation().getY() + (3 * dir));
    	TilePosition buildTile = getBuildTile(myUnit, UnitType.Terran_Barracks, check);//new TilePosition(self.getStartLocation().getX() + 5, self.getStartLocation().getY() + 5);
    	if (buildTile == null) dir *= -1;
    	if (buildTile != null){
    		myUnit.build(UnitType.Terran_Barracks, buildTile );
    		game.sendText("built a barracks");

    	}
    	break;
    }
    else if (self.minerals() >= 100 && (self.supplyTotal() - self.supplyUsed() < 3)){
    	TilePosition check = new TilePosition(self.getStartLocation().getX() + (5 * dir), self.getStartLocation().getY());
    	TilePosition buildTile = getBuildTile(myUnit, UnitType.Terran_Supply_Depot, check);
    	if (buildTile == null) dir *= -1;
    	if (buildTile != null){
    		myUnit.build(UnitType.Terran_Supply_Depot, buildTile );
    		break;
    	}
    }
    //if it's a worker and it's idle, send it to the closest mineral patch
    if (myUnit.isIdle()) {
        Unit closestMineral = null;

        //find the closest mineral
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {
                if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                    closestMineral = neutralUnit;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
            myUnit.gather(closestMineral, false);
        }
    }
}
*/

/*

//	PositionOrUnit attackPos = new PositionOrUnit(mainBase.CC);
        		//	fangState.Action(myUnit, game, FangSM.Role.RANGED, mainBase.CC);

   		/*	if (game.enemy().getUnits().size() > 0){
    				Position enemyCenter = builder.getCenter(game.enemy().getUnits());
    				enemyLastSeen = enemyCenter;
    				PositionOrUnit pouOrUnit = new PositionOrUnit(enemyCenter);
    				myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
    				//builder.drawLine(game, myUnit.getPosition(), enemyCenter);
    			}*/
    			//else {
    		/*		if (numUnits(UnitType.Terran_Marine) > 36) {
    					if (game.enemy().getUnits().size() > 0){
    					}
    					for (Position p : enemyBuildingMemory){
    						if (p != null){
    							//System.out.println("attackinging..." + p.getPoint());
    							PositionOrUnit pouOrUnit = new PositionOrUnit(p);
    							myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
    							break;
    						}
    					}
    					
    					//Position[] e = (Position[]) enemyBuildingMemory.toArray(); 
    					//PositionOrUnit pouOrUnit = new PositionOrUnit(e[0]);
						//myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
						//builder.drawLine(game, myUnit.getPosition(), enemyLastSeen);
						
    				}*/
    			//}
    /*				else if (enemyLastSeen == null && numUnits(UnitType.Terran_Marine) > 24) {
    					
    				
    				if (target == null){
    					target = getDest();
    				}
    				else if (target != null && myUnit.getDistance(target.getPosition()) < 10) {
    					checkedBases.add(target);
        				System.out.println("Dist to Target: " + myUnit.getDistance(target));
        				System.out.println(checkedBases.size());
        				target = null;
    				}
						PositionOrUnit pouOrUnit = new PositionOrUnit(target.getPosition());
						myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
						builder.drawLine(game, myUnit.getPosition(), target.getPosition());*/
					//	if (myUnit.getDistance(baseLocation.getPosition()) < 10){
					//		checkedBases.add(baseLocation);
					//	}
    						
    						//target
    					
    				
    				//myUnit.issueCommand(UnitCommand.)
    			//}
    		//	}
