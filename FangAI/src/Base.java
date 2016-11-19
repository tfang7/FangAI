import java.util.ArrayList;
import bwapi.*;
import java.util.List;

public class Base {
	public Unit CC;
	public Unit Refinery;
	
	public Unit scout;
	public ArrayList<Unit> builders = new ArrayList<Unit>();
	private BuildingUtil builder = new BuildingUtil();
	public int maxBarracks = 4;
	public int maxWorkers = 27;
	public int gasWorkers = 3;
	
	Position resourceCenter;
	Position dir;
	public Position supplies;
	Position barracks;
	int xOffset = 25, yOffset = 25;
	
	public Base(Unit u, Game g){
		if (u != null && u.getType() == UnitType.Terran_Command_Center){
			CC = u;
		}
		setLayout(g);
	}
	
	public void addBuilder(Unit u){
		if (!builders.contains(u)){
			builders.add(u);
		}
	}
	public Unit checkBuilders(){
		for (Unit b : builders){
			if (!b.isConstructing()){
				return b;
			}
		}
		return null;
	}
	public void setLayout(Game game){
		resourceCenter = builder.getCenter(builder.getBaseResources(CC.getPosition(), game));
		dir = builder.normalize(builder.getDir(CC.getPosition(), resourceCenter));
	//	System.out.println("The direction is" + dir.getX() + dir.getY());
		supplies = new Position(CC.getPosition().getX()  + ((100 + UnitType.Terran_Command_Center.width())* dir.getX()), CC.getPosition().getY() + (150 * dir.getY()));
		barracks = new Position(supplies.getX() + ((200 + xOffset) * dir.getX()), supplies.getY());
	}
	public void drawLayout(Game game){
		builder.drawBox(game, supplies, 200, 400, null);
		builder.drawBox(game, barracks, 200, 400, null);
		builder.drawLine(game, resourceCenter, CC.getPosition());
	}
	// Returns a suitable TilePosition to build a given building type near 
	// specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
	
		
}
