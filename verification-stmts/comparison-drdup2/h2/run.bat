@echo off
call ..\..\setEnv.bat
set SRC_DIR="/research/h2/h2database-version-1.4.196/h2/src/main"

java %OPTIONS% -jar %DRDUPLEX_JAR% drduplex-h2.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-h2.xml

java %OPTIONS% -jar %DRDUP2_JAR% drdup2-h2.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drdup2-h2.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-h2-separated.xml drdup2-h2-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drdup2-h2-separated.xml drduplex-h2-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
