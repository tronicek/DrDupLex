@echo off
call ..\..\setEnv.bat
set SRC_DIR="/research/jEdit5.3.0/src"

java %OPTIONS% -jar %DRDUPLEX_JAR% drduplex-jedit.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-jedit.xml

java %OPTIONS% -jar %DRDUP2_JAR% drdup2-jedit.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drdup2-jedit.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-jedit-separated.xml drdup2-jedit-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drdup2-jedit-separated.xml drduplex-jedit-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
