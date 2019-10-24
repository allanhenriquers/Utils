#!/bin/bash


if  [ grep -q 'get_path_install' '~./zshrc' && echo ${?} eq 0 ]
    exit 1
else
    echo 'get_path_install = ~/.scripts/get_path_install.sh\' >> ~./zshrc
fi

[ -d ~/.scripts ] || mkdir ~/.scripts 
cd ~/.scripts
touch get_path_install.sh
sudo get_path_install.sh

