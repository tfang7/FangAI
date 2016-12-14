import java.util.ArrayList;
import bwapi.*;
import java.util.List;

public class Base {
	public Unit CC;
	public Unit Refinery;
	
	public Unit scout;
	public ArrayList<Unit> builders;
	public ArrayList<Unit> gassers;
	public ArrayList<Unit> workers;
	private BuildingUtil builder = new BuildingUtil();
	public int maxBarracks = 2;
	public int maxWorkers = 27;
	public int gasWorkers = 3;
	
	public Unit refinery;
	public boolean refineryBuilt;
	public boolean ebayBuilt;
	Position resourceCenter;
	Position dir;
	public Position tech;
	public Position supplies;
	public Position barracks;
	public TilePosition tilePos;
	int xOffset = 25, yOffset = 25;
	
	public Base(Unit u, Game g)
	{
		if (u != null && u.getType() == UnitType.Terran_Command_Center)
		{
			CC = u;
			tilePos = u.getTilePosition();
		}
		else{
			System.out.println("Base created: Not CC");
		}
		ebayBuilt = false;
		refineryBuilt = false;
		gassers = new ArrayList<Unit>();
		builders = new ArrayList<Unit>();
		workers = new ArrayList<Unit>();
		setLayout(g);
	}
	
	public void addBuilder(Unit u)
	{
		if (!builders.contains(u) && !gassers.contains(u) && scout != u){
			builders.add(u);
		}
	}
	public void addGasWorker(Unit u)
	{
		if (!gassers.contains(u) && !builders.contains(u) && scout != u){
			gassers.add(u);
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
	public Unit checkBuilders()
	{
		for (Unit b : builders)
		{
			if (!b.isConstructing())
			{
				return b;
			}
		}
		return null;
	}
	public void setLayout(Game game)
	{
		resourceCenter = builder.getCenter(builder.getBaseResources(CC.getPosition(), game));
		dir = builder.normalize(builder.getDir(CC.getPosition(), resourceCenter));
		System.out.println("The direction is" + dir.getX() + " " +  dir.getY());
		supplies = new Position(CC.getPosition().getX()  + ((100 + UnitType.Terran_Command_Center.width())* dir.getX()), CC.getPosition().getY() + (300 * dir.getY()));
		barracks = new Position(supplies.getX() + ((200 + xOffset) * dir.getX()), supplies.getY());
	//	tech = new Position(CC.getPosition().getX() + (200 * dir.getX()), CC.getPosition().getY()  + (int)(300 * builder.getY(notNormalized)));
	}
	public void drawLayout(Game game)
	{
		builder.drawBox(game, supplies, 200, 400, null);
		builder.drawBox(game, barracks, 200, 400, null);
	//	builder.drawBox(game, tech, 300, 300, null);
		builder.drawLine(game, resourceCenter, CC.getPosition());
	}
	// Returns a suitable TilePosition to build a given building type near 
	// specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
	
		
}
