import java.util.ArrayList;
import bwapi.*;

import java.util.List;

import bwapi.*;
public class Base {
	private Unit CC;
	private ArrayList<Unit> workers;
	private ArrayList<Unit> builders;
	private BuildingUtil builder = new BuildingUtil();
	Position resourceCenter;
	Position dir;
	Position supplies;
	Position barracks;
	int xOffset = 25, yOffset = 25;
	
	public Base(Unit u, Game g){
		if (u != null && u.getType() == UnitType.Terran_Command_Center){
			CC = u;
		}
		setLayout(g);
	}
	public void setBuilder(Unit u){
		if (builders.size() < 1){
			builders.add(u);
		}
	}
	public void setLayout(Game game){
		resourceCenter = builder.getCenter(builder.getBaseResources(CC.getPosition(), game));
		dir = builder.normalize(builder.getDir(CC.getPosition(), resourceCenter));
		System.out.println("The direction is" + dir.getX() + dir.getY());
		supplies = new Position(CC.getPosition().getX()  + ((200 + UnitType.Terran_Command_Center.width())* dir.getX()), CC.getPosition().getY() + (150 * dir.getY()));
		barracks = new Position(supplies.getX() + 200 + xOffset, supplies.getY());
	}
	public void drawLayout(Game game){
		builder.drawBox(game, supplies, 200, 400, null);
		builder.drawBox(game, barracks, 200, 400, null);
		builder.drawLine(game, resourceCenter, CC.getPosition());
	}
}
