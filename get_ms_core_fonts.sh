#!/bin/sh
# Download and install the Microsoft Core Fonts for the Web
#
# (C) 2000,2001 Eric Sharkey.
# You may freely distribute this file under the terms of the GNU General
# Public License, version 2 or later.

# This script has been simplified and adapted for dokarkivavleverings specific needs november 2025

FONTDIR="/ttf/"
EXITCODE=0

export http_proxy

mstt_exit_with_error() {
    echo "$1" >&2
    echo "The fonts are NOT installed." >&2
    echo "Please consult the logs" >&2
    exit 1
}

# Mirrors for Microsoft fonts at soruceforge
# Can be more than one to try, but here we just use SF.net's redirection,
# which will work in most cases. The others serve as fallbacks to retry.
URLROOTS="https://downloads.sourceforge.net/corefonts/
	https://jaist.dl.sourceforge.net/sourceforge/corefonts/
	https://nchc.dl.sourceforge.net/sourceforge/corefonts/
	https://ufpr.dl.sourceforge.net/sourceforge/corefonts/
	https://internode.dl.sourceforge.net/sourceforge/corefonts/
	https://netcologne.dl.sourceforge.net/sourceforge/corefonts/
	https://vorboss.dl.sourceforge.net/sourceforge/corefonts/
	https://netix.dl.sourceforge.net/sourceforge/corefonts/"

SCRATCHDIR=`mktemp -t -d ttf-mscorefonts-installer.XXXXXX`
chmod 0755 $SCRATCHDIR
cd $SCRATCHDIR

cat <<EOF > msfonts.info
0524fe42951adc3a7eb870e32f0920313c71f170c859b5f770d82b4ee111e970	48d9bc613917709d3b0e0f4a6d4fe33a5c544c5035dffe9e90bc11e50e822071	andale32.exe	AndaleMo.TTF
a425f0ffb6a1a5ede5b979ed6177f4f4f4fdef6ae7c302a7b7720ef332fec0a8	dad7c04acb26e23dfe4780e79375ca193ddaf68409317e81577a30674668830e	arialb32.exe	AriBlk.TTF
85297a4d146e9c87ac6f74822734bdee5f4b2a722d7eaa584b7f2cbf76f478f6	35c0f3559d8db569e36c31095b8a60d441643d95f59139de40e23fada819b833	arial32.exe	Arial.TTF
85297a4d146e9c87ac6f74822734bdee5f4b2a722d7eaa584b7f2cbf76f478f6	4044aa6b5bebbc36980206b45b0aaaaa5681552a48bcadb41746d5d1d71fd7b4	arial32.exe	Arialbd.TTF
85297a4d146e9c87ac6f74822734bdee5f4b2a722d7eaa584b7f2cbf76f478f6	2f371cd9d96b3ac544519d85c16dc43ceacdfcea35090ee8ddf3ec5857c50328	arial32.exe	Arialbi.TTF
85297a4d146e9c87ac6f74822734bdee5f4b2a722d7eaa584b7f2cbf76f478f6	70ade233175a6a6675e4501461af9326e6f78b1ffdf787ca0da5ab0fc8c9cfd6	arial32.exe	Ariali.TTF
9c6df3feefde26d4e41d4a4fe5db2a89f9123a772594d7f59afd062625cd204e	b82c53776058f291382ff7e008d4675839d2dc21eb295c66391f6fb0655d8fc0	comic32.exe	Comic.TTF
9c6df3feefde26d4e41d4a4fe5db2a89f9123a772594d7f59afd062625cd204e	873361465d994994762d0b9845c99fc7baa2a600442ea8db713a7dd19f8b0172	comic32.exe	Comicbd.TTF
bb511d861655dde879ae552eb86b134d6fae67cb58502e6ff73ec5d9151f3384	6715838c52f813f3821549d3f645db9a768bd6f3a43d8f85a89cb6875a546c61	courie32.exe	cour.ttf
bb511d861655dde879ae552eb86b134d6fae67cb58502e6ff73ec5d9151f3384	edf8a7c5bfcac2e1fe507faab417137cbddc9071637ef4648238d0768c921e02	courie32.exe	courbd.ttf
bb511d861655dde879ae552eb86b134d6fae67cb58502e6ff73ec5d9151f3384	f3f6b09855b6700977e214aab5eb9e5be6813976a24f894bd7766e92c732fbe1	courie32.exe	couri.ttf
bb511d861655dde879ae552eb86b134d6fae67cb58502e6ff73ec5d9151f3384	66dbfa20b534fba0e203da140fec7276a45a1069e424b1b9c35547538128bbe8	courie32.exe	courbi.ttf
2c2c7dcda6606ea5cf08918fb7cd3f3359e9e84338dc690013f20cd42e930301	7d0bb20c632bb59e81a0885f573bd2173f71f73204de9058feb68ce032227072	georgi32.exe	Georgia.TTF
2c2c7dcda6606ea5cf08918fb7cd3f3359e9e84338dc690013f20cd42e930301	82d2fbadb88a8632d7f2e8ad50420c9fd2e7d3cbc0e90b04890213a711b34b93	georgi32.exe	Georgiab.TTF
2c2c7dcda6606ea5cf08918fb7cd3f3359e9e84338dc690013f20cd42e930301	1523f19bda6acca42c47c50da719a12dd34f85cc2606e6a5af15a7728b377b60	georgi32.exe	Georgiai.TTF
2c2c7dcda6606ea5cf08918fb7cd3f3359e9e84338dc690013f20cd42e930301	c983e037d8e4e694dd0fb0ba2e625bca317d67a41da2dc81e46a374e53d0ec8a	georgi32.exe	Georgiaz.TTF
6061ef3b7401d9642f5dfdb5f2b376aa14663f6275e60a51207ad4facf2fccfb	00f1fc230ac99f9b97ba1a7c214eb5b909a78660cb3826fca7d64c3af5a14848	impact32.exe	Impact.TTF
db56595ec6ef5d3de5c24994f001f03b2a13e37cee27bc25c58f6f43e8f807ab	4e98adeff8ccc8ef4e3ece8d4547e288ff85fdc9c7ca711a4599c234874bbe86	times32.exe	Times.TTF
db56595ec6ef5d3de5c24994f001f03b2a13e37cee27bc25c58f6f43e8f807ab	4357b63cef20c01661a53c5dae70ffd20cb4765503aaed6d38b17a57c5a90bff	times32.exe	Timesbd.TTF
db56595ec6ef5d3de5c24994f001f03b2a13e37cee27bc25c58f6f43e8f807ab	192e1b0d18e90334e999a99f8c32808d6a2e74b3698b8cd90c943c2249a46549	times32.exe	Timesbi.TTF
db56595ec6ef5d3de5c24994f001f03b2a13e37cee27bc25c58f6f43e8f807ab	c25ae529b4cecdbca148b6ccb862ee0abad770af8b1fd29c8dba619d1b8da78a	times32.exe	Timesi.TTF
5a690d9bb8510be1b8b4fe49f1f2319651fe51bbe54775ddddd8ef0bd07fdac9	ec3ffb302488251e1b67fb09dd578b364c5339e27c1cfb26eb627666236453d0	trebuc32.exe	trebuc.ttf
5a690d9bb8510be1b8b4fe49f1f2319651fe51bbe54775ddddd8ef0bd07fdac9	f65941f9487c0a0a3b7445996ecbbd24466d7ae76ea2a597ced55f438fa63838	trebuc32.exe	Trebucbd.ttf
5a690d9bb8510be1b8b4fe49f1f2319651fe51bbe54775ddddd8ef0bd07fdac9	db56fdac7d3ba20b7aededcb6ee86c46687489d17b759e1708ea4e2d21e38410	trebuc32.exe	trebucit.ttf
5a690d9bb8510be1b8b4fe49f1f2319651fe51bbe54775ddddd8ef0bd07fdac9	c0a6bdf31f9f2953b2f08a0c1734c892bc825f0fb17c604d420f7acf203a213b	trebuc32.exe	trebucbi.ttf
c1cb61255e363166794e47664e2f21af8e3a26cb6346eb8d2ae2fa85dd5aad96	96ed14949ca4b7392cff235b9c41d55c125382abbe0c0d3c2b9dd66897cae0cb	verdan32.exe	Verdana.TTF
c1cb61255e363166794e47664e2f21af8e3a26cb6346eb8d2ae2fa85dd5aad96	c8f5065ba91680f596af3b0378e2c3e713b95a523be3d56ae185ca2b8f5f0b23	verdan32.exe	Verdanab.TTF
c1cb61255e363166794e47664e2f21af8e3a26cb6346eb8d2ae2fa85dd5aad96	91b59186656f52972531a11433c866fd56e62ec4e61e2621a2dba70c8f19a828	verdan32.exe	Verdanai.TTF
c1cb61255e363166794e47664e2f21af8e3a26cb6346eb8d2ae2fa85dd5aad96	698e220f48f4a40e77af7eb34958c8fd02f1e18c3ba3f365d93bfa2ed4474c80	verdan32.exe	Verdanaz.TTF
64595b5abc1080fba8610c5c34fab5863408e806aafe84653ca8575bed17d75a	10d099c88521b1b9e380b7690cbe47b54bb19396ca515358cfdc15ac249e2f5d	webdin32.exe	Webdings.TTF
EOF

FONTFILES=$( awk '{ print $3 }' msfonts.info | sort -u )

FFDONE=""
FFFAILED=""

if [ -z "$QUIET_MODE" ] ; then
  cat <<EOF

These fonts were provided by Microsoft "in the interest of cross-
platform compatibility".  This is no longer the case, but they are
still available from third parties.

You are free to download these fonts and use them for your own use,
but you may not redistribute them in modified form, including changes
to the file name or packaging format.

EOF
fi

if [ -n "$QUIET_MODE" ] ; then
  QUIET_ARG="--quiet"
else
  QUIET_ARG=""
fi
for ff in $FONTFILES; do
  for URLROOT in $URLROOTS ; do
    if [ ! -e $ff.done ] || [ ! -e $ff ] ; then
      if [ -z "$LOCALCOPY" ] ; then
        if ! wget --continue --tries=1 --connect-timeout=60 --read-timeout=300 $QUIET_ARG --directory-prefix . --no-directories --no-background --progress=dot:default $URLROOT$ff ; then
          continue 1
        fi
      else
        cp $LOCALCOPY/$ff .
      fi
      touch $ff.done
      break
    fi
  done
  if [ -e "$ff" ]; then
    FFDONE="$FFDONE $ff"
  else
    FFFAILED="$FFFAILED $ff"
    EXITCODE=1
  fi
done

# Reset counters for checksum
FONTFILES=$FFDONE
FFDONE=""

for ff in $FONTFILES; do
  # verify checksum before unpacking, to be safe
  DOWNLOADED_CHECK=`sha256sum $ff | awk '{print $1}'`
  CORRECT_CHECK=`awk "/$ff/ {print \\$1; exit }" msfonts.info`
  if [ $DOWNLOADED_CHECK == $CORRECT_CHECK ]; then
    cabextract -F "*.TTF" -d $FONTDIR $ff 1>&2
    FFDONE="$FFDONE $ff"
  else
    FFFAILED="$FFFAILED $ff"
    EXITCODE=1
  fi
  rm $ff
done

cd /
rm -rf $SCRATCHDIR

if [ -z "$QUIETMODE" ] ; then
  if [ $EXITCODE = 0 ] ; then
    echo "All fonts downloaded and installed."
  else
    if [ -n "$FFFAILED" ]; then
      mstt_exit_with_error "The following fonts failed to install : $FFFAILED."
    else
      mstt_exit_with_error "One or more fonts could not be extracted."
    fi
  fi
fi

exit $EXITCODE
