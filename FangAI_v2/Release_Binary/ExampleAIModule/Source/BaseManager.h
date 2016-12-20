#pragma once

#include <BWAPI.h>
#include <Base.h>

class BaseManager
{
public:
	BaseManager(BWAPI::Player player);
	~BaseManager();
	void addBase(BWAPI::Unit unit);
	
};

