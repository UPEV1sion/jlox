SRC_DIR := src
OUT_DIR := out
MAIN    := net.v1sion.lox.Lox

SOURCES := $(shell find $(SRC_DIR) -name "*.java")

.PHONY: all run clean

all:
	mkdir -p $(OUT_DIR)
	javac -d $(OUT_DIR) $(SOURCES)

run: all
	java -cp $(OUT_DIR) $(MAIN)

clean:
	rm -rf $(OUT_DIR)

