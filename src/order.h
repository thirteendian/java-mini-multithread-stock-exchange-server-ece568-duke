#ifndef __ORDER_H__
#define __ORDER_H__

#include "symbol.h"
#include "exception.h"

enum Status {OPEN, EXECUTED, CANCELLED};

class Order{
  private:
    Symbol symbol;
    int amount;
    double limitPrice;
    int status;

  public:
    Order(Symbol _symbol, int _amount, double _limitPrice, int _status):
      symbol(_symbol), amount(_amount), limitPrice(_limitPrice), status(Status::OPEN){
        if(_limitPrice <= 0){
          throw NumberDomainException("error: amount must be greater than 0");
        }
    }
    Order(const Order& rhs):
      symbol(rhs.symbol), amount(rhs.amount), limitPrice(rhs.limitPrice), status(rhs.status){}
    bool operator==(const Order& rhs) const;
    ~Order(){}

    void set_status(int newStatus);
};

#endif