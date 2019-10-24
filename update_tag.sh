#!/bin/bash

if [ $# -eq 0 ]
  then
    echo 'Você deveria informar ao menos uma versão "-v <vxx>"'
    exit 1
fi

set -x
VERSION=''
REPO='origin'
TAG_CONTENT='default_value'


while getopts ":v:r:t:" opt; do
  case $opt in
    v) VERSION="$OPTARG"
    ;;
    r) REPO="$OPTARG"
    ;;
    t) TAG_CONTENT="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

TAG_NAME=${VERSION}'-latest'

# delete tag local
git tag -d ${TAG_NAME}

# deleta tag remoto
git push --delete ${REPO} ${TAG_NAME}

# cria nova tag baseada no nome informado e com conteudo descrito em ${TAG_CONTENT}
git tag -a ${TAG_NAME} -m ${TAG_CONTENT}

# empurra a alteração para o remoto
git push ${REPO} ${TAG_NAME}
