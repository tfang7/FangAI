#pragma once
#include <BWAPI.h>

using namespace BWAPI;
class FangController
{
public:
	FangController();
	FangController(BWAPI::Player p);
	~FangController();
	int availableSupply();

private:
	int armySize;
	BWAPI::Player player;
	void initArray(Unit unit, UnitType uType);
	void addUnit(Unit unit);
	void removeUnit(Unit unit);
	std::map<UnitType, std::vector<Unit>> armyUnits;
};

