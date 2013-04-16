@ECHO OFF

set CYGWIN=nontsec

IF EXIST ..\build\forge\mcp\src\minecraft (
	echo Syncing Source
	rsync -arv --existing ../build/forge/mcp/src/minecraft/ common/
)

PAUSE
