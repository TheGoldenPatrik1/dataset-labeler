default: build

compile:
	@echo "Compiling..."
	@mvn clean package

run:
	@echo "Running..."
	@java -jar target/my-labeler-project-2.1.4-SNAPSHOT.jar -i $(images) -l $(labels) -o $(options)

clean: compile
	@rm -f labels.json
	@echo "Removed labels.json"
	@$(MAKE) run

build: compile run

# Allow passing command-line arguments to the Makefile
.PHONY: build

# Prevent make from interpreting arguments as targets
%:
	@: