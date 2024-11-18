default: build

compile:
	@echo "Compiling..."
	@mvn clean package

run:
	@echo "Running..."
	@java -jar target/my-labeler-project-1.2.1-SNAPSHOT.jar

clean: compile
	@rm -f labels.json
	@echo "Removed labels.json"
	@$(MAKE) run

build: compile run