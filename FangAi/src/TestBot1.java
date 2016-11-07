import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    
    private Game game;

    private Player self;
    private FangAi fang = new FangAi(self, game);
    public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile){
    	int stopDist = 40;
    	int maxDist = 3;
    	TilePosition ret = null;
    	if (buildingType.isRefinery()){
    		for (Unit n: game.neutral().getUnits()){
    			if ((n.getType() == UnitType.Resource_Vespene_Geyser) && 
    			(Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist )
    			&& (Math.abs(n.getTilePosition().getY() - aroundTile.getY())) < stopDist){
    				return n.getTilePosition();
    			}
    		}
    	}
    	while ((maxDist < stopDist) && (ret == null)){
    		for(int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++){
    			for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++){
    				if (game.canBuildHere(new TilePosition(i,j), buildingType, builder)){
    					boolean unitsInWay = false;
    					for (Unit u : game.getAllUnits()){
    						if (u.getID() == builder.getID()) continue;
    						if ((Math.abs(u.getTilePosition().getX() - i) < 4) && Math.abs(u.getTilePosition().getY() - j) < 4){
    							unitsInWay = true;
    						}
    						if (!unitsInWay) {
    							return new TilePosition(i,j);
    						}
    						if (buildingType.requiresCreep()){
    							boolean creepMissing = false;
    							for(int k=i;k<=i+buildingType.tileWidth();k++){
    								for (int l = j; l <= j+buildingType.tileHeight(); l++){
    									if(!game.hasCreep(k,l)) creepMissing = true;
    									break;
    								}
    							}
    							if (creepMissing) continue;
    						}
    						
    					}
    				}
    			}
    		}
    		maxDist += 2;
    	}
    	if (ret == null) game.printf("Can't find suitable build position for" + buildingType.toString());
    	return ret;
    }
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
    	
        //System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        game.setLocalSpeed(1);
        self = game.self();
        //game.sendText("show me the money");
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        int i = 0;
       /* for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }*/

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
    	game.drawTextScreen(10, 10, "Playing as " + fang.workers + " - " + fang.barracksBuilt);
    	int remainingFood = self.supplyTotal() - self.supplyUsed();
    	if (self.hasUnitTypeRequirement(UnitType.Terran_Barracks)){
    		fang.barracksBuilt = true;
    	}

        int dir = -1;
        StringBuilder units = new StringBuilder("My units:\n");
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
            
            //if there's enough minerals, train an SCV
            boolean isWorker = myUnit.getType().isWorker();
            boolean isBuilding = myUnit.getType().isBuilding();
            if (isBuilding){
            	if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.isCompleted() && myUnit.isIdle()){
                	myUnit.train(UnitType.Terran_Marine);   
                	break;
            	}
            	else if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50 && myUnit.isIdle() && fang.workers <= 10) {
                   myUnit.train(UnitType.Terran_SCV);
                   fang.workers++;
                   break;
                }

            }
            if (isWorker){
            	fang.addWorker(myUnit);
                if (self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) && self.minerals() >= 150 && !fang.barracksBuilt && remainingFood >= 2){
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

        }

        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
        //game.drawTextScreen(50, 25,"" + myUnit.getType());
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}