#include "Base.h"


Base::Base(Unit unit)
{
	if (unit->getType() == UnitTypes::Terran_Command_Center)
	{
		CommandCenter = unit;
		Broodwar << "found base..." << std::endl;
	}
}


Base::~Base()
{
}
