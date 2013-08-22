let SessionLoad = 1
if &cp | set nocp | endif
let s:so_save = &so | let s:siso_save = &siso | set so=0 siso=0
let v:this_session=expand("<sfile>:p")
silent only
cd ~/code/sc/seco/vlive/v4
if expand('%') == '' && !&modified && line('$') <= 1 && getline(1) == ''
  let s:wipebuf = bufnr('%')
endif
set shortmess=aoO
badd +1 ~/code/sc/seco/live/dev2.scd
badd +1 ~/code/sc/seco/live/dev3.scd
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/main.scd
badd +67 ~/.local/share/SuperCollider/Extensions/seco/seco/exp/piano.scd
badd +1 ~/code/sc/seco/live/dev2.sc
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/exp/launchpad.scd
badd +1 ~/code/sc/seco/live/crap72.scd
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/exp/tile.scd
badd +3 ~/.vim/sctile.vim
badd +64 ~/.vim/sc.vim
badd +68 a.scd
badd +1 init.scd
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/..
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/synthpool.scd
badd +30 z.scd
badd +32 e.scd
badd +1 r.scd
badd +5 a.1.scd
badd +7 a.2.scd
badd +3 a.3.scd
badd +1 a.0.scd
badd +3 a.4.scd
badd +1 ~/.local/share/SuperCollider/Extensions/seco/seco/veco/tile.scd
badd +1 a.6.scd
badd +1 1.scd
badd +20 q.scd
badd +4 t.scd
badd +2 t.8.scd
badd +1 t.1.scd
badd +7 t.7.scd
badd +2 k.scd
badd +28 ~/code/sc/seco/vlive/v1/a.scd
badd +1 ~/code/sc/seco/vlive/v1/z.scd
badd +1 ~/code/sc/seco/vlive/v1/e.scd
badd +1 ~/code/sc/seco/vlive/v1/r.scd
badd +1 ~/code/sc/seco/vlive/v1/q.scd
badd +29 ~/code/sc/seco/vlive/v1/t.scd
badd +46 y.scd
badd +5 y.1.scd
badd +8 y.2.scd
badd +0 ~/.local/share/SuperCollider/Extensions/seco/seco/veco/launchpad.scd
args y.2.scd
edit ~/code/sc/seco/live/dev2.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 4 - ((3 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
4
normal! 0
tabedit y.2.scd
set splitbelow splitright
wincmd _ | wincmd |
split
1wincmd k
wincmd w
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
exe '1resize ' . ((&lines * 20 + 20) / 41)
exe '2resize ' . ((&lines * 17 + 20) / 41)
argglobal
edit ~/.scvim/doc/UGens/Delays/DelayL.scd
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 1 - ((0 * winheight(0) + 10) / 20)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
1
normal! 0
wincmd w
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 8 - ((7 * winheight(0) + 8) / 17)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
8
normal! 024l
wincmd w
exe '1resize ' . ((&lines * 20 + 20) / 41)
exe '2resize ' . ((&lines * 17 + 20) / 41)
tabedit init.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 28 - ((27 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
28
normal! 019l
tabedit ~/.local/share/SuperCollider/Extensions/seco/seco/synthpool.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 475 - ((15 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
475
normal! 08l
lcd ~/.local/share/SuperCollider/Extensions/seco/seco
tabedit ~/.local/share/SuperCollider/Extensions/seco/seco/veco/launchpad.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 22 - ((21 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
22
normal! 0
lcd ~/.local/share/SuperCollider/Extensions/seco/seco
tabedit ~/.vim/sc.vim
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 64 - ((19 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
64
normal! 011l
lcd ~/.local/share/SuperCollider/Extensions/seco/seco
tabedit ~/.local/share/SuperCollider/Extensions/seco/seco/main.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 1211 - ((8 * winheight(0) + 8) / 17)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
1211
normal! 03l
lcd ~/.local/share/SuperCollider/Extensions/seco/seco
tabedit ~/.local/share/SuperCollider/Extensions/seco/seco/veco/tile.scd
set splitbelow splitright
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal nofen
let s:l = 207 - ((8 * winheight(0) + 8) / 17)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
207
normal! 035l
lcd ~/.local/share/SuperCollider/Extensions/seco/seco
tabnext 5
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
