#include "order.h"

bool Order::operator==(const Order& rhs) const{
  return this->symbol == rhs.symbol &&
    this->amount == rhs.amount &&
    this->limitPrice == rhs.limitPrice &&
    this->status == rhs.status;
}

void Order::set_status(int newStatus){
  this->status = newStatus;
}
