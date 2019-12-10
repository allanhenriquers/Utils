#!/bin/sh

#cria alias para mostrar ultimas versões
echo alias clogger=\'~/.scripts/changeLogger\' >> ~/.bashrc
export PATH="/home/ahmr/.scripts:${PATH}"

[ -d ~/.scripts ] || mkdir ~/.scripts 
cd ~/.scripts
touch changeLogger
sudo chmod +x changeLogger
source ~/.bashrc
echo "#!/bin/sh

SISTEMA=\${PWD##*/}
LOG_PATH='./CHANGELOG.md'

while getopts \":o:n:\" opt; do
  case \$opt in
    o) OLD_TAG=\"\$OPTARG\"
    ;;
    n) NEW_TAG=\"\$OPTARG\"
    ;;
    \?) echo \"Invalid option -\$OPTARG\" >&2
    ;;
  esac
done


echo 'Tag do '\${SISTEMA}':\n    '\${NEW_TAG}'\nDemandas:'  > \${LOG_PATH}

git log --pretty=format:'    %s' \${OLD_TAG}..\${NEW_TAG} | grep -v -e 'Updating' -e 'Merging' -e 'Releasing' -e 'Merge' -e 'Automati'>> \${LOG_PATH}

echo 'Entregue em:\\n''    '\`date\` >> \${LOG_PATH} " > ~/.scripts/changeLogger

