#!/bin/bash

#cria alias para mostrar ultimas versões
echo alias latest_versions=\'git tag \| grep latest\' >> ~/.bashrc

[ -d ~/.scripts ] || mkdir ~/.scripts 
cd ~/.scripts
touch update_tag.sh
sudo chmod +x update_tag.sh
echo "#!/bin/bash

if [ \$# -eq 0 ]
  then
    echo 'Você deveria informar uma versão'
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

git tag -a \$TAG_NAME

git push \$REPO \$TAG_NAME" >> ~/.scripts/update_tag.sh

#deleta a tag tida como última no contexto atual e cria a nova ultima versão
echo alias change_latest=\'~/.scripts/update_tag.sh -v \$1\' >> ~/.bashrc

source ~/.bashrc