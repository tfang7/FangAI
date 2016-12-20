#pragma once
#ifndef __BASE_H_INCLUDED__
#define __BASE_H_INCLUDED__
#include <BWAPI.h>
#include <map>
#include "Base.h"
class BaseManager
{
public:
	BaseManager();
	BaseManager(BWAPI::Player player);
	~BaseManager();
	void addBase(BWAPI::Unit unit);
	Base getBase(BWAPI::Unit unit);
private:
//	std::vector<Base> bases;
	std::vector<Base> bases;
};

#endif

