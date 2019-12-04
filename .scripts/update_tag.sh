#!/bin/bash

VERSION=''
REPO='origin'

while getopts ":v:r:" opt; do
  case $opt in
    v) VERSION="$OPTARG"
    ;;
    r) REPO="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

TAG_NAME=${VERSION}'-latest'

#exibe o conteudo da ultima tag
git show $TAG_NAME

# delete local tag 
git tag -d $TAG_NAME
# delete remote tag 
git push --delete $REPO $TAG_NAME

git tag -a $TAG_NAME

git push $REPO $TAG_NAME
