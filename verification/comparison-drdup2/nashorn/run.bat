@echo off
call ..\..\setEnv.bat
set SRC_DIR="/research/OpenJDK/nashorn/src"

java %OPTIONS% -jar %DRDUPLEX_JAR% drduplex-nashorn.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-nashorn.xml

java %OPTIONS% -jar %DRDUP2_JAR% drdup2-nashorn.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drdup2-nashorn.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-nashorn-separated.xml drdup2-nashorn-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drdup2-nashorn-separated.xml drduplex-nashorn-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
