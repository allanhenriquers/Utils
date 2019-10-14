#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Você deveria informar uma versão"
    exit 1
fi

VERSION=''
REPO=''

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

TAG_NAME='${VERSION}-latest'


# delete local tag '12345'
git tag -d $TAG_NAME
# delete remote tag '12345' (eg, GitHub version too)
git push --delete $REPO $TAG_NAME


git tag -a $TAG_NAME

git push origin $TAG_NAME#!/bin/bash

if [ $\# -eq 0 ]
  then
    echo 'Você deveria informar uma versão'
    exit 1
fi

VERSION=''
REPO=''

while getopts ":v:r:" opt; do
  case  in
    v) VERSION=""
    ;;
    r) REPO=""
    ;;
    \?) echo "Invalid option -" >&2
    ;;
  esac
done

TAG_NAME='-latest'


# delete local tag '12345'
git tag -d 
# delete remote tag '12345' (eg, GitHub version too)
git push --delete  


git tag -a 

git push origin 
