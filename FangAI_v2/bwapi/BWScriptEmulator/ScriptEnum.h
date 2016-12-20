#pragma once
#include <BWAPI.h>

namespace AISCRIPT
{
  const char * const getOpcodeName(int iOpcode);
  const char * const getUnitName(int iUnitType);
  int getUnitInternal(int iUnitType);
  BWAPI::Orders::Enum::Enum getUnitIdleOrder(int iUnitType);

  namespace Enum
  {
    enum Enum
    {
      GOTO,
      NOTOWNS_JUMP,
      WAIT,
      START_TOWN,
      START_AREATOWN,
      EXPAND,
      BUILD,
      UPGRADE,
      TECH,
      WAIT_BUILD,
      WAIT_BUILDSTART,
      ATTACK_CLEAR,
      ATTACK_ADD,
      ATTACK_PREPARE,
      ATTACK_DO,
      WAIT_SECURE,
      CAPT_EXPAND,
      BUILD_BUNKERS,
      WAIT_BUNKERS,
      DEFENSEBUILD_GG,
      DEFENSEBUILD_AG,
      DEFENSEBUILD_GA,
      DEFENSEBUILD_AA,
      DEFENSEUSE_GG,
      DEFENSEUSE_AG,
      DEFENSEUSE_GA,
      DEFENSEUSE_AA,
      DEFENSECLEAR_GG,
      DEFENSECLEAR_AG,
      DEFENSECLEAR_GA,
      DEFENSECLEAR_AA,
      SEND_SUICIDE,
      PLAYER_ENEMY,
      PLAYER_ALLY,
      DEFAULT_MIN,
      DEFAULTBUILD_OFF,
      STOP,
      SWITCH_RESCUE,
      MOVE_DT,
      DEBUG,
      FATAL_ERROR,
      ENTER_BUNKER,
      VALUE_AREA,
      TRANSPORTS_OFF,
      CHECK_TRANSPORTS,
      NUKE_RATE,
      MAX_FORCE,
      CLEAR_COMBATDATA,
      RANDOM_JUMP,
      TIME_JUMP,
      FARMS_NOTIMING,
      FARMS_TIMING,
      BUILD_TURRETS,
      WAIT_TURRETS,
      DEFAULT_BUILD,
      HARASS_FACTOR,
      START_CAMPAIGN,
      RACE_JUMP,
      REGION_SIZE,
      GET_OLDPEONS,
      GROUNDMAP_JUMP,
      PLACE_GUARD,
      WAIT_FORCE,
      GUARD_RESOURCES,
      CALL,
      RETURN,
      EVAL_HARASS,
      CREEP,
      PANIC,
      PLAYER_NEED,
      DO_MORPH,
      WAIT_UPGRADES,
      MULTIRUN,
      RUSH,
      SCOUT_WITH,
      DEFINE_MAX,
      TRAIN,
      TARGET_EXPANSION,
      WAIT_TRAIN,
      SET_ATTACKS,
      SET_GENCMD,
      MAKE_PATROL,
      GIVE_MONEY,
      PREP_DOWN,
      RESOURCES_JUMP,
      ENTER_TRANSPORT,
      EXIT_TRANSPORT,
      SHAREDVISION_ON,
      SHAREDVISION_OFF,
      NUKE_LOCATION,
      HARASS_LOCATION,
      IMPLODE,
      GUARD_ALL,
      ENEMYOWNS_JUMP,
      ENEMYRESOURCES_JUMP,
      IF_DIF,
      EASY_ATTACK,
      KILL_THREAD,
      KILLABLE,
      WAIT_FINISHATTACK,
      QUICK_ATTACK,
      JUNKYARD_DOG,
      FAKE_NUKE,
      DISRUPTION_WEB,
      RECALL_LOCATION,
      SET_RANDOMSEED,
      IF_OWNED,
      CREATE_NUKE,
      CREATE_UNIT,
      NUKE_POS,
      HELP_IFTROUBLE,
      ALLIES_WATCH,
      TRY_TOWNPOINT,
      LAST
    };
  }

}
