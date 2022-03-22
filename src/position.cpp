#include "position.h"

bool Position::operator==(const Position& rhs) const{
  return this->symbol == rhs.symbol && this->amount == rhs.amount;
}

Symbol& Position::get_symbol(){
  return this->symbol;
}

int Position::get_amount(){
  return this->amount;
}