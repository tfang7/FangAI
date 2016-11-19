import java.util.ArrayList;
import java.util.List;

import bwapi.*;
public class BuildingUtil {
	public BuildingUtil(){
		
	}
	public void drawBox(Game g, Position p, int width, int height, UnitType u){
		Position buildLoc;
		Position start;
		if (u == null){
			start = p;
			buildLoc = new Position(p.getX() + width,p.getY() + height);
		}
		else{
			start = new Position(p.getX() - u.width(), p.getY() - u.height());
			buildLoc = new Position(p.getX() + width,p.getY() + height);
		}
		g.drawBoxMap(start, buildLoc, Color.Blue);

	}
	public void drawLine(Game g, Position p1, Position p2){
		g.drawLineMap(p1, p2,Color.Green);
	}
	public void drawCircle(Game g, Position p, int r){
		g.drawCircleMap(p, r, Color.Green);
	}
	public Position getDir(Position a, Position b){
		Position dir = new Position(a.getX() - b.getX(), a.getY() - b.getY());
		return dir;
	}
	public Position normalize(Position a){
	//	System.out.println("The normal is " + Math.sqrt((Math.pow(a.getX(), 2)+ Math.pow(a.getY(), 2))));
		float magnitude =  (float)Math.sqrt((Math.pow(a.getX(), 2)+ Math.pow(a.getY(), 2)));
		return new Position((int) Math.round(a.getX()/magnitude),(int) Math.round(a.getY()/magnitude));
	}
	public ArrayList<Unit> getBaseResources(Position p, Game g){
		ArrayList<Unit> resourcesInRadius = new ArrayList<Unit>();
		List<Unit> units = g.getUnitsInRadius(p, 300);
		for (Unit u : units){
			
			if (u.getType().isMineralField() || u.getType() == UnitType.Resource_Vespene_Geyser){
				if (!resourcesInRadius.contains(u)) resourcesInRadius.add(u);
			}
		}
		return resourcesInRadius;
	}
	public Position getCenter(ArrayList<Unit> units){
		int avgX = 0;
		int avgY = 0;
		for (Unit u : units){
			int x = u.getPosition().getX();
			int y = u.getPosition().getY();
			avgX += x;
			avgY += y;
		}
		avgX = avgX / units.size();
		avgY = avgY / units.size();
		return new Position(avgX, avgY);
	}

	public Position getCenter(List<Unit> units){
		int avgX = 0;
		int avgY = 0;
		for (Unit u : units){
			int x = u.getPosition().getX();
			int y = u.getPosition().getY();
			avgX += x;
			avgY += y;
		}
		avgX = avgX / units.size();
		avgY = avgY / units.size();
		return new Position(avgX, avgY);
	}
	public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile, Game game) {
		TilePosition ret = null;
		int maxDist = 3;
		int stopDist = 40;
		
		// Refinery, Assimilator, Extractor
		if (buildingType.isRefinery()) {
			for (Unit n : game.neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser) && 
						( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
						( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
						) return n.getTilePosition();
			}
		}
		
		while ((maxDist < stopDist) && (ret == null)) {
			for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
				for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
					if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : game.getAllUnits()) {
							if (u.getID() == builder.getID()) continue;
							if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
						}
						if (!unitsInWay) {
							return new TilePosition(i, j);
						}
						// creep for Zerg
						if (buildingType.requiresCreep()) {
							boolean creepMissing = false;
							for (int k=i; k<=i+buildingType.tileWidth(); k++) {
								for (int l=j; l<=j+buildingType.tileHeight(); l++) {
									if (!game.hasCreep(k, l)) creepMissing = true;
									break;
								}
							}
							if (creepMissing) continue; 
						}
					}
				}
			}
			maxDist += 2;
		}
		
		if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
		return ret;
	}
}
