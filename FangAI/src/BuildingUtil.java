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
}
