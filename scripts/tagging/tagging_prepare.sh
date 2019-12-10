#!/bin/sh

#cria alias para mostrar ultimas versões
echo alias latest_versions=\'git tag \| grep latest\' >> ~/.bashrc

#deleta a tag tida como última no contexto atual e cria a nova ultima versão
# echo alias change_latest=\'~/.scripts/change_latest.sh -v \$1\' >> ~/.bashrc

#insert scripts paths in PATH
export PATH="/home/ahmr/.scripts:${PATH}"

[ -d ~/.scripts ] || mkdir ~/.scripts 
cd ~/.scripts
touch change_latest
sudo chmod +x change_latest
source ~/.bashrc

echo "#!/bin/bash

if [ \$# -eq 0 ]
  then
    echo \"Você deveria informar uma versao\"
    exit 1
fi

VERSION=''
REPO='origin'

set -x
while getopts \":v:r:\" opt; do
  case \$opt in
    v) VERSION=\"\$OPTARG\"
    ;;
    r) REPO=\"\$OPTARG\"
    ;;
    \?) echo \"Invalid option -\$OPTARG\" >&2
    ;;
  esac
done

TAG_NAME=\${VERSION}'-latest'

#exibe o conteudo da ultima tag
git show \$TAG_NAME

# delete local tag 
git tag -d \$TAG_NAME

# delete remote tag 
git push --delete \$REPO \$TAG_NAME

#create a tag 
git tag -a \$TAG_NAME

git push \$REPO \$TAG_NAME" >> ~/.scripts/change_latest



