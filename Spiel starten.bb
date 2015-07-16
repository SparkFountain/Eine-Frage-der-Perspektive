AppTitle("Eine Frage der Perspektive")
Graphics3D(600,600,32,2)
SetBuffer(BackBuffer())
Global frameTimer = CreateTimer(30)
SeedRnd(MilliSecs())

Global cam = CreateCamera()
CameraRange(cam,1,50)
Global camResult = CreateCamera()
CameraViewport(camResult,300-170/2,600-170,165,165)
CameraProjMode(camResult,0)
CameraRange(camResult,1,80)
Global camResultAngle# = 0, camResultUp# = 45, camZoom# = 2
Global camPitch, camYaw, camRoll
CameraClsColor(cam,47,109,199)
CameraClsColor(camResult,47,109,199)

CreateLights()

Global plane = CreatePlane()
EntityColor(plane,128,255,0)
Global planeTex = LoadTexture("gfx/ground.png")
ScaleTexture(planeTex,2.5,2.5)
EntityTexture(plane,planeTex)

Global resultBorder = LoadImage("gfx/resultBorder.png")
MaskImage(resultBorder,0,128,0)



;level (number of cubic fields)
Global level, maxLevel
Global difficulty=2
Dim dif_name$(4)
dif_name(0)="Total simpel"
dif_name(1)="Einfach"
dif_name(2)="Schaffbar"
dif_name(3)="Nicht ganz ohne"
dif_name(4)="Extrem hart"

Global levelTime
Global lastTick

;camera positions
Global camPos# = 1

;where in the game are we right now?
Global gameSection$ = "pregame"

;figurs
Global fig_placeholder = CreateCube()
EntityAlpha(fig_placeholder,0.7)
EntityTexture(fig_placeholder,LoadTexture("gfx/cube_placeholder.png"))
EntityFX(fig_placeholder,1)
HideEntity(fig_placeholder)
Global fig_cube = CreateCube()
HideEntity(fig_cube)
Global fig_cone = CreateCone(32)
HideEntity(fig_cone)
Global fig_cylinder = CreateCylinder(32)
HideEntity(fig_cylinder)
Global fig_sphere = CreateSphere(32)
HideEntity(fig_sphere)

;figure which is selected -> blinking
Global selected_fig

;graphics
Global btn_new_game = LoadAnimImage("gfx/new_game.png",250,50,0,2)
Global btn_continue_game = LoadAnimImage("gfx/continue_game.png",250,50,0,2)
Global btn_highscore = LoadAnimImage("gfx/highscore.png",250,50,0,2)
Global btn_quit = LoadAnimImage("gfx/quit.png",250,50,0,2)
Global btn_change_level = LoadAnimImage("gfx/change_highscore.png",150,30,0,2)
Global btn_change_difficulty = LoadAnimImage("gfx/change_difficulty.png",150,30,0,2)
Global btn_hold = LoadAnimImage("gfx/offline.png",150,30,0,2)
Global btn_next_level = LoadAnimImage("gfx/online.png",150,30,0,2)
Global btn_nickname = LoadAnimImage("gfx/nickname.png",300,30,0,2)
Global hudBox = LoadImage("gfx/hudBox.png")
Global red = LoadAnimImage("gfx/red.png",20,20,0,5)
Global green = LoadAnimImage("gfx/green.png",20,20,0,5)
Global yellow = LoadAnimImage("gfx/yellow.png",20,20,0,5)
Global blue = LoadAnimImage("gfx/blue.png",20,20,0,5)
Global questionmark = LoadImage("gfx/questionmark.png")
Global medal = LoadImage("gfx/medal.png")
Global btn_small = LoadAnimImage("gfx/small.png",30,30,0,2)
Global forwards = LoadImage("gfx/forwards.png")
Global backwards = LoadImage("gfx/backwards.png")
Global repeat_lvl = LoadImage("gfx/repeat.png")
Global mute = LoadAnimImage("gfx/mute.png",30,30,0,2)
Global dif = LoadAnimImage("gfx/difficulty.png",30,30,0,5)
Global help = LoadImage("gfx/help.png")
MaskImage(help,105,9,14)

;sound and music
Global enableSound = True
Global channelSfx, channelMusic
Global sfx_new_game = LoadSound("sfx/new_game.mp3")
Global sfx_applause = LoadSound("sfx/applause.mp3")
Global sfx_click = LoadSound("sfx/click.mp3")
Global sfx_up = LoadSound("sfx/up.mp3")
Global sfx_down = LoadSound("sfx/down.mp3")
Dim sfx_ding(4)
For i=0 To 4
	sfx_ding(i) = LoadSound("sfx/ding"+(i+1)+".mp3")
Next
Global sfx_repeat = LoadSound("sfx/repeat.mp3")
Global sfx_switch = LoadSound("sfx/switch.mp3")
Global music_win = LoadSound("sfx/win.mp3")
Global music_final = LoadSound("sfx/final.mp3")
Global music_back = LoadSound("sfx/back.mp3")

;IO stream
Global stream

;mouse hit
Global mh = MouseHit(1)
Global msx, msy
Global mscroll = MouseZSpeed()

;fonts
Global menuFont = LoadFont("fonts/a song for jennifer bold.ttf",30)
Global hudFont = LoadFont("fonts/a song for jennifer.ttf",25)
Global buttonFont = LoadFont("fonts/a song for jennifer bold.ttf",20)

Global hold = 0
Global doHelp = 0

;highscore
Dim high(19)
Dim highOnline$(19)
If(FileType("highscore.sav")=1) Then LoadHighScore()
Global points

Global nickname$ = ""
Global blinkTimer = -1
Global online = False
Global showOnlineHighscore = False

;medals
Global medalHighscore = False
Global medalOnlineHighscore = False

;Reset savegames if not exist
If(FileType("highscore.sav")<>1) Then
	stream = WriteFile("highscore.sav")
	WriteString(stream,ConvertHighScore("150",0))
	Local s$ = "0"
	For i=0 To 18
		WriteString(stream,ConvertHighScore(s,0))
	Next
	CloseFile(stream)
EndIf

;load nickname if exists
If(FileType("name.dat")=1) Then
	stream = ReadFile("name.dat")
	nickname = ReadLine(stream)
	CloseFile(stream)
EndIf


Repeat
	
	Cls()
	WaitTimer(frameTimer)
	
	UpdateWorld()
	RenderWorld()
	
	mh = MouseHit(1)
	msx = MouseXSpeed()
	msy = MouseYSpeed()
	mscroll = MouseZSpeed()
	
	Select gameSection
		Case "pregame"
			PreGame()
		Case "menu"
			DrawMenu()
		Case "generateLevel"
			GenerateLevel()
		Case "playing"
			If(enableSound And (Not(ChannelPlaying(channelMusic)))) Then channelMusic = PlaySound(music_back)
			Local done = CheckSolution()
			If(Not(done)) Then
				SwitchCamera(cam,camPos)
				RotateCamResult()
				PickFigures()
				BlinkFigure()
				ChangeFigure()
				DrawHUD()
				RenderResult()
				DrawImage(resultBorder,300-180/2,600-180)
				If(KeyHit(1)) Then
					;reset everything
					ResetGame()
					If(enableSound) Then
						channelSfx = PlaySound(sfx_down)
						StopChannel(channelMusic)
					EndIf
					;back to menu
					gameSection="menu"
				EndIf
			EndIf
		Case "won"
			DrawHUD()
			For f.Figure = Each Figure
				EntityAlpha(f\figure,1)
			Next
		Case "congratulations"
			Congratulations()
		Case "highscore"
			DrawHighScore()
	End Select
	
	Flip(0)
	
Forever
End

Function PreGame()
	Color(240,210,70)
	Rect(0,0,600,600)
	SetFont(menuFont)
	Color(205,08,09)
	Text(300,30,"Eine Frage der Perspektive",300)
	
	SetFont(hudFont)
	Color(40,40,40)
	Text(300,100,"Möchtest du, dass deine Spielstände im",300)
	Text(300,130,"Online-Highscore eingetragen werden?",300)
	Text(300,180,"Dann gib bitte einen Nicknamen ein,",300)
	Text(300,210,"unter welchem deine Ergebnisse auf",300)
	Text(300,240,"den Server hochgeladen werden.",300)
	SetFont(buttonFont)
	Text(300,300,"(Für den Online-Highscore wird eine Internetverbindung benötigt)",300)
	
	If(blinkTimer=-1 Or ((blinkTimer-MilliSecs()) Mod 400 < 200)) Then
		DrawImage(btn_nickname,225,379,0)
	Else
		DrawImage(btn_nickname,225,379,1)
	EndIf
	SetFont(hudFont)
	Text(80,380,"Dein Nickname:")
	Text(225+150,380,nickname,225+150)
	
	If(blinkTimer<MilliSecs()) Then blinkTimer=-1
	
	If(MouseX()>=(120) And MouseX()<=(120+150) And MouseY()>=470 And MouseY()<=470+30) Then
		DrawImage(btn_next_level,120,470,1)
		If(mh) Then
			If(Len(nickname)>0) Then
				If(enableSound) Then channelSfx = PlaySound(sfx_click)
				online=True
				;save name for next time
				stream = WriteFile("name.dat")
				WriteLine(stream,nickname)
				CloseFile(stream)
				gameSection="menu"
			Else
				If(enableSound) Then channelSfx = PlaySound(sfx_ding(2))
				blinkTimer=MilliSecs()+1600
			EndIf
		EndIf
	Else
		DrawImage(btn_next_level,120,470,0)
	EndIf
	SetFont(buttonFont)
	Text(120+75,474,"Online eintragen",120+75)
	
	If(MouseX()>=(330) And MouseX()<=(330+150) And MouseY()>=470 And MouseY()<=470+30) Then
		DrawImage(btn_hold,330,470,1)
		If(mh) Then
			If(enableSound) Then channelSfx = PlaySound(sfx_click)
			online=False
			gameSection="menu"
		EndIf
	Else
		DrawImage(btn_hold,330,470,0)
	EndIf
	Text(330+75,474,"Offline spielen",330+75)
	
	;get user input
	Local char = GetKey()
	If(char>=32 And char<=126 And (char<48 Or char>57) And Len(nickname)<10) Then
		nickname=nickname+Chr(char)
	ElseIf(char=8 And Len(nickname)>0) Then
		nickname=Left(nickname,Len(nickname)-1)
	EndIf
End Function

Function CreateLights()
	AmbientLight(0,0,0)
	Local l1 = CreateLight()
	LightColor(l1,255,0,0)
	Local l2 = CreateLight()
	LightColor(l2,255,255,0)
	RotateEntity(l2,0,90,0)
	Local l3 = CreateLight()
	LightColor(l3,0,0,255)
	RotateEntity(l3,0,180,0)
	Local l4 = CreateLight()
	LightColor(l4,0,255,0)
	RotateEntity(l4,0,270,0)
	Local l5 = CreateLight()
	LightColor(l5,128,128,128)
	RotateEntity(l5,90,0,0)
End Function

Function DrawMenu()
	SetFont(menuFont)
	Background()
	If(MouseX()>=(300-250/2) And MouseX()<=(300+250/2) And MouseY()>=100 And MouseY()<=150) Then
		DrawImage(btn_new_game,300-250/2,100,1)
		If(mh) Then
			If(enableSound) Then channelSfx=PlaySound(sfx_new_game)
			level=1
			If(FileType("savegame.sav")=1) Then
				stream = ReadFile("savegame.sav")
				maxLevel = Int(Asc(ReadString(stream)))-53
				CloseFile(stream)
			Else
				maxLevel=1
			EndIf
			gameSection = "generateLevel"
		EndIf
	Else
		DrawImage(btn_new_game,300-250/2,100,0)
	EndIf
	If(MouseX()>=(300-250/2) And MouseX()<=(300+250/2) And MouseY()>=200 And MouseY()<=250) Then
		DrawImage(btn_continue_game,300-250/2,200,1)
		If(mh) Then
			If(enableSound) Then channelSfx=PlaySound(sfx_new_game)
			;check if there is a savegame file; otherwise start new game
			If(FileType("savegame.sav")<>1) Then
				level=1
			Else
				stream = ReadFile("savegame.sav")
				level = Int(Asc(ReadString(stream)))-53 : maxLevel=level
				CloseFile(stream)
			EndIf
			gameSection="generateLevel"
		EndIf
	Else
		DrawImage(btn_continue_game,300-250/2,200,0)
	EndIf
	If(MouseX()>=(300-250/2) And MouseX()<=(300+250/2) And MouseY()>=300 And MouseY()<=350) Then
		DrawImage(btn_highscore,300-250/2,300,1)
		If(mh) Then
			If(enableSound) Then channelSfx=PlaySound(sfx_click)
			;load highscore files
			gameSection="highscore"
		EndIf
	Else
		DrawImage(btn_highscore,300-250/2,300,0)
	EndIf
	If(MouseX()>=(300-250/2) And MouseX()<=(300+250/2) And MouseY()>=400 And MouseY()<=450) Then
		DrawImage(btn_quit,300-250/2,400,1)
		If(mh) Then
			If(enableSound) Then channelSfx=PlaySound(sfx_click)
			Color(0,0,0)
			Rect(0,0,600,600)
			Color(240,210,70)
			SetFont(menuFont)
			Text(300,300,"www.sparkfountain.de",300,300)
			Flip(0)
			Delay(3000)
			End
		EndIf
	Else
		DrawImage(btn_quit,300-250/2,400,0)
	EndIf
	
	;sound
	If(MouseX()>=550 And MouseX()<=580 And MouseY()>=30 And MouseY()<=60) Then
		DrawImage(btn_small,550,25,1)
		If(mh) Then
			enableSound=1-enableSound
			StopChannel(channelMusic)
			StopChannel(channelSfx)
			If(enableSound) Then PlaySound(sfx_ding(2))
		EndIf
	Else
		DrawImage(btn_small,550,25,0)
	EndIf
	If(enableSound) Then
		DrawImage(mute,550,25,1)
	Else
		DrawImage(mute,550,25,0)
	EndIf
	
	;replace with bitmap fonts later
	Color(40,40,40)
	Text(300,110,"Neues Spiel starten",300)
	Text(300,210,"Spiel fortsetzen",300)
	Text(300,310,"Highscore ansehen",300)
	Text(300,410,"Spiel beenden",300)
End Function

Function SwitchCamera(c,pos#)
	Local add=0, offset=0
	If(c=camResult) Then add=100
	If(level=1) Then
		offset=0
	ElseIf(level>=2 And level<=8) Then
		offset=1
	ElseIf(level>=9 And level<=25) Then
		offset=2
	;usw.
	EndIf
	
	PositionEntity(c,Sin(45*(pos-1))*-7+add+offset,7+offset*2,Cos(45*(pos-1))*-7+offset)
	RotateEntity(c,45,-45*(pos-1),0)
	PositionEntity(plane,Sin(45*(pos-1))*-7+add+offset,0,Cos(45*(pos-1))*-7+offset)
	RotateEntity(plane,0,-45*(pos-1),0)
	
	;rotate camera
	If(MouseDown(2)) Then
		camPos=camPos+(Float(msx)/50)
	EndIf
End Function

Function RotateCamResult()
	Local offset=0
	If(level=1) Then
		offset=0
	ElseIf(level>=2 And level<=8) Then
		offset=1
	ElseIf(level>=9 And level<=25) Then
		offset=2
	;usw.
	EndIf
	If(hold=0) Then
		If(difficulty=0) Then
			camResultAngle=(camResultAngle+1) Mod 360
		ElseIf(difficulty=1) Then
			camResultAngle=(camResultAngle+1.5) Mod 360
		ElseIf(difficulty=2) Then
			camResultAngle=(camResultAngle+2) Mod 360
			If((MilliSecs() Mod 3000) < 1500) Then
				camResultUp=camResultUp+0.1
			Else
				camResultUp=camResultUp-0.1
			EndIf
		ElseIf(difficulty=3)
			camResultAngle=(camResultAngle+2.5) Mod 360
			If((MilliSecs() Mod 3000) < 1500) Then
				camResultUp=camResultUp+0.3
			Else
				camResultUp=camResultUp-0.3
			EndIf
		Else
			camResultAngle=(camResultAngle+3) Mod 360
			If((MilliSecs() Mod 3000) < 1500) Then
				camZoom=camZoom+0.02
				camResultUp=camResultUp+0.3
			Else
				camZoom=camZoom-0.02
				camResultUp=camResultUp-0.3
			EndIf
		EndIf
		PositionEntity(camResult,Sin(camResultAngle)*-7+100+offset,7+offset*2-100,Cos(camResultAngle)*-7+offset)
		RotateEntity(camResult,camResultUp,-camResultAngle,0)
		CameraZoom(camResult,camZoom)
	EndIf
End Function

Function GenerateLevel()
	If(level=1) Then
		;only one figure possible (in total)
		Local f.Figure = New Figure
		f\x=0 : f\y=1 : f\z=0
		f\figure = CopyEntity(fig_placeholder)
		PositionEntity(f\figure,f\x,f\y,f\z)
		EntityPickMode(f\figure,2)
		f\fig_type=1	;placeholder
		f\res_fig_type = Rnd(2,5)
		f\res_fig = UpdateFigure(0,f\res_fig_type,f\x+100,f\y-100,f\z,0)
	ElseIf(level>=2 And level<=8) Then
		Local remainingFigures=level
		For i=8 To 1 Step -1
			Local coin = Rnd(0,1)
			If((coin Or i=remainingFigures) And (remainingFigures>0)) Then
				f.Figure = New Figure
				Select i
					Case 1 : f\x=0 : f\y=1 : f\z=0
					Case 2 : f\x=2 : f\y=1 : f\z=0
					Case 3 : f\x=0 : f\y=1 : f\z=2
					Case 4 : f\x=2 : f\y=1 : f\z=2
					Case 5 : f\x=0 : f\y=3 : f\z=0
					Case 6 : f\x=2 : f\y=3 : f\z=0
					Case 7 : f\x=0 : f\y=3 : f\z=2
					Case 8 : f\x=2 : f\y=3 : f\z=2
				End Select
				remainingFigures=remainingFigures-1
				f\figure = CopyEntity(fig_placeholder)
				PositionEntity(f\figure,f\x,f\y,f\z)
				EntityPickMode(f\figure,2)
				f\fig_type=1	;placeholder
				f\res_fig_type = Rnd(2,5)
				f\res_fig = UpdateFigure(0,f\res_fig_type,f\x+100,f\y-100,f\z,0)
			EndIf
		Next
	ElseIf(level>=9 And level<=20) Then
		remainingFigures=level
		For i=26 To 1 Step -1
			coin = Rnd(0,1)
			If((coin Or i=remainingFigures) And (remainingFigures>0)) Then
				f.Figure = New Figure
				Select i
					Case 1 : f\x=0 : f\y=1 : f\z=0
					Case 2 : f\x=2 : f\y=1 : f\z=0
					Case 3 : f\x=4 : f\y=1 : f\z=0
					Case 4 : f\x=0 : f\y=3 : f\z=0
					Case 5 : f\x=2 : f\y=3 : f\z=0
					Case 6 : f\x=4 : f\y=3 : f\z=0
					Case 7 : f\x=0 : f\y=5 : f\z=0
					Case 8 : f\x=2 : f\y=5 : f\z=0
					Case 9 : f\x=4 : f\y=5 : f\z=0
					Case 10 : f\x=0 : f\y=1 : f\z=2
					Case 11 : f\x=2 : f\y=1 : f\z=2
					Case 12 : f\x=4 : f\y=1 : f\z=2
					Case 13 : f\x=0 : f\y=3 : f\z=2
					Case 14 : f\x=4 : f\y=3 : f\z=2
					Case 15 : f\x=0 : f\y=5 : f\z=2
					Case 16 : f\x=2 : f\y=5 : f\z=2
					Case 17 : f\x=4 : f\y=5 : f\z=2
					Case 18 : f\x=0 : f\y=1 : f\z=4
					Case 19 : f\x=2 : f\y=1 : f\z=4
					Case 20 : f\x=4 : f\y=1 : f\z=4
					Case 21 : f\x=0 : f\y=3 : f\z=4
					Case 22 : f\x=2 : f\y=3 : f\z=4
					Case 23 : f\x=4 : f\y=3 : f\z=4
					Case 24 : f\x=0 : f\y=5 : f\z=4
					Case 25 : f\x=2 : f\y=5 : f\z=4
					Case 26 : f\x=4 : f\y=5 : f\z=4
				End Select
				remainingFigures=remainingFigures-1
				f\figure = CopyEntity(fig_placeholder)
				PositionEntity(f\figure,f\x,f\y,f\z)
				EntityPickMode(f\figure,2)
				f\fig_type=1	;placeholder
				f\res_fig_type = Rnd(2,5)
				f\res_fig = UpdateFigure(0,f\res_fig_type,f\x+100,f\y-100,f\z,0)
			EndIf
		Next
	Else
		;all levels passed : congratulations! :-)
		If(enableSound) Then
			channelSfx = PlaySound(sfx_applause)
			StopChannel(channelMusic)
			channelMusic = PlaySound(music_final)
		EndIf
		gameSection="congratulations"
		Return
	EndIf
	SwitchCamera(camResult,1)
	
	;start game
	FlushKeys()
	FlushMouse()
	If(online) Then GetOnlineHighscore()
	medalHighscore=False : medalOnlineHighscore=False
	camZoom=2
	camResultUp=45
	gameSection = "playing"
End Function

Function PickFigures()
	If(mh) Then
		Local picked = CameraPick(cam,MouseX(),MouseY())
		If(picked) Then 
			If(enableSound) Then channelSfx = PlaySound(sfx_ding(Rand(0,4)))
		EndIf
		If(selected_fig<>0) Then EntityAlpha(selected_fig,0.7)	;reset alpha value from old figure
		selected_fig = picked	;if nothing is picked, then nothing will be selected
	EndIf
End Function

Function BlinkFigure()
	If(selected_fig<>0) Then
		Local alpha#
		If(MilliSecs() Mod 1000.0 < 500) Then
			alpha# = (MilliSecs() Mod 1000.0)/1000.0
		Else
			alpha# = 1-(MilliSecs() Mod 1000.0)/1000.0
		EndIf
		EntityAlpha(selected_fig,alpha)
	EndIf
End Function

Function ChangeFigure()
	If(selected_fig<>0) Then
		If(mscroll>0) Then
			If(enableSound) Then channelSfx = PlaySound(sfx_switch)
			For f.Figure = Each Figure
				If(f\figure=selected_fig) Then
					If(f\fig_type>1) Then
						f\fig_type=f\fig_type-1
					Else
						f\fig_type=5
					EndIf
					f\figure = UpdateFigure(f\figure,f\fig_type,f\x,f\y,f\z)
					selected_fig=f\figure
					Exit
				EndIf
			Next
		ElseIf(mscroll<0) Then
			If(enableSound) Then channelSfx = PlaySound(sfx_switch)
			For f.Figure = Each Figure
				If(f\figure=selected_fig) Then
					If(f\fig_type<5) Then
						f\fig_type=f\fig_type+1
					Else
						f\fig_type=1
					EndIf
					f\figure = UpdateFigure(f\figure,f\fig_type,f\x,f\y,f\z)
					selected_fig=f\figure
					Exit
				EndIf
			Next
		EndIf
	EndIf
End Function

Function UpdateFigure(figure,fig_type,x,y,z,pick=1)
	If(figure<>0) Then FreeEntity(figure)
	Select fig_type
		Case 1 : figure = CopyEntity(fig_placeholder)
		Case 2 : figure = CopyEntity(fig_cube)
		Case 3 : figure = CopyEntity(fig_cylinder)
		Case 4 : figure = CopyEntity(fig_cone)
		Case 5 : figure = CopyEntity(fig_sphere)
	End Select
	PositionEntity(figure,x,y,z)
	If(pick) Then EntityPickMode(figure,2)
	Return(figure)
End Function

Function CountLevelTime()
	levelTime=levelTime+1
	
	SetFont(hudFont)
	Text(53,90,FormatLevelTime(levelTime))
End Function

Function RenderResult()
	CameraProjMode(cam,0)
	CameraProjMode(camResult,1)
	RenderWorld()
	CameraProjMode(cam,1)
	CameraProjMode(camResult,0)
End Function

Function CheckSolution()
	;calculate points
	points = (difficulty+1)*level*100
	If(levelTime<=BonusTime()*30) Then points=points+(1-(Float(levelTime)/(BonusTime()*30)))*level*100
	Local solutionCorrect = True
	For f.Figure = Each Figure
		If(f\fig_type <> f\res_fig_type) Then
			solutionCorrect = False
		EndIf
	Next
	If(solutionCorrect) Then 
		FlushKeys() : FlushMouse()
		;new highscore? -> save
		If(points>high(level-1)) Then
			high(level-1)=points
			medalHighscore = True
		EndIf
		;new online-highscore?
		If(online) Then
			If(Left(highOnline(level-1),5) = "Level") Then
				Local pos=0
				;find the number of points
				For i=1 To Len(highOnline(level-1))-1
					Local char$ = Mid(highOnline(level-1),i,1)
					Local char2$ = Mid(highOnline(level-1),i+1,1)
					If(Asc(char)>=49 And Asc(char)<=57 And Asc(char2)>=48 And Asc(char2)<=57) Then
						;found it!
						pos=i
						Exit
					EndIf
				Next
				;extract the actual number
				Local highPoints$ = Mid(highOnline(level-1),pos,2)
				For i=pos+2 To Len(highOnline(level-1))
					char$ = Mid(highOnline(level-1),i,1)
					If(Asc(char)>=48 And Asc(char)<=57) Then
						highPoints=highPoints+char
					Else
						Exit
					EndIf
				Next
				;is the highscore really new?
				If(points>Int(highPoints)) Then
					medalOnlineHighscore=True
					;update online highscore
					SetOnlineHighscore()
				EndIf
			EndIf
		EndIf
		
		stream = WriteFile("highscore.sav")
		For i=0 To 19
			WriteString(stream,ConvertHighScore(Str(high(i)),0))
		Next
		CloseFile(stream)
		
		camZoom=2
		camResultUp=45
		StopChannel(channelMusic)
		If(enableSound) Then channelMusic = PlaySound(music_win)
		gameSection="won"
		
		Return(True)
	Else
		Return(False)
	EndIf
End Function

Function DrawHUD()
	;hud box
	DrawImage(hudBox,0,0)
	;text
	Color(20,20,20)
	SetFont(menuFont)
	Text(100,15,"Level "+level,100)
	SetFont(hudFont)
	Text(100,60,dif_name(difficulty),100)
	If(online) Then
		;try to draw current online highscore for this level
		If(Left(highOnline(level-1),5) = "Level") Then
			Local delimiter = 9
			Local del2 = Instr(highOnline(level-1),"Punkte")
			If(Mid(highOnline(level-1),delimiter-1,1) <> ":") Then delimiter=10
			Text(470,30,"Bester: "+Mid(highOnline(level-1),delimiter,del2-delimiter),470)
		EndIf
	EndIf
	If(high(level-1)=0) Then
		Text(470,60,"Kein Highscore",470)
	Else
		Text(470,60,"Dein Highscore: "+high(level-1),470)
	EndIf
	Color(240+Sin(MilliSecs()/9)*10,210+Sin(MilliSecs()/9)*30,70)
	SetFont(buttonFont)
	If(gameSection="playing") Then
		If(levelTime<=BonusTime()*30) Then Text(470+Sin(MilliSecs()/5)*10,90-Cos(MilliSecs()/6)*3,"Noch gibt es Bonuspunkte!",470)
	EndIf
	Color(20,20,20)
	SetFont(hudFont)
	
	DrawBlock(questionmark,300-10,(20+110)/2)
	Local m=(MilliSecs() Mod 1600)
	
	If(m<200) Then
		DrawImage(red,290+Sin((camPos-1)*45)*40,65+Cos((camPos-1)*45)*40,0)
		DrawImage(green,290+Sin((camPos-1)*45-90)*40,65+Cos((camPos-1)*45-90)*40,0)
		DrawImage(yellow,290+Sin((camPos-1)*45+90)*40,65+Cos((camPos-1)*45+90)*40,0)
		DrawImage(blue,290+Sin((camPos-1)*45+180)*40,65+Cos((camPos-1)*45+180)*40,0)
	ElseIf(m<400 Or (m>1400 And m<=1600)) Then
		DrawImage(red,290+Sin((camPos-1)*45)*40,65+Cos((camPos-1)*45)*40,1)
		DrawImage(green,290+Sin((camPos-1)*45-90)*40,65+Cos((camPos-1)*45-90)*40,1)
		DrawImage(yellow,290+Sin((camPos-1)*45+90)*40,65+Cos((camPos-1)*45+90)*40,1)
		DrawImage(blue,290+Sin((camPos-1)*45+180)*40,65+Cos((camPos-1)*45+180)*40,1)
	ElseIf(m<600 Or (m>1200 And m<=1400)) Then
		DrawImage(red,290+Sin((camPos-1)*45)*40,65+Cos((camPos-1)*45)*40,2)
		DrawImage(green,290+Sin((camPos-1)*45-90)*40,65+Cos((camPos-1)*45-90)*40,2)
		DrawImage(yellow,290+Sin((camPos-1)*45+90)*40,65+Cos((camPos-1)*45+90)*40,2)
		DrawImage(blue,290+Sin((camPos-1)*45+180)*40,65+Cos((camPos-1)*45+180)*40,2)
	ElseIf(m<800 Or (m>1000 And m<=1200)) Then
		DrawImage(red,290+Sin((camPos-1)*45)*40,65+Cos((camPos-1)*45)*40,3)
		DrawImage(green,290+Sin((camPos-1)*45-90)*40,65+Cos((camPos-1)*45-90)*40,3)
		DrawImage(yellow,290+Sin((camPos-1)*45+90)*40,65+Cos((camPos-1)*45+90)*40,3)
		DrawImage(blue,290+Sin((camPos-1)*45+180)*40,65+Cos((camPos-1)*45+180)*40,3)
	Else
		DrawImage(red,290+Sin((camPos-1)*45)*40,65+Cos((camPos-1)*45)*40,3)
		DrawImage(green,290+Sin((camPos-1)*45-90)*40,65+Cos((camPos-1)*45-90)*40,3)
		DrawImage(yellow,290+Sin((camPos-1)*45+90)*40,65+Cos((camPos-1)*45+90)*40,3)
		DrawImage(blue,290+Sin((camPos-1)*45+180)*40,65+Cos((camPos-1)*45+180)*40,3)
	EndIf
	
	;only ingame
	If(gameSection="playing") Then
		CountLevelTime()
		
		SetFont(buttonFont)
		If(MouseX()>=40 And MouseX()<=70 And MouseY()>=500 And MouseY()<=530) Then
			DrawImage(btn_small,40,500,1)
			If(mh) Then
				If(level<maxLevel) Then
					If(enableSound) Then channelSfx = PlaySound(sfx_up)
					level=level+1
					ResetGame()
					gameSection="generateLevel"
				EndIf
			EndIf
		Else
			DrawImage(btn_small,40,500,0)
		EndIf
		DrawImage(forwards,40,500)
		
		If(MouseX()>=90 And MouseX()<=120 And MouseY()>=500 And MouseY()<=530) Then
			DrawImage(btn_small,90,500,1)
			If(mh) Then
				If(level>1) Then
					If(enableSound) Then channelSfx = PlaySound(sfx_down)
					level=level-1
					ResetGame()
					gameSection="generateLevel"
				EndIf
			EndIf
		Else
			DrawImage(btn_small,90,500,0)
		EndIf
		DrawImage(backwards,90,500)
		
		If(MouseX()>=140 And MouseX()<=170 And MouseY()>=500 And MouseY()<=530) Then
			DrawImage(btn_small,140,500,1)
			If(mh) Then
				If(enableSound) Then channelSfx = PlaySound(sfx_repeat)
				ResetGame()
				gameSection="generateLevel"
			EndIf
		Else
			DrawImage(btn_small,140,500,0)
		EndIf
		DrawImage(repeat_lvl,140,500)
		
		If(MouseX()>=90 And MouseX()<=120 And MouseY()>=450 And MouseY()<=480) Then
			DrawImage(btn_small,90,450,1)
			If(mh) Then
				If(enableSound) Then channelSfx=PlaySound(sfx_click)
				If(difficulty=4) Then
					difficulty=0
				Else
					difficulty=difficulty+1
				EndIf
				camZoom=2
				camResultUp=45
			EndIf
		Else
			DrawImage(btn_small,90,450,0)
		EndIf
		DrawImage(dif,90,450,difficulty)
		
		If(MouseX()>=32 And MouseX()<=182 And MouseY()>=550 And MouseY()<=580) Then
			DrawImage(btn_change_difficulty,32,550,1)
			If(mh) Then
				doHelp = 1-doHelp
			EndIf
		Else
			DrawImage(btn_change_difficulty,32,550,0)
		EndIf
		If(doHelp) Then 
			Text(50,555,"Hilfe ausblenden")
			DrawImage(help,0,0)
		Else
			Text(60,555,"Hilfe anzeigen")
		EndIf
		
		;hold target object
		If(MouseX()>=300-170/2 And MouseX()<=300-170/2+165 And MouseY()>=600-170 And MouseY()<=600-170+165) Then
			If(MouseDown(1)) Then
				hold=1
			Else
				hold=0
			EndIf
		Else
			hold=0
		EndIf
		
	EndIf
	
	;if game won: show button to next level
	If(gameSection="won") Then
		If(MouseX()>=300-150/2 And MouseX()<=300+150/2 And MouseY()>=500 And MouseY()<=530) Then
			DrawImage(btn_next_level,300-150/2,500,1)
			If(mh) Then
				;new level
				level=level+1
				If(level>maxLevel) Then maxLevel=level
				StopChannel(channelMusic)
				StopChannel(channelSfx)
				SaveGame()
				ResetGame()
				gameSection="generateLevel"
			EndIf
		Else
			DrawImage(btn_next_level,300-150/2,500,0)
		EndIf
		Text(300,503,"weiter",300)
		
		Color(255,0,0)
		SetFont(menuFont)
		
		DrawImage(btn_continue_game,300-125,420,0)
		Text(300,430,"Deine Punkte: "+points,300)
		
		Color(40,40,40)
		SetFont(hudFont)
		Text(100,90,FormatLevelTime(levelTime),100)
		
		;medals
		If(medalHighscore) Then
			DrawImage(medal,60,250)
			SetFont(buttonFont)
			Text(60+64,280,"Neuer",60+64)
			Text(60+64,300,"persönlicher",60+64)
			Text(60+64,320,"Highscore!",60+64)
		EndIf
		If(medalOnlineHighscore) Then
			DrawImage(medal,600-128-60,250)
			SetFont(buttonFont)
			Text(600-128-60+64,280,"Neuer",600-128-60+64)
			Text(600-128-60+64,300,"Online-",600-128-60+64)
			Text(600-128-60+64,320,"Highscore!",600-128-60+64)
		EndIf
	EndIf
End Function

Function ResetGame(fig=True)
	If(fig) Then
		For f.Figure = Each Figure
			FreeEntity(f\figure) : FreeEntity(f\res_fig)
			Delete f
		Next
	EndIf
	camPos=1
	selected_fig=0
	levelTime=0 : lastTick=0
	camZoom=2
	camResultUp=45
	
	;save game level and number of figures
	If(fig) Then SaveGame()
End Function

Function SaveGame()
	stream = WriteFile("savegame.sav")
	WriteString(stream,Chr(maxLevel+53))
	CloseFile(stream)
End Function

Function LoadHighScore()
	stream = ReadFile("highscore.sav")
	For i=0 To 19
		high(i) = Int(ConvertHighScore(ReadString(stream)))
	Next
End Function

Function ConvertHighScore$(s$, load=1)
	Local word_converted$ = ""
	Local char$ = ""
	For i=1 To Len(s)
		If(load) Then
			char$ = Chr(Asc(Mid(s,i,1))-200)
		Else
			char$ = Chr(Asc(Mid(s,i,1))+200)
		EndIf
		word_converted=word_converted+char
	Next
	
	Return(word_converted)
End Function

Function DrawHighScore()
	Background()
	SetFont(menuFont)
	Color(200,200,200)
	Text(300,20,"Highscore",300)
	
	SetFont(hudFont)
	If(showOnlineHighscore) Then
		If(highOnline(19)="") Then
			GetOnlineHighscore()
		Else
			For i=0 To 19
				Text(300,70+(19-i)*26,highOnline(i),300)
			Next
		EndIf
	Else
		Local off=0
		For i=19 To 0 Step -1
			If(high(i)>0) Then
				If(off=0) Then off=i
				Text(300,70+(off-i)*26,"Level "+(i+1)+": "+high(i)+" Punkte",300)
			EndIf
		Next
	EndIf
	
	If(online) Then
		If(MouseX()>=430 And MouseX()<=430+150 And MouseY()>=20 And MouseY()<=20+30) Then
			DrawImage(btn_change_level,430,20,1)
			If(mh) Then 
				showOnlineHighscore=1-showOnlineHighscore 
				highOnline(19)=""
				If(enableSound) Then channelSfx = PlaySound(sfx_ding(1))
			EndIf
		Else
			DrawImage(btn_change_level,430,20,0)
		EndIf
		SetFont(buttonFont)
		If(showOnlineHighscore) Then
			Text(430+75,24,"Eigener Highscore",430+75)
		Else
			Text(430+75,24,"Online-Highscore",430+75)
		EndIf
	EndIf
	
	If(KeyHit(1)) Then
		If(enableSound) Then channelSfx = PlaySound(sfx_down)
		gameSection="menu"
	EndIf
End Function

Function GetOnlineHighscore()
	stream = OpenTCPStream("www.sparkfountain.de",80)
	If(stream=0) Then
		highOnline(19)="Der Server ist nicht erreichbar." 
		highOnline(18)="Bitte überprüfe deine Internetverbindung."
		Return
	EndIf
	WriteLine(stream,"GET http://www.sparkfountain.de/perspektive?method=get HTTP/1.0")
	WriteLine(stream,"")
	
	;timeout
	Local startMillis = MilliSecs()
	While Eof(stream)
		If MilliSecs()-startMillis > 2000 Then
			highOnline(19) = "Der Server antwortet nicht."
			highOnline(18) = "Versuche es später nochmal."
			For i=0 To 17
				highOnline(i)=""
			Next
			Exit
		EndIf
	Wend
	
	;get data
	Local o=0
	While(Not(Eof(stream)))
		Local msg$ = ReadLine(stream)
		If(Left(msg,5) = "Level") Then
			highOnline(o) = msg
			o=o+1
		EndIf
	Wend
	
	CloseTCPStream(stream)
End Function

Function SetOnlineHighscore()
	stream = OpenTCPStream("www.sparkfountain.de",80)
	If(stream=0)
		Return
	EndIf
	Local decodedPoints=((((points*23)-1768)*14)-841)*4
	WriteLine(stream,"GET http://www.sparkfountain.de/perspektive?method=set&level="+level+"&name="+nickname+"&points="+decodedPoints+" HTTP/1.0")
	WriteLine(stream,"")
	
	;timeout
	Local startMillis = MilliSecs()
	While Eof(stream)
		If MilliSecs()-startMillis > 2000 Then
			Return
		EndIf
	Wend
	
	CloseTCPStream(stream)
End Function

Function FormatLevelTime$(l)
	Local min$ = Str(l/30/60)
	If(Len(min)=1) Then min = "0"+min
	Local sec$ = Str(l/30 Mod 60)
	If(Len(sec)=1) Then sec = "0"+sec
	Local milli$ = Str(l/3 Mod 10)
	Return("Zeit: "+min+":"+sec+"."+milli)
End Function

Function Background()
	Local m#=Float(MilliSecs() Mod 12000)
	If(m<2000) Then
		Color(40+50*(m/2000),40+50*(m/2000),40+50*(m/2000))
	ElseIf(m<4000)
		Color(90+140*((m-2000)/2000),90-40*((m-2000)/2000),90-40*((m-2000)/2000))
	ElseIf(m<6000)
		Color(230-160*((m-4000)/2000),50+150*((m-4000)/2000),50-20*((m-4000)/2000))
	ElseIf(m<8000)
		Color(70-50*((m-6000)/2000),200-70*((m-6000)/2000),30+170*((m-6000)/2000))
	ElseIf(m<10000)
		Color(20+220*((m-8000)/2000),130+80*((m-8000)/2000),200-130*((m-8000)/2000))
	Else
		Color(240-200*((m-10000)/2000),210-170*((m-10000)/2000),70-30*((m-10000)/2000))
	EndIf
	Rect(0,0,600,600)
End Function

Function BonusTime()
	Local result=0
	For i=1 To (level+3)
		result=result+i
	Next
	Return(result)
End Function

Function Congratulations()
	Color(240,210,70)
	Rect(0,0,600,600)
	SetFont(menuFont)
	Color(205,08,09)
	Text(300,30,"Herzlichen Glückwunsch!",300)
	
	SetFont(hudFont)
	Color(40,40,40)
	Text(300,100,"Du hast es geschafft, alle Level dieses",300)
	Text(300,130,"Spiels zu absolvieren. Damit hast du",300)
	Text(300,160,"bewiesen, dass deine Fähigkeiten im",300)
	Text(300,190,"räumlichen Denken absolut phänomenal sind.",300)
	Text(300,250,"Du kannst stolz auf dich sein!!",300)
	SetFont(buttonFont)
	Text(300,300,"Um zurück ins Menü zu gelangen, drücke einfach Escape.",300)
	
	DrawImage(medal,300-64-200+Sin(MilliSecs()/3)*20,400+Cos(MilliSecs()/3)*20)
	DrawImage(medal,300-64-Sin(360-(MilliSecs()/3))*20,400-Cos(MilliSecs()/3)*20)
	DrawImage(medal,300-64+200+Sin(MilliSecs()/3)*20,400+Cos(MilliSecs()/3)*20)
	
	If(KeyHit(1)) Then
		StopChannel(channelMusic)
		StopChannel(channelSfx)
		level=20
		gameSection="menu"
		If(enableSound) Then channelSfx = PlaySound(sfx_down)
	EndIf
End Function

Type Figure
	Field x,y,z
	Field figure	;the figure which can be changed
	Field fig_type	;what kind of figure is this? -> 1=placeholder, 2=cube, 3=cylinder, 4=cone, 5=sphere
	Field res_fig	;same for the result figure
	Field res_fig_type	;dito
End Type
;~IDEal Editor Parameters:
;~F#DC#123#238#243#24F#271#27F#286#28E#39A#3AB#3B1#3B8#3C7#431#43A#44C#454#472
;~C#Blitz3D