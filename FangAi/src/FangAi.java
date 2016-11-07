import java.util.List;

import bwapi.*;
public class FangAi {
	int numBarracks = 0;
	int numRefineries = 0;
	boolean barracksBuilt;
	int workers;
	private Player player;
	private Game game;
	int height;
	int width;
	List<Unit> SCV;
	public enum state{
		MACRO,
		MICRO
	}
	public state State;
	public FangAi(Player p,Game g){
		player = p;
		game = g;
		workers = 0;
		barracksBuilt = false;
		State = state.MACRO;
	}
	public void addWorker(Unit scv){
		if (SCV.contains(scv))SCV.add(scv);
	}
	public void constructBuilding(Unit builder, UnitType unit, TilePosition buildLoc, int dir){
    	TilePosition check = new TilePosition(buildLoc.getX() + (8 * dir), buildLoc.getY() + (3 * dir));
    	TilePosition buildTile = getBuildTile(builder, UnitType.Terran_Barracks, check);//new TilePosition(self.getStartLocation().getX() + 5, self.getStartLocation().getY() + 5);
    	if (buildTile == null) dir *= -1;
    	if (buildTile != null){
    		builder.build(UnitType.Terran_Barracks, buildTile );
    		barracksBuilt = true;
    	}
    	
	}
	public int numUnitType( UnitType uType ){
		int counter = 0;
		for (Unit u : player.getUnits())
		{
			if  (u.getType() == uType){
				counter++;
			}
		}
		return counter;
	}

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
}
