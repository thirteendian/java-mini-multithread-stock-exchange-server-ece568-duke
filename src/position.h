#ifndef __POSITION_H__
#define __POSITION_H__

#include "symbol.h"
#include "exception.h"

class Position{
  private:
    Symbol& symbol;
    int amount;

  public:
    Position(Symbol _symbol, int _amount):symbol(_symbol), amount(_amount){
      if(_amount <= 0){
        throw NumberDomainException("error: amount must be greater than 0");
      }
    }
    explicit Position(const Position& rhs):symbol(rhs.symbol), amount(rhs.amount){};
    bool operator==(const Position& rhs) const;
    ~Position(){};

    Symbol& get_symbol();
    int get_amount();
};

#endif