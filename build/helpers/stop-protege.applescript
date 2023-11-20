tell application "iTerm"
	activate
	set theWindows to the windows whose name is "protege-test-run"
end tell

if theWindows is not {} then
	set theWindow to the first item of theWindows
	focus(theWindow)
	tell application "System Events" to keystroke "c" using control down
--	repeat while the name of theWindow contains "/run.command"
--		delay 1
--	end repeat
	focus(theWindow) -- just in case
	tell application "System Events" to keystroke "w" using command down
end if

on focus(theWindow)
	tell application "iTerm" to activate
	if index of theWindow is not 1 then
		set index of theWindow to 1
		set visible of theWindow to false
		set visible of theWindow to true
	end if
end focus

