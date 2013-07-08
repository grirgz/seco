let SessionLoad = 1
if &cp | set nocp | endif
let s:so_save = &so | let s:siso_save = &siso | set so=0 siso=0
let v:this_session=expand("<sfile>:p")
silent only
cd ~/code/sc/seco
if expand('%') == '' && !&modified && line('$') <= 1 && getline(1) == ''
  let s:wipebuf = bufnr('%')
endif
set shortmess=aoO
badd +12 ~/code/sc/seco/live/dev1.sc
badd +676 timeline.sc
badd +80 bindings.sc
badd +1361 matrix.sc
badd +279 node_manager.sc
badd +854 main.sc
badd +264 classinstr.sc
badd +1981 param.sc
badd +765 player.sc
badd +112 gui.sc
badd +235 live/crap64.sc
badd +123 ~/.vim/UltiSnips-2.1/UltiSnips/supercollider.snippets
badd +1511 midi.sc
badd +73 ~/.local/share/SuperCollider/Extensions/custom/BufferPool.sc
badd +134 live/dev_secoscript.sc
badd +2518 tracks.sc
badd +274 script.sc
badd +2289 side.sc
badd +37 live/crap65.sc
badd +742 modulation.sc
badd +1501 ci_pool.sc
badd +1 classinstr2.sc
badd +1 ci_inline.sc
badd +453 wavetable.sc
badd +115 live/crap44.sc
badd +319 player_display.sc
badd +125 samplelib.sc
badd +13 ~/.local/share/SuperCollider/Extensions/custom/ArgThunk.sc
badd +0 live/dev2.sc
badd +0 live/live33.sc
badd +90 synthpool.sc
silent! argdel *
edit live/dev2.sc
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
let s:l = 5 - ((4 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
5
normal! 0
tabedit main.sc
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
let s:l = 854 - ((19 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
854
normal! 04l
tabedit player.sc
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
let s:l = 765 - ((19 * winheight(0) + 19) / 39)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
765
normal! 04l
tabnext 1
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
