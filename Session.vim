let SessionLoad = 1
if &cp | set nocp | endif
let s:cpo_save=&cpo
set cpo&vim
imap <F4>g :call SendToSC("if(GUI.current == SwingGUI) { GUI.qt } { GUI.swing };")
imap <F4>k :call SendToSC("Quarks.gui;")a
imap <F4>b :call SendToSC("s.boot;")a
inoremap <silent> <C-Tab> =UltiSnips_ListSnippets()
inoremap <silent> <S-Tab> =UltiSnips_JumpBackwards()
inoremap <silent> <SNR>9_AutoPairsReturn =AutoPairsReturn()
xnoremap 	 :call UltiSnips_SaveLastVisualSelection()gvs
snoremap <silent> 	 :call UltiSnips_ExpandSnippetOrJump()
xmap S <Plug>VSurround
nmap cs <Plug>Csurround
nmap ds <Plug>Dsurround
nmap gx <Plug>NetrwBrowseX
xmap gS <Plug>VgSurround
xmap s <Plug>Vsurround
nmap ySS <Plug>YSsurround
nmap ySs <Plug>YSsurround
nmap yss <Plug>Yssurround
nmap yS <Plug>YSurround
nmap ys <Plug>Ysurround
map <F4>j :silent !jack_connect A-PRO:midi/playback_2 SuperCollider:midi/capture_1:redraw!
map <F4>h :call SendToSC("Help.gui;")
map <F4>s :call SendToSC("Stethoscope.new(s);")
map <F4>f :call SendToSC("FreqScope.new;")
map <F4>m :call SendToSC("s.meter;")
map <F4>t :call SendToSC("s.plotTree;")
map <F4>q :call SendToSC("s.queryAllNodes;")
map <F4>g :call SendToSC("if(GUI.current == SwingGUI) { GUI.qt } { GUI.swing };")
map <F4>k :call SendToSC("Quarks.gui;")
map <F4>b :call SendToSC("s.boot;")
map <F3> mZ'X<F5>'Z
map <F2> mZ'W<F5>'Z
snoremap <silent> <BS> c
snoremap <silent> <C-Tab> :call UltiSnips_ListSnippets()
snoremap <silent> <S-Tab> :call UltiSnips_JumpBackwards()
nnoremap <silent> <Plug>NetrwBrowseX :call netrw#NetrwBrowseX(expand("<cWORD>"),0)
imap S <Plug>ISurround
imap s <Plug>Isurround
inoremap <silent> 	 =UltiSnips_ExpandSnippetOrJump()
imap  <Plug>Isurround
let &cpo=s:cpo_save
unlet s:cpo_save
set autoindent
set backspace=indent,eol,start
set fileencodings=ucs-bom,utf-8,default,latin1
set helplang=fr
set history=50
set hlsearch
set nomodeline
set printoptions=paper:a4
set ruler
set runtimepath=~/.vim,/var/lib/vim/addons,/usr/share/vim/vimfiles,/usr/share/vim/vim73,/usr/share/vim/vimfiles/after,/var/lib/vim/addons/after,~/.vim/after,~/.vim/UltiSnips-2.1/,~/.vim/,~/.scvim
set scrolloff=4
set shiftwidth=4
set suffixes=.bak,~,.swp,.o,.info,.aux,.log,.dvi,.bbl,.blg,.brf,.cb,.ind,.idx,.ilg,.inx,.out,.toc
set tabstop=4
set undodir=~/.vim/undo
set undofile
set window=40
let s:so_save = &so | let s:siso_save = &siso | set so=0 siso=0
let v:this_session=expand("<sfile>:p")
silent only
cd ~/code/sc/seco
if expand('%') == '' && !&modified && line('$') <= 1 && getline(1) == ''
  let s:wipebuf = bufnr('%')
endif
set shortmess=aoO
badd +1 live31.sc
badd +328 main.sc
badd +1405 modulation.sc
badd +1946 classinstr.sc
badd +1714 side.sc
badd +1273 eventscore.sc
badd +1 sidematrix.sc
badd +554 nodematrix.sc
badd +576 player.sc
badd +20 crap45.sc
badd +77 ~/.local/share/SuperCollider/Extensions/custom/Seco.sc
badd +173 bindings.sc
badd +3354 tracks.sc
badd +102 gui.sc
badd +2638 param.sc
badd +431 midi.sc
badd +6 crap50.sc
badd +1 node_manager.sc
badd +600 keycode.sc
badd +160 ~/.local/share/SuperCollider/Extensions/custom/ModKnob.sc
badd +1 player_display
badd +10 player_display.sc
badd +77 synth.sc
badd +11 live/live31.sc
badd +24 deprecated/score.sc
badd +49 live/crap53.sc
badd +4 live/crap54.sc
badd +1 live/live32.sc
badd +14 live/dev1.sc
badd +357 live/crap55.sc
badd +168 samplelib.sc
badd +118 synthpool.sc
silent! argdel *
edit live/dev1.sc
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
let s:cpo_save=&cpo
set cpo&vim
inoremap <buffer> <silent> <BS> =AutoPairsDelete()
imap <buffer> <F12> :call SClang_thisProcess_stop()a
imap <buffer> <F8> :call SClang_free("s")a
imap <buffer> <F7> :call SClang_TempoClock_clear()a
imap <buffer> <F6> :call SClang_send()a
imap <buffer> <F5> :call SClang_block()a
imap <buffer> <F1> :call HelpBrowser(expand('<cword>'))a
nmap <buffer>  :call SCdef(expand('<cword>'))
inoremap <buffer> <silent> î :call AutoPairsJump()a
inoremap <buffer> <silent> <expr> ð AutoPairsToggle()
inoremap <buffer> <silent> â =AutoPairsBackInsert()
inoremap <buffer> <silent> å =AutoPairsFastWrap()
nmap <buffer> K :call SChelp(expand('<cword>'))
nmap <buffer> <F12> :call SClang_thisProcess_stop()
nmap <buffer> <F8> :call SClang_free("s")
nmap <buffer> <F7> :call SClang_TempoClock_clear()
nmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F5> :call SClang_send()
nmap <buffer> <F5> :call SClang_block()
vmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
nmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
imap <buffer>  :call SCdef(expand('<cword>'))
imap <buffer>  :call SChelp(expand('<cword>'))
inoremap <buffer> <silent>   =AutoPairsSpace()
inoremap <buffer> <silent> " =AutoPairsInsert('"')
inoremap <buffer> <silent> ' =AutoPairsInsert('''')
inoremap <buffer> <silent> ( =AutoPairsInsert('(')
inoremap <buffer> <silent> ) =AutoPairsInsert(')')
nnoremap <buffer> <silent> î :call AutoPairsJump()
xnoremap <buffer> <silent> î :call AutoPairsJump()
onoremap <buffer> <silent> î :call AutoPairsJump()
nnoremap <buffer> <silent> ð :call AutoPairsToggle()
xnoremap <buffer> <silent> ð :call AutoPairsToggle()
onoremap <buffer> <silent> ð :call AutoPairsToggle()
inoremap <buffer> <silent> [ =AutoPairsInsert('[')
inoremap <buffer> <silent> ] =AutoPairsInsert(']')
inoremap <buffer> <silent> ` =AutoPairsInsert('`')
inoremap <buffer> <silent> { =AutoPairsInsert('{')
inoremap <buffer> <silent> } =AutoPairsInsert('}')
let &cpo=s:cpo_save
unlet s:cpo_save
setlocal keymap=
setlocal noarabic
setlocal autoindent
setlocal nobinary
setlocal bufhidden=
setlocal buflisted
setlocal buftype=
setlocal nocindent
setlocal cinkeys=0{,0},0),:,0#,!^F,o,O,e
setlocal cinoptions=
setlocal cinwords=if,else,while,do,for,switch
setlocal colorcolumn=
setlocal comments=s1:/*,mb:*,ex:*/,://,b:#,:%,:XCOMM,n:>,fb:-
setlocal commentstring=/*%s*/
setlocal complete=.,w,b,u,t,i
setlocal concealcursor=
setlocal conceallevel=0
setlocal completefunc=
setlocal nocopyindent
setlocal cryptmethod=
setlocal nocursorbind
setlocal nocursorcolumn
setlocal nocursorline
setlocal define=
setlocal dictionary=
setlocal nodiff
setlocal equalprg=
setlocal errorformat=
setlocal noexpandtab
if &filetype != 'supercollider'
setlocal filetype=supercollider
endif
setlocal foldcolumn=0
set nofoldenable
setlocal nofoldenable
setlocal foldexpr=0
setlocal foldignore=#
setlocal foldlevel=0
setlocal foldmarker={{{,}}}
set foldmethod=indent
setlocal foldmethod=indent
setlocal foldminlines=1
setlocal foldnestmax=20
setlocal foldtext=foldtext()
setlocal formatexpr=
setlocal formatoptions=tcq
setlocal formatlistpat=^\\s*\\d\\+[\\]:.)}\\t\ ]\\s*
setlocal grepprg=
setlocal iminsert=0
setlocal imsearch=0
setlocal include=
setlocal includeexpr=
setlocal indentexpr=
setlocal indentkeys=0{,0},:,0#,!^F,o,O,e
setlocal noinfercase
setlocal iskeyword=@,48-57,_,192-255
setlocal keywordprg=
setlocal nolinebreak
setlocal nolisp
setlocal nolist
setlocal makeprg=
setlocal matchpairs=(:),{:},[:]
setlocal nomodeline
setlocal modifiable
setlocal nrformats=octal,hex
setlocal nonumber
setlocal numberwidth=4
setlocal omnifunc=
setlocal path=
setlocal nopreserveindent
setlocal nopreviewwindow
setlocal quoteescape=\\
setlocal noreadonly
setlocal norelativenumber
setlocal norightleft
setlocal rightleftcmd=search
setlocal noscrollbind
setlocal shiftwidth=4
setlocal noshortname
setlocal nosmartindent
setlocal softtabstop=0
setlocal nospell
setlocal spellcapcheck=[.?!]\\_[\\])'\"\	\ ]\\+
setlocal spellfile=
setlocal spelllang=en
setlocal statusline=
setlocal suffixesadd=
setlocal swapfile
setlocal synmaxcol=3000
if &syntax != 'supercollider'
setlocal syntax=supercollider
endif
setlocal tabstop=4
setlocal tags=
setlocal textwidth=0
setlocal thesaurus=
setlocal undofile
setlocal nowinfixheight
setlocal nowinfixwidth
setlocal wrap
setlocal wrapmargin=0
let s:l = 182 - ((23 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
182
normal! 032l
tabedit eventscore.sc
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
let s:cpo_save=&cpo
set cpo&vim
inoremap <buffer> <silent> <BS> =AutoPairsDelete()
imap <buffer> <F12> :call SClang_thisProcess_stop()a
imap <buffer> <F8> :call SClang_free("s")a
imap <buffer> <F7> :call SClang_TempoClock_clear()a
imap <buffer> <F6> :call SClang_send()a
imap <buffer> <F5> :call SClang_block()a
imap <buffer> <F1> :call HelpBrowser(expand('<cword>'))a
nmap <buffer>  :call SCdef(expand('<cword>'))
inoremap <buffer> <silent> î :call AutoPairsJump()a
inoremap <buffer> <silent> <expr> ð AutoPairsToggle()
inoremap <buffer> <silent> â =AutoPairsBackInsert()
inoremap <buffer> <silent> å =AutoPairsFastWrap()
nmap <buffer> K :call SChelp(expand('<cword>'))
nmap <buffer> <F12> :call SClang_thisProcess_stop()
nmap <buffer> <F8> :call SClang_free("s")
nmap <buffer> <F7> :call SClang_TempoClock_clear()
nmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F5> :call SClang_send()
nmap <buffer> <F5> :call SClang_block()
vmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
nmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
imap <buffer>  :call SCdef(expand('<cword>'))
imap <buffer>  :call SChelp(expand('<cword>'))
inoremap <buffer> <silent>   =AutoPairsSpace()
inoremap <buffer> <silent> " =AutoPairsInsert('"')
inoremap <buffer> <silent> ' =AutoPairsInsert('''')
inoremap <buffer> <silent> ( =AutoPairsInsert('(')
inoremap <buffer> <silent> ) =AutoPairsInsert(')')
nnoremap <buffer> <silent> î :call AutoPairsJump()
xnoremap <buffer> <silent> î :call AutoPairsJump()
onoremap <buffer> <silent> î :call AutoPairsJump()
nnoremap <buffer> <silent> ð :call AutoPairsToggle()
xnoremap <buffer> <silent> ð :call AutoPairsToggle()
onoremap <buffer> <silent> ð :call AutoPairsToggle()
inoremap <buffer> <silent> [ =AutoPairsInsert('[')
inoremap <buffer> <silent> ] =AutoPairsInsert(']')
inoremap <buffer> <silent> ` =AutoPairsInsert('`')
inoremap <buffer> <silent> { =AutoPairsInsert('{')
inoremap <buffer> <silent> } =AutoPairsInsert('}')
let &cpo=s:cpo_save
unlet s:cpo_save
setlocal keymap=
setlocal noarabic
setlocal autoindent
setlocal nobinary
setlocal bufhidden=
setlocal buflisted
setlocal buftype=
setlocal nocindent
setlocal cinkeys=0{,0},0),:,0#,!^F,o,O,e
setlocal cinoptions=
setlocal cinwords=if,else,while,do,for,switch
setlocal colorcolumn=
setlocal comments=s1:/*,mb:*,ex:*/,://,b:#,:%,:XCOMM,n:>,fb:-
setlocal commentstring=/*%s*/
setlocal complete=.,w,b,u,t,i
setlocal concealcursor=
setlocal conceallevel=0
setlocal completefunc=
setlocal nocopyindent
setlocal cryptmethod=
setlocal nocursorbind
setlocal nocursorcolumn
setlocal nocursorline
setlocal define=
setlocal dictionary=
setlocal nodiff
setlocal equalprg=
setlocal errorformat=
setlocal noexpandtab
if &filetype != 'supercollider'
setlocal filetype=supercollider
endif
setlocal foldcolumn=0
set nofoldenable
setlocal nofoldenable
setlocal foldexpr=0
setlocal foldignore=#
setlocal foldlevel=0
setlocal foldmarker={{{,}}}
set foldmethod=indent
setlocal foldmethod=indent
setlocal foldminlines=1
setlocal foldnestmax=20
setlocal foldtext=foldtext()
setlocal formatexpr=
setlocal formatoptions=tcq
setlocal formatlistpat=^\\s*\\d\\+[\\]:.)}\\t\ ]\\s*
setlocal grepprg=
setlocal iminsert=0
setlocal imsearch=0
setlocal include=
setlocal includeexpr=
setlocal indentexpr=
setlocal indentkeys=0{,0},:,0#,!^F,o,O,e
setlocal noinfercase
setlocal iskeyword=@,48-57,_,192-255
setlocal keywordprg=
setlocal nolinebreak
setlocal nolisp
setlocal nolist
setlocal makeprg=
setlocal matchpairs=(:),{:},[:]
setlocal nomodeline
setlocal modifiable
setlocal nrformats=octal,hex
setlocal nonumber
setlocal numberwidth=4
setlocal omnifunc=
setlocal path=
setlocal nopreserveindent
setlocal nopreviewwindow
setlocal quoteescape=\\
setlocal noreadonly
setlocal norelativenumber
setlocal norightleft
setlocal rightleftcmd=search
setlocal noscrollbind
setlocal shiftwidth=4
setlocal noshortname
setlocal nosmartindent
setlocal softtabstop=0
setlocal nospell
setlocal spellcapcheck=[.?!]\\_[\\])'\"\	\ ]\\+
setlocal spellfile=
setlocal spelllang=en
setlocal statusline=
setlocal suffixesadd=
setlocal swapfile
setlocal synmaxcol=3000
if &syntax != 'supercollider'
setlocal syntax=supercollider
endif
setlocal tabstop=4
setlocal tags=
setlocal textwidth=0
setlocal thesaurus=
setlocal undofile
setlocal nowinfixheight
setlocal nowinfixwidth
setlocal wrap
setlocal wrapmargin=0
let s:l = 1386 - ((34 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
1386
normal! 02l
tabedit nodematrix.sc
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
let s:cpo_save=&cpo
set cpo&vim
inoremap <buffer> <silent> <BS> =AutoPairsDelete()
imap <buffer> <F12> :call SClang_thisProcess_stop()a
imap <buffer> <F8> :call SClang_free("s")a
imap <buffer> <F7> :call SClang_TempoClock_clear()a
imap <buffer> <F6> :call SClang_send()a
imap <buffer> <F5> :call SClang_block()a
imap <buffer> <F1> :call HelpBrowser(expand('<cword>'))a
nmap <buffer>  :call SCdef(expand('<cword>'))
inoremap <buffer> <silent> î :call AutoPairsJump()a
inoremap <buffer> <silent> <expr> ð AutoPairsToggle()
inoremap <buffer> <silent> â =AutoPairsBackInsert()
inoremap <buffer> <silent> å =AutoPairsFastWrap()
nmap <buffer> K :call SChelp(expand('<cword>'))
nmap <buffer> <F12> :call SClang_thisProcess_stop()
nmap <buffer> <F8> :call SClang_free("s")
nmap <buffer> <F7> :call SClang_TempoClock_clear()
nmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F5> :call SClang_send()
nmap <buffer> <F5> :call SClang_block()
vmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
nmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
imap <buffer>  :call SCdef(expand('<cword>'))
imap <buffer>  :call SChelp(expand('<cword>'))
inoremap <buffer> <silent>   =AutoPairsSpace()
inoremap <buffer> <silent> " =AutoPairsInsert('"')
inoremap <buffer> <silent> ' =AutoPairsInsert('''')
inoremap <buffer> <silent> ( =AutoPairsInsert('(')
inoremap <buffer> <silent> ) =AutoPairsInsert(')')
nnoremap <buffer> <silent> î :call AutoPairsJump()
xnoremap <buffer> <silent> î :call AutoPairsJump()
onoremap <buffer> <silent> î :call AutoPairsJump()
nnoremap <buffer> <silent> ð :call AutoPairsToggle()
xnoremap <buffer> <silent> ð :call AutoPairsToggle()
onoremap <buffer> <silent> ð :call AutoPairsToggle()
inoremap <buffer> <silent> [ =AutoPairsInsert('[')
inoremap <buffer> <silent> ] =AutoPairsInsert(']')
inoremap <buffer> <silent> ` =AutoPairsInsert('`')
inoremap <buffer> <silent> { =AutoPairsInsert('{')
inoremap <buffer> <silent> } =AutoPairsInsert('}')
let &cpo=s:cpo_save
unlet s:cpo_save
setlocal keymap=
setlocal noarabic
setlocal autoindent
setlocal nobinary
setlocal bufhidden=
setlocal buflisted
setlocal buftype=
setlocal nocindent
setlocal cinkeys=0{,0},0),:,0#,!^F,o,O,e
setlocal cinoptions=
setlocal cinwords=if,else,while,do,for,switch
setlocal colorcolumn=
setlocal comments=s1:/*,mb:*,ex:*/,://,b:#,:%,:XCOMM,n:>,fb:-
setlocal commentstring=/*%s*/
setlocal complete=.,w,b,u,t,i
setlocal concealcursor=
setlocal conceallevel=0
setlocal completefunc=
setlocal nocopyindent
setlocal cryptmethod=
setlocal nocursorbind
setlocal nocursorcolumn
setlocal nocursorline
setlocal define=
setlocal dictionary=
setlocal nodiff
setlocal equalprg=
setlocal errorformat=
setlocal noexpandtab
if &filetype != 'supercollider'
setlocal filetype=supercollider
endif
setlocal foldcolumn=0
set nofoldenable
setlocal nofoldenable
setlocal foldexpr=0
setlocal foldignore=#
setlocal foldlevel=0
setlocal foldmarker={{{,}}}
set foldmethod=indent
setlocal foldmethod=indent
setlocal foldminlines=1
setlocal foldnestmax=20
setlocal foldtext=foldtext()
setlocal formatexpr=
setlocal formatoptions=tcq
setlocal formatlistpat=^\\s*\\d\\+[\\]:.)}\\t\ ]\\s*
setlocal grepprg=
setlocal iminsert=0
setlocal imsearch=0
setlocal include=
setlocal includeexpr=
setlocal indentexpr=
setlocal indentkeys=0{,0},:,0#,!^F,o,O,e
setlocal noinfercase
setlocal iskeyword=@,48-57,_,192-255
setlocal keywordprg=
setlocal nolinebreak
setlocal nolisp
setlocal nolist
setlocal makeprg=
setlocal matchpairs=(:),{:},[:]
setlocal nomodeline
setlocal modifiable
setlocal nrformats=octal,hex
setlocal nonumber
setlocal numberwidth=4
setlocal omnifunc=
setlocal path=
setlocal nopreserveindent
setlocal nopreviewwindow
setlocal quoteescape=\\
setlocal noreadonly
setlocal norelativenumber
setlocal norightleft
setlocal rightleftcmd=search
setlocal noscrollbind
setlocal shiftwidth=4
setlocal noshortname
setlocal nosmartindent
setlocal softtabstop=0
setlocal nospell
setlocal spellcapcheck=[.?!]\\_[\\])'\"\	\ ]\\+
setlocal spellfile=
setlocal spelllang=en
setlocal statusline=
setlocal suffixesadd=
setlocal swapfile
setlocal synmaxcol=3000
if &syntax != 'supercollider'
setlocal syntax=supercollider
endif
setlocal tabstop=4
setlocal tags=
setlocal textwidth=0
setlocal thesaurus=
setlocal undofile
setlocal nowinfixheight
setlocal nowinfixwidth
setlocal wrap
setlocal wrapmargin=0
let s:l = 607 - ((18 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
607
normal! 04l
tabedit side.sc
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
let s:cpo_save=&cpo
set cpo&vim
inoremap <buffer> <silent> <BS> =AutoPairsDelete()
imap <buffer> <F12> :call SClang_thisProcess_stop()a
imap <buffer> <F8> :call SClang_free("s")a
imap <buffer> <F7> :call SClang_TempoClock_clear()a
imap <buffer> <F6> :call SClang_send()a
imap <buffer> <F5> :call SClang_block()a
imap <buffer> <F1> :call HelpBrowser(expand('<cword>'))a
nmap <buffer>  :call SCdef(expand('<cword>'))
inoremap <buffer> <silent> î :call AutoPairsJump()a
inoremap <buffer> <silent> <expr> ð AutoPairsToggle()
inoremap <buffer> <silent> â =AutoPairsBackInsert()
inoremap <buffer> <silent> å =AutoPairsFastWrap()
nmap <buffer> K :call SChelp(expand('<cword>'))
nmap <buffer> <F12> :call SClang_thisProcess_stop()
nmap <buffer> <F8> :call SClang_free("s")
nmap <buffer> <F7> :call SClang_TempoClock_clear()
nmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F6> :call SClang_send()
vmap <buffer> <F5> :call SClang_send()
nmap <buffer> <F5> :call SClang_block()
vmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
nmap <buffer> <F1> :call HelpBrowser(expand('<cword>'))
imap <buffer>  :call SCdef(expand('<cword>'))
imap <buffer>  :call SChelp(expand('<cword>'))
inoremap <buffer> <silent>   =AutoPairsSpace()
inoremap <buffer> <silent> " =AutoPairsInsert('"')
inoremap <buffer> <silent> ' =AutoPairsInsert('''')
inoremap <buffer> <silent> ( =AutoPairsInsert('(')
inoremap <buffer> <silent> ) =AutoPairsInsert(')')
noremap <buffer> <silent> î :call AutoPairsJump()
noremap <buffer> <silent> ð :call AutoPairsToggle()
inoremap <buffer> <silent> [ =AutoPairsInsert('[')
inoremap <buffer> <silent> ] =AutoPairsInsert(']')
inoremap <buffer> <silent> ` =AutoPairsInsert('`')
inoremap <buffer> <silent> { =AutoPairsInsert('{')
inoremap <buffer> <silent> } =AutoPairsInsert('}')
let &cpo=s:cpo_save
unlet s:cpo_save
setlocal keymap=
setlocal noarabic
setlocal autoindent
setlocal nobinary
setlocal bufhidden=
setlocal buflisted
setlocal buftype=
setlocal nocindent
setlocal cinkeys=0{,0},0),:,0#,!^F,o,O,e
setlocal cinoptions=
setlocal cinwords=if,else,while,do,for,switch
setlocal colorcolumn=
setlocal comments=s1:/*,mb:*,ex:*/,://,b:#,:%,:XCOMM,n:>,fb:-
setlocal commentstring=/*%s*/
setlocal complete=.,w,b,u,t,i
setlocal concealcursor=
setlocal conceallevel=0
setlocal completefunc=
setlocal nocopyindent
setlocal cryptmethod=
setlocal nocursorbind
setlocal nocursorcolumn
setlocal nocursorline
setlocal define=
setlocal dictionary=
setlocal nodiff
setlocal equalprg=
setlocal errorformat=
setlocal noexpandtab
if &filetype != 'supercollider'
setlocal filetype=supercollider
endif
setlocal foldcolumn=0
set nofoldenable
setlocal nofoldenable
setlocal foldexpr=0
setlocal foldignore=#
setlocal foldlevel=0
setlocal foldmarker={{{,}}}
set foldmethod=indent
setlocal foldmethod=indent
setlocal foldminlines=1
setlocal foldnestmax=20
setlocal foldtext=foldtext()
setlocal formatexpr=
setlocal formatoptions=tcq
setlocal formatlistpat=^\\s*\\d\\+[\\]:.)}\\t\ ]\\s*
setlocal grepprg=
setlocal iminsert=0
setlocal imsearch=0
setlocal include=
setlocal includeexpr=
setlocal indentexpr=
setlocal indentkeys=0{,0},:,0#,!^F,o,O,e
setlocal noinfercase
setlocal iskeyword=@,48-57,_,192-255
setlocal keywordprg=
setlocal nolinebreak
setlocal nolisp
setlocal nolist
setlocal makeprg=
setlocal matchpairs=(:),{:},[:]
setlocal nomodeline
setlocal modifiable
setlocal nrformats=octal,hex
setlocal nonumber
setlocal numberwidth=4
setlocal omnifunc=
setlocal path=
setlocal nopreserveindent
setlocal nopreviewwindow
setlocal quoteescape=\\
setlocal noreadonly
setlocal norelativenumber
setlocal norightleft
setlocal rightleftcmd=search
setlocal noscrollbind
setlocal shiftwidth=4
setlocal noshortname
setlocal nosmartindent
setlocal softtabstop=0
setlocal nospell
setlocal spellcapcheck=[.?!]\\_[\\])'\"\	\ ]\\+
setlocal spellfile=
setlocal spelllang=en
setlocal statusline=
setlocal suffixesadd=
setlocal swapfile
setlocal synmaxcol=3000
if &syntax != 'supercollider'
setlocal syntax=supercollider
endif
setlocal tabstop=4
setlocal tags=
setlocal textwidth=0
setlocal thesaurus=
setlocal undofile
setlocal nowinfixheight
setlocal nowinfixwidth
setlocal wrap
setlocal wrapmargin=0
let s:l = 1 - ((0 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
1
normal! 0
tabnext 4
if exists('s:wipebuf')
  silent exe 'bwipe ' . s:wipebuf
endif
unlet! s:wipebuf
set winheight=1 winwidth=20 shortmess=filnxtToO
let s:sx = expand("<sfile>:p:r")."x.vim"
if file_readable(s:sx)
  exe "source " . fnameescape(s:sx)
endif
let &so = s:so_save | let &siso = s:siso_save
doautoall SessionLoadPost
unlet SessionLoad
" vim: set ft=vim :
