#!/bin/bash

#cria alias para mostrar ultimas vers�es
echo alias clogger=\'~/.scripts/changeLogger.sh\' >> ~/.bashrc

[ -d ~/.scripts ] || mkdir ~/.scripts 
cd ~/.scripts
touch changeLogger.sh
sudo chmod +x changeLogger.sh
echo 
" #!/bin/sh

while getopts \":o:d:s:\" opt; do
  case \$opt in
    o) OLD_TAG=\"\$OPTARG\"
    ;;
    d) NEW_TAG=\"\$OPTARG\"
    ;;
    s) SISTEMA=\"\$OPTARG\"
    ;;
    \?) echo \"Invalid option -\$OPTARG\" >&2
    ;;
  esac
done

LOG_PATH='./CHANGELOG.md'

echo 'Tag do '\${SISTEMA}':\n    '\${NEW_TAG}'\nDemandas:'  > \${LOG_PATH}

git log --pretty=format:'    %s' \${OLD_TAG}..${NEW_TAG} | grep -v -e 'Updating'
-e 'Merging' -e 'Releasing' -e 'Merge' -e 'Automati'  >> \${LOG_PATH}

echo 'Entregue em:\n''    '`date` >> \${LOG_PATH} " >> ~/.scripts/changeLogger.sh

source ~/.bashrc