default: build

compile:
	@echo "Compiling..."
	@mvn clean package

run:
	@echo "Running..."
	@java -jar target/my-labeler-project-1.0-SNAPSHOT.jar

build: compile run