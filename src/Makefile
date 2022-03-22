CC=g++
CPPFLAGS=-std=c++11 -pthread -Irapidxml -lpqxx -lpq

SRCS=$(wildcard *.cpp)
OBJS=$(patsubst %.cpp, %.o, $(SRCS))

main: $(OBJS)
  $(CC) $(CPPFLAGS) -o $@ $(OBJS)

%.o: %.cpp
	$(CC) $(CPPFLAGS) -c -o $@ $< 

.PHONY: clean
clean:
	rm -rf *.o *~ main
