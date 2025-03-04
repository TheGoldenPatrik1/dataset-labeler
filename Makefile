default: build

compile:
	@echo "Compiling..."
	@mvn clean package

run:
	@echo "Running..."
	@java -jar target/my-labeler-project-2.0.0-SNAPSHOT.jar

clean: compile
	@rm -f labels.json
	@echo "Removed labels.json"
	@$(MAKE) run

build: compile run